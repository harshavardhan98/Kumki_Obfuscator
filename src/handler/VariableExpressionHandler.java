package handler;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import model.ReplacementDataNode;
import model.Scope;
import obfuscator.Obfuscator;
import obfuscator.VariableObfuscator;
import java.util.List;
import static utils.Encryption.getHexValue;

public class VariableExpressionHandler extends ExpressionHandler {


    public VariableExpressionHandler() {
    }

    public VariableExpressionHandler(ExpressionHandler object) {
        super(object);
    }

    @Override
    public void handleNameExpr(Expression exp, Scope parentScope) {

        if (exp == null || !exp.isNameExpr())
            return;

//        // todo check
        NameExpr nameExpr = exp.asNameExpr();
        String name = nameExpr.getName().getIdentifier();

        int vstart_line_num = nameExpr.getRange().get().begin.line;
        int vstart_col_num = nameExpr.getRange().get().begin.column;
        int vend_col_num = nameExpr.getRange().get().end.column;

        if(parentScope.checkIfGivenVariableExistsInScope(name)){
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(getHexValue(name));
            Obfuscator.updateObfuscatorConfig(rnode);
        }

    }

    @Override
    public void handleFieldAccessExpr(Expression exp, Scope parentScope) {

        if (exp == null || !exp.isFieldAccessExpr())
            return;

        boolean condition=true;
        FieldAccessExpr fieldAccessExpr = exp.asFieldAccessExpr();
        handleExpression(fieldAccessExpr.getScope(),parentScope);

        FieldAccessExpr temp=fieldAccessExpr;
        if(temp.getScope()!=null && temp.getScope().isFieldAccessExpr()){
            temp= temp.getScope().asFieldAccessExpr();
            if(temp!=null && temp.getScope().isNameExpr()){
                if(temp.getScope().asNameExpr().getNameAsString().equals("R"))
                    return;
            }
        }


        if(parentScope.checkIfGivenVariableExistsInScope(fieldAccessExpr.getNameAsString()) && condition ){
            String name = fieldAccessExpr.getName().getIdentifier();
            int vstart_line_num = fieldAccessExpr.getName().getRange().get().begin.line;
            int vstart_col_num = fieldAccessExpr.getName().getRange().get().begin.column;
            int vend_col_num = fieldAccessExpr.getName().getRange().get().end.column;

            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(vstart_line_num);
            rnode.setStartColNo(vstart_col_num);
            rnode.setEndColNo(vend_col_num);
            rnode.setReplacementString(getHexValue(name));
            Obfuscator.updateObfuscatorConfig(rnode);
        }


    }

    @Override
    public void handleMethodCallExpr(Expression exp, Scope parentScope) {

        if (exp == null || !exp.isMethodCallExpr())
            return;

        MethodCallExpr methodCall = exp.asMethodCallExpr();
        List<Expression> argList = methodCall.getArguments();
        if (argList != null) {
            for (Expression i : argList)
                handleExpression(i,parentScope);
        }

        //Static method calls
        handleExpression(methodCall.getScope().orElse(null),parentScope);
    }



    public void handleCastExpr(Expression exp, Scope parentScope) {

        if (exp == null || !exp.isCastExpr())
            return;

        CastExpr castExpr=exp.asCastExpr();
        handleExpression(castExpr.getExpression(),parentScope);
    }

    public void handleObjectCreationExpr(Expression exp, Scope parentScope) {

        if (exp == null || !exp.isObjectCreationExpr())
            return;

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
                    VariableObfuscator.handleMethodDeclaration(e.asMethodDeclaration(),parentScope);
        }
    }

    public void handleArrayCreationExpr(Expression exp, Scope parentScope) {

        if (exp == null || !exp.isArrayCreationExpr())
            return;

        ArrayCreationExpr expr = exp.asArrayCreationExpr();

        List<ArrayCreationLevel> acl = expr.getLevels();
        if (!acl.isEmpty()) {
            for (ArrayCreationLevel ac : acl)
                handleExpression(ac.getDimension().orElse(null),parentScope);
        }

        handleExpression(expr.getInitializer().orElse(null),parentScope);
    }


    public void handleParameter(Parameter p, Scope parentScope) {

        if (p == null)
            return;
        if (p.getName()!=null) {
            String pname=p.getNameAsString();
            String ptype=p.getType().asString();

            if(!Obfuscator.keepField.contains(pname)){
                parentScope.setData(pname,ptype);

                int startLineNo=p.getName().getRange().get().begin.line;
                int startColNo=p.getName().getRange().get().begin.column;
                int endColNo=p.getName().getRange().get().end.column;

                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(startLineNo);
                rnode.setStartColNo(startColNo);
                rnode.setEndColNo(endColNo);
                rnode.setReplacementString(getHexValue(pname));
                Obfuscator.updateObfuscatorConfig(rnode);
            }

        }

    }
}
