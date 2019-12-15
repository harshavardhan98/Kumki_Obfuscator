package model;

import java.util.ArrayList;
import java.util.HashMap;

public class IdentifierDependency {
    private String name;
    private HashMap<String,ArrayList<String>> entries;

    public IdentifierDependency(String name) {
        this.name = name;
        entries=new HashMap<String,ArrayList<String>>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, ArrayList<String>> getEntries() {
        return entries;
    }

    public void setEntries(HashMap<String, ArrayList<String>> entries) {
        this.entries = entries;
    }

}