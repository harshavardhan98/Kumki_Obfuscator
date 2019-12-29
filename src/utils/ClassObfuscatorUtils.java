package utils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import model.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static utils.CommonUtils.getClassNameFromFilePath;
import static utils.Constants.classList;
import static utils.Constants.methodMap;

public class ClassObfuscatorUtils {

    private static String currentFile;
    ArrayList<String> currentClassList;

    public static void setCurrentFile(String fileName) {
        currentFile = fileName;
    }

    private static String getCurrentFile() {
        return currentFile;
    }






    public static void handleClass(ClassOrInterfaceDeclaration clas) {
        if (clas != null) {

            //Global_fields
            List<FieldDeclaration> global_fields = clas.getFields();
            if (!global_fields.isEmpty()) {

                for (FieldDeclaration field : global_fields) {
                    List<VariableDeclarator> global_variables = field.getVariables();
                    if (!global_variables.isEmpty()) {
                        for (VariableDeclarator variable : global_variables) {
                            String name = variable.getName().getIdentifier();
                            String type = variable.getType().asString();

                            int start_line_num = variable.getName().getRange().get().begin.line;
                            int start_col_num = variable.getName().getRange().get().begin.column;

                            int end_line_num = variable.getName().getRange().get().end.line;
                            int end_col_num = variable.getName().getRange().get().end.column;

                        }
                    }
                }
            }

            List<MethodDeclaration> methods = clas.getMethods();
            if (!methods.isEmpty()) {
                for (MethodDeclaration method : methods) {

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
                                for (int i = 0; i < stList.size(); i++) {
                                    Statement st = stList.get(i);
                                    handleExpressionStatement(st);

                                }
                            }
                        }

                    }
                }
            }
        }
    }



    private static void handleExpressionStatement(Statement st) {

        if (st == null || !st.isExpressionStmt())
            return;

        Expression exp = st.asExpressionStmt().getExpression();

        if (exp.isVariableDeclarationExpr()) {

            VariableDeclarationExpr vdexp = exp.asVariableDeclarationExpr();
            List<VariableDeclarator> variables = vdexp.getVariables();

            if (!variables.isEmpty()) {
                for (VariableDeclarator variable : variables) {
                    String vname = variable.getName().getIdentifier();
                    String vtype = variable.getType().asString();

                    Expression expression=variable.getInitializer().orElse(null);
                    if(expression!=null && expression.isObjectCreationExpr()){

                        ClassOrInterfaceType expr=expression.asObjectCreationExpr().getType();

                        String type=expr.getName().getIdentifier();

                        boolean b=compare(type);


                        int vstart_line_num = expr.getRange().get().begin.line;
                        int vstart_col_num = expr.getRange().get().begin.column;

                        int vend_line_num = expr.getRange().get().end.line;
                        int vend_col_num = expr.getRange().get().end.column;

                        System.out.println(" ");

                    }


                    int vstart_line_num = variable.getType().getRange().get().begin.line;
                    int vstart_col_num = variable.getType().getRange().get().begin.column;

                    int vend_line_num = variable.getType().getRange().get().end.line;
                    int vend_col_num = variable.getType().getRange().get().end.column;


                    Boolean xys=compare(vtype);
                    System.out.println("found");

                }
            }
        }



        if (exp.isMethodCallExpr()) {

            MethodCallExpr methodCall = exp.asMethodCallExpr();
            List<Expression> argList=methodCall.getArguments();
            if(argList!=null){
                for(Expression i:argList){
                    if(i.isThisExpr()){

                        Expression expr=i.asThisExpr().getClassExpr().orElse(null);
                        String name=expr.asNameExpr().getName().getIdentifier();
                        Boolean p=compare(name);

                        int vstart_line_num = expr.getRange().get().begin.line;
                        int vstart_col_num = expr.getRange().get().begin.column;

                        int vend_line_num = expr.getRange().get().end.line;
                        int vend_col_num = expr.getRange().get().end.column;

                        System.out.print("");

                    }
                }
            }



//            Expression obj_name_exp = methodCall.getScope().orElse(null);
//            if (obj_name_exp != null && obj_name_exp.isNameExpr()) {
//                String obj_name = obj_name_exp.asNameExpr().getName().getIdentifier();
//                String obj_type = "dsfds";
//
//                if (obj_type != null) {
//                    int x;
//                    for (x = 0; x < classList.size(); x++) {
//                        if (getClassNameFromFilePath(classList.get(x)).equals(obj_type)) {
//                            System.out.println("replace " + mname);
//                            break;
//                        }
//                    }
//                    if (x == classList.size()) {
//                        System.out.println("DONT replace " + mname);
//                    }
//                }
//            } else {
//                //might be class methods
//                ArrayList<String> temp = methodMap.get(getCurrentFile());
//                if (Collections.binarySearch(temp, mname) >= 0) {
//                    System.out.println("replace " + mname);
//                } else
//                    System.out.println("DONT replace " + mname);
//            }
//
//            int mstart_line_num = methodCall.getName().getRange().get().begin.line;
//            int mstart_col_num = methodCall.getName().getRange().get().begin.column;
//
//            int mend_line_num = methodCall.getName().getRange().get().end.line;
//            int mend_col_num = methodCall.getName().getRange().get().end.column;
        }
    }


    public static Boolean compare(String ObjType){
        int x;
        for (x = 0; x < classList.size(); x++) {
            if (getClassNameFromFilePath(classList.get(x)).equals(ObjType)) {
                return true;
            }
        }

        return false;
    }


}
