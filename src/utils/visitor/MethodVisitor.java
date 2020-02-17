package utils.visitor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodVisitor extends VoidVisitorAdapter {

    public Map<String, ArrayList<String>> methodMap;

    public MethodVisitor(Map<String, ArrayList<String>> methodMap) {
        this.methodMap = methodMap;
    }

    @Override
    public void visit(MethodDeclaration n, Object fileNameObj) {
        //Methods list
        String filePath = (String) fileNameObj;
        if (!isOverride(n))
            populateMethodMap(filePath, n.getNameAsString());
    }

    public void populateMethodMap(String fileName, String method){
        ArrayList<String> methodList;
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

    public Boolean isOverride(MethodDeclaration n) {
        List<AnnotationExpr> annotationExpr = n.getAnnotations();
        if (annotationExpr != null) {
            String annotations = annotationExpr.toString();
            if (annotations.contains("@Override"))
                return true;
        }
        return false;
    }
}
