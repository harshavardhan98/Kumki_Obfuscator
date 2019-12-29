package utils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import model.Scope;

import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static utils.CommonUtils.getClassNameFromFilePath;
import static utils.Constants.classList;
import static utils.Constants.methodMap;

public class ParserOperation {
    //
    private static String currentFile;

    public static void setCurrentFile(String fileName) {
        currentFile = fileName;
    }

    private static String getCurrentFile() {
        return currentFile;
    }


    public static void handleClass(ClassOrInterfaceDeclaration clas) {
        if (clas != null) {
            Scope global_scope = new Scope();
            Scope.setScope(null, global_scope);

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

                            global_scope.addIdentifier(name, type);
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
                            Scope methodScope = new Scope();
                            Scope.setScope(global_scope, methodScope);

                            if (!stList.isEmpty()) {
                                for (int i = 0; i < stList.size(); i++) {
                                    Statement st = stList.get(i);
                                    handleStatement(st,methodScope);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void handleStatement(Statement statement, Scope parentScope) {

        if(statement==null || statement.isEmptyStmt())
            return;

        handleExpressionStatement(statement, parentScope);
        handleIfStatement(statement, parentScope);
        handleBlockStatement(statement, parentScope);
    }

    public static void handleBlockStatement(Statement statement, Scope parentScope) {

        if (statement == null || !statement.isBlockStmt())
            return;

        List<Statement> stmtBlock = statement.asBlockStmt().getStatements();

        if (!stmtBlock.isEmpty()) {

            Scope scope = new Scope();
            Scope.setScope(parentScope, scope);

            for (Statement stmt : stmtBlock) {
                handleStatement(stmt, scope);
            }
        }
    }

    private static void handleIfStatement(Statement st, Scope parentScope) {

        if (st == null || !st.isIfStmt())
            return;


        Scope ifScope;

        Statement thenStmt = st.asIfStmt().getThenStmt();
        ifScope = new Scope();
        Scope.setScope(parentScope, ifScope);
        handleStatement(thenStmt,ifScope);


        Statement elseStmt = st.asIfStmt().getElseStmt().orElse(null);
        ifScope = new Scope();
        Scope.setScope(parentScope, ifScope);
        handleStatement(elseStmt,ifScope);

    }


    private static void handleExpressionStatement(Statement st, Scope parentScope) {

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

                    int vstart_line_num = variable.getName().getRange().get().begin.line;
                    int vstart_col_num = variable.getName().getRange().get().begin.column;

                    int vend_line_num = variable.getName().getRange().get().end.line;
                    int vend_col_num = variable.getName().getRange().get().end.column;

                    parentScope.addIdentifier(vname, vtype);
                }
            }
        }
        if (exp.isMethodCallExpr()) {
            MethodCallExpr methodCall = exp.asMethodCallExpr();
            String mname = methodCall.getName().getIdentifier();

            Expression obj_name_exp = methodCall.getScope().orElse(null);
            if (obj_name_exp != null && obj_name_exp.isNameExpr()) {
                String obj_name = obj_name_exp.asNameExpr().getName().getIdentifier();
                String obj_type = Scope.findDataTypeOfIdentifier(obj_name, parentScope);
                if (obj_type != null) {
                    int x;
                    for (x = 0; x < classList.size(); x++) {
                        if (getClassNameFromFilePath(classList.get(x)).equals(obj_type)) {
                            System.out.println("replace " + mname);
                            break;
                        }
                    }
                    if (x == classList.size()) {
                        System.out.println("DONT replace " + mname);
                    }
                }
            } else {
                //might be class methods
                ArrayList<String> temp = methodMap.get(getCurrentFile());
                if (Collections.binarySearch(temp, mname) >= 0) {
                    System.out.println("replace " + mname);
                } else
                    System.out.println("DONT replace " + mname);
            }

            int mstart_line_num = methodCall.getName().getRange().get().begin.line;
            int mstart_col_num = methodCall.getName().getRange().get().begin.column;

            int mend_line_num = methodCall.getName().getRange().get().end.line;
            int mend_col_num = methodCall.getName().getRange().get().end.column;
        }
    }


}
