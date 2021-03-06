package handler;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import model.Scope;
import obfuscator.ClassObfuscator;
import obfuscator.MethodObfuscator;
import obfuscator.VariableObfuscator;
import utils.Constants.*;

import java.util.List;

public class ExpressionHandler {
    Mode currentMode;

    public ExpressionHandler() {
    }

    public ExpressionHandler(ExpressionHandler object) {
        if (object instanceof ClassExpressionHandler)
            this.currentMode = Mode.CLASS;
        else if (object instanceof MethodExpressionHandler)
            this.currentMode = Mode.METHOD;
        else if (object instanceof VariableExpressionHandler)
            this.currentMode = Mode.VARIABLE;
    }

    public void handleExpression(Expression exp, Scope parentScope) {

        if (exp == null)
            return;

        handleVariableDeclarationExpr(exp, parentScope);
        handleAssignExpr(exp, parentScope);
        handleUnaryExpr(exp, parentScope);
        handleBinaryExpr(exp, parentScope);
        handleThisExpr(exp, parentScope);
        handleEnclosedExpr(exp, parentScope);
        handleArrayAccessExpr(exp, parentScope);
        handleArrayInitializerExpr(exp, parentScope);
        handleConditionalExpr(exp, parentScope);
        handleCastExpr(exp, parentScope);
        handleObjectCreationExpr(exp, parentScope);
        handleArrayCreationExpr(exp, parentScope);
        handleMethodCallExpr(exp, parentScope);
        handleClassExpr(exp, parentScope);
        handleFieldAccessExpr(exp, parentScope);
        handleNameExpr(exp, parentScope);
        handleInstanceOfExpr(exp, parentScope);
        handleStringLiteralExpr(exp, parentScope);
        handleDoubleLiteralExpr(exp, parentScope);
        handleIntegerLiteralExpr(exp, parentScope);
        handleCharLiteralExpr(exp, parentScope);

    }

    public void handleVariableDeclarationExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isVariableDeclarationExpr())
            return;

        VariableDeclarationExpr vdexp = exp.asVariableDeclarationExpr();
        List<VariableDeclarator> variables = vdexp.getVariables();

        if (currentMode == Mode.CLASS)
            ClassObfuscator.handleVariables(variables, parentScope);
        else if (currentMode == Mode.METHOD)
            MethodObfuscator.handleVariables(variables, parentScope);
        else if (currentMode == Mode.VARIABLE)
            VariableObfuscator.handleVariables(variables, parentScope);
    }

    public void handleAssignExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isAssignExpr())
            return;

        AssignExpr expr = exp.asAssignExpr();
        handleExpression(expr.getTarget(), parentScope);
        handleExpression(expr.getValue(), parentScope);
    }

    public void handleUnaryExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isUnaryExpr())
            return;

        UnaryExpr expr = exp.asUnaryExpr();
        handleExpression(expr.getExpression(), parentScope);
    }

    public void handleThisExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isThisExpr())
            return;

        exp = exp.asThisExpr().getClassExpr().orElse(null);
        handleExpression(exp, parentScope);
    }

    public void handleEnclosedExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isEnclosedExpr())
            return;

        EnclosedExpr expr = exp.asEnclosedExpr();
        handleExpression(expr.getInner(), parentScope);
    }

    public void handleBinaryExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isBinaryExpr())
            return;

        BinaryExpr expr = exp.asBinaryExpr();
        handleExpression(expr.getLeft(), parentScope);
        handleExpression(expr.getRight(), parentScope);
    }

    public void handleArrayAccessExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isArrayAccessExpr())
            return;

        ArrayAccessExpr arrayAccessExpr = exp.asArrayAccessExpr();
        handleExpression(arrayAccessExpr.getName(), parentScope);
    }

    public void handleArrayInitializerExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isArrayInitializerExpr())
            return;

        ArrayInitializerExpr expr = exp.asArrayInitializerExpr();
        List<Expression> exList = expr.getValues();
        if (!exList.isEmpty()) {
            for (Expression e : exList)
                handleExpression(e, parentScope);
        }
    }

    public void handleConditionalExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isConditionalExpr())
            return;

        ConditionalExpr conditionalExpr = exp.asConditionalExpr();
        handleExpression(conditionalExpr.getCondition(), parentScope);
        handleExpression(conditionalExpr.getThenExpr(), parentScope);
        handleExpression(conditionalExpr.getElseExpr(), parentScope);
    }

    public void handleInstanceOfExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isInstanceOfExpr())
            return;

        InstanceOfExpr instanceOfExpr = exp.asInstanceOfExpr();
        handleExpression(instanceOfExpr.getExpression(), parentScope);
    }

    /*******************************************************************/

    public void handleMethodCallExpr(Expression exp, Scope parentScope) {
    }

    public void handleCastExpr(Expression exp, Scope parentScope) {
    }

    public void handleObjectCreationExpr(Expression exp, Scope parentScope) {
    }

    public void handleArrayCreationExpr(Expression exp, Scope parentScope) {
    }

    public void handleClassExpr(Expression exp, Scope parentScope) {
    }

    public void handleFieldAccessExpr(Expression exp, Scope parentScope) {
    }

    public void handleNameExpr(Expression exp, Scope parentScope) {
    }

    public void handleParameter(Parameter p, Scope parentScope) {
    }

    public void handleStringLiteralExpr(Expression exp, Scope parentScope) {
    }

    public void handleIntegerLiteralExpr(Expression exp, Scope parentScope) {
    }

    public void handleDoubleLiteralExpr(Expression exp, Scope parentScope) {
    }

    public void handleCharLiteralExpr(Expression exp, Scope parentScope) {
    }
}
