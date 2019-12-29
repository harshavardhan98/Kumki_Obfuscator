package model;

import java.util.ArrayList;
import java.util.HashMap;

public class Scope {
    private int startLine;
    private int endLine;
    private HashMap<String, String> data;
    private Scope parentScope;
    private ArrayList<Scope> childScope;

    public Scope() {
        data = new HashMap<>();
        childScope = new ArrayList<Scope>();
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public void setData(HashMap<String, String> data) {
        this.data = data;
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

    public void addIdentifier(String vname, String vtype) {
        data.put(vname, vtype);
    }

    public void addChildScope(Scope s) {
        childScope.add(s);
    }

    public static String findDataTypeOfIdentifier(String name, Scope currentScope) {
        if (currentScope == null)
            return null;

        if (currentScope.data.containsKey(name))
            return currentScope.data.get(name);

        return findDataTypeOfIdentifier(name, currentScope.parentScope);
    }

    public static void setScope(Scope parentScope,Scope childScope){

        childScope.setParentScope(parentScope);

        if(parentScope!=null)
            parentScope.addChildScope(childScope);
    }
};