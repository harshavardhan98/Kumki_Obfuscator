import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.FileSystem;
import model.ReplacementDataNode;
import model.Scope;
import utils.ClassObfuscatorUtils;
import utils.ParserOperation;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static utils.CommonUtils.*;
import static utils.Constants.*;
import static utils.FileOperation.*;

public class Main {

    public static void main(String[] args) {

        backupProject();
        analyseProjectStructure();
        getDependencyData();

        PackageObfuscation();
        ClassObfuscation();
        MethodObfuscation();

        try {
            for (String filePath : classList) {
                //TODO: Comment out filePath
                if (filePath.contains("MainActivity")) {
                    File file = new File(filePath);
                    CompilationUnit cu = JavaParser.parse(file);
                    ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(file.getName())).orElse(null);
                    ClassObfuscatorUtils.handleClass(clas);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***********************************************************/

    private static void analyseProjectStructure() {
        File f = new File(projectRootDirectory + packageName);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootJO = new JsonObject();
        JsonArray pkgJA = new JsonArray();

        buildJson(f.getAbsolutePath(), pkgJA, false, null);
        rootJO.add("package", pkgJA);
        String fileStructureJS = gson.toJson(rootJO);

        File file = new File(projectDirectory + fileStructureJsonPath);
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
        loadPredefinedClassList();

        ArrayList<FileSystem> fsTemp = parseFileStructureJson(projectDirectory + fileStructureJsonPath);
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
    public static void PackageObfuscation() {
        renameAllDirectories(projectRootDirectory + packageName);
        analyseProjectStructure();
    }

    public static void ClassObfuscation() {
        renameAllFiles(projectRootDirectory + packageName, isFile);
        analyseProjectStructure();
    }

    public static void MethodObfuscation() {

        try {
            for (String filePath : classList) {
                //TODO: Comment out filePath
                if (filePath.contains("MainActivity")) {
                    File file = new File(filePath);
                    CompilationUnit cu = JavaParser.parse(file);
                    ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(file.getName())).orElse(null);
                    ParserOperation.setCurrentFile(file.getName());
                    ParserOperation.handleClass(clas);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /***********************************************************/
}