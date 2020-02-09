package model;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static utils.Constants.classList;
import static utils.Constants.innerClassList;
import static utils.FileOperation.getClassNameFromFilePath;

public class Scope {
    private HashMap<String, String> data;
    private Scope parentScope;
    private ArrayList<Scope> childScope;

    public Scope() {
        data = new HashMap<>();
        childScope = new ArrayList<Scope>();
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
    }

    public void setData(String vname, String vtype) {
        this.data.put(vname, vtype);
    }

    public Scope getParentScope() {
        return parentScope;
    }

    public void setParentScope(Scope parentScope) {
        this.parentScope = parentScope;
    }

    public ArrayList<Scope> getChildScope() {
        return childScope;
    }

    public void setChildScope(ArrayList<Scope> childScope) {
        this.childScope = childScope;
    }

    public void setChildScope(Scope s) {
        childScope.add(s);
    }

    /***********************************************************/

    public void setScope(Scope parentScope) {
        // Set scope in both sides
        // P -> C and C -> P

        this.setParentScope(parentScope);

        if (parentScope != null)
            parentScope.setChildScope(this);
    }

    public String findDataTypeOfIdentifier(String name) {
        if (data.containsKey(name))
            return data.get(name);

        if(parentScope != null)
            return parentScope.findDataTypeOfIdentifier(name);
        else
            return null;
    }

    public boolean checkIfGivenVariableExistsInScope(String identifierName){
        if(data.containsKey(identifierName))
            return true;

        if(parentScope!=null)
            return parentScope.checkIfGivenVariableExistsInScope(identifierName);
        else
            return false;
    }

    public boolean checkIfGivenVariableIsFromUserDefinedClass(String identifierName){

        if(data.containsKey(identifierName)){

            String dataType=data.get(identifierName);
            // check if the variable belongs to user defined class

            for(String s:classList){
                File f=new File(s);
                if(f.getName().equals(dataType+".java")){
                    return true;
                }
            }

            for(String s:innerClassList){
                if(s.equals(dataType))
                    return true;
            }

            return  false;
        }


        if(parentScope!=null)
            return parentScope.checkIfGivenVariableIsFromUserDefinedClass(identifierName);
        else
            return false;
    }
};