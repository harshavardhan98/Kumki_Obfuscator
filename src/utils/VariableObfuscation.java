package utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import model.Obfuscator;
import model.ReplacementDataNode;
import model.Scope;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static utils.CommonUtils.*;
import static utils.Constants.classList;
import static utils.FileOperation.getClassNameFromFilePath;

/*
    getViewById(R.id.name) -> R.id.btn_testing must never be changed
    Camel.b -> then b must be static and need to add b to the global scope
    Student arr[]; -> java parser library error
    constant obfuscation using unicode character
    need to implement array access expression in class obfuscator
 */

public class VariableObfuscation {

    private Obfuscator obfuscator;
    ArrayList<String> predefinedClassList;

    public void obfuscate(){
        predefinedClassList= loadPredefinedClassList();
        for (String s : classList) {
            try {
                File f = new File(s);
                if(!f.getName().contains("Main"))
                    continue;
                CompilationUnit cu = JavaParser.parse(f);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(f.getName())).orElse(null);
                if (clas == null)
                    clas = cu.getInterfaceByName(getClassNameFromFilePath(f.getName())).orElse(null);

                obfuscator = new Obfuscator();
                obfuscator.setCurrentFile(f);
                handleClass(clas);
                System.out.print("");
               obfuscator.replaceInFiles();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handleClass(ClassOrInterfaceDeclaration clas) {

        if (clas != null) {
            Scope global_scope = new Scope();
            global_scope.setScope(null);

            //Constructors
            List<ConstructorDeclaration> constructors = clas.getConstructors();
            if (!constructors.isEmpty()) {
                for (ConstructorDeclaration constructor : constructors) {

                    List<Parameter> parameterList = constructor.getParameters();
                    if (!parameterList.isEmpty()) {
                        for (Parameter p : parameterList)
                            handleParameter(p,global_scope);
                    }

                    BlockStmt block = constructor.getBody();
                    handleBlockStatement(block,global_scope);
                }
            }

            List<FieldDeclaration> global_fields = clas.getFields();
            if (!global_fields.isEmpty()) {
                for (FieldDeclaration field : global_fields) {
                    List<VariableDeclarator> global_variables = field.getVariables();
                    handleVariables(global_variables, global_scope);
                }
            }

            List<BodyDeclaration<?>> members = clas.getMembers();
            if (!members.isEmpty()) {
                for (BodyDeclaration<?> bd : members) {

                    if(bd.isMethodDeclaration()){
                        MethodDeclaration method=bd.asMethodDeclaration();
                        handleMethodDeclaration(method,global_scope);
                    }
                    else if(bd.isClassOrInterfaceDeclaration()){
                        handleClass(bd.asClassOrInterfaceDeclaration());
                    }
                }
            }
        }
    }

    public void handleStatement(Statement statement, Scope parentScope) {

        if (statement == null || statement.isEmptyStmt())
            return;

        handleExpressionStatement(statement,parentScope);
        handleBlockStatement(statement, parentScope);
        handleForStatement(statement,parentScope);
        handleForEachStatement(statement,parentScope);
        handleIfStatement(statement,parentScope);
        handleReturnStatement(statement,parentScope);
        handleSwitchStatement(statement,parentScope);
        handleTryCatchStatement(statement,parentScope);
        handleDoWhileStatement(statement,parentScope);
        handleSynchronisedStatement(statement,parentScope);
        handleExplicitConstructorInvocationStmt(statement,parentScope);
    }

    public void handleExpressionStatement(Statement st,Scope parentScope) {
        if (st == null || !st.isExpressionStmt())
            return;

        Expression exp = st.asExpressionStmt().getExpression();
        handleExpression(exp,parentScope);
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


    public void handleExpression(Expression exp,Scope parentScope) {

        if (exp == null)
            return;

        if (exp.isVariableDeclarationExpr()) {
            VariableDeclarationExpr vdexp = exp.asVariableDeclarationExpr();
            List<VariableDeclarator> variables = vdexp.getVariables();
            handleVariables(variables,parentScope);
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

        }else if(exp.isNameExpr()){

            NameExpr nameExpr=exp.asNameExpr();
            String name = nameExpr.getName().getIdentifier();
            int vstart_line_num = nameExpr.getRange().get().begin.line;
            int vstart_col_num = nameExpr.getRange().get().begin.column;
            int vend_col_num = nameExpr.getRange().get().end.column;
            // Collections-> Collections.binarySearch(predefinedClassList,name)<0
            boolean found=parentScope.checkIfGivenVariableExistsInScope(name);

            if(found){
                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(vstart_line_num);
                rnode.setStartColNo(vstart_col_num);
                rnode.setEndColNo(vend_col_num);
                rnode.setReplacementString(getHexValue(name));
                obfuscator.setArrayList(rnode);
            }

        }else if(exp.isFieldAccessExpr()){

            FieldAccessExpr fieldAccessExpr=exp.asFieldAccessExpr();
            handleExpression(fieldAccessExpr.getScope(),parentScope);

            SimpleName sname = fieldAccessExpr.getName();
            String name = sname.getIdentifier();
            int vstart_line_num = sname.getRange().get().begin.line;
            int vstart_col_num = sname.getRange().get().begin.column;
            int vend_col_num = sname.getRange().get().end.column;

            if(parentScope.checkIfGivenVariableExistsInScope(name)){
                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(vstart_line_num);
                rnode.setStartColNo(vstart_col_num);
                rnode.setEndColNo(vend_col_num);
                rnode.setReplacementString(getHexValue(name));
                obfuscator.setArrayList(rnode);
            }

        }else if(exp.isCastExpr()){
            CastExpr castExpr=exp.asCastExpr();
            handleExpression(castExpr.getExpression(),parentScope);
        } else if (exp.isMethodCallExpr()) {

            //Button btn_testing=findViewById(R.id.btn_testing);
            // R.id.btn_testing must not be changed

            MethodCallExpr methodCall = exp.asMethodCallExpr();
            List<Expression> argList = methodCall.getArguments();
            if (argList != null) {
                for (Expression i : argList)
                    handleExpression(i,parentScope);
            }
            //Static method calls
            exp = methodCall.getScope().orElse(null);
            handleExpression(exp,parentScope);
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

            List<BodyDeclaration<?>> bodyList = expr.getAnonymousClassBody().orElse(null);
            if (bodyList != null) {
                for (BodyDeclaration<?> e : bodyList)
                    if (e.isMethodDeclaration())
                        handleMethodDeclaration(e.asMethodDeclaration(),parentScope);
            }
        }else if (exp.isClassExpr()) {
            ClassExpr expr = exp.asClassExpr();
            if (expr.getType().isClassOrInterfaceType())
                handleClassInterfaceType(expr.getType().asClassOrInterfaceType());
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
            if (expr.getElementType().isClassOrInterfaceType())
                handleClassInterfaceType(expr.getElementType().asClassOrInterfaceType());

            List<ArrayCreationLevel> acl = expr.getLevels();
            if (!acl.isEmpty()) {
                for (ArrayCreationLevel ac : acl)
                    handleExpression(ac.getDimension().orElse(null),parentScope);
            }

            handleExpression(expr.getInitializer().orElse(null),parentScope);
        }
    }

    public void handleMethodDeclaration(MethodDeclaration method,Scope parentScope) {

        BlockStmt block = method.getBody().orElse(null);
        List<Statement> stList = block.getStatements();

        Scope methodScope = new Scope();
        methodScope.setScope(parentScope);

        List<Parameter> parametersList=method.getParameters();
        if(!parametersList.isEmpty()){
            for(Parameter p:parametersList)
                handleParameter(p,methodScope);
        }

        if (!stList.isEmpty()) {
            for (int i = 0; i < stList.size(); i++) {
                Statement st = stList.get(i);
                handleStatement(st, methodScope);
            }
        }
    }

    private void handleReturnStatement(Statement st,Scope parentScope) {
        if (st == null || !st.isReturnStmt())
            return;

        handleExpression(st.asReturnStmt().getExpression().orElse(null),parentScope);
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

    public void handleIfStatement(Statement st, Scope parentScope) {
        if (st == null || !st.isIfStmt())
            return;

        Scope ifScope;

        Expression condition = st.asIfStmt().getCondition();
        handleExpression(condition,parentScope);

        Statement thenStmt = st.asIfStmt().getThenStmt();
        ifScope = new Scope();
        ifScope.setScope(parentScope);
        handleStatement(thenStmt, ifScope);

        Statement elseStmt = st.asIfStmt().getElseStmt().orElse(null);
        ifScope = new Scope();
        ifScope.setScope(parentScope);
        handleStatement(elseStmt, ifScope);
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

    private void handleDoWhileStatement(Statement st,Scope parentScope){

        if(st==null || !st.isDoStmt())
            return;

        handleStatement(st.asDoStmt().getBody(),parentScope);
        handleExpression(st.asDoStmt().getCondition(),parentScope);
    }

    private void handleSynchronisedStatement(Statement st,Scope parentScope){

        if(st==null || !st.isSynchronizedStmt())
            return;

        handleExpression(st.asSynchronizedStmt().getExpression(),parentScope);
        handleStatement(st.asSynchronizedStmt().getBody(),parentScope);
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




    /*********************************************/

    public void handleVariables(List<VariableDeclarator> variables,Scope parentScope) {
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {

                String vtype = variable.getName().asString();
                String vname = variable.getName().getIdentifier();
                parentScope.setData(vname, vtype);

                int vstart_line_num = variable.getName().getRange().get().begin.line;
                int vstart_col_num = variable.getName().getRange().get().begin.column;
                int vend_col_num = variable.getName().getRange().get().end.column;


                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(vstart_line_num);
                rnode.setStartColNo(vstart_col_num);
                rnode.setEndColNo(vend_col_num);
                rnode.setReplacementString(getHexValue(vtype));
                obfuscator.setArrayList(rnode);

                Expression expression = variable.getInitializer().orElse(null);
                handleExpression(expression,parentScope);
            }
        }
    }

    public void handleParameter(Parameter p,Scope parentScope) {

        if (p == null)
            return;
        if (p.getName()!=null) {
            String pname=p.getNameAsString();
            String ptype=p.getType().asString();
            parentScope.setData(pname,ptype);

            int startLineNo=p.getName().getRange().get().begin.line;
            int startColNo=p.getName().getRange().get().begin.column;
            int endColNo=p.getName().getRange().get().end.column;

            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(startLineNo);
            rnode.setStartColNo(startColNo);
            rnode.setEndColNo(endColNo);
            rnode.setReplacementString(getHexValue(pname));
            obfuscator.setArrayList(rnode);
        }

    }

    public void handleClassInterfaceType(ClassOrInterfaceType cit) {
        if (cit == null)
            return;

        String name = cit.getName().getIdentifier();
        int start_line_num = cit.getName().getRange().get().begin.line;
        int start_col_num = cit.getName().getRange().get().begin.column;
        int end_col_num = cit.getName().getRange().get().end.column;

        Boolean flag = compare(name);
        if (flag) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(start_line_num);
            rnode.setStartColNo(start_col_num);
            rnode.setEndColNo(end_col_num);
            rnode.setReplacementString(getHexValue(name));
            obfuscator.setArrayList(rnode);
        }

        NodeList<Type> args = cit.getTypeArguments().orElse(null);
        if (args != null) {
            for (Type t : args) {
                if (t.isClassOrInterfaceType())
                    handleClassInterfaceType(t.asClassOrInterfaceType());
            }
        }

        ClassOrInterfaceType scope = cit.getScope().orElse(null);
        handleClassInterfaceType(scope);
    }

    /*********************************************/



}
