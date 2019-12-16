import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import utils.*;
import static utils.CommonUtils.*;
import static utils.FileOperation.*;

import model.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        //backupProject();
        //analyseProjectStructure();

        /*Main obj = new Main();
        ArrayList<String> list = getMethods(obj);
        System.out.println(list.toString());*/

        /*FileSystem obj = new FileSystem();
        ArrayList<String> list = getIdentifiers(obj);
        System.out.println(list.toString());*/

        getDependencyData();
        analyseProjectStructure();
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
}