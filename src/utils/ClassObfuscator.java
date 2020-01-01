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
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import model.Obfuscator;
import model.ReplacementDataNode;

import java.io.File;
import java.util.List;

import static utils.CommonUtils.getClassNameFromFilePath;
import static utils.CommonUtils.getHexValue;
import static utils.Constants.classList;

public class ClassObfuscator {

    private Obfuscator obfuscator;

    public void obfuscate() {
        try {
            for (String filePath : classList) {
                File file = new File(filePath);
                CompilationUnit cu = JavaParser.parse(file);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(file.getName())).orElse(null);
                if (clas == null)
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
            //Global_fields
            List<FieldDeclaration> global_fields = clas.getFields();
            if (!global_fields.isEmpty()) {

                for (FieldDeclaration field : global_fields) {
                    List<VariableDeclarator> global_variables = field.getVariables();
                    handleVariables(global_variables);
                }
            }

            List<MethodDeclaration> methods = clas.getMethods();
            if (!methods.isEmpty()) {
                for (MethodDeclaration method : methods) {
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

    public void handleExpressionStatement(Statement st) {
        if (st == null || !st.isExpressionStmt())
            return;

        Expression exp = st.asExpressionStmt().getExpression();
        if (exp.isVariableDeclarationExpr()) {
            VariableDeclarationExpr vdexp = exp.asVariableDeclarationExpr();
            List<VariableDeclarator> variables = vdexp.getVariables();
            handleVariables(variables);
        }
        else if (exp.isMethodCallExpr()) {
            MethodCallExpr methodCall = exp.asMethodCallExpr();
            List<Expression> argList = methodCall.getArguments();
            if (argList != null) {
                for (Expression i : argList) {

                    if (i.isThisExpr()) {
                        Expression expr = i.asThisExpr().getClassExpr().orElse(null);
                        if(expr != null && expr.isNameExpr()) {
                            String name = expr.asNameExpr().getName().getIdentifier();
                            Boolean flag = compare(name);

                            int vstart_line_num = expr.getRange().get().begin.line;
                            int vstart_col_num = expr.getRange().get().begin.column;
                            int vend_col_num = expr.getRange().get().end.column;

                            if(flag){
                                ReplacementDataNode rnode = new ReplacementDataNode();
                                rnode.setLineNo(vstart_line_num);
                                rnode.setStartColNo(vstart_col_num);
                                rnode.setEndColNo(vend_col_num);
                                rnode.setReplacementString(getHexValue(name));
                                obfuscator.setArrayList(rnode);
                            }
                        }
                    }
                }
            }
        }
    }

    public void handleVariables(List<VariableDeclarator> variables) {
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {
                String vname = variable.getName().getIdentifier();
                String vtype = variable.getType().asString();

                Expression expression = variable.getInitializer().orElse(null);
                if (expression != null && expression.isObjectCreationExpr()) {
                    ClassOrInterfaceType expr = expression.asObjectCreationExpr().getType();
                    String type = expr.getName().getIdentifier();

                    int vstart_line_num = expr.getRange().get().begin.line;
                    int vstart_col_num = expr.getRange().get().begin.column;
                    int vend_col_num = expr.getRange().get().end.column;

                    boolean flag = compare(type);
                    if(flag){
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        rnode.setLineNo(vstart_line_num);
                        rnode.setStartColNo(vstart_col_num);
                        rnode.setEndColNo(vend_col_num);
                        rnode.setReplacementString(getHexValue(vname));
                        obfuscator.setArrayList(rnode);
                    }
                }

                int vstart_line_num = variable.getType().getRange().get().begin.line;
                int vstart_col_num = variable.getType().getRange().get().begin.column;
                int vend_col_num = variable.getType().getRange().get().end.column;

                Boolean flag = compare(vtype);
                if(flag){
                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(vstart_line_num);
                    rnode.setStartColNo(vstart_col_num);
                    rnode.setEndColNo(vend_col_num);
                    rnode.setReplacementString(getHexValue(vname));
                    obfuscator.setArrayList(rnode);
                }
            }
        }
    }

    public Boolean compare(String ObjType) {
        for (int x = 0; x < classList.size(); x++) {
            if (getClassNameFromFilePath(classList.get(x)).equals(ObjType))
                return true;
        }
        return false;
    }
}