package refactor.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import obfuscator.Obfuscator;
import model.ReplacementDataNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static refactor.utils.CommonUtils.*;
import static refactor.utils.Constants.classList;
import static refactor.utils.FileOperation.getClassNameFromFilePath;
import static refactor.utils.FileOperation.renameFile;

public class ClassObfuscator {

    private Obfuscator obfuscator;

    public void obfuscate() {
        ArrayList<String> classes = getClassName();

        for (String s : classList) {

            File file = new File(s);
            String className = getClassNameFromFilePath(file.getName());

            try {
                CompilationUnit cu = JavaParser.parse(file);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(className).orElse(null);
                if (clas == null)
                    clas = cu.getInterfaceByName(className).orElse(null);

                obfuscator = new Obfuscator();
                obfuscator.setCurrentFile(file);

                // handling import statements
                for (int i = 0; i < cu.getImports().size(); i++) {
                    String imports = cu.getImports().get(i).getName().toString();
                    if (imports.startsWith(getBasePackage()))
                        handleImport(cu.getImports().get(i).getName(), classes);
                }

                handleClass(clas);
                obfuscator.replaceInFiles();
                System.out.println("last file replaced in class obfuscation is "+file.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            renameFile(file.getAbsolutePath(), file.getParent() + File.separator + CommonUtils.getHexValue(className) + ".java");
        }
    }

    /*********************************************/

    public void handleClass(ClassOrInterfaceDeclaration clas) {
        if (clas != null) {
            //Class name
            String name = clas.getName().getIdentifier();
            int start_line_num = clas.getName().getRange().get().begin.line;
            int start_col_num = clas.getName().getRange().get().begin.column;
            int end_col_num = clas.getName().getRange().get().end.column;

            boolean flag = compare(name);
            if (flag) {
                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(start_line_num);
                rnode.setStartColNo(start_col_num);
                rnode.setEndColNo(end_col_num);
                rnode.setReplacementString(getHexValue(name));
                obfuscator.setArrayList(rnode);
            }

            //Extends class
            List<ClassOrInterfaceType> citList = clas.getExtendedTypes();
            if (citList != null) {
                for (ClassOrInterfaceType cit : citList)
                    handleClassInterfaceType(cit);
            }

            //Implements interface
            citList = clas.getImplementedTypes();
            if (citList != null) {
                for (ClassOrInterfaceType cit : citList)
                    handleClassInterfaceType(cit);
            }

            //Global_fields
            List<FieldDeclaration> global_fields = clas.getFields();
            if (!global_fields.isEmpty()) {
                for (FieldDeclaration field : global_fields) {
                    List<VariableDeclarator> global_variables = field.getVariables();
                    handleVariables(global_variables);
                }
            }

            //Construction
            List<ConstructorDeclaration> constructors = clas.getConstructors();
            if (!constructors.isEmpty()) {
                for (ConstructorDeclaration constructor : constructors) {
                    String cname = constructor.getName().getIdentifier();
                    int cstart_line_num = constructor.getName().getRange().get().begin.line;
                    int cstart_col_num = constructor.getName().getRange().get().begin.column;
                    int cend_col_num = constructor.getName().getRange().get().end.column;

                    flag = compare(cname);
                    if (flag) {
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        rnode.setLineNo(cstart_line_num);
                        rnode.setStartColNo(cstart_col_num);
                        rnode.setEndColNo(cend_col_num);
                        rnode.setReplacementString(getHexValue(cname));
                        obfuscator.setArrayList(rnode);
                    }

                    List<Parameter> parameterList = constructor.getParameters();
                    if (!parameterList.isEmpty()) {
                        for (Parameter p : parameterList)
                            handleParameter(p);
                    }

                    BlockStmt block = constructor.getBody();
                    handleBlockStmt(block);
                }
            }

            //Class Members
            List<BodyDeclaration<?>> members = clas.getMembers();
            if (!members.isEmpty()) {
                for (BodyDeclaration<?> bd : members) {
                    //Methods
                    if (bd.isMethodDeclaration())
                        handleMethodDeclaration(bd.asMethodDeclaration());

                        //Inner Class
                    else if (bd.isClassOrInterfaceDeclaration())
                        handleClass(bd.asClassOrInterfaceDeclaration());
                }
            }
        }
    }

    public void handleMethodDeclaration(MethodDeclaration method) {
        if (method.getType().isClassOrInterfaceType()) {
            ClassOrInterfaceType cit = method.getType().asClassOrInterfaceType();
            handleClassInterfaceType(cit);
        }

        if (method.getType().isArrayType()) {
            ArrayType cit = method.getType().asArrayType();
            handleArrayType(cit);
        }

        if (compare(method.getType().asString())) {
            ReplacementDataNode r = new ReplacementDataNode();
            Range range = method.getType().getRange().orElse(null);

            if (range != null) {
                Position begin = range.begin;
                Position end = range.end;
                r.setLineNo(begin.line);
                r.setStartColNo(begin.column);
                r.setEndColNo(end.column);
                r.setReplacementString(getHexValue(method.getType().asString()));
                obfuscator.setArrayList(r);
            }
        }

        BlockStmt block = method.getBody().orElse(null);
        handleBlockStmt(block);

        //Method Arguments
        List<Parameter> parameterList = method.getParameters();

        if (!parameterList.isEmpty()) {
            for (Parameter p : parameterList)
                handleParameter(p);
        }

    }

    public void handleArrayType(ArrayType cit) {
        if (cit == null)
            return;

        if (cit.getComponentType().isClassOrInterfaceType())
            handleClassInterfaceType(cit.getComponentType().asClassOrInterfaceType());
    }

    public void handleBlockStmt(BlockStmt block) {
        if (block == null)
            return;

        List<Statement> stList = block.getStatements();
        if (!stList.isEmpty()) {
            for (int i = 0; i < stList.size(); i++) {
                Statement st = stList.get(i);
                handleStatement(st);
            }
        }
    }

    public void handleStatement(Statement statement) {
        if (statement == null || statement.isEmptyStmt())
            return;

        handleExpressionStatement(statement);
        handleExplicitConstructorInvocationStmt(statement);
        handleIfStatement(statement);
        handleSwitchStatement(statement);
        handleBlockStatement(statement);
        handleTryCatchStatement(statement);
        handleForStatement(statement);
        handleForEachStatement(statement);
        handleDoWhileStatement(statement);
        handleReturnStatement(statement);
        handleSynchronisedStatement(statement);
    }

    /*********************************************/

    private void handleDoWhileStatement(Statement st){
        if(st==null || !st.isDoStmt())
            return;

        handleStatement(st.asDoStmt().getBody());
        handleExpression(st.asDoStmt().getCondition());
    }

    private void handleSynchronisedStatement(Statement st){
        if(st==null || !st.isSynchronizedStmt())
            return;

        handleExpression(st.asSynchronizedStmt().getExpression());
        handleStatement(st.asSynchronizedStmt().getBody());
    }

    private void handleReturnStatement(Statement st) {

        if (st == null || !st.isReturnStmt())
            return;

        handleExpression(st.asReturnStmt().getExpression().orElse(null));
    }

    private void handleTryCatchStatement(Statement st) {

        if (st == null || !st.isTryStmt())
            return;

        handleStatement(st.asTryStmt().getTryBlock());

        List<CatchClause> l = st.asTryStmt().getCatchClauses();
        if (!l.isEmpty()) {
            for (CatchClause i : l) {
                handleStatement(i.getBody());
                handleParameter(i.getParameter());
            }
        }
    }

    private void handleForStatement(Statement st) {

        if (st == null || !st.isForStmt())
            return;

        NodeList<Expression> n1 = st.asForStmt().getInitialization();
        if (n1.isNonEmpty()) {
            for (Expression e : n1)
                handleExpression(e);
        }

        handleExpression(st.asForStmt().getCompare().orElse(null));

        NodeList<Expression> n2 = st.asForStmt().getUpdate();
        if (n2.isNonEmpty()) {
            for (Expression e : n2)
                handleExpression(e);
        }

        Statement forStmt = st.asForStmt().getBody();
        handleStatement(forStmt);
    }

    private void handleForEachStatement(Statement st) {
        if (st == null || !st.isForeachStmt())
            return;

        NodeList<VariableDeclarator> variableDeclarators = st.asForeachStmt().getVariable().getVariables();
        handleVariables(variableDeclarators);
        handleExpression(st.asForeachStmt().getIterable());
        Statement forEach = st.asForeachStmt().getBody();
        handleStatement(forEach);
    }

    public void handleIfStatement(Statement st) {
        if (st == null || !st.isIfStmt())
            return;

        Expression condition = st.asIfStmt().getCondition();
        handleExpression(condition);

        Statement thenStmt = st.asIfStmt().getThenStmt();
        handleStatement(thenStmt);

        Statement elseStmt = st.asIfStmt().getElseStmt().orElse(null);
        handleStatement(elseStmt);
    }

    public void handleSwitchStatement(Statement st) {
        if (st == null || !st.isSwitchStmt())
            return;

        SwitchStmt switchStmt = st.asSwitchStmt();
        handleExpression(switchStmt.getSelector());

        List<SwitchEntryStmt> stList = switchStmt.getEntries();
        if (stList != null) {
            for (SwitchEntryStmt set : stList) {
                handleExpression(set.getLabel().orElse(null));
                List<Statement> expst = set.getStatements();
                if (!expst.isEmpty()) {
                    for (Statement est : expst)
                        handleStatement(est);
                }
            }
        }
    }

    public void handleBlockStatement(Statement statement) {
        if (statement == null || !statement.isBlockStmt())
            return;

        List<Statement> stmtBlock = statement.asBlockStmt().getStatements();
        if (!stmtBlock.isEmpty()) {
            for (Statement stmt : stmtBlock)
                handleStatement(stmt);
        }
    }

    public void handleExpressionStatement(Statement st) {
        if (st == null || !st.isExpressionStmt())
            return;

        Expression exp = st.asExpressionStmt().getExpression();
        handleExpression(exp);
    }

    public void handleExplicitConstructorInvocationStmt(Statement st) {
        if (st == null || !st.isExplicitConstructorInvocationStmt())
            return;

        ExplicitConstructorInvocationStmt ecst = st.asExplicitConstructorInvocationStmt();
        List<Expression> exp = ecst.getArguments();
        if (exp != null) {
            for (Expression e : exp)
                handleExpression(e);
        }
    }

    /*********************************************/

    public void handleExpression(Expression exp) {
        if (exp == null)
            return;

        if (exp.isVariableDeclarationExpr()) {
            VariableDeclarationExpr vdexp = exp.asVariableDeclarationExpr();
            List<VariableDeclarator> variables = vdexp.getVariables();
            handleVariables(variables);

        } else if (exp.isEnclosedExpr()) {
            EnclosedExpr expr = exp.asEnclosedExpr();
            handleExpression(expr.getInner());

        } else if (exp.isCastExpr()) {
            CastExpr expr = exp.asCastExpr();
            if (expr.getType().isClassOrInterfaceType())
                handleClassInterfaceType(expr.getType().asClassOrInterfaceType());

            handleExpression(expr.getExpression());

        } else if (exp.isClassExpr()) {
            ClassExpr expr = exp.asClassExpr();
            if (expr.getType().isClassOrInterfaceType())
                handleClassInterfaceType(expr.getType().asClassOrInterfaceType());

        } else if (exp.isMethodCallExpr()) {
            MethodCallExpr methodCall = exp.asMethodCallExpr();
            List<Expression> argList = methodCall.getArguments();
            if (argList != null) {
                for (Expression i : argList)
                    handleExpression(i);
            }

            //Static method calls
            exp = methodCall.getScope().orElse(null);
            handleExpression(exp);

        } else if (exp.isUnaryExpr()) {
            UnaryExpr expr = exp.asUnaryExpr();
            handleExpression(expr.getExpression());

        } else if (exp.isBinaryExpr()) {
            BinaryExpr expr = exp.asBinaryExpr();
            handleExpression(expr.getLeft());
            handleExpression(expr.getRight());

        } else if (exp.isThisExpr()) {
            exp = exp.asThisExpr().getClassExpr().orElse(null);
            handleExpression(exp);

        } else if (exp.isFieldAccessExpr()) {
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

        } else if (exp.isNameExpr()) {
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
                obfuscator.setArrayList(rnode);
            }

        } else if (exp.isObjectCreationExpr()) {
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
                obfuscator.setArrayList(rnode);
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

        } else if (exp.isAssignExpr()) {
            AssignExpr expr = exp.asAssignExpr();
            if (expr.getTarget().isFieldAccessExpr())
                handleExpression(expr.getTarget().asFieldAccessExpr().getScope());
            //eg: darkMode = MainActivity.getHelloMain("dark_mode");
            handleExpression(expr.getValue());

        } else if (exp.isArrayCreationExpr()) {
            ArrayCreationExpr expr = exp.asArrayCreationExpr();
            if (expr.getElementType().isClassOrInterfaceType())
                handleClassInterfaceType(expr.getElementType().asClassOrInterfaceType());

            List<ArrayCreationLevel> acl = expr.getLevels();
            if (!acl.isEmpty()) {
                for (ArrayCreationLevel ac : acl)
                    handleExpression(ac.getDimension().orElse(null));
            }

            handleExpression(expr.getInitializer().orElse(null));

        } else if (exp.isArrayInitializerExpr()) {
            ArrayInitializerExpr expr = exp.asArrayInitializerExpr();
            List<Expression> exList = expr.getValues();
            if(!exList.isEmpty()){
                for(Expression e:exList)
                    handleExpression(e);
            }
        }
    }

    /*********************************************/

    public void handleVariables(List<VariableDeclarator> variables) {
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {

                if (variable.getType().isClassOrInterfaceType())
                    handleClassInterfaceType(variable.getType().asClassOrInterfaceType());

                String vtype = variable.getType().asString();

                int vstart_line_num = variable.getType().getRange().get().begin.line;
                int vstart_col_num = variable.getType().getRange().get().begin.column;
                int vend_col_num = variable.getType().getRange().get().end.column;

                Boolean flag = compare(vtype);
                if (flag) {
                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(vstart_line_num);
                    rnode.setStartColNo(vstart_col_num);
                    rnode.setEndColNo(vend_col_num);
                    rnode.setReplacementString(getHexValue(vtype));
                    obfuscator.setArrayList(rnode);
                }

                //Object Initialisation
                Expression expression = variable.getInitializer().orElse(null);
                handleExpression(expression);
            }
        }
    }

    public void handleParameter(Parameter p) {

        if (p == null)
            return;
        if (p.getType().isClassOrInterfaceType()) {
            ClassOrInterfaceType type = p.getType().asClassOrInterfaceType();
            handleClassInterfaceType(type);
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


    public void handleImport(Name name, ArrayList<String> replacementPattern) {

        if (name == null)
            return;

        for (String str : replacementPattern) {
            if (str.equals(name.getIdentifier())) {
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
                        obfuscator.setArrayList(rnode);
                    }
                }
            }
        }

        handleImport(name.getQualifier().orElse(null), replacementPattern);
    }
}
