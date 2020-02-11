package utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.FileSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static utils.Constants.*;
import static utils.FileOperation.getClassNameFromFilePath;

public class CommonUtils {

    /****************************************************************************/
    //Vignere Cipher
    public static String getHexValue(String value) {

//        String temp=globalDictionary.getIdentifier(value);
//        System.out.println(temp+" "+temp.length());
//
//        for(int i=0;i<temp.length();i++)
//            if(!Character.isJavaIdentifierPart(temp.charAt(i))){
//                System.out.println(value);
//                System.out.println(globalDictionary.lastString);
//            }
//
//        return temp;


        String keyValue = Constants.keyValue;
        String tempKey = keyValue;

        int len1 = value.length();
        int len2 = keyValue.length();
        int len3 = (int) Math.ceil(len1 / (double) len2);

        for (int i = 1; i < len3; i++)
            keyValue += tempKey;

        String text = "";
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            char k = keyValue.charAt(i);
            if(Character.isAlphabetic(c)) {

                if(Character.isUpperCase(c)){
                    int cix = c - 65;
                    int key = k - 65;
                    cix = (cix + key) % 26 + 65;
                    text += (char) (cix);

                }else{
                    int cix = c - 97;
                    int key = k - 97;
                    cix = (cix + key) % 26 + 97;
                    text += (char) (cix);
                }
            }
            else
                text += c;
        }

        return text;

    }

    /****************************************************************************/

    public static ArrayList<String> loadPredefinedClassList() {
        ArrayList<String> temp = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/utils/androidClassList.txt"));
            String line = reader.readLine();

            while (line != null) {
                line = reader.readLine();
                temp.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return temp;
    }

    /****************************************************************************/
    // Build JSON file
    // Parses the JSON file
    // Stores the files & folders path into constant variables
    public static void buildJson(String path, JsonArray pkgJA, boolean flag, ArrayList<FileSystem> filesList) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (files != null) {
            for (File file : files) {
                if (!file.getName().startsWith(".")) {
                    FileSystem fs = new FileSystem();
                    if (file.isFile()) {
                        fs.setName(file.getName());
                        fs.setType("file");
                        fs.setPath(file.getParent());

                        if (!flag) {
                            JsonObject fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
                            pkgJA.add(fsJO);
                        } else
                            filesList.add(fs);
                    } else if (file.isDirectory()) {
                        fs.setName(file.getName());
                        fs.setType("directory");
                        fs.setPath(file.getParent());

                        ArrayList<FileSystem> fl = new ArrayList<>();
                        buildJson(file.getAbsolutePath(), pkgJA, true, fl);
                        fs.setFiles(fl);

                        JsonObject fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
                        pkgJA.add(fsJO);
                    }
                }
            }
        }
    }

    public static ArrayList<FileSystem> parseFileStructureJson(String path) {
        ArrayList<FileSystem> fs = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            JsonObject pkgJO = gson.fromJson(bufferedReader, JsonObject.class);
            JsonArray pkgJA = pkgJO.get("package").getAsJsonArray();

            FileSystem[] fileSystems = gson.fromJson(pkgJA, FileSystem[].class);
            Collections.addAll(fs, fileSystems);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return fs;
    }

    public static void getFilesList(ArrayList<FileSystem> fs) {
        for (FileSystem f : fs) {
            if (f.getType().equals("file") && f.getName().endsWith(".java")) {
                String path = f.getPath() + File.separator + f.getName();
                classList.add(f.getPath() + File.separator + f.getName());

                try {
                    File file = new File(f.getPath() + File.separator + f.getName());
                    CompilationUnit cu = JavaParser.parse(file);

                    if(!keepClass.contains(getClassNameFromFilePath(file.getAbsolutePath())))
                        cu.accept(new MethodVisitor(), file.getAbsolutePath());
                } catch (Exception e) {e.printStackTrace();
                }
            } else if (f.getType().equals("directory")) {
                String path = f.getPath() + File.separator + f.getName();
                folderList.add(f.getPath() + File.separator + f.getName());
            }

            if (f.getFiles() != null)
                getFilesList(f.getFiles());
        }
    }

    /****************************************************************************/

    public static class MethodVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(MethodDeclaration n, Object arg) {
            //Methods list
            String filePath = (String) arg;
            if (!isOverride(n))
                addValues(filePath, n.getNameAsString());
        }

        public void addValues(String fileName, String method) {
            ArrayList<String> methodList;
            if (methodMap.containsKey(fileName)) {
                methodList = methodMap.get(fileName);
                if (methodList == null)
                    methodList = new ArrayList<String>();
                methodList.add(method);
            } else {
                methodList = new ArrayList<String>();
                methodList.add(method);
            }
            methodMap.put(fileName, methodList);
        }

        public static Boolean isOverride(MethodDeclaration n) {
            List<AnnotationExpr> annotationExprs = n.getAnnotations();
            if (annotationExprs != null) {
                String annotations = annotationExprs.toString();
                if (annotations.contains("@Override"))
                    return true;
            }
            return false;
        }
    }

    /****************************************************************************/

    public static ArrayList<String> getClassName() {
        ArrayList<String> classNameList = new ArrayList<>();
        for (String s : classList) {
            classNameList.add(getClassNameFromFilePath(s));
        }
        return classNameList;
    }

    public static Boolean compare(String ObjType) {
        for (int x = 0; x < classList.size(); x++) {
            if (getClassNameFromFilePath(classList.get(x)).equals(ObjType))
                return true;
        }
        return false;
    }

    /****************************************************************************/

    public static String getBasePackage(){
        char[] myNameChars = packageName.toCharArray();
        for (int i = 0; i < myNameChars.length; i++) {
            if(File.separator.equals(myNameChars[i] + ""))
                myNameChars[i] = '.';
        }
        return String.valueOf(myNameChars);
    }

    public static String getPackageNameFromPath(String path) {
        String[] arr = path.split("java");
        char[] myNameChars = arr[1].toCharArray();
        myNameChars[0] = '\0';
        for (int i = 1; i < myNameChars.length; i++) {
            if(File.separator.equals(myNameChars[i] + ""))
                myNameChars[i] = '.';
        }

        String packageName = String.valueOf(myNameChars);
        return packageName;
    }

    public static ArrayList<String> getPackageName() {
        ArrayList<String> packageNameToRename = new ArrayList<>();

        for (String s : Constants.folderList)
            packageNameToRename.add(getPackageNameFromPath(s));

        return packageNameToRename;
    }

    /****************************************************************************/
}