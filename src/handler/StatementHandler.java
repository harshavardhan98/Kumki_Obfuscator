package handler;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.*;
import model.*;
import obfuscator.ClassObfuscator;
import obfuscator.MethodObfuscator;
import obfuscator.VariableObfuscator;

import java.util.List;

public class StatementHandler {

    private static ExpressionHandler expressionHandler;

    public StatementHandler(ExpressionHandler object) {

        if (object instanceof ClassExpressionHandler)
            expressionHandler = new ClassExpressionHandler(object);
        else if (object instanceof MethodExpressionHandler)
            expressionHandler = new MethodExpressionHandler(object);
        else if (object instanceof VariableExpressionHandler)
            expressionHandler = new VariableExpressionHandler(object);
    }

    public static void handleStatement(Statement statement, Scope parentScope) {
        if (statement == null || statement.isEmptyStmt())
            return;

        handleBlockStatement(statement, parentScope);
        handleExpressionStatement(statement, parentScope);
        handleIfStatement(statement, parentScope);
        handleForStatement(statement, parentScope);
        handleForEachStatement(statement, parentScope);
        handleWhileStatement(statement, parentScope);
        handleDoWhileStatement(statement, parentScope);
        handleReturnStatement(statement, parentScope);
        handleSwitchStatement(statement, parentScope);
        handleTryCatchStatement(statement, parentScope);
        handleSynchronisedStatement(statement, parentScope);
        handleExplicitConstructorInvocationStmt(statement, parentScope);
        handleAssertStmt(statement, parentScope);
    }

    private static void handleBlockStatement(Statement statement, Scope parentScope) {
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

    private static void handleExpressionStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isExpressionStmt())
            return;

        Expression exp = st.asExpressionStmt().getExpression();
        expressionHandler.handleExpression(exp, parentScope);
    }

    private static void handleIfStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isIfStmt())
            return;

        Scope ifScope = new Scope();
        ifScope.setScope(parentScope);

        Expression condition = st.asIfStmt().getCondition();
        expressionHandler.handleExpression(condition, parentScope);

        Statement thenStmt = st.asIfStmt().getThenStmt();
        ifScope = new Scope();
        ifScope.setScope(parentScope);
        handleStatement(thenStmt, ifScope);

        Statement elseStmt = st.asIfStmt().getElseStmt().orElse(null);
        ifScope = new Scope();
        ifScope.setScope(parentScope);
        handleStatement(elseStmt, ifScope);
    }

    private static void handleForStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isForStmt())
            return;

        Scope forLoopScope = new Scope();
        forLoopScope.setScope(parentScope);

        NodeList<Expression> n1 = st.asForStmt().getInitialization();
        if (n1.isNonEmpty()) {
            for (Expression e : n1)
                expressionHandler.handleExpression(e, forLoopScope);
        }

        expressionHandler.handleExpression(st.asForStmt().getCompare().orElse(null), forLoopScope);

        NodeList<Expression> n2 = st.asForStmt().getUpdate();
        if (n2.isNonEmpty()) {
            for (Expression e : n2)
                expressionHandler.handleExpression(e, forLoopScope);
        }

        Statement forStmt = st.asForStmt().getBody();
        handleStatement(forStmt, forLoopScope);
    }

    private static void handleForEachStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isForeachStmt())
            return;

        Scope forEachScope = new Scope();
        forEachScope.setScope(parentScope);

        NodeList<VariableDeclarator> variableDeclarators = st.asForeachStmt().getVariable().getVariables();
        if (expressionHandler instanceof ClassExpressionHandler)
            ClassObfuscator.handleVariables(variableDeclarators, forEachScope);
        else if (expressionHandler instanceof MethodExpressionHandler)
            MethodObfuscator.handleVariables(variableDeclarators, forEachScope);
        else if (expressionHandler instanceof VariableExpressionHandler)
            VariableObfuscator.handleVariables(variableDeclarators, forEachScope);

        expressionHandler.handleExpression(st.asForeachStmt().getIterable(), forEachScope);
        Statement forEach = st.asForeachStmt().getBody();
        handleStatement(forEach, forEachScope);
    }

    private static void handleWhileStatement(Statement statement, Scope parentScope) {
        if (statement == null || !statement.isWhileStmt())
            return;

        Scope whileScope = new Scope();
        whileScope.setScope(parentScope);

        WhileStmt whileStmt = statement.asWhileStmt();
        expressionHandler.handleExpression(whileStmt.getCondition(), whileScope);
        handleStatement(whileStmt.getBody(), whileScope);
    }

    private static void handleDoWhileStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isDoStmt())
            return;

        handleStatement(st.asDoStmt().getBody(), parentScope);
        expressionHandler.handleExpression(st.asDoStmt().getCondition(), parentScope);
    }

    private static void handleReturnStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isReturnStmt())
            return;

        expressionHandler.handleExpression(st.asReturnStmt().getExpression().orElse(null), parentScope);
    }

    private static void handleSwitchStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isSwitchStmt())
            return;

        SwitchStmt switchStmt = st.asSwitchStmt();
        expressionHandler.handleExpression(switchStmt.getSelector(), parentScope);

        List<SwitchEntryStmt> stList = switchStmt.getEntries();
        if (stList != null) {
            for (SwitchEntryStmt set : stList) {
                expressionHandler.handleExpression(set.getLabel().orElse(null), parentScope);
                List<Statement> expst = set.getStatements();
                if (!expst.isEmpty()) {
                    for (Statement est : expst)
                        handleStatement(est, parentScope);
                }
            }
        }
    }

    private static void handleTryCatchStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isTryStmt())
            return;

        handleStatement(st.asTryStmt().getTryBlock(), parentScope);

        List<CatchClause> l = st.asTryStmt().getCatchClauses();
        if (!l.isEmpty()) {
            for (CatchClause i : l) {
                handleParameter(i.getParameter(), parentScope);
                handleStatement(i.getBody(), parentScope);
            }
        }
    }

    private static void handleSynchronisedStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isSynchronizedStmt())
            return;

        expressionHandler.handleExpression(st.asSynchronizedStmt().getExpression(), parentScope);
        handleStatement(st.asSynchronizedStmt().getBody(), parentScope);
    }

    private static void handleExplicitConstructorInvocationStmt(Statement st, Scope parentScope) {
        if (st == null || !st.isExplicitConstructorInvocationStmt())
            return;

        ExplicitConstructorInvocationStmt ecst = st.asExplicitConstructorInvocationStmt();
        List<Expression> exp = ecst.getArguments();
        if (exp != null) {
            for (Expression e : exp)
                expressionHandler.handleExpression(e, parentScope);
        }
    }

    public static void handleAssertStmt(Statement statement, Scope parentScope) {
        if (statement == null || !statement.isAssertStmt())
            return;
        expressionHandler.handleExpression(statement.asAssertStmt().getCheck(), parentScope);
    }

    public static void handleParameter(Parameter p, Scope parentScope) {
        if (p == null)
            return;
        expressionHandler.handleParameter(p, parentScope);
    }


    public ExpressionHandler getExpressionHandler() {
        return expressionHandler;
    }
}
