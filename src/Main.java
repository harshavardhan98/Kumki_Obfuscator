import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import utils.*;
import static utils.CommonUtils.*;
import static utils.FileOperation.*;
import javax.lang.model.SourceVersion;
import model.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args){


        analyseProjectStructure();
        backupProject();
        loadClassList();
        renameMethods();


        /*Main obj = new Main();
        ArrayList<String> list = getMethods(obj);
        System.out.println(list.toString());*/

        /*FileSystem obj = new FileSystem();
        ArrayList<String> list = getIdentifiers(obj);
        System.out.println(list.toString());*/

        //getDependencyData();
        //renamePackage();
        //analyseProjectStructure();
    }

    private static void analyseProjectStructure() {
        File f = new File(Constants.projectRootDirectory + Constants.packageName);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootJO = new JsonObject();
        JsonArray pkgJA = new JsonArray();

        buildJson(f.getAbsolutePath(), pkgJA,false, null);
        rootJO.add("package", pkgJA);
        String fileStructureJS = gson.toJson(rootJO);
        //System.out.println(fileStructureJS);

        File file = new File(Constants.projectDirectory + Constants.fileStructureJsonPath);
        try {
            FileWriter fr = new FileWriter(file);
            fr.write(fileStructureJS);
            fr.close();
            //System.out.println(file.getName() + " created");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void backupProject() {
        try{
            File sourceFolder = new File(Constants.projectDirectory);
            File destinationFolder = new File(sourceFolder.getAbsolutePath() + "1");

            Constants.backupProjectDirectory = destinationFolder.getAbsolutePath() + File.separator;
            copyFolder(sourceFolder, destinationFolder);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void getDependencyData() {
        /*
         *   Algorithm:
         *   Step 1: Parses the json file and gets the list of java class files
         *   Step 2: Call the renameAllFiles Method to populate the dependency data for each file and renames the classes
         * */

        // get the list of class files
        ArrayList<FileSystem> fsTemp = parseFileStructureJson(Constants.projectDirectory + Constants.fileStructureJsonPath);
        ArrayList<String> classList = new ArrayList<>();
        getFilesList(fsTemp, classList);

        // rename all the java class
        renameAllFiles(classList, Constants.projectRootDirectory + Constants.packageName);
    }

    public static void renamePackage(){

        // get the list of folders
        ArrayList<FileSystem> fsTemp = parseFileStructureJson(Constants.projectDirectory + Constants.fileStructureJsonPath);
        ArrayList<String> folderList = new ArrayList<>();
        getFolderList(fsTemp,folderList);


        // rename the folders
        FileOperation.renameDirectory(folderList,Constants.projectRootDirectory+Constants.packageName);

    }

    public static void renameMethods(){
        /*
        *   O.P: Renames all the method name except overridden methods of inbuilt android classes and class constructors
        *   Algorithm:
        *   Step 1: Parse all the files. When parsing a file store the name of the file and the class it extends
        *   Step 2: If it extends a class which is a part of android SDK do not rename its override methods
        *   Step 3: For each method that matches the regular expression
        *           3.1. Check if it a constructor if so do not rename it
        *           3.2. Check if the method is overridden and the parent class is a user defined class then add the method to set of methods
        *           3.3. If the method is user defined add it to the set of methods
        *   Step 4: Now generate the regular expression matcher and visit all the files and rename the matched strings accordingly
        * */
        ArrayList<FileSystem> fsTemp = parseFileStructureJson(Constants.projectDirectory + Constants.fileStructureJsonPath);
        ArrayList<String> classList = new ArrayList<>();
        getFilesList(fsTemp, classList);

        HashSet<String> methodList=new HashSet<>();
        FileOperation.collectMethodNames(Constants.projectRootDirectory+Constants.packageName,methodList);

        HashMap<String,String> newNames=new HashMap<>();
        for(String s:methodList)
            newNames.put(s,getHexValue(s));

        String patternString = "(";
        for(String i : newNames.keySet()){
                patternString += i + "|";
        }

        if(patternString.length()>1)
            patternString = patternString.substring(0,patternString.length() - 1);
        patternString += ")";

        Pattern pattern = Pattern.compile(patternString);

       FileOperation.renameMethodNames(Constants.projectRootDirectory+Constants.packageName,newNames,pattern);

    }
}