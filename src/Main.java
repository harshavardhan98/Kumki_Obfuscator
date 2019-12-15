import model.FileSystem;
import org.unix4j.Unix4j;
import org.unix4j.unix.Grep;
import org.unix4j.unix.sed.SedOptions;
import utils.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import utils.Constants;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static utils.CommonUtils.*;
import static utils.fileOperations.copy;

public class Main {
    public static void main(String[] args){
        analyseProjectStructure();
        //backupProject();

        Unix4j.cat(Constants.projectRootDirectory + Constants.packageName + "MainActivity.java").sed("s/import/logesh/g").toFile(Constants.projectRootDirectory + Constants.packageName + "MainActivity1.java");

        /*Main obj = new Main();
        ArrayList<String> list = getMethods(obj);
        System.out.println(list.toString());*/

        /*FileSystem obj = new FileSystem();
        ArrayList<String> list = getIdentifiers(obj);
        System.out.println(list.toString());*/

        getDependencyData(Constants.projectDirectory + "originalFileStructure.json");
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

        File file = new File(Constants.projectDirectory + "originalFileStructure.json");
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

            Constants.backupProjectDirectory = destinationFolder.getAbsolutePath();
            if(destinationFolder.getAbsolutePath().contains("/"))
                Constants.backupProjectDirectory += "/";        //Linux
            else
                Constants.backupProjectDirectory += "\\";       //Windows

            copy(sourceFolder, destinationFolder);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}