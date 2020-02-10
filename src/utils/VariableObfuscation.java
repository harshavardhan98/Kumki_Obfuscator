package utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
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
import java.util.*;

import static utils.CommonUtils.*;
import static utils.Constants.*;
import static utils.FileOperation.getClassNameFromFilePath;

/*
    getViewById(R.id.name) -> R.id.btn_testing must never be changed
    Camel.b
    -> if Camel is a class then b must be static and need to add b to the global scope
    -> if Camel is a member of a class then b must be a public data member, need to change the b also

    while loop without a body
    static inner class
    assert statement
    new Club[100] -> then Club must not be obfuscated
    Configuration configuration.SSID

    Student arr[]; -> java parser library error
    constant obfuscation using unicode character
    need to implement array access expression in class obfuscator
 */

public class VariableObfuscation {

    enum Mode{
      INTEGER,STRING,DOUBLE,CHAR;
    };

    private Obfuscator obfuscator;

    HashMap<String,HashMap<String,String>> dataMembers=new HashMap<>();

    public void obfuscate(){


        collectPublicDataMembersOfClass();
        Scope global_scope = new Scope();
        global_scope.setScope(null);
        buildGlobalSymbolTable(global_scope);

        for (String s : classList) {
            try {
                File f = new File(s);
//                if(!f.getName().contains("MainActivity"))
//                    continue;

                CompilationUnit cu = JavaParser.parse(f);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(f.getName())).orElse(null);
                if (clas == null)
                    clas = cu.getInterfaceByName(getClassNameFromFilePath(f.getName())).orElse(null);

                obfuscator = new Obfuscator();
                obfuscator.setCurrentFile(f);
                Scope classScope=new Scope();
                classScope.setScope(global_scope);
                if(clas.getExtendedTypes().size()>0)
                    handleExtendingClass(classScope,clas.getExtendedTypes().get(0).getName().toString());
                handleClass(clas,classScope);
                System.out.print("");
                obfuscator.replaceInFiles();
               System.out.println("last file replaced in variable obfuscation is "+f.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    void collectPublicDataMembersOfClass(){

        for(String s:classList){

            try{
                File f=new File(s);
                CompilationUnit cu = JavaParser.parse(f);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(f.getName())).orElse(null);
                if (clas == null)
                    clas = cu.getInterfaceByName(getClassNameFromFilePath(f.getName())).orElse(null);

                List<FieldDeclaration> memberField = clas.getFields();
                if (!memberField.isEmpty()) {
                    for (FieldDeclaration field : memberField) {
                        List<VariableDeclarator> memberVariables = field.getVariables();
                        EnumSet<Modifier> modifiers=field.getModifiers();
                        Boolean isPublic=false,isStatic=false;

                        for(Modifier m:modifiers){
                            if(m.toString().equals("PUBLIC"))
                                isPublic=true;
                            if(m.toString().equals("STATIC"))
                                isStatic=true;
                        }

                        if(isPublic && !isStatic){

                            HashMap<String,String> variables=new HashMap<>();

                            if (!memberVariables.isEmpty()) {
                                for (VariableDeclarator variable : memberVariables) {
                                    String vtype = variable.getType().asString();
                                    String vname = variable.getName().getIdentifier();
                                    //parentScope.setData(vname, vtype);
                                    variables.put(vname,vtype);
                                }
                            }

                            dataMembers.put(s,variables);
                        }

                    }
                }


            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }


    void handleExtendingClass(Scope scope,String extendingClassName){
        // check if the class being extended is a user defined class
        // if user defined add the public data members to the

        for(String s:classList){

            File f=new File(s);
            if(f.getName().equals(extendingClassName+".java")) {

                HashMap<String,String> temp=dataMembers.get(s);

                for(String str:temp.keySet()){
                    scope.setData(str,temp.get(str));
                }

                break;
            }
        }
    }

    void buildGlobalSymbolTable(Scope globalScope){

        // what if 2 static members have same identifier name but different datatype

        for(String s:classList){

            try{
                File f = new File(s);

                CompilationUnit cu = JavaParser.parse(f);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(f.getName())).orElse(null);
                if (clas == null)
                    clas = cu.getInterfaceByName(getClassNameFromFilePath(f.getName())).orElse(null);

                List<FieldDeclaration> global_fields = clas.getFields();
                if (!global_fields.isEmpty()) {
                    for (FieldDeclaration field : global_fields) {
                        List<VariableDeclarator> global_variables = field.getVariables();
                        EnumSet<Modifier> modifiers=field.getModifiers();
                        Boolean isPublic=false,isStatic=false;

                        for(Modifier m:modifiers){
                            if(m.toString().equals("PUBLIC"))
                                isPublic=true;
                            if(m.toString().equals("STATIC"))
                                isStatic=true;
                        }

                        if(isPublic&&isStatic)
                            addVariablesToScope(global_variables,globalScope);
                    }
                }

                List<BodyDeclaration<?>> members = clas.getMembers();
                if (!members.isEmpty()) {
                    for (BodyDeclaration<?> bd : members) {
                        if(bd.isClassOrInterfaceDeclaration()){
                            FileOperation.getClassNameFromFilePath(f.getAbsolutePath());
                            innerClassList.add(FileOperation.getClassNameFromFilePath(f.getAbsolutePath())+"."+bd.asClassOrInterfaceDeclaration().getName());
                            innerClassList.add(bd.asClassOrInterfaceDeclaration().getName().asString());
                        }
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }


    }

    public void handleClass(ClassOrInterfaceDeclaration clas,Scope global_scope) {

        if (clas != null) {
//            Scope global_scope = new Scope();
//            global_scope.setScope(null);

            List<FieldDeclaration> global_fields = clas.getFields();
            if (!global_fields.isEmpty()) {
                for (FieldDeclaration field : global_fields) {
                    List<VariableDeclarator> global_variables = field.getVariables();
                    handleVariables(global_variables, global_scope);
                }
            }

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


            List<BodyDeclaration<?>> members = clas.getMembers();
            if (!members.isEmpty()) {
                for (BodyDeclaration<?> bd : members) {

                    if(bd.isMethodDeclaration()){
                        MethodDeclaration method=bd.asMethodDeclaration();
                        handleMethodDeclaration(method,global_scope);
                    }
                    else if(bd.isClassOrInterfaceDeclaration()){
                        handleClass(bd.asClassOrInterfaceDeclaration(),global_scope);
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
        handleWhileStatement(statement,parentScope);
        handleDoWhileStatement(statement,parentScope);
        handleIfStatement(statement,parentScope);
        handleReturnStatement(statement,parentScope);
        handleSwitchStatement(statement,parentScope);
        handleTryCatchStatement(statement,parentScope);
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
            if (parentScope.checkIfGivenVariableExistsInScope(name)) {
                int vstart_line_num = nameExpr.getRange().get().begin.line;
                int vstart_col_num = nameExpr.getRange().get().begin.column;
                int vend_col_num = nameExpr.getRange().get().end.column;

                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(vstart_line_num);
                rnode.setStartColNo(vstart_col_num);
                rnode.setEndColNo(vend_col_num);
                rnode.setReplacementString(getHexValue(name));
                obfuscator.setArrayList(rnode);
            }
        }else if(exp.isFieldAccessExpr()){

            FieldAccessExpr fieldAccessExpr = exp.asFieldAccessExpr();
            System.out.print("");

            if(fieldAccessExpr.getScope().isThisExpr()){
                String name = fieldAccessExpr.getName().getIdentifier();
                int vstart_line_num = fieldAccessExpr.getName().getRange().get().begin.line;
                int vstart_col_num = fieldAccessExpr.getName().getRange().get().begin.column;
                int vend_col_num = fieldAccessExpr.getName().getRange().get().end.column;

                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(vstart_line_num);
                rnode.setStartColNo(vstart_col_num);
                rnode.setEndColNo(vend_col_num);
                rnode.setReplacementString(getHexValue(name));
                obfuscator.setArrayList(rnode);
            }
            else if(fieldAccessExpr.getScope().isFieldAccessExpr()){


                String identifier = fieldAccessExpr.getScope().asFieldAccessExpr().getName().getIdentifier();
                if(parentScope.checkIfGivenVariableIsFromUserDefinedClass(identifier)){
                    String name = fieldAccessExpr.getName().getIdentifier();
                    int vstart_line_num = fieldAccessExpr.getName().getRange().get().begin.line;
                    int vstart_col_num = fieldAccessExpr.getName().getRange().get().begin.column;
                    int vend_col_num = fieldAccessExpr.getName().getRange().get().end.column;

                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(vstart_line_num);
                    rnode.setStartColNo(vstart_col_num);
                    rnode.setEndColNo(vend_col_num);
                    rnode.setReplacementString(getHexValue(name));
                    obfuscator.setArrayList(rnode);
                }
                else if(fieldAccessExpr.isNameExpr())
                    handleExpression(fieldAccessExpr.asNameExpr(), parentScope);
                else if(fieldAccessExpr.getScope().isFieldAccessExpr())
                    handleExpression(fieldAccessExpr.getScope().asFieldAccessExpr(), parentScope);
            }
            else if(fieldAccessExpr.getScope().isNameExpr()) {
                String identifier = fieldAccessExpr.getScope().asNameExpr().getName().getIdentifier();
                if (checkForStaticVariableAccess(identifier)) {
                    //Constants.BUS_ALERTS

                    String name = fieldAccessExpr.getName().getIdentifier();
                    int vstart_line_num = fieldAccessExpr.getName().getRange().get().begin.line;
                    int vstart_col_num = fieldAccessExpr.getName().getRange().get().begin.column;
                    int vend_col_num = fieldAccessExpr.getName().getRange().get().end.column;

                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(vstart_line_num);
                    rnode.setStartColNo(vstart_col_num);
                    rnode.setEndColNo(vend_col_num);
                    rnode.setReplacementString(getHexValue(name));
                    obfuscator.setArrayList(rnode);
                }
                else {
                    if (parentScope.checkIfGivenVariableIsFromUserDefinedClass(identifier)) {
                        String name = fieldAccessExpr.getName().getIdentifier();
                        int vstart_line_num = fieldAccessExpr.getName().getRange().get().begin.line;
                        int vstart_col_num = fieldAccessExpr.getName().getRange().get().begin.column;
                        int vend_col_num = fieldAccessExpr.getName().getRange().get().end.column;

                        ReplacementDataNode rnode = new ReplacementDataNode();
                        rnode.setLineNo(vstart_line_num);
                        rnode.setStartColNo(vstart_col_num);
                        rnode.setEndColNo(vend_col_num);
                        rnode.setReplacementString(getHexValue(name));
                        obfuscator.setArrayList(rnode);
                    }
                    handleExpression(fieldAccessExpr.getScope().asNameExpr(), parentScope);
                }
            }else{

                System.out.print("");
                System.out.print("");


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
//            ClassExpr expr = exp.asClassExpr();
//            if (expr.getType().isClassOrInterfaceType())
//                handleClassInterfaceType(expr.getType().asClassOrInterfaceType());
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
        else if(exp.isConditionalExpr()){
            ConditionalExpr conditionalExpr=exp.asConditionalExpr();
            handleExpression(conditionalExpr.getCondition(),parentScope);
            handleExpression(conditionalExpr.getThenExpr(),parentScope);
            handleExpression(conditionalExpr.getElseExpr(),parentScope);
        }


//        else if(exp.isStringLiteralExpr()){
//
//            StringLiteralExpr sExpr=exp.asStringLiteralExpr();
//            ReplacementDataNode replacementDataNode=new ReplacementDataNode();
//            replacementDataNode.setReplacementString(getUnicodeExpression(sExpr.getValue(),Mode.STRING));
//            replacementDataNode.setLineNo(sExpr.getRange().get().begin.line);
//            replacementDataNode.setStartColNo(sExpr.getRange().get().begin.column);
//            replacementDataNode.setEndColNo(sExpr.getRange().get().end.column);
//            obfuscator.setArrayList(replacementDataNode);
//
//        }else if(exp.isIntegerLiteralExpr()){
//
//            IntegerLiteralExpr iExpr=exp.asIntegerLiteralExpr();
//            ReplacementDataNode replacementDataNode=new ReplacementDataNode();
//            replacementDataNode.setReplacementString(getUnicodeExpression(iExpr.getValue(),Mode.INTEGER));
//            replacementDataNode.setLineNo(iExpr.getRange().get().begin.line);
//            replacementDataNode.setStartColNo(iExpr.getRange().get().begin.column);
//            replacementDataNode.setEndColNo(iExpr.getRange().get().end.column);
//            obfuscator.setArrayList(replacementDataNode);
//
//        }else if(exp.isDoubleLiteralExpr()){
//
//            DoubleLiteralExpr dExpr=exp.asDoubleLiteralExpr();
//            ReplacementDataNode replacementDataNode=new ReplacementDataNode();
//            replacementDataNode.setReplacementString(getUnicodeExpression(dExpr.getValue(),Mode.DOUBLE));
//            replacementDataNode.setLineNo(dExpr.getRange().get().begin.line);
//            replacementDataNode.setStartColNo(dExpr.getRange().get().begin.column);
//            replacementDataNode.setEndColNo(dExpr.getRange().get().end.column);
//            obfuscator.setArrayList(replacementDataNode);
//
//        }else if(exp.isCharLiteralExpr()){
//            CharLiteralExpr cExpr=exp.asCharLiteralExpr();
//            ReplacementDataNode replacementDataNode=new ReplacementDataNode();
//            replacementDataNode.setReplacementString(getUnicodeExpression(cExpr.getValue(),Mode.INTEGER));
//            replacementDataNode.setLineNo(cExpr.getRange().get().begin.line);
//            replacementDataNode.setStartColNo(cExpr.getRange().get().begin.column);
//            replacementDataNode.setEndColNo(cExpr.getRange().get().end.column);
//            obfuscator.setArrayList(replacementDataNode);
//        }
    }

    public void handleMethodDeclaration(MethodDeclaration method,Scope parentScope) {

        BlockStmt block = method.getBody().orElse(null);

        if(block!=null && block.getStatements().size()>0){

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

    public void handleWhileStatement(Statement statement,Scope parentScope){

        if(statement==null||!statement.isWhileStmt())
            return;

        WhileStmt whileStmt=statement.asWhileStmt();
        handleExpression(whileStmt.getCondition(),parentScope);
        handleBlockStatement(whileStmt.getBody(),parentScope);

    }




    /*********************************************/

    public void handleVariables(List<VariableDeclarator> variables,Scope parentScope) {
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {

                String vtype = variable.getType().asString();
                String vname = variable.getName().getIdentifier();
                parentScope.setData(vname, vtype);

                int vstart_line_num = variable.getName().getRange().get().begin.line;
                int vstart_col_num = variable.getName().getRange().get().begin.column;
                int vend_col_num = variable.getName().getRange().get().end.column;


                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(vstart_line_num);
                rnode.setStartColNo(vstart_col_num);
                rnode.setEndColNo(vend_col_num);
                rnode.setReplacementString(getHexValue(vname));
                obfuscator.setArrayList(rnode);

                Expression expression = variable.getInitializer().orElse(null);
                handleExpression(expression,parentScope);
            }
        }
    }

    public void addVariablesToScope(List<VariableDeclarator> variables,Scope parentScope) {
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {
                String vtype = variable.getType().asString();
                String vname = variable.getName().getIdentifier();
                parentScope.setData(vname, vtype);
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

    public String getUnicodeExpression(String val,Mode m){

        String str="";

        if(m==Mode.INTEGER||m==Mode.DOUBLE) {
            return toUnicode (val);
        }
        else if(m==Mode.STRING){
            val="\""+val+"\"";
            return toUnicode(val);
        }

        return str;
    }
    public String toUnicode(String uString) {
        String unicode = "";
        for (char c: uString.toCharArray()) {
            unicode += toUnicode(c);
        }
        return unicode;
    }

    public String toUnicode(Character uChar) {
        return "\\u" + Integer.toHexString(uChar | 0x10000).substring(1);
    }

    public Boolean checkForStaticVariableAccess(String name){

        for(String s:classList){
            File f=new File(s);
            if(f.getName().equals(name+".java"))
                return true;
        }
        for(String s:innerClassList){
            if(name.equals(s))
                return true;
        }

        return false;
    }



}
