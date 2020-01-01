package utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import model.Obfuscator;
import model.ReplacementDataNode;
import model.Scope;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static utils.CommonUtils.*;
import static utils.Constants.classList;
import static utils.Constants.methodMap;

public class MethodObfuscator {

    private Obfuscator obfuscator;

    public void obfuscate(){
        try {
            for (String filePath : classList) {
                File file = new File(filePath);
                CompilationUnit cu = JavaParser.parse(file);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(file.getName())).orElse(null);
                if(clas == null)
                    clas = cu.getInterfaceByName(getClassNameFromFilePath(file.getName())).orElse(null);

                obfuscator = new Obfuscator();
                obfuscator.setCurrentFile(file);
                handleClass(clas);
                obfuscator.replaceInFiles();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleClass(ClassOrInterfaceDeclaration clas) {
        if (clas != null) {
            Scope global_scope = new Scope();
            global_scope.setScope(null);

            //Global_fields
            List<FieldDeclaration> global_fields = clas.getFields();
            if (!global_fields.isEmpty()) {

                for (FieldDeclaration field : global_fields) {
                    List<VariableDeclarator> global_variables = field.getVariables();
                    handleVariables(global_variables, global_scope);
                }
            }

            List<MethodDeclaration> methods = clas.getMethods();
            if (!methods.isEmpty()) {
                for (MethodDeclaration method : methods) {
                    BlockStmt block = method.getBody().orElse(null);

                    if (block != null) {
                        List<Statement> stList = block.getStatements();
                        Scope methodScope = new Scope();
                        methodScope.setScope(global_scope);

                        if (!stList.isEmpty()) {
                            for (int i = 0; i < stList.size(); i++) {
                                Statement st = stList.get(i);
                                handleStatement(st, methodScope);
                            }
                        }
                    }

                    String name = method.getName().getIdentifier();

                    int start_line_num = method.getName().getRange().get().begin.line;
                    int start_col_num = method.getName().getRange().get().begin.column;
                    int end_col_num = method.getName().getRange().get().end.column;

                    if (!MethodVisitor.isOverride(method) || toBeReplaced(name)) {
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        rnode.setLineNo(start_line_num);
                        rnode.setStartColNo(start_col_num);
                        rnode.setEndColNo(end_col_num);
                        rnode.setReplacementString(getHexValue(name));
                        obfuscator.setArrayList(rnode);
                    }
                }
            }
        }
    }

    public void handleStatement(Statement statement, Scope parentScope) {

        if (statement == null || statement.isEmptyStmt())
            return;

        handleExpressionStatement(statement, parentScope);
        handleIfStatement(statement, parentScope);
        handleBlockStatement(statement, parentScope);
    }

    public void handleExpressionStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isExpressionStmt())
            return;

        Expression exp = st.asExpressionStmt().getExpression();
        if (exp.isVariableDeclarationExpr()) {
            VariableDeclarationExpr vdexp = exp.asVariableDeclarationExpr();
            List<VariableDeclarator> variables = vdexp.getVariables();
            handleVariables(variables, parentScope);
        } else if (exp.isMethodCallExpr()) {
            MethodCallExpr methodCall = exp.asMethodCallExpr();
            String mname = methodCall.getName().getIdentifier();

            int mstart_line_num = methodCall.getName().getRange().get().begin.line;
            int mstart_col_num = methodCall.getName().getRange().get().begin.column;
            int mend_col_num = methodCall.getName().getRange().get().end.column;

            Expression obj_name_exp = methodCall.getScope().orElse(null);
            if (obj_name_exp != null && obj_name_exp.isNameExpr()) {
                String obj_name = obj_name_exp.asNameExpr().getName().getIdentifier();
                String obj_type = parentScope.findDataTypeOfIdentifier(obj_name);
                if (obj_type != null) {
                    int x;
                    for (x = 0; x < classList.size(); x++) {
                        if (getClassNameFromFilePath(classList.get(x)).equals(obj_type)) {
                            ReplacementDataNode rnode = new ReplacementDataNode();
                            rnode.setLineNo(mstart_line_num);
                            rnode.setStartColNo(mstart_col_num);
                            rnode.setEndColNo(mend_col_num);
                            rnode.setReplacementString(getHexValue(mname));
                            obfuscator.setArrayList(rnode);
                            break;
                        }
                    }
                }
            } else {
                //might be class methods
                if (toBeReplaced(mname)) {
                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(mstart_line_num);
                    rnode.setStartColNo(mstart_col_num);
                    rnode.setEndColNo(mend_col_num);
                    rnode.setReplacementString(getHexValue(mname));
                    obfuscator.setArrayList(rnode);
                }
            }
        }
    }

    public void handleIfStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isIfStmt())
            return;

        Scope ifScope;

        Statement thenStmt = st.asIfStmt().getThenStmt();
        ifScope = new Scope();
        ifScope.setScope(parentScope);
        handleStatement(thenStmt, ifScope);

        Statement elseStmt = st.asIfStmt().getElseStmt().orElse(null);
        ifScope = new Scope();
        ifScope.setScope(parentScope);
        handleStatement(elseStmt, ifScope);
    }

    public void handleBlockStatement(Statement statement, Scope parentScope) {
        if (statement == null || !statement.isBlockStmt())
            return;

        List<Statement> stmtBlock = statement.asBlockStmt().getStatements();
        if (!stmtBlock.isEmpty()) {
            Scope scope = new Scope();
            scope.setScope(parentScope);

            for (Statement stmt : stmtBlock)
                handleStatement(stmt, scope);
        }
    }

    public void handleVariables(List<VariableDeclarator> variables, Scope parentScope){
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {
                String vname = variable.getName().getIdentifier();
                String vtype = variable.getType().asString();
                parentScope.setData(vname, vtype);
            }
        }
    }

    public Boolean toBeReplaced(String name) {
        //1. Check if method present in current file.
        //2. Check if method present in other files.

        Boolean flag = false;
        ArrayList<String> temp = methodMap.get(obfuscator.getCurrentFile().getAbsolutePath());
        if (temp != null) {
            if (Collections.binarySearch(temp, name) >= 0)
                flag = true;
            else {
                for (Map.Entry<String, ArrayList<String>> entry : methodMap.entrySet()) {
                    temp = entry.getValue();
                    int i;
                    for (i = 0; i < temp.size(); i++) {
                        if (temp.get(i).equals(name)) {
                            flag = true;
                            break;
                        }
                    }
                    if (i != temp.size())
                        break;
                }
            }
        }
        return flag;
    }
}