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

import static utils.CommonUtils.getHexValue;
import static utils.Constants.*;

public class PackageObfuscator {

    private Obfuscator obfuscator;

    public void obfuscate(){

        ArrayList<String> renameData=getPackageNameToRename();

//        for(String s:classList){
//            try{
//                File f=new File(s);
//                CompilationUnit cu = JavaParser.parse(f);
//                obfuscator = new Obfuscator();
//                obfuscator.setCurrentFile(f);
//                handleImport(cu,renameData);
//                obfuscator.replaceInFiles();
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }

        ArrayList<String> classPattern=new ArrayList<>();
        classPattern.add("ViewPager");
        classPattern.add("viewPageAdapter");

        for(String s:classList){
            try{
                File f=new File(s);
                CompilationUnit cu=JavaParser.parse(f);
                obfuscator=new Obfuscator();
                obfuscator.setCurrentFile(f);
                handleImportClasses(cu,classPattern);
            }catch (Exception e){
                e.printStackTrace();
            }
        }



//
//        for(String s:folderList){
//            File f=new File(s);
//            String className=getFileNameFromFilePath(f.getAbsolutePath());
//            renameDirectory(f.getAbsolutePath(),f.getParent()+File.separator+getHexValue(className));
//        }

    }


    public void handleImport(CompilationUnit cu,ArrayList<String> packageToRename) {

        for (int i = 0; i < cu.getImports().size(); i++) {

            Name nm = cu.getImports().get(i).getName().getQualifier().orElse(null);
            boolean isAsterisk = cu.getImports().get(i).isAsterisk();
            String identifier = cu.getImports().get(i).getName().getIdentifier();

            while (nm != null) {
                String temp = nm.asString();

                if (isAsterisk)
                    temp += "." + identifier;

                for(String str:packageToRename){
                    if(str.compareTo(temp)==0){
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

    public void handleImportClasses(CompilationUnit cu,ArrayList<String> replacementPattern){
        //com.example.dsc_onboarding.Adapter.viewPageAdapter -> com.example.dsc_onboarding.Adapter

        for(int i=0;i<cu.getImports().size();i++){
            Name nm = cu.getImports().get(i).getName().getQualifier().orElse(null);
            boolean isAsterisk = cu.getImports().get(i).isAsterisk();

            //cu.getImports().get(i).getTokenRange().orElse(null).getEnd().getRange().orElse(null).begin;

            if(!isAsterisk) {
                try{
                    String identifier = cu.getImports().get(i).getName().getIdentifier();
                    String qualifier  = cu.getImports().get(i).getName().getQualifier().orElse(null).asString();
                    String pattern = qualifier+"."+identifier;

                    for(String str:replacementPattern){
                        if(str.equals(identifier)){
                            TokenRange tokenRange=cu.getImports().get(i).getName().getTokenRange().orElse(null);
                            handleRange(tokenRange,identifier);
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
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

    public ArrayList<String> getPackageNameToRename(){

        ArrayList<String> packageNameToRename=new ArrayList<>();

        for(String s:Constants.folderList){
            packageNameToRename.add(getPackageNameFromPath(s));
        }

        return packageNameToRename;
    }

    public String getPackageNameFromPath(String path){


        String arr[]=path.split(File.separator,100);

        boolean javaFound=false;
        String packageName="";

        for(String s:arr)
        {
            if(s.equals("java")){
                javaFound=true;
                continue;
            }

            if(javaFound)
                packageName+=s+".";
        }

        packageName=packageName.substring(0,packageName.length()-1);

        return packageName;

    }


    public static void renameDirectory(String src, String dst) {
        File sourceFolder = new File(src);
        File destinationFolder = new File(dst);
        sourceFolder.renameTo(destinationFolder);
    }


    public static String getFileNameFromFilePath(String filePath) {
        //  usr/Desktop/file1.java -> file1.java
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        return fileName;
    }


}
