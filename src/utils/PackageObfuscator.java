package utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Name;
import model.Obfuscator;
import model.ReplacementDataNode;

import java.io.File;

import static utils.CommonUtils.getHexValue;
import static utils.Constants.packageName;

public class PackageObfuscator {

    private Obfuscator obfuscator;

    public void obfuscate(String projectRootDirectory) {
        File folder = new File(projectRootDirectory);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory())
                    obfuscate(file.getAbsolutePath());
                else if (file.isFile()) {
                    try {
                        CompilationUnit cu = JavaParser.parse(file);

                        obfuscator = new Obfuscator();
                        obfuscator.setCurrentFile(file);
                        handleImport(cu, packageName);
                        obfuscator.replaceInFiles();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void handleImport(CompilationUnit cu, String packageName) {
        for (int i = 0; i < cu.getImports().size(); i++) {
            Name nm = cu.getImports().get(i).getName().getQualifier().orElse(null);
            boolean isAsterisk = cu.getImports().get(i).isAsterisk();
            String identifier = cu.getImports().get(i).getName().getIdentifier();

            while (nm != null) {
                String temp = nm.asString();
                if (isAsterisk)
                    temp += "." + identifier;

                if (temp.compareTo(packageName) == 0) {
                    try {
                        if (isAsterisk) {
                            TokenRange tokenRange = cu.getImports().get(i).getName().getTokenRange().orElse(null);
                            handleRange(tokenRange, identifier);
                            isAsterisk = false;
                        } else {
                            TokenRange tokenRange = nm.getTokenRange().orElse(null);
                            handleRange(tokenRange, identifier);
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                nm = nm.getQualifier().orElse(null);
            }
        }
    }

    public void handleRange(TokenRange tokenRange, String identifier) {
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

    public void getPackageName(){

    }
}