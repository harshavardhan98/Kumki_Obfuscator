package utils;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
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

public class ParserOperation {
    private File currentFile;
    private ArrayList<ReplacementDataNode> arrayList;

    public ParserOperation() {
        arrayList = new ArrayList<ReplacementDataNode>();
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public ArrayList<ReplacementDataNode> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<ReplacementDataNode> arrayList) {
        this.arrayList = arrayList;
    }

    /*************************************************/

    public void handleClass(ClassOrInterfaceDeclaration clas) {
        if (clas != null) {
            Scope global_scope = new Scope();
            global_scope.setScope(null);

            //Global_fields
            List<FieldDeclaration> global_fields = clas.getFields();
            if (!global_fields.isEmpty()) {

                for (FieldDeclaration field : global_fields) {
                    List<VariableDeclarator> global_variables = field.getVariables();
                    if (!global_variables.isEmpty()) {
                        for (VariableDeclarator variable : global_variables) {
                            String name = variable.getName().getIdentifier();
                            String type = variable.getType().asString();

                            int start_line_num = variable.getType().getRange().get().begin.line;
                            int start_col_num = variable.getType().getRange().get().begin.column;
                            int end_col_num = variable.getType().getRange().get().end.column;

                            global_scope.setData(name, type);
                        }
                    }
                }
            }

            List<MethodDeclaration> methods = clas.getMethods();
            if (!methods.isEmpty()) {
                for (MethodDeclaration method : methods) {
                    String name = method.getName().getIdentifier();

                    int start_line_num = method.getName().getRange().get().begin.line;
                    int start_col_num = method.getName().getRange().get().begin.column;

                    int end_line_num = method.getName().getRange().get().end.line;
                    int end_col_num = method.getName().getRange().get().end.column;

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

                    if (!MethodVisitor.isOverride(method) || toBeReplaced(name)) {
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        rnode.setLineNo(start_line_num);
                        rnode.setStartColNo(start_col_num);
                        rnode.setEndColNo(end_col_num);
                        rnode.setReplacementString(getHexValue(name));
                        arrayList.add(rnode);
                    }
                }
            }

            replaceInFiles();
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

            if (!variables.isEmpty()) {
                for (VariableDeclarator variable : variables) {
                    String vname = variable.getName().getIdentifier();
                    String vtype = variable.getType().asString();

                    int vstart_line_num = variable.getName().getRange().get().begin.line;
                    int vstart_col_num = variable.getName().getRange().get().begin.column;
                    int vend_col_num = variable.getName().getRange().get().end.column;

                    parentScope.setData(vname, vtype);
                }
            }
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
                            arrayList.add(rnode);
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
                    arrayList.add(rnode);

                    System.out.println("replace " + mname);
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

    public Boolean toBeReplaced(String name) {
        //1. Check if method present in current file.
        //2. Check if method present in other files.

        Boolean flag = false;
        ArrayList<String> temp = methodMap.get(getCurrentFile().getAbsolutePath());
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

    public void replaceInFiles() {
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(getCurrentFile().toPath()));
            for (ReplacementDataNode r : getArrayList()) {
                String temp = fileContent.get(r.getLineNo() - 1);
                temp = temp.substring(0, r.getStartColNo() - 1) + r.getReplacementString() + temp.substring(r.getEndColNo());
                fileContent.set(r.getLineNo() - 1, temp);
            }
            Files.write(getCurrentFile().toPath(), fileContent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}