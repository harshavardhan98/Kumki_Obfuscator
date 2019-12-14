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

import static utils.fileOperations.copy;

public class Main {
    public static void main(String[] args){
        //analyseProjectStructure();
        //backupProject();

        /*List<String> result = Unix4j.cat(Constants.projectRootDirectory + Constants.packageName + "MainActivity.java").grep("import").sed("s/import/logesh/g").toStringList();
        System.out.println(result.toString());*/

        /*Main obj = new Main();
        ArrayList<String> list = CommonUtils.getMethods(obj);
        System.out.println(list.toString());*/

        /*FileSystem obj = new FileSystem();
        ArrayList<String> list = CommonUtils.getIdentifiers(obj);
        System.out.println(list.toString());*/
    }

    private static void analyseProjectStructure() {
        File f = new File(Constants.projectRootDirectory + Constants.packageName);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootJO = new JsonObject();
        JsonArray pkgJA = new JsonArray();

        CommonUtils.buildJson(f.getAbsolutePath(), pkgJA,false, null);
        rootJO.add("package", pkgJA);
        String fileStructureJS = gson.toJson(rootJO);
        //System.out.println(fileStructureJS);

        File file = new File(Constants.projectDirectory + "originalFileStructure.json");
        try {
            FileWriter fr = new FileWriter(file);
            fr.write(fileStructureJS);
            fr.close();
            System.out.println(file.getName() + " created");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void backupProject() {
        try{
            File sourceFolder = new File(Constants.projectDirectory);
            File destinationFolder = new File(sourceFolder.getAbsolutePath() + "1");
            copy(sourceFolder, destinationFolder);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}