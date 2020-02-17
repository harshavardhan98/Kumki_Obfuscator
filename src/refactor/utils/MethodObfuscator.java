package refactor.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import obfuscator.Obfuscator;
import model.ReplacementDataNode;
import model.Scope;

import java.io.File;
import java.util.*;

import static refactor.utils.CommonUtils.*;
import static refactor.utils.FileOperation.getClassNameFromFilePath;

public class MethodObfuscator {

    private Obfuscator obfuscator;
    HashMap<String,HashMap<String,String>> dataMembers=new HashMap<>();
    public ArrayList<String> staticMethods=new ArrayList<>();
    public String extendingClassPath="null";
    public ArrayList<String> keepMethods=new ArrayList<String>();

    public void obfuscate() {

            updateKeepMethods();
            collectPublicDataMembersOfClass();
            Scope global_scope = new Scope();
            global_scope.setScope(null);
            buildGlobalSymbolTable(global_scope);



            for (String filePath : classList) {
                try {
                File file = new File(filePath);

//                if(!file.getName().contains("SavedPostAdapter"))
//                    continue;

                CompilationUnit cu = JavaParser.parse(file);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(file.getName())).orElse(null);
                if (clas == null)
                    clas = cu.getInterfaceByName(getClassNameFromFilePath(file.getName())).orElse(null);

                obfuscator = new Obfuscator();
                obfuscator.setCurrentFile(file);
                Scope classScope=new Scope();
                classScope.setScope(global_scope);

                if(clas.getExtendedTypes().size()>0)
                    handleExtendingClass(classScope,clas.getExtendedTypes().get(0).getName().toString());

                handleClass(clas,classScope);
                obfuscator.replaceInFiles();

            }catch(Exception e) {
                    e.printStackTrace();
                }
        }
    }

    public void handleClass(ClassOrInterfaceDeclaration clas,Scope global_scope) {
        if (clas != null) {

            //Global_fields
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

            List<MethodDeclaration> methods = clas.getMethods();
            if (!methods.isEmpty()) {
                for (MethodDeclaration method : methods) {


                    Scope methodScope = new Scope();
                    methodScope.setScope(global_scope);

                    List<Parameter> parametersList=method.getParameters();
                    if(!parametersList.isEmpty()){
                        for(Parameter p:parametersList)
                            handleParameter(p,methodScope);
                    }

                    BlockStmt block = method.getBody().orElse(null);

                    if (block != null) {

                        List<Statement> stList = block.getStatements();

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

                    Boolean isKeepClass=false,isKeepMethod=false;
                    for(String s:keepClass)
                        if(obfuscator.getCurrentFile().getName().equals(s+".java"))
                            isKeepClass=true;

                    if(isKeepClass)
                        continue;

                    for(String s:keepMethods)
                        if(s.equals(name)){
                            isKeepMethod=true;
                        }

                    if(isKeepMethod)
                        continue;

                    if (!MethodVisitor.isOverride(method) || toBeReplaced1(name,null)) {
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        rnode.setLineNo(start_line_num);
                        rnode.setStartColNo(start_col_num);
                        rnode.setEndColNo(end_col_num);
                        rnode.setReplacementString(getHexValue(name));
                        obfuscator.setArrayList(rnode);
                    }
                }
            }

            List<BodyDeclaration<?>> members = clas.getMembers();
            if (!members.isEmpty()) {
                for (BodyDeclaration<?> bd : members) {
                    if(bd.isClassOrInterfaceDeclaration()){
                        handleClass(bd.asClassOrInterfaceDeclaration(),global_scope);
                    }
                }
            }

        }
    }

    void handleExtendingClass(Scope scope,String extendingClassName){
        // check if the class being extended is a user defined class
        // if user defined add the public data members to the

        for(String s:classList){

            File f=new File(s);
            if(f.getName().equals(extendingClassName+".java")) {

                extendingClassPath=f.getAbsolutePath();

                HashMap<String,String> temp=dataMembers.get(s);
                for(String str:temp.keySet()){
                    scope.setData(str,temp.get(str));
                }

                break;
            }
        }
    }

    public void handleExpression(Expression exp,Scope parentScope) {

        if (exp == null)
            return;

        if (exp.isVariableDeclarationExpr()) {
            VariableDeclarationExpr vdexp = exp.asVariableDeclarationExpr();
            List<VariableDeclarator> variables = vdexp.getVariables();
            handleVariables(variables, parentScope);

        }
        else if (exp.isMethodCallExpr()) {
            MethodCallExpr methodCall = exp.asMethodCallExpr();
            String mname = methodCall.getName().getIdentifier();
            List<Expression> argList = methodCall.getArguments();
            if (argList != null) {
                for (Expression i : argList)
                    handleExpression(i,parentScope);
            }

            int mstart_line_num = methodCall.getName().getRange().get().begin.line;
            int mstart_col_num = methodCall.getName().getRange().get().begin.column;
            int mend_col_num = methodCall.getName().getRange().get().end.column;

            Expression obj_name_exp = methodCall.getScope().orElse(null);

            if (obj_name_exp != null) {
                if (obj_name_exp.isNameExpr()) {
                    String obj_name = obj_name_exp.asNameExpr().getName().getIdentifier();
                    String obj_type = parentScope.findDataTypeOfIdentifier(obj_name);

                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(mstart_line_num);
                    rnode.setStartColNo(mstart_col_num);
                    rnode.setEndColNo(mend_col_num);
                    rnode.setReplacementString(getHexValue(mname));

                    if (obj_type != null && compare(obj_type)&&toBeReplaced1(mname,obj_type)) {
                        obfuscator.setArrayList(rnode);
                    }

                    else if(checkForStaticVariableAccess(obj_name)&&toBeReplaced1(mname,obj_type)){
                        obfuscator.setArrayList(rnode);
                    }else{
                        System.out.print("");
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
                else if(obj_name_exp.isMethodCallExpr()){
                    handleExpression(obj_name_exp.asMethodCallExpr(),parentScope);
                }
                else if(obj_name_exp.isThisExpr() && toBeReplaced1(mname,null)){
                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(mstart_line_num);
                    rnode.setStartColNo(mstart_col_num);
                    rnode.setEndColNo(mend_col_num);
                    rnode.setReplacementString(getHexValue(mname));
                    obfuscator.setArrayList(rnode);
                }else if(obj_name_exp.isEnclosedExpr()){

                    EnclosedExpr enclosedExpr=obj_name_exp.asEnclosedExpr();
                    if(enclosedExpr.getInner().isCastExpr()){
                        String obj_type=enclosedExpr.getInner().asCastExpr().getType().asString();
                        if(toBeReplaced1(mname,obj_type)){
                            ReplacementDataNode rnode = new ReplacementDataNode();
                            rnode.setLineNo(mstart_line_num);
                            rnode.setStartColNo(mstart_col_num);
                            rnode.setEndColNo(mend_col_num);
                            rnode.setReplacementString(getHexValue(mname));
                            obfuscator.setArrayList(rnode);
                        }

                    }
                }
            }
            else if(toBeReplaced1(mname,null)){
                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(mstart_line_num);
                rnode.setStartColNo(mstart_col_num);
                rnode.setEndColNo(mend_col_num);
                rnode.setReplacementString(getHexValue(mname));
                obfuscator.setArrayList(rnode);
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
        }
        else if (exp.isBinaryExpr()) {

            BinaryExpr expr = exp.asBinaryExpr();
            handleExpression(expr.getLeft(),parentScope);
            handleExpression(expr.getRight(),parentScope);

        }
        else if(exp.isCastExpr()){
            CastExpr castExpr=exp.asCastExpr();
            handleExpression(castExpr.getExpression(),parentScope);
        }
        else if (exp.isThisExpr()) {
            exp = exp.asThisExpr().getClassExpr().orElse(null);
            handleExpression(exp,parentScope);
        }
        else if(exp.isObjectCreationExpr()) {

            ObjectCreationExpr expr = exp.asObjectCreationExpr();

            List<Expression> expList = expr.getArguments();
            if (expList != null) {
                for (Expression e : expList)
                    handleExpression(e, parentScope);
            }

            List<BodyDeclaration<?>> bodyList = expr.getAnonymousClassBody().orElse(null);
            if (bodyList != null) {
                for (BodyDeclaration<?> e : bodyList)
                    if(e.isMethodDeclaration()){
                        NodeList<Parameter> parameters=e.asMethodDeclaration().getParameters();

                        for(Parameter p:parameters)
                            handleParameter(p,parentScope);

                        handleStatement(e.asMethodDeclaration().getBody().orElse(null),parentScope);
                    }
            }
        }
        else if (exp.isEnclosedExpr()) {
            EnclosedExpr expr = exp.asEnclosedExpr();
            handleExpression(expr.getInner(),parentScope);
        }
        else if(exp.isArrayAccessExpr()){
            ArrayAccessExpr arrayAccessExpr=exp.asArrayAccessExpr();
            handleExpression(arrayAccessExpr.getName(),parentScope);
        }
        else if (exp.isArrayInitializerExpr()) {
            ArrayInitializerExpr expr = exp.asArrayInitializerExpr();
            List<Expression> exList = expr.getValues();
            if(!exList.isEmpty()){
                for(Expression e:exList)
                    handleExpression(e,parentScope);
            }
        }
        else if (exp.isArrayCreationExpr()) {
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

        for (Map.Entry<String, ArrayList<String>> entry : methodMap.entrySet()) {
            ArrayList<String>  temp = entry.getValue();
            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i).equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean toBeReplaced1(String name,String objType){

        File f=obfuscator.getCurrentFile();

        // check in keep classes
        for(String s:keepClass)
            if(f.getName().equals(s+".java"))
                return false;

        // check in keep methods
        for(String s:keepMethods)
            if(name.equals(s))
                return false;


        if(objType==null){

            // it must be a static method
            for(String s:staticMethods)
                if(s.equals(name))
                    return true;

             // the function must defined in the class itself
            ArrayList<String> temp=methodMap.get(f.getAbsolutePath());
            if(temp!=null){
                for (int i = 0; i < temp.size(); i++) {
                    if (temp.get(i).equals(name)) {
                        return true;
                    }
                }
            }

            // the function can be from the extending class
            temp=methodMap.get(extendingClassPath);
            if(temp!=null){
                for (int i = 0; i < temp.size(); i++) {
                    if (temp.get(i).equals(name)) {
                        return true;
                    }
                }
            }

        }
        else{

            for (String fileName: methodMap.keySet()) {

                f=new File(fileName);
                if(f.getName().equals(objType+".java")){

                    ArrayList<String> temp=methodMap.get(f.getAbsolutePath());
                    for (int i = 0; i < temp.size(); i++) {
                        if (temp.get(i).equals(name)) {
                            return true;
                        }
                    }
                }
            }
        }


        return false;
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
                        else if(bd.isMethodDeclaration()){
                            EnumSet<Modifier> modifiers=bd.asMethodDeclaration().getModifiers();
                            Boolean isPublic=false,isStatic=false;

                            for(Modifier m:modifiers){
                                if(m.toString().equals("PUBLIC"))
                                    isPublic=true;
                                if(m.toString().equals("STATIC"))
                                    isStatic=true;
                            }

                            if(isPublic&&isStatic){
                                staticMethods.add(bd.asMethodDeclaration().getNameAsString());
                            }
                        }
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
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

    void updateKeepMethods(){
        keepMethods.add("getString");
        keepMethods.add("compare");
    }


}