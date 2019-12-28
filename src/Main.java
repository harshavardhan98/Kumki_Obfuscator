import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.FileSystem;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static utils.CommonUtils.*;
import static utils.Constants.*;
import static utils.FileOperation.*;

public class Main {
    public static void main(String[] args) {
        //backupProject();
        //analyseProjectStructure();
        getDependencyData();

        //PackageObfuscation();
        //ClassObfuscation();
        //MethodObfuscation();

        try {
            for (String filePath : classList) {
                //TODO: Comment out filePath
                if (filePath.contains("MainActivity")) {
                    File file = new File(filePath);
                    CompilationUnit cu = JavaParser.parse(file);
                    ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(file.getName())).orElse(null);
                    if (clas != null) {
                        List<FieldDeclaration> global_fields = clas.getFields();
                        if(!global_fields.isEmpty()){
                            for(FieldDeclaration field : global_fields) {
                                List<VariableDeclarator> global_variables = field.getVariables();
                                if(!global_variables.isEmpty())
                                    for(VariableDeclarator variable : global_variables) {
                                        String name = variable.getName().getIdentifier();
                                        String type = variable.getType().asString();

                                        int start_line_num = variable.getName().getRange().get().begin.line;
                                        int start_col_num = variable.getName().getRange().get().begin.column;

                                        int end_line_num = variable.getName().getRange().get().end.line;
                                        int end_col_num = variable.getName().getRange().get().end.column;

                                        System.out.println(name + "-" + type);
                                    }
                            }
                        }

                        List<MethodDeclaration> methods = clas.getMethods();
                        if (!methods.isEmpty()) {
                            for(MethodDeclaration method : methods) {
                                if (method.getNameAsString().equals("onCreate")) {
                                    String name = method.getName().getIdentifier();

                                    Boolean isOverride = false;
                                    List<AnnotationExpr> annotationExprs = method.getAnnotations();
                                    if(annotationExprs != null){
                                        String annotations = annotationExprs.toString();
                                        if(annotations.contains("@Override")) {
                                            isOverride = true;
                                        }
                                    }

                                    int start_line_num = method.getName().getRange().get().begin.line;
                                    int start_col_num = method.getName().getRange().get().begin.column;

                                    int end_line_num = method.getName().getRange().get().end.line;
                                    int end_col_num = method.getName().getRange().get().end.column;

                                    BlockStmt block = method.getBody().orElse(null);
                                    if (block != null) {
                                        List<Statement> stList = block.getStatements();
                                        if (!stList.isEmpty()) {
                                            for(Statement st: stList){
                                                if(st.isExpressionStmt()) {
                                                    ExpressionStmt est = st.asExpressionStmt();
                                                    if(est.isExpressionStmt()) {
                                                        Expression exp = est.getExpression();
                                                        if(exp.isMethodCallExpr()) {
                                                            MethodCallExpr methodCall = exp.asMethodCallExpr();
                                                            //Idemtify method call variable type as follows:
                                                        }
                                                    }
                                                }
                                                else{

                                                }
                                            }
                                        }
                                    }
                                    //}
                                }
                            }
                        }
                        System.out.println("came");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***********************************************************/

    private static void analyseProjectStructure() {
        File f = new File(projectRootDirectory + packageName);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootJO = new JsonObject();
        JsonArray pkgJA = new JsonArray();

        buildJson(f.getAbsolutePath(), pkgJA, false, null);
        rootJO.add("package", pkgJA);
        String fileStructureJS = gson.toJson(rootJO);

        File file = new File(projectDirectory + fileStructureJsonPath);
        try {
            FileWriter fr = new FileWriter(file);
            fr.write(fileStructureJS);
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void backupProject() {
        try {
            File sourceFolder = new File(projectDirectory);
            File destinationFolder = new File(sourceFolder.getAbsolutePath() + "1");

            backupProjectDirectory = destinationFolder.getAbsolutePath() + File.separator;
            copyFolder(sourceFolder, destinationFolder);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void getDependencyData() {
        loadPredefinedClassList();

        ArrayList<FileSystem> fsTemp = parseFileStructureJson(projectDirectory + fileStructureJsonPath);
        getFilesList(fsTemp);

        //Comment out to filter non-user defined classNames
        /*for (int i = 0; i < classList.size(); i++) {
            if (Collections.binarySearch(predefinedClassList, classList.get(i)) >= 0)
                classList.remove(i--);
        }

        for (int i = 0; i < folderList.size(); i++) {
            if (Collections.binarySearch(predefinedClassList, folderList.get(i)) >= 0)
                folderList.remove(i--);
        }*/
    }

    /***********************************************************/
    //Obfuscations
    public static void PackageObfuscation() {
        renameAllDirectories(projectRootDirectory + packageName);
        analyseProjectStructure();
    }

    public static void ClassObfuscation() {
        renameAllFiles(projectRootDirectory + packageName, isFile);
        analyseProjectStructure();
    }

    public static void MethodObfuscation() {
        renameAllFiles(projectRootDirectory + packageName, isMethod);
    }

    /***********************************************************/
}