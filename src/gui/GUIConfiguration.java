package gui;

import utils.Constants.*;

import java.util.ArrayList;

public class GUIConfiguration {
    String rootFolder;
    String packageName;
    ArrayList<Mode> obfuscationMode;
    ArrayList<String> keepClasses;
    ArrayList<String> keepMethods;
    ArrayList<String> keepFields;

    public GUIConfiguration() {
        obfuscationMode=new ArrayList<>();
        keepClasses=new ArrayList<>();
        keepMethods=new ArrayList<>();
        keepFields=new ArrayList<>();
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public ArrayList<Mode> getObfuscationMode() {
        return obfuscationMode;
    }

    public void setObfuscationMode(ArrayList<Mode> obfuscationMode) {
        this.obfuscationMode = obfuscationMode;
    }

    public ArrayList<String> getKeepClasses() {
        return keepClasses;
    }

    public void setKeepClasses(ArrayList<String> keepClasses) {
        this.keepClasses = keepClasses;
    }

    public ArrayList<String> getKeepMethods() {
        return keepMethods;
    }

    public void setKeepMethods(ArrayList<String> keepMethods) {
        this.keepMethods = keepMethods;
    }

    public ArrayList<String> getKeepFields() {
        return keepFields;
    }

    public void setKeepFields(ArrayList<String> keepFields) {
        this.keepFields = keepFields;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
