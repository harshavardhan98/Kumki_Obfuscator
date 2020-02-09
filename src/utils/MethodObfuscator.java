package utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import model.Obfuscator;
import model.ReplacementDataNode;
import model.Scope;

import javax.sound.midi.SysexMessage;
import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static utils.CommonUtils.*;
import static utils.Constants.classList;
import static utils.Constants.methodMap;
import static utils.FileOperation.getClassNameFromFilePath;

public class MethodObfuscator {

    private Obfuscator obfuscator;

    public void obfuscate() {
        try {
            for (String filePath : classList) {
                File file = new File(filePath);

//                if(!file.getName().contains("BusRoutesActivity"))
//                    continue;

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
        handleForStatement(statement,parentScope);
        handleForEachStatement(statement,parentScope);
        handleWhileStatement(statement,parentScope);
        handleDoWhileStatement(statement,parentScope);
        handleReturnStatement(statement,parentScope);
        handleSwitchStatement(statement,parentScope);
        handleTryCatchStatement(statement,parentScope);
        handleSynchronisedStatement(statement,parentScope);
        handleExplicitConstructorInvocationStmt(statement,parentScope);
    }

    public void handleExplicitConstructorInvocationStmt(Statement st,Scope parentScope) {
        if (st == null || !st.isExplicitConstructorInvocationStmt())
            return;

        ExplicitConstructorInvocationStmt ecst = st.asExplicitConstructorInvocationStmt();
        List<Expression> exp = ecst.getArguments();
        if (exp != null) {
            for (Expression e : exp)
                handleExpression(e,parentScope);
        }
    }


    private void handleSynchronisedStatement(Statement st,Scope parentScope){

        if(st==null || !st.isSynchronizedStmt())
            return;

        handleExpression(st.asSynchronizedStmt().getExpression(),parentScope);
        handleStatement(st.asSynchronizedStmt().getBody(),parentScope);
    }

    private void handleTryCatchStatement(Statement st,Scope parentScope) {

        if (st == null || !st.isTryStmt())
            return;

        handleStatement(st.asTryStmt().getTryBlock(),parentScope);

        List<CatchClause> l = st.asTryStmt().getCatchClauses();
        if (!l.isEmpty()) {
            for (CatchClause i : l) {
                handleParameter(i.getParameter(),parentScope);
                handleStatement(i.getBody(),parentScope);
            }
        }
    }


    public void handleSwitchStatement(Statement st,Scope parentScope) {
        if (st == null || !st.isSwitchStmt())
            return;

        SwitchStmt switchStmt = st.asSwitchStmt();
        handleExpression(switchStmt.getSelector(),parentScope);

        List<SwitchEntryStmt> stList = switchStmt.getEntries();
        if (stList != null) {
            for (SwitchEntryStmt set : stList) {
                handleExpression(set.getLabel().orElse(null),parentScope);
                List<Statement> expst = set.getStatements();
                if (!expst.isEmpty()) {
                    for (Statement est : expst)
                        handleStatement(est,parentScope);
                }
            }
        }
    }


    private void handleReturnStatement(Statement st,Scope parentScope) {
        if (st == null || !st.isReturnStmt())
            return;

        handleExpression(st.asReturnStmt().getExpression().orElse(null),parentScope);
    }

    private void handleDoWhileStatement(Statement st,Scope parentScope){

        if(st==null || !st.isDoStmt())
            return;

        handleStatement(st.asDoStmt().getBody(),parentScope);
        handleExpression(st.asDoStmt().getCondition(),parentScope);
    }

    public void handleWhileStatement(Statement statement,Scope parentScope){

        if(statement==null||!statement.isWhileStmt())
            return;

        WhileStmt whileStmt=statement.asWhileStmt();
        handleExpression(whileStmt.getCondition(),parentScope);
        handleBlockStatement(whileStmt.getBody(),parentScope);

    }

    private void handleForEachStatement(Statement st,Scope parentScope) {
        if (st == null || !st.isForeachStmt())
            return;

        Scope forEachScope=new Scope();
        forEachScope.setScope(parentScope);

        NodeList<VariableDeclarator> variableDeclarators = st.asForeachStmt().getVariable().getVariables();
        handleVariables(variableDeclarators,forEachScope);
        handleExpression(st.asForeachStmt().getIterable(),forEachScope);
        Statement forEach = st.asForeachStmt().getBody();
        handleStatement(forEach,forEachScope);
    }

    private void handleForStatement(Statement st,Scope parentScope) {

        if (st == null || !st.isForStmt())
            return;

        Scope forLoopScope=new Scope();
        forLoopScope.setScope(parentScope);

        NodeList<Expression> n1 = st.asForStmt().getInitialization();
        if (n1.isNonEmpty()) {
            for (Expression e : n1)
                handleExpression(e,forLoopScope);
        }

        handleExpression(st.asForStmt().getCompare().orElse(null),forLoopScope);

        NodeList<Expression> n2 = st.asForStmt().getUpdate();
        if (n2.isNonEmpty()) {
            for (Expression e : n2)
                handleExpression(e,forLoopScope);
        }

        Statement forStmt = st.asForStmt().getBody();
        handleStatement(forStmt,forLoopScope);
    }

    public void handleExpression(Expression exp,Scope parentScope) {

        if (exp == null)
            return;

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
            if (obj_name_exp != null) {
                if (obj_name_exp.isNameExpr()) {
                    String obj_name = obj_name_exp.asNameExpr().getName().getIdentifier();
                    String obj_type = parentScope.findDataTypeOfIdentifier(obj_name);
                    if (obj_type != null && compare(obj_type)&&toBeReplaced(mname)) {
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        rnode.setLineNo(mstart_line_num);
                        rnode.setStartColNo(mstart_col_num);
                        rnode.setEndColNo(mend_col_num);
                        rnode.setReplacementString(getHexValue(mname));
                        obfuscator.setArrayList(rnode);
                    }
                }
                else if (obj_name_exp.isSuperExpr() && toBeReplaced(mname)){
                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(mstart_line_num);
                    rnode.setStartColNo(mstart_col_num);
                    rnode.setEndColNo(mend_col_num);
                    rnode.setReplacementString(getHexValue(mname));
                    obfuscator.setArrayList(rnode);
                }
            }
            else if(toBeReplaced(mname)){
                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(mstart_line_num);
                rnode.setStartColNo(mstart_col_num);
                rnode.setEndColNo(mend_col_num);
                rnode.setReplacementString(getHexValue(mname));
                obfuscator.setArrayList(rnode);
            }else{
                System.out.print("");
            }

        }
        else if(exp.isAssignExpr()){
            AssignExpr expr = exp.asAssignExpr();

            //eg: darkMode = MainActivity.getHelloMain("dark_mode");
            handleExpression(expr.getTarget(),parentScope);
            handleExpression(expr.getValue(),parentScope);
        }
        else if (exp.isUnaryExpr()) {
            UnaryExpr expr = exp.asUnaryExpr();
            handleExpression(expr.getExpression(),parentScope);
        } else if (exp.isBinaryExpr()) {

            BinaryExpr expr = exp.asBinaryExpr();
            handleExpression(expr.getLeft(),parentScope);
            handleExpression(expr.getRight(),parentScope);

        }else if(exp.isCastExpr()){
            CastExpr castExpr=exp.asCastExpr();
            handleExpression(castExpr.getExpression(),parentScope);
        }else if (exp.isThisExpr()) {
            exp = exp.asThisExpr().getClassExpr().orElse(null);
            handleExpression(exp,parentScope);
        }else if(exp.isObjectCreationExpr()){

            ObjectCreationExpr expr=exp.asObjectCreationExpr();

            List<Expression> expList = expr.getArguments();
            if (expList != null) {
                for (Expression e : expList)
                    handleExpression(e,parentScope);
            }

//            List<BodyDeclaration<?>> bodyList = expr.getAnonymousClassBody().orElse(null);
//            if (bodyList != null) {
//                for (BodyDeclaration<?> e : bodyList)
//                    if (e.isMethodDeclaration())
//                        handleMethodDeclaration(e.asMethodDeclaration(),parentScope);
//            }
        }else if (exp.isEnclosedExpr()) {
            EnclosedExpr expr = exp.asEnclosedExpr();
            handleExpression(expr.getInner(),parentScope);
        }else if(exp.isArrayAccessExpr()){
            ArrayAccessExpr arrayAccessExpr=exp.asArrayAccessExpr();
            handleExpression(arrayAccessExpr.getName(),parentScope);
        }else if (exp.isArrayInitializerExpr()) {
            ArrayInitializerExpr expr = exp.asArrayInitializerExpr();
            List<Expression> exList = expr.getValues();
            if(!exList.isEmpty()){
                for(Expression e:exList)
                    handleExpression(e,parentScope);
            }
        }else if (exp.isArrayCreationExpr()) {
            ArrayCreationExpr expr = exp.asArrayCreationExpr();
//            if (expr.getElementType().isClassOrInterfaceType())
//                handleClassInterfaceType(expr.getElementType().asClassOrInterfaceType());

            List<ArrayCreationLevel> acl = expr.getLevels();
            if (!acl.isEmpty()) {
                for (ArrayCreationLevel ac : acl)
                    handleExpression(ac.getDimension().orElse(null),parentScope);
            }

            handleExpression(expr.getInitializer().orElse(null),parentScope);
        }
        else if(exp.isConditionalExpr()){
            ConditionalExpr conditionalExpr=exp.asConditionalExpr();
            handleExpression(conditionalExpr.getCondition(),parentScope);
            handleExpression(conditionalExpr.getThenExpr(),parentScope);
            handleExpression(conditionalExpr.getElseExpr(),parentScope);
        }
    }


    public void handleExpressionStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isExpressionStmt())
            return;

        Expression exp = st.asExpressionStmt().getExpression();
        handleExpression(exp,parentScope);

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

    public void handleVariables(List<VariableDeclarator> variables, Scope parentScope) {
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {
                String vname = variable.getName().getIdentifier();
                String vtype = variable.getType().asString();
                parentScope.setData(vname, vtype);

                Expression expression = variable.getInitializer().orElse(null);
                handleExpression(expression,parentScope);
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

    public void handleParameter(Parameter p,Scope parentScope) {

        if (p == null)
            return;
        if (p.getName()!=null) {
            String pname=p.getNameAsString();
            String ptype=p.getType().asString();
            parentScope.setData(pname,ptype);
        }

    }
}