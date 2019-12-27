package utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import static utils.Constants.*;

public class CommonUtils {

    /****************************************************************************/
    //Vignere Cipher

    public static String getHexValue(String value) {
        value = value.toLowerCase();
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
            int cix = c - 97;
            int key = k - 97;
            cix = (cix + key) % 26 + 97;
            text += (char) (cix);
        }

        return text;
    }

    /****************************************************************************/

    public static void loadPredefinedClassList() {
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
        Constants.predefinedClassList.addAll(temp);
    }

    /****************************************************************************/
    // Build JSON file
    // Parses the JSON file
    // Stores the files & folders path into constant variables

    public static void buildJson(String path, JsonArray pkgJA, boolean flag, List<FileSystem> filesList) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (files != null) {
            for (File file : files) {
                if (!file.getName().startsWith(".")) {
                    FileSystem fs = new FileSystem();
                    if (file.isFile()) {
                        if (!flag) {
                            fs.setName(file.getName());
                            fs.setType("file");
                            fs.setPath(file.getParent());

                            JsonObject fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
                            pkgJA.add(fsJO);
                        } else {
                            fs.setName(file.getName());
                            fs.setType("file");
                            fs.setPath(file.getParent());
                            filesList.add(fs);
                        }
                    } else if (file.isDirectory()) {
                        fs.setName(file.getName());
                        fs.setType("directory");
                        fs.setPath(file.getParent());

                        List<FileSystem> fl = new ArrayList<>();
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
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return fs;
    }

    public static void getFilesList(ArrayList<FileSystem> fs) {
        for (FileSystem f : fs) {
            if (f.getType().equals("file") && f.getName().endsWith(".java")) {
                classList.add(f.getPath() + File.separator + f.getName());

                try {
                    File file = new File(f.getPath() + File.separator + f.getName());
                    CompilationUnit cu = JavaParser.parse(file);

                    //Get method & method calls & store it in constant variables
                    //cu.accept(new MethodVisitor(), null);     //(using JaveParser)

                    TypeSolver tp = new TypeSolver() {
                        @Override
                        public TypeSolver getParent() {
                            return null;
                        }

                        @Override
                        public void setParent(TypeSolver typeSolver) {

                        }

                        @Override
                        public SymbolReference<ResolvedReferenceTypeDeclaration> tryToSolveType(String s) {
                            return null;
                        }
                    };
                    cu.accept(new TypeCalculatorVisitor(), JavaParserFacade.get(tp));   //(using SymbolSolver)
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (f.getType().equals("directory"))
                folderList.add(f.getPath() + File.separator + f.getName());
            if(f.getFiles() != null)
                getFilesList(new ArrayList<>(f.getFiles()));
        }
    }

    /****************************************************************************/

    public static class MethodVisitor extends VoidVisitorAdapter {
        /*@Override
        public void visit(MethodDeclaration n, Object arg) {
            //Methods list
            List<AnnotationExpr> annotationExprs = n.getAnnotations();
            if(annotationExprs != null){
                String annotations = annotationExprs.toString();
                if(!annotations.contains("@Override")) {
                    System.out.println("Method: " + n.getNameAsString());
                    methodList.add(n.getNameAsString());
                }
            }
            else {
                System.out.println("Method: " + n.getNameAsString());
                methodList.add(n.getNameAsString());
            }
        }*/

        @Override
        public void visit(MethodCallExpr n, Object arg) {
            //Method Calls
            System.out.println("Call: " + n.getNameAsString());
            methodCall.add(n.getNameAsString());
        }
    }

    public static class TypeCalculatorVisitor extends VoidVisitorAdapter<JavaParserFacade> {
        @Override
        public void visit(MethodCallExpr n, JavaParserFacade javaParserFacade) {
            super.visit(n, javaParserFacade);
            System.out.println(n.getNameAsString());
        }
    }

    /****************************************************************************/

    public static String getFileNameFromFilePath(String filePath) {
        //  usr/Desktop/file1.java -> file1.java
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        return fileName;
    }

    public static String getClassNameFromFilePath(String filePath) {
        //  usr/Desktop/file1.java -> file1
        String fileName = getFileNameFromFilePath(filePath);
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /****************************************************************************/
}