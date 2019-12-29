import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.FileSystem;
import model.Scope;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

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
                        Scope global_scope = new Scope();
                        global_scope.setParentScope(null);

                        //Global_fields
                        List<FieldDeclaration> global_fields = clas.getFields();
                        if(!global_fields.isEmpty()){

                            for(FieldDeclaration field : global_fields) {
                                List<VariableDeclarator> global_variables = field.getVariables();
                                if(!global_variables.isEmpty()) {
                                    for (VariableDeclarator variable : global_variables) {
                                        String name = variable.getName().getIdentifier();
                                        String type = variable.getType().asString();

                                        int start_line_num = variable.getName().getRange().get().begin.line;
                                        int start_col_num = variable.getName().getRange().get().begin.column;

                                        int end_line_num = variable.getName().getRange().get().end.line;
                                        int end_col_num = variable.getName().getRange().get().end.column;

                                        global_scope.addIdentifier(name, type);
                                    }
                                }
                            }
                        }

                        List<MethodDeclaration> methods = clas.getMethods();
                        if (!methods.isEmpty()) {
                            for(MethodDeclaration method : methods) {
                                Scope methodScope = new Scope();
                                methodScope.setParentScope(global_scope);
                                global_scope.addChildScope(methodScope);

                                if (method.getNameAsString().equals("onCreate")) {
                                    String name = method.getName().getIdentifier();

                                    int start_line_num = method.getName().getRange().get().begin.line;
                                    int start_col_num = method.getName().getRange().get().begin.column;

                                    int end_line_num = method.getName().getRange().get().end.line;
                                    int end_col_num = method.getName().getRange().get().end.column;

                                    BlockStmt block = method.getBody().orElse(null);
                                    if (block != null) {
                                        List<Statement> stList = block.getStatements();

                                        if (!stList.isEmpty()) {
                                            for(int i=0; i< stList.size(); i++){
                                                Statement st = stList.get(i);

                                                if(st.isExpressionStmt()) {
                                                    Expression exp = st.asExpressionStmt().getExpression();

                                                    if(exp.isVariableDeclarationExpr()){
                                                        VariableDeclarationExpr vdexp = exp.asVariableDeclarationExpr();
                                                        List<VariableDeclarator> variables = vdexp.getVariables();

                                                        if(!variables.isEmpty()){
                                                            for(VariableDeclarator variable : variables) {
                                                                String vname = variable.getName().getIdentifier();
                                                                String vtype = variable.getType().asString();

                                                                int vstart_line_num = variable.getName().getRange().get().begin.line;
                                                                int vstart_col_num = variable.getName().getRange().get().begin.column;

                                                                int vend_line_num = variable.getName().getRange().get().end.line;
                                                                int vend_col_num = variable.getName().getRange().get().end.column;

                                                                methodScope.addIdentifier(vname, vtype);
                                                            }
                                                        }
                                                    }
                                                    if(exp.isMethodCallExpr()) {
                                                        MethodCallExpr methodCall = exp.asMethodCallExpr();
                                                        String mname = methodCall.getName().getIdentifier();

                                                        Expression obj_name_exp = methodCall.getScope().orElse(null);
                                                        if(obj_name_exp != null && obj_name_exp.isNameExpr()){
                                                            String obj_name = obj_name_exp.asNameExpr().getName().getIdentifier();
                                                            String obj_type = Scope.findDataTypeOfIdentifier(obj_name, methodScope);
                                                            if(obj_type != null){
                                                                int x;
                                                                for(x = 0;  x < classList.size(); x++) {
                                                                    if (getClassNameFromFilePath(classList.get(x)).equals(obj_type)) {
                                                                        System.out.println("replace " + mname);
                                                                        break;
                                                                    }
                                                                }
                                                                if(x == classList.size()){
                                                                    System.out.println("DONT replace " + mname);
                                                                }
                                                            }
                                                        }
                                                        else {
                                                            //might be class methods

                                                            ArrayList<String> temp = methodMap.get(file.getName());
                                                            if(Collections.binarySearch(temp, mname) >= 0){
                                                                System.out.println("replace " + mname);
                                                            }
                                                            else
                                                                System.out.println("DONT replace " + mname);
                                                        }

                                                        int mstart_line_num = methodCall.getName().getRange().get().begin.line;
                                                        int mstart_col_num = methodCall.getName().getRange().get().begin.column;

                                                        int mend_line_num = methodCall.getName().getRange().get().end.line;
                                                        int mend_col_num = methodCall.getName().getRange().get().end.column;
                                                    }
                                                }
                                                else{

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
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

        for (int i = 0; i < classList.size(); i++) {
            if (Collections.binarySearch(predefinedClassList, classList.get(i)) >= 0)
                classList.remove(i--);
        }

        for (int i = 0; i < folderList.size(); i++) {
            if (Collections.binarySearch(predefinedClassList, folderList.get(i)) >= 0)
                folderList.remove(i--);
        }
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
        //renameAllFiles(projectRootDirectory + packageName, isMethod);
    }

    /***********************************************************/
}