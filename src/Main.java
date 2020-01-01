import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.FileSystem;
import utils.MethodObfuscator;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

import static utils.CommonUtils.*;
import static utils.Constants.*;
import static utils.FileOperation.copyFolder;

public class Main {

    private static int jsonFileNameCount = 0;

    public static void main(String[] args) {
        backupProject();
        analyseProjectStructure();
        getDependencyData();

        MethodObfuscation();
    }

    /***********************************************************/

    public static void analyseProjectStructure() {
        File f = new File(projectRootDirectory + packageName);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootJO = new JsonObject();
        JsonArray pkgJA = new JsonArray();

        buildJson(f.getAbsolutePath(), pkgJA, false, null);
        rootJO.add("package", pkgJA);
        String fileStructureJS = gson.toJson(rootJO);

        File file = new File(projectDirectory + (jsonFileNameCount++) + fileStructureJsonPath);
        try {
            FileWriter fr = new FileWriter(file);
            fr.write(fileStructureJS);
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void backupProject() {
        try {
            File sourceFolder = new File(projectDirectory);
            File destinationFolder = new File(sourceFolder.getAbsolutePath() + "1");

            backupProjectDirectory = destinationFolder.getAbsolutePath() + File.separator;
            copyFolder(sourceFolder, destinationFolder);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void getDependencyData() {
        ArrayList<String> predefinedClassList = loadPredefinedClassList();

        ArrayList<FileSystem> fsTemp = parseFileStructureJson(projectDirectory + (jsonFileNameCount - 1) + fileStructureJsonPath);
        getFilesList(fsTemp);

        for (int i = 0; i < classList.size(); i++) {
            if (Collections.binarySearch(predefinedClassList, classList.get(i)) >= 0)
                classList.remove(i--);
        }

        for (int i = 0; i < folderList.size(); i++) {
            if (Collections.binarySearch(predefinedClassList, folderList.get(i)) >= 0)
                folderList.remove(i--);
        }
    }

    /***********************************************************/
    //Obfuscations
    public static void MethodObfuscation() {
        MethodObfuscator mo = new MethodObfuscator();
        mo.obfuscate();
    }

    /***********************************************************/
}