package model;

import java.util.ArrayList;
import java.util.HashMap;

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

    public boolean checkIfGivenVariableExistsInScope(String identifierName) {
        if (data.containsKey(identifierName))
            return true;

        if (parentScope != null)
            return parentScope.checkIfGivenVariableExistsInScope(identifierName);
        else
            return false;
    }
}
