package obfuscator;

import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.stmt.BlockStmt;
import handler.MethodExpressionHandler;
import handler.StatementHandler;
import model.MethodModel;
import model.ReplacementDataNode;
import model.Scope;

import java.util.List;

import static utils.CommonUtils.getBasePackage;
import static utils.Encryption.getHexValue;

public class MethodObfuscator extends Obfuscator implements Obfuscate {

    public static StatementHandler statementHandler;

    public MethodObfuscator() {
        super();
        statementHandler = new StatementHandler(new MethodExpressionHandler());
    }

    public void handleImport(Name name) {

        if (name == null)
            return;

        if (verifyUserDefinedMethodbyName(name.getIdentifier())) {
            TokenRange tokenRange = name.getTokenRange().orElse(null);
            if (tokenRange != null) {
                Range range = tokenRange.getEnd().getRange().orElse(null);
                if (range != null) {
                    int start_line_no = range.begin.line;
                    int start_col_no = range.begin.column;
                    int end_col_no = range.end.column;

                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(start_line_no);
                    rnode.setStartColNo(start_col_no);
                    rnode.setEndColNo(end_col_no);
                    rnode.setReplacementString(getHexValue(name.getIdentifier()));
                    obfuscatorConfig.setArrayList(rnode);
                }
            }
        }


        handleImport(name.getQualifier().orElse(null));
    }

    @Override
    public void obfuscate(CompilationUnit cu) {
        for (int i = 0; i < cu.getImports().size(); i++) {
            String imports = cu.getImports().get(i).getName().toString();
            if (imports.startsWith(getBasePackage()))
                handleImport(cu.getImports().get(i).getName());
        }

    }

    @Override
    public void handleClass(ClassOrInterfaceDeclaration clas,Scope scope) {

        if (clas == null)
            return;

        Scope classScope = new Scope();
        classScope.setParentScope(scope);

        // variable declarator
        List<FieldDeclaration> global_fields = clas.getFields();
        if (!global_fields.isEmpty()) {
            for (FieldDeclaration field : global_fields) {
                List<VariableDeclarator> global_variables = field.getVariables();
                handleVariables(global_variables, classScope);
            }
        }

        //Constructors
        List<ConstructorDeclaration> constructors = clas.getConstructors();
        if (!constructors.isEmpty()) {
            for (ConstructorDeclaration constructor : constructors) {
                BlockStmt block = constructor.getBody();
                statementHandler.handleStatement(block, classScope);
            }
        }

        List<MethodDeclaration> methods = clas.getMethods();
        if (!methods.isEmpty()) {
            for (MethodDeclaration method : methods) {

                Scope methodScope = new Scope();
                methodScope.setScope(methodScope);

                BlockStmt block = method.getBody().orElse(null);
                statementHandler.handleStatement(block, methodScope);

                handleMethodDeclaration(method);
            }
        }

        List<BodyDeclaration<?>> members = clas.getMembers();
        if (!members.isEmpty()) {
            for (BodyDeclaration<?> bd : members) {
                if (bd.isClassOrInterfaceDeclaration()) {
                    handleClass(bd.asClassOrInterfaceDeclaration(),classScope);
                }
            }
        }
    }

    public static void handleVariables(List<VariableDeclarator> variables, Scope parentScope) {
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {
                //Object Initialisation
                Expression expression = variable.getInitializer().orElse(null);
                statementHandler.getExpressionHandler().handleExpression(expression, parentScope);
            }
        }
    }


    public static void handleMethodDeclaration(MethodDeclaration method) {

        if (method == null)
            return;

        String name = method.getName().getIdentifier();
        MethodModel input = new MethodModel();
        input.setName(name);
        if (method.getParameters() != null)
            input.setNoOfParameters(method.getParameters().size());

        int start_line_num = method.getName().getRange().get().begin.line;
        int start_col_num = method.getName().getRange().get().begin.column;
        int end_col_num = method.getName().getRange().get().end.column;

        if (verifyUserDefinedMethod(input)) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(start_line_num);
            rnode.setStartColNo(start_col_num);
            rnode.setEndColNo(end_col_num);
            rnode.setReplacementString(getHexValue(name));
            obfuscatorConfig.setArrayList(rnode);
        }
    }


}
