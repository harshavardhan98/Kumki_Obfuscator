package handler;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.expr.*;
import model.ReplacementDataNode;
import model.Scope;

import java.util.List;

import static utils.Encryption.*;

public class ClassExpressionHandler extends ExpressionHandler {

    @Override
    public void handleCastExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        CastExpr expr = exp.asCastExpr();
        if (expr.getType().isClassOrInterfaceType())
            handleClassInterfaceType(expr.getType().asClassOrInterfaceType());

        handleExpression(expr.getExpression());
    }

    @Override
    public void handleClassExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        ClassExpr expr = exp.asClassExpr();
        if (expr.getType().isClassOrInterfaceType())
            handleClassInterfaceType(expr.getType().asClassOrInterfaceType());
    }

    @Override
    public void handleMethodCallExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        //TODO: CHECK
        MethodCallExpr methodCall = exp.asMethodCallExpr();
        List<Expression> argList = methodCall.getArguments();
        if (argList != null) {
            for (Expression i : argList)
                handleExpression(i);
        }

        //Static method calls
        exp = methodCall.getScope().orElse(null);
        handleExpression(exp);
    }

    @Override
    public void handleFieldAccessExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        SimpleName sname = exp.asFieldAccessExpr().getName();
        String name = sname.getIdentifier();
        int vstart_line_num = sname.getRange().get().begin.line;
        int vstart_col_num = sname.getRange().get().begin.column;
        int vend_col_num = sname.getRange().get().end.column;

        Boolean flag = compare(name);
        if (flag) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(getHexValue(name));
            obfuscator.setArrayList(rnode);
        }
        //eg: m.behaviour(Mammal.count);
        handleExpression(exp.asFieldAccessExpr().getScope());
    }

    @Override
    public void handleNameExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        String name = exp.asNameExpr().getName().getIdentifier();
        int vstart_line_num = exp.getRange().get().begin.line;
        int vstart_col_num = exp.getRange().get().begin.column;
        int vend_col_num = exp.getRange().get().end.column;

        Boolean flag = compare(name);
        if (flag) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(getHexValue(name));
            //obfuscator.setArrayList(rnode);
        }
    }

    @Override
    public void handleObjectCreationExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        //TODO: CHECK
        ObjectCreationExpr expr = exp.asObjectCreationExpr();
        String type = expr.getType().getName().getIdentifier();
        int vstart_line_num = expr.getType().getRange().get().begin.line;
        int vstart_col_num = expr.getType().getRange().get().begin.column;
        int vend_col_num = expr.getType().getRange().get().end.column;

        Boolean flag = compare(type);
        if (flag) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(getHexValue(type));
            //obfuscator.setArrayList(rnode);
        }

        if (expr.getType().isClassOrInterfaceType())
            handleClassInterfaceType(expr.getType().asClassOrInterfaceType());

        List<Expression> expList = expr.getArguments();
        if (expList != null) {
            for (Expression e : expList)
                handleExpression(e);
        }

        List<BodyDeclaration<?>> bodyList = expr.getAnonymousClassBody().orElse(null);
        if (bodyList != null) {
            for (BodyDeclaration<?> e : bodyList)
                if (e.isMethodDeclaration())
                    handleMethodDeclaration(e.asMethodDeclaration());
        }
    }

    @Override
    public void handleArrayCreationExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        ArrayCreationExpr expr = exp.asArrayCreationExpr();
        if (expr.getElementType().isClassOrInterfaceType())
            handleClassInterfaceType(expr.getElementType().asClassOrInterfaceType());

        List<ArrayCreationLevel> acl = expr.getLevels();
        if (!acl.isEmpty()) {
            for (ArrayCreationLevel ac : acl)
                handleExpression(ac.getDimension().orElse(null));
        }

        handleExpression(expr.getInitializer().orElse(null));
    }
}
