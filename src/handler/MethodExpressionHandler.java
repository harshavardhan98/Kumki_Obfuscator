package handler;

import com.github.javaparser.ast.ArrayCreationLevel;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import model.ReplacementDataNode;
import model.Scope;

import java.util.List;

import static utils.Encryption.getHexValue;

public class MethodExpressionHandler extends ExpressionHandler {


    public void handleMethodCallExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isMethodCallExpr())
            return;

        //TODO: CHECK
        MethodCallExpr methodCall = exp.asMethodCallExpr();
        String mname = methodCall.getName().getIdentifier();
        List<Expression> argList = methodCall.getArguments();
        if (argList != null) {
            for (Expression i : argList)
                handleExpression(i, parentScope);
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

                if (obj_type != null && compare(obj_type) && toBeReplaced1(mname, obj_type)) {
                    obfuscator.setArrayList(rnode);
                } else if (checkForStaticVariableAccess(obj_name) && toBeReplaced1(mname, obj_type)) {
                    obfuscator.setArrayList(rnode);
                } else {
                    System.out.print("");
                }
            } else if (obj_name_exp.isSuperExpr() && toBeReplaced(mname)) {
                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(mstart_line_num);
                rnode.setStartColNo(mstart_col_num);
                rnode.setEndColNo(mend_col_num);
                rnode.setReplacementString(getHexValue(mname));
                obfuscator.setArrayList(rnode);
            } else if (obj_name_exp.isMethodCallExpr()) {
                handleExpression(obj_name_exp.asMethodCallExpr(), parentScope);
            } else if (obj_name_exp.isThisExpr() && toBeReplaced1(mname, null)) {
                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(mstart_line_num);
                rnode.setStartColNo(mstart_col_num);
                rnode.setEndColNo(mend_col_num);
                rnode.setReplacementString(getHexValue(mname));
                obfuscator.setArrayList(rnode);
            } else if (obj_name_exp.isEnclosedExpr()) {

                EnclosedExpr enclosedExpr = obj_name_exp.asEnclosedExpr();
                if (enclosedExpr.getInner().isCastExpr()) {
                    String obj_type = enclosedExpr.getInner().asCastExpr().getType().asString();
                    if (toBeReplaced1(mname, obj_type)) {
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        rnode.setLineNo(mstart_line_num);
                        rnode.setStartColNo(mstart_col_num);
                        rnode.setEndColNo(mend_col_num);
                        rnode.setReplacementString(getHexValue(mname));
                        obfuscator.setArrayList(rnode);
                    }

                }
            }
        } else if (toBeReplaced1(mname, null)) {
            ReplacementDataNode rnode = new ReplacementDataNode();
            rnode.setLineNo(mstart_line_num);
            rnode.setStartColNo(mstart_col_num);
            rnode.setEndColNo(mend_col_num);
            rnode.setReplacementString(getHexValue(mname));
            obfuscator.setArrayList(rnode);
        }
    }

    public void handleCastExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isCastExpr())
            return;

        CastExpr castExpr = exp.asCastExpr();
        handleExpression(castExpr.getExpression(), parentScope);
    }

    public void handleObjectCreationExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isObjectCreationExpr())
            return;

        ObjectCreationExpr expr = exp.asObjectCreationExpr();
        List<Expression> expList = expr.getArguments();
        if (expList != null) {
            for (Expression e : expList)
                handleExpression(e, parentScope);
        }

        List<BodyDeclaration<?>> bodyList = expr.getAnonymousClassBody().orElse(null);
        if (bodyList != null) {
            for (BodyDeclaration<?> e : bodyList) {
                if (e.isMethodDeclaration()) {
                    NodeList<Parameter> parameters = e.asMethodDeclaration().getParameters();

                    for (Parameter p : parameters)
                        handleParameter(p, parentScope);

                    StatementHandler.handleStatement(e.asMethodDeclaration().getBody().orElse(null), parentScope);
                }
            }
        }
    }

    public void handleArrayCreationExpr(Expression exp, Scope parentScope) {
        if (exp == null || !exp.isArrayCreationExpr())
            return;

        ArrayCreationExpr expr = exp.asArrayCreationExpr();
        List<ArrayCreationLevel> acl = expr.getLevels();
        if (!acl.isEmpty()) {
            for (ArrayCreationLevel ac : acl)
                handleExpression(ac.getDimension().orElse(null), parentScope);
        }

        handleExpression(expr.getInitializer().orElse(null), parentScope);
    }

    @Override
    public void handleParameter(Parameter p, Scope parentScope) {

        if (p == null)
            return;
        if (p.getName()!=null) {
            String pname=p.getNameAsString();
            String ptype=p.getType().asString();
            parentScope.setData(pname,ptype);
        }
    }
}
