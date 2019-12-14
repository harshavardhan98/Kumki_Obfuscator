package Model;

import java.util.ArrayList;
import java.util.HashMap;

public class IdentifierDependency {

    String identifierName;
    HashMap<String,ArrayList<Integer>> hm;

    public IdentifierDependency() {
    }

    public IdentifierDependency(String identifierName, HashMap<String, ArrayList<Integer>> hm) {
        this.identifierName = identifierName;
        this.hm = hm;
    }

    public String getIdentifierName() {
        return identifierName;
    }

    public void setIdentifierName(String identifierName) {
        this.identifierName = identifierName;
    }

    public HashMap<String, ArrayList<Integer>> getHm() {
        return hm;
    }

    public void setHm(HashMap<String, ArrayList<Integer>> hm) {
        this.hm = hm;
    }
}
