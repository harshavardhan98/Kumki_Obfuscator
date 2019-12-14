package model;

import java.util.ArrayList;
import java.util.HashMap;

public class IdentifierDependency {
    private String name;
    private HashMap<String,ArrayList<Integer>> entries;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, ArrayList<Integer>> getEntries() {
        return entries;
    }

    public void setEntries(HashMap<String, ArrayList<Integer>> entries) {
        this.entries = entries;
    }
}