package model;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.type.Type;
import com.sun.jdi.connect.Connector;

import java.util.ArrayList;
import java.util.EnumSet;

public class MethodModel {

    String name;
    Type returnType;
    ArrayList<Modifier> accessModifiers;
    int noOfParameters;

    public MethodModel(){
        accessModifiers=new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public ArrayList<Modifier> getAccessModifiers() {
        return accessModifiers;
    }

    public void setAccessModifiers(EnumSet<Modifier> accessModifierSet) {
        accessModifiers.addAll(accessModifierSet);
    }

    public int getNoOfParameters() {
        return noOfParameters;
    }

    public void setNoOfParameters(int noOfParameters) {
        this.noOfParameters = noOfParameters;
    }
}
