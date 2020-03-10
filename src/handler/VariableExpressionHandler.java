package handler;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import model.ReplacementDataNode;
import model.Scope;
import obfuscator.Obfuscator;
import obfuscator.VariableObfuscator;
import utils.Constants.*;

import java.util.List;

import static utils.Encryption.*;

public class VariableExpressionHandler extends ExpressionHandler {

    public VariableExpressionHandler() {
    }

    public VariableExpressionHandler(ExpressionHandler object) {
        super(object);
    }

    @Override
    public void handleNameExpr(Expression exp, Scope parentScope) {

        if (exp == null || !exp.isNameExpr())
            return;

        NameExpr nameExpr = exp.asNameExpr();
        String name = nameExpr.getName().getIdentifier();

        int vstart_line_num = nameExpr.getRange().get().begin.line;
        int vstart_col_num = nameExpr.getRange().get().begin.column;
        int vend_col_num = nameExpr.getRange().get().end.column;

        if (parentScope.checkIfGivenVariableExistsInScope(name)) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(appendUnderScore(getHexValue(name)));
            Obfuscator.updateObfuscatorConfig(rnode);
        }
    }

    @Override
    public void handleFieldAccessExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isFieldAccessExpr())
            return;

        FieldAccessExpr fieldAccessExpr = exp.asFieldAccessExpr();

        FieldAccessExpr temp = fieldAccessExpr;
        if (temp.getScope() != null && temp.getScope().isFieldAccessExpr()) {
            temp = temp.getScope().asFieldAccessExpr();
            if (temp != null && temp.getScope().isNameExpr()) {
                if (temp.getScope().asNameExpr().getNameAsString().equals("R"))
                    return;
            }
        }

        handleExpression(fieldAccessExpr.getScope(), parentScope);

        if (parentScope.checkIfGivenVariableExistsInScope(fieldAccessExpr.getNameAsString())) {
            String name = fieldAccessExpr.getName().getIdentifier();
            int vstart_line_num = fieldAccessExpr.getName().getRange().get().begin.line;
            int vstart_col_num = fieldAccessExpr.getName().getRange().get().begin.column;
            int vend_col_num = fieldAccessExpr.getName().getRange().get().end.column;

            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(appendUnderScore(getHexValue(name)));
            Obfuscator.updateObfuscatorConfig(rnode);
        }
    }

    @Override
    public void handleMethodCallExpr(Expression exp, Scope parentScope) {

        if (exp == null || !exp.isMethodCallExpr())
            return;

        MethodCallExpr methodCall = exp.asMethodCallExpr();
        List<Expression> argList = methodCall.getArguments();
        if (argList != null) {
            for (Expression i : argList)
                handleExpression(i, parentScope);
        }

        //Static method calls
        handleExpression(methodCall.getScope().orElse(null), parentScope);
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
            for (BodyDeclaration<?> e : bodyList)
                if (e.isMethodDeclaration())
                    VariableObfuscator.handleMethodDeclaration(e.asMethodDeclaration(), parentScope);
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

    /*********************************************************
    //Constant Obfuscation

    @Override
    public void handleStringLiteralExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isStringLiteralExpr())
            return;

        StringLiteralExpr sExpr = exp.asStringLiteralExpr();
        if (sExpr.getValue().contains("\\u"))
            return;

        ReplacementDataNode replacementDataNode = new ReplacementDataNode();
        replacementDataNode.setReplacementString(getUnicodeExpression(sExpr.getValue(), Mode.STRING_CONSTANT_OBFUSCATION));
        replacementDataNode.setLineNo(sExpr.getRange().get().begin.line);
        replacementDataNode.setStartColNo(sExpr.getRange().get().begin.column);
        replacementDataNode.setEndColNo(sExpr.getRange().get().end.column);
        Obfuscator.updateObfuscatorConfig(replacementDataNode);
    }

    @Override
    public void handleIntegerLiteralExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isIntegerLiteralExpr())
            return;

        IntegerLiteralExpr iExpr = exp.asIntegerLiteralExpr();
        ReplacementDataNode replacementDataNode = new ReplacementDataNode();
        replacementDataNode.setReplacementString(getUnicodeExpression(iExpr.getValue(), Mode.INTEGER_CONSTANT_OBFUSCATION));
        replacementDataNode.setLineNo(iExpr.getRange().get().begin.line);
        replacementDataNode.setStartColNo(iExpr.getRange().get().begin.column);
        replacementDataNode.setEndColNo(iExpr.getRange().get().end.column);
        Obfuscator.updateObfuscatorConfig(replacementDataNode);
    }

    @Override
    public void handleDoubleLiteralExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isDoubleLiteralExpr())
            return;

        DoubleLiteralExpr dExpr = exp.asDoubleLiteralExpr();
        ReplacementDataNode replacementDataNode = new ReplacementDataNode();
        replacementDataNode.setReplacementString(getUnicodeExpression(dExpr.getValue(), Mode.DOUBLE_CONSTANT_OBFUSCATION));
        replacementDataNode.setLineNo(dExpr.getRange().get().begin.line);
        replacementDataNode.setStartColNo(dExpr.getRange().get().begin.column);
        replacementDataNode.setEndColNo(dExpr.getRange().get().end.column);
        Obfuscator.updateObfuscatorConfig(replacementDataNode);
    }

    @Override
    public void handleCharLiteralExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCharLiteralExpr())
            return;

        CharLiteralExpr cExpr = exp.asCharLiteralExpr();
        ReplacementDataNode replacementDataNode = new ReplacementDataNode();
        replacementDataNode.setReplacementString(getUnicodeExpression(cExpr.getValue(), Mode.CHAR_CONSTANT_OBFUSCATION));
        replacementDataNode.setLineNo(cExpr.getRange().get().begin.line);
        replacementDataNode.setStartColNo(cExpr.getRange().get().begin.column);
        replacementDataNode.setEndColNo(cExpr.getRange().get().end.column);
        Obfuscator.updateObfuscatorConfig(replacementDataNode);
    }

    /**********************************************************/

    public void handleParameter(Parameter p, Scope parentScope) {

        if (p == null)
            return;
        if (p.getName() != null) {
            String pname = p.getNameAsString();
            String ptype = p.getType().asString();

            if (!Obfuscator.keepField.contains(pname)) {
                parentScope.setData(pname, ptype);

                int startLineNo = p.getName().getRange().get().begin.line;
                int startColNo = p.getName().getRange().get().begin.column;
                int endColNo = p.getName().getRange().get().end.column;

                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(startLineNo);
                rnode.setStartColNo(startColNo);
                rnode.setEndColNo(endColNo);
                rnode.setReplacementString(appendUnderScore(getHexValue(pname)));
                Obfuscator.updateObfuscatorConfig(rnode);
            }
        }
    }
}
