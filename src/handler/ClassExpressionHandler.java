package handler;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import model.ReplacementDataNode;
import model.Scope;
import obfuscator.ClassObfuscator;
import obfuscator.Obfuscator;
import java.util.List;
import static obfuscator.ClassObfuscator.*;
import static obfuscator.Obfuscator.*;
import static utils.Encryption.*;

public class ClassExpressionHandler extends ExpressionHandler {

    public ClassExpressionHandler() {
    }

    public ClassExpressionHandler(ExpressionHandler p) {
        super(p);
    }

    @Override
    public void handleCastExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        CastExpr expr = exp.asCastExpr();
        if (expr.getType().isClassOrInterfaceType())
            handleClassInterfaceType(expr.getType().asClassOrInterfaceType());

        handleExpression(expr.getExpression(),parentScope);
    }

    @Override
    public void handleClassExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isClassExpr())
            return;

        ClassExpr expr = exp.asClassExpr();
        if (expr.getType().isClassOrInterfaceType())
            handleClassInterfaceType(expr.getType().asClassOrInterfaceType());
    }

    @Override
    public void handleMethodCallExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isMethodCallExpr())
            return;

        //TODO: CHECK
        MethodCallExpr methodCall = exp.asMethodCallExpr();
        List<Expression> argList = methodCall.getArguments();
        if (argList != null) {
            for (Expression i : argList)
                handleExpression(i,parentScope);
        }

        //Static method calls
        exp = methodCall.getScope().orElse(null);
        handleExpression(exp,parentScope);
    }

    @Override
    public void handleFieldAccessExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isFieldAccessExpr())
            return;

        SimpleName sname = exp.asFieldAccessExpr().getName();
        String name = sname.getIdentifier();
        int vstart_line_num = sname.getRange().get().begin.line;
        int vstart_col_num = sname.getRange().get().begin.column;
        int vend_col_num = sname.getRange().get().end.column;

        Boolean flag = verifyUserDefinedClass(name);
        if (flag) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(getHexValue(name));
            Obfuscator.updateObfuscatorConfig(rnode);
        }
        //eg: m.behaviour(Mammal.count);
        handleExpression(exp.asFieldAccessExpr().getScope(),parentScope);
    }

    @Override
    public void handleNameExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isNameExpr())
            return;

        String name = exp.asNameExpr().getName().getIdentifier();
        int vstart_line_num = exp.getRange().get().begin.line;
        int vstart_col_num = exp.getRange().get().begin.column;
        int vend_col_num = exp.getRange().get().end.column;

        Boolean flag = verifyUserDefinedClass(name);
        if (flag) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(getHexValue(name));
            Obfuscator.updateObfuscatorConfig(rnode);
        }
    }

    @Override
    public void handleObjectCreationExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isObjectCreationExpr())
            return;

        //TODO: CHECK
        ObjectCreationExpr expr = exp.asObjectCreationExpr();
        String type = expr.getType().getName().getIdentifier();
        int vstart_line_num = expr.getType().getRange().get().begin.line;
        int vstart_col_num = expr.getType().getRange().get().begin.column;
        int vend_col_num = expr.getType().getRange().get().end.column;

        Boolean flag = verifyUserDefinedClass(type);
        if (flag) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(getHexValue(type));
            Obfuscator.updateObfuscatorConfig(rnode);
        }

        if (expr.getType().isClassOrInterfaceType())
            handleClassInterfaceType(expr.getType().asClassOrInterfaceType());

        List<Expression> expList = expr.getArguments();
        if (expList != null) {
            for (Expression e : expList)
                handleExpression(e,parentScope);
        }

        List<BodyDeclaration<?>> bodyList = expr.getAnonymousClassBody().orElse(null);
        if (bodyList != null) {
            for (BodyDeclaration<?> e : bodyList)
                if (e.isMethodDeclaration())
                    ClassObfuscator.handleMethodDeclaration(e.asMethodDeclaration(),parentScope);
        }
    }

    @Override
    public void handleArrayCreationExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isArrayCreationExpr())
            return;

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

    @Override
    public void handleParameter(Parameter p, Scope parentScope) {
        if (p == null)
            return;

        if (p.getType().isClassOrInterfaceType()) {
            ClassOrInterfaceType type = p.getType().asClassOrInterfaceType();
            handleClassInterfaceType(type);
        }

    }
}
