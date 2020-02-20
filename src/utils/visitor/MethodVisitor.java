package utils.visitor;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import model.MethodModel;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class MethodVisitor extends VoidVisitorAdapter {

    public Map<String, ArrayList<MethodModel>> methodMap;

    public MethodVisitor(Map<String, ArrayList<MethodModel>> methodMap) {
        this.methodMap = methodMap;
    }

    @Override
    public void visit(MethodDeclaration n, Object fileNameObj) {

        MethodModel model=new MethodModel();
        model.setName(n.getNameAsString());
        model.setAccessModifiers(n.getModifiers());
        if(n.getParameters()!=null)
            model.setNoOfParameters(n.getParameters().size());
        model.setReturnType(n.getType());

        //Methods list
        String filePath = (String) fileNameObj;
        if (!isOverride(n))
            populateMethodMap(filePath,model);
    }

    public void populateMethodMap(String fileName, MethodModel method){
        ArrayList<MethodModel> methodList;
        if (methodMap.containsKey(fileName)) {
            methodList = methodMap.get(fileName);
            if (methodList == null)
                methodList = new ArrayList<>();
            methodList.add(method);
        } else {
            methodList = new ArrayList<>();
            methodList.add(method);
        }
        methodMap.put(fileName, methodList);
    }

    public static Boolean isOverride(MethodDeclaration n) {
        List<AnnotationExpr> annotationExpr = n.getAnnotations();
        if (annotationExpr != null) {
            String annotations = annotationExpr.toString();
            if (annotations.contains("@Override"))
                return true;
        }
        return false;
    }
}
