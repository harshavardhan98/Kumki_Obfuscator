package handler;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import model.MethodModel;
import model.ReplacementDataNode;
import model.Scope;
import obfuscator.Obfuscator;

import java.util.List;

import static obfuscator.Obfuscator.*;
import static utils.Encryption.getHexValue;

public class MethodExpressionHandler extends ExpressionHandler {

    public MethodExpressionHandler() {
    }

    public MethodExpressionHandler(ExpressionHandler object) {
        super(object);
    }

    public void handleMethodCallExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isMethodCallExpr())
            return;

        MethodCallExpr methodCall = exp.asMethodCallExpr();

        String methodName = methodCall.getName().getIdentifier();
        MethodModel input = new MethodModel();
        input.setName(methodName);
        if (methodCall.getArguments() != null)
            input.setNoOfParameters(methodCall.getArguments().size());

        int mstart_line_num = methodCall.getName().getRange().get().begin.line;
        int mstart_col_num = methodCall.getName().getRange().get().begin.column;
        int mend_col_num = methodCall.getName().getRange().get().end.column;

        if (verifyUserDefinedMethod(input)) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(mstart_line_num);
            rnode.setStartColNo(mstart_col_num);
            rnode.setEndColNo(mend_col_num);
            rnode.setReplacementString(getHexValue(methodName));
            obfuscatorConfig.setArrayList(rnode);
        }

        // handling arguments
        List<Expression> argList = methodCall.getArguments();
        if (argList != null) {
            for (Expression i : argList)
                handleExpression(i, parentScope);
        }

        // handle the calling object
        Expression obj_name_exp = methodCall.getScope().orElse(null);
        handleExpression(obj_name_exp, parentScope);
    }

    @Override
    public void handleNameExpr(Expression exp, Scope parentScope) {

        //System.out.print("Name");

    }

    public void handleCastExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        CastExpr castExpr = exp.asCastExpr();
        handleExpression(castExpr.getExpression(), parentScope);
    }

    public void handleObjectCreationExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isObjectCreationExpr())
            return;

        ObjectCreationExpr expr = exp.asObjectCreationExpr();
        List<Expression> expList = expr.getArguments();
        if (expList != null) {
            for (Expression e : expList)
                handleExpression(e, parentScope);
        }

        List<BodyDeclaration<?>> bodyList = expr.getAnonymousClassBody().orElse(null);
        if (bodyList != null) {
            for (BodyDeclaration<?> e : bodyList) {
                if (e.isMethodDeclaration()) {
                    NodeList<Parameter> parameters = e.asMethodDeclaration().getParameters();

                    for (Parameter p : parameters)
                        handleParameter(p, parentScope);

                    StatementHandler.handleStatement(e.asMethodDeclaration().getBody().orElse(null), parentScope);
                }
            }
        }
    }

    public void handleArrayCreationExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isArrayCreationExpr())
            return;

        ArrayCreationExpr expr = exp.asArrayCreationExpr();
        List<ArrayCreationLevel> acl = expr.getLevels();
        if (!acl.isEmpty()) {
            for (ArrayCreationLevel ac : acl)
                handleExpression(ac.getDimension().orElse(null), parentScope);
        }

        handleExpression(expr.getInitializer().orElse(null), parentScope);
    }

    @Override
    public void handleParameter(Parameter p, Scope parentScope) {

        if (p == null)
            return;
        if (p.getName() != null) {
            String pname = p.getNameAsString();
            String ptype = p.getType().asString();
            parentScope.setData(pname, ptype);
        }
    }
}
