package obfuscator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import handler.StatementHandler;
import handler.VariableExpressionHandler;
import model.ReplacementDataNode;
import model.Scope;
import java.util.List;
import static utils.Encryption.*;

public class VariableObfuscator extends Obfuscator implements Obfuscate {

    public static StatementHandler statementHandler;

    public VariableObfuscator() {
        super();
        statementHandler = new StatementHandler(new VariableExpressionHandler());
    }

    @Override
    public void obfuscate(CompilationUnit cu) {

    }

    @Override
    public void handleClass(ClassOrInterfaceDeclaration clas, Scope scope) {
        if (clas == null)
            return;

        Scope class_scope = new Scope();
        class_scope.setScope(scope);

        List<FieldDeclaration> global_fields = clas.getFields();
        if (!global_fields.isEmpty()) {
            for (FieldDeclaration field : global_fields) {
                List<VariableDeclarator> global_variables = field.getVariables();
                handleVariables(global_variables, class_scope);
            }
        }

        //Constructors
        List<ConstructorDeclaration> constructors = clas.getConstructors();
        if (!constructors.isEmpty()) {
            for (ConstructorDeclaration constructor : constructors) {

                List<Parameter> parameterList = constructor.getParameters();
                if (!parameterList.isEmpty()) {
                    for (Parameter p : parameterList)
                        statementHandler.handleParameter(p, class_scope);
                }

                BlockStmt block = constructor.getBody();
                statementHandler.handleStatement(block, class_scope);
            }
        }

        List<BodyDeclaration<?>> members = clas.getMembers();
        if (!members.isEmpty()) {
            for (BodyDeclaration<?> bd : members) {

                if (bd.isMethodDeclaration()) {
                    MethodDeclaration method = bd.asMethodDeclaration();
                    handleMethodDeclaration(method, class_scope);
                } else if (bd.isClassOrInterfaceDeclaration())
                    handleClass(bd.asClassOrInterfaceDeclaration(), class_scope);
            }
        }
    }

    public static void handleVariables(List<VariableDeclarator> variables, Scope parentScope) {
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {

                String vtype = variable.getType().asString();
                String vname = variable.getName().getIdentifier();

                if (!keepField.contains(vname)) {
                    parentScope.setData(vname, vtype);

                    int vstart_line_num = variable.getName().getRange().get().begin.line;
                    int vstart_col_num = variable.getName().getRange().get().begin.column;
                    int vend_col_num = vstart_col_num + vname.length() - 1;

                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(vstart_line_num);
                    rnode.setStartColNo(vstart_col_num);
                    rnode.setEndColNo(vend_col_num);
                    rnode.setReplacementString(appendUnderScore(getHexValue(vname)));
                    obfuscatorConfig.setArrayList(rnode);
                }

                Expression expression = variable.getInitializer().orElse(null);
                statementHandler.getExpressionHandler().handleExpression(expression, parentScope);
            }
        }
    }

    public static void handleMethodDeclaration(MethodDeclaration method, Scope parentScope) {
        BlockStmt block = method.getBody().orElse(null);
        if (block != null && block.getStatements().size() > 0) {

            Scope methodScope = new Scope();
            methodScope.setScope(parentScope);

            List<Parameter> parametersList = method.getParameters();
            if (!parametersList.isEmpty()) {
                for (Parameter p : parametersList)
                    statementHandler.handleParameter(p, methodScope);
            }

            statementHandler.handleStatement(block, methodScope);
        }
    }
}
