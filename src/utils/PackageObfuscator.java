package utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Name;
import model.Obfuscator;
import model.ReplacementDataNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static utils.CommonUtils.getHexValue;
import static utils.CommonUtils.getPackageName;
import static utils.Constants.*;
import static utils.FileOperation.getFileNameFromFilePath;
import static utils.FileOperation.renameFile;

public class PackageObfuscator {

    private Obfuscator obfuscator;

    public void obfuscate() {

        ArrayList<String> renameData = getPackageName();
        ArrayList<String> replacementPattern=new ArrayList<>();

        for(String i:renameData){
            String arr[]=i.split("\\.");
            replacementPattern.add(arr[arr.length-1]);
        }

        Collections.sort(replacementPattern);

        for (String s : classList) {
            try {

                File f = new File(s);
                CompilationUnit cu = JavaParser.parse(f);
                obfuscator = new Obfuscator();
                obfuscator.setCurrentFile(f);

                if(cu.getPackageDeclaration().orElse(null)!=null) {
                    handlePackageDeclaration(cu.getPackageDeclaration().orElse(null).getName(), replacementPattern);
                }
                handleImport(cu, renameData);
                obfuscator.replaceInFiles();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (String s : folderList) {
            File f = new File(s);
            String className = getFileNameFromFilePath(f.getAbsolutePath());
            renameFile(f.getAbsolutePath(), f.getParent() + File.separator + getHexValue(className));
        }
    }

    public void handleImport(CompilationUnit cu, ArrayList<String> packageToRename) {

        for (int i = 0; i < cu.getImports().size(); i++) {
            Name nm = cu.getImports().get(i).getName().getQualifier().orElse(null);
            boolean isAsterisk = cu.getImports().get(i).isAsterisk();
            String identifier = cu.getImports().get(i).getName().getIdentifier();

            while (nm != null) {
                String temp = nm.asString();

                if (isAsterisk)
                    temp += "." + identifier;

                for (String str : packageToRename) {
                    if (str.trim().compareTo(temp) == 0) {
                        try {
                            if (isAsterisk) {
                                TokenRange tokenRange = cu.getImports().get(i).getName().getTokenRange().orElse(null);
                                handleRange(tokenRange, identifier);
                            } else {
                                TokenRange tokenRange = nm.getTokenRange().orElse(null);
                                handleRange(tokenRange, nm.getIdentifier());
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
                isAsterisk = false;
                nm = nm.getQualifier().orElse(null);
            }
        }
    }

    private void handleRange(TokenRange tokenRange, String identifier) {
        if (tokenRange != null) {
            Range range = tokenRange.getEnd().getRange().orElse(null);
            if (range != null) {
                int start_line_no = range.begin.line;
                int start_col_no = range.begin.column;
                int end_col_no = range.end.column;

                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(start_line_no);
                rnode.setStartColNo(start_col_no);
                rnode.setEndColNo(end_col_no);
                rnode.setReplacementString(getHexValue(identifier));
                obfuscator.setArrayList(rnode);
            }
        }
    }

    private void handlePackageDeclaration(Name name,ArrayList<String> replacementPattern){

        if(name!=null){
            if(name.getIdentifier()!=null && Collections.binarySearch(replacementPattern,name.getIdentifier())>=0){
                TokenRange tokenRange=name.getTokenRange().orElse(null);
                if(tokenRange!=null){
                    Range range=tokenRange.getEnd().getRange().orElse(null);

                    if(range!=null)
                    {
                        ReplacementDataNode r=new ReplacementDataNode();
                        r.setLineNo(range.begin.line);
                        r.setStartColNo(range.begin.column);
                        r.setEndColNo(range.end.column);
                        r.setReplacementString(getHexValue(name.getIdentifier()));
                        obfuscator.setArrayList(r);
                    }
                }
            }

            handlePackageDeclaration(name.getQualifier().orElse(null),replacementPattern);
        }

    }
}