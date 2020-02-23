package obfuscator;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import handler.*;
import model.*;

import java.util.ArrayList;
import java.util.List;

import static utils.CommonUtils.*;
import static utils.Encryption.*;

public class ClassObfuscator extends Obfuscator implements Obfuscate {

    public static StatementHandler statementHandler;

    public ClassObfuscator() {
        super();
        statementHandler = new StatementHandler(new ClassExpressionHandler());
    }

    /*********************************************/

    public static void handleArrayType(ArrayType cit) {
        if (cit == null)
            return;

        if (cit.getComponentType().isClassOrInterfaceType())
            handleClassInterfaceType(cit.getComponentType().asClassOrInterfaceType());
    }

    /*********************************************/

    public static void handleVariables(List<VariableDeclarator> variables, Scope parentScope) {
        if (!variables.isEmpty()) {
            for (VariableDeclarator variable : variables) {

                if (variable.getType().isClassOrInterfaceType())
                    handleClassInterfaceType(variable.getType().asClassOrInterfaceType());

                String vtype = variable.getType().asString();

                int vstart_line_num = variable.getType().getRange().get().begin.line;
                int vstart_col_num = variable.getType().getRange().get().begin.column;
                int vend_col_num = variable.getType().getRange().get().end.column;

                Boolean flag = verifyUserDefinedClass(vtype);
                if (flag) {
                    ReplacementDataNode rnode = new ReplacementDataNode();
                    rnode.setLineNo(vstart_line_num);
                    rnode.setStartColNo(vstart_col_num);
                    rnode.setEndColNo(vend_col_num);
                    rnode.setReplacementString(getHexValue(vtype));
                    obfuscatorConfig.setArrayList(rnode);
                }

                //Object Initialisation
                Expression expression = variable.getInitializer().orElse(null);
                statementHandler.getExpressionHandler().handleExpression(expression, parentScope);
            }
        }
    }

    /*********************************************/

    public void handleImport(Name name) {
        if (name == null)
            return;

        // todo optimise the function
        for (String str : classNameList) {
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
                        obfuscatorConfig.setArrayList(rnode);
                    }
                }
            }
        }

        handleImport(name.getQualifier().orElse(null));
    }

    @Override
    public void obfuscate(CompilationUnit cu) {
        for (int i = 0; i < cu.getImports().size(); i++) {
            String imports = cu.getImports().get(i).getName().toString();
            if (imports.startsWith(getBasePackage()))
                handleImport(cu.getImports().get(i).getName());
        }
    }

    @Override
    public void handleClass(ClassOrInterfaceDeclaration clas,Scope scope) {
        if (clas != null) {

            Scope classScope = new Scope();
            classScope.setParentScope(null);

            //Class name
            String name = clas.getName().getIdentifier();
            int start_line_num = clas.getName().getRange().get().begin.line;
            int start_col_num = clas.getName().getRange().get().begin.column;
            int end_col_num = clas.getName().getRange().get().end.column;

            boolean flag = verifyUserDefinedClass(name);
            if (flag) {
                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(start_line_num);
                rnode.setStartColNo(start_col_num);
                rnode.setEndColNo(end_col_num);
                rnode.setReplacementString(getHexValue(name));
                obfuscatorConfig.setArrayList(rnode);
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
                    handleVariables(global_variables, classScope);
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

                    flag = verifyUserDefinedClass(cname);
                    if (flag) {
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        rnode.setLineNo(cstart_line_num);
                        rnode.setStartColNo(cstart_col_num);
                        rnode.setEndColNo(cend_col_num);
                        rnode.setReplacementString(getHexValue(cname));
                        obfuscatorConfig.setArrayList(rnode);
                    }

                    List<Parameter> parameterList = constructor.getParameters();
                    if (!parameterList.isEmpty()) {
                        for (Parameter p : parameterList)
                            statementHandler.handleParameter(p, classScope);
                    }

                    BlockStmt block = constructor.getBody();
                    statementHandler.handleStatement(block, classScope);
                }
            }

            //Class Members
            List<BodyDeclaration<?>> members = clas.getMembers();
            if (!members.isEmpty()) {
                for (BodyDeclaration<?> bd : members) {
                    //Methods
                    if (bd.isMethodDeclaration())
                        handleMethodDeclaration(bd.asMethodDeclaration(), classScope);
                        //Inner Class
                    else if (bd.isClassOrInterfaceDeclaration())
                        handleClass(bd.asClassOrInterfaceDeclaration(),classScope);
                }
            }
        }
    }

    public static void handleClassInterfaceType(ClassOrInterfaceType cit) {
        if (cit == null)
            return;

        String name = cit.getName().getIdentifier();
        ClassOrInterfaceType scope = cit.getScope().orElse(null);
        int start_line_num = cit.getName().getRange().get().begin.line;
        int start_col_num = cit.getName().getRange().get().begin.column;
        int end_col_num = cit.getName().getRange().get().end.column;

        Boolean flag = verifyUserDefinedClass(name);
        if (!flag) {
            if (scope != null && (scope.asString() + ".").contains(getBasePackage()) && folderNameList.contains(name))
                flag = true;
        }

        if (flag) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(start_line_num);
            rnode.setStartColNo(start_col_num);
            rnode.setEndColNo(end_col_num);
            rnode.setReplacementString(getHexValue(name));
            updateObfuscatorConfig(rnode);
        }

        NodeList<Type> args = cit.getTypeArguments().orElse(null);
        if (args != null) {
            for (Type t : args) {
                if (t.isClassOrInterfaceType())
                    handleClassInterfaceType(t.asClassOrInterfaceType());
            }
        }

        handleClassInterfaceType(scope);
    }

    public static void handleMethodDeclaration(MethodDeclaration method, Scope parentScope) {
        if (method.getType().isClassOrInterfaceType()) {
            ClassOrInterfaceType cit = method.getType().asClassOrInterfaceType();
            handleClassInterfaceType(cit);
        }

        if (method.getType().isArrayType()) {
            ArrayType cit = method.getType().asArrayType();
            handleArrayType(cit);
        }

        if (verifyUserDefinedClass(method.getType().asString())) {
            ReplacementDataNode r = new ReplacementDataNode();
            Range range = method.getType().getRange().orElse(null);

            if (range != null) {
                Position begin = range.begin;
                Position end = range.end;
                r.setLineNo(begin.line);
                r.setStartColNo(begin.column);
                r.setEndColNo(end.column);
                r.setReplacementString(getHexValue(method.getType().asString()));
                updateObfuscatorConfig(r);
            }
        }

        BlockStmt block = method.getBody().orElse(null);
        statementHandler.handleStatement(block, parentScope);

        //Method Arguments
        List<Parameter> parameterList = method.getParameters();

        if (!parameterList.isEmpty()) {
            for (Parameter p : parameterList)
                statementHandler.handleParameter(p, parentScope);
        }
    }
}
