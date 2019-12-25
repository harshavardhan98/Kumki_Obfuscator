import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.*;

import model.*;
import utils.Constants;
import utils.FileOperation;

import static utils.CommonUtils.*;
import static utils.Constants.*;
import static utils.FileOperation.*;

public class Main {
    public static void main(String[] args) {
        analyseProjectStructure();
        //backupProject();
        getDependencyData();

        //PackageObfuscation();

        /*try {
            //CompilationUnit cu = JavaParser.parse(file);
            //new MethodVisitor().visit(cu, null);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
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
        getFilesList(fsTemp, classList, folderList);


        for(String s:folderList)
            if(Collections.binarySearch(predefinedClassList,s)>=0)
                // todo remove this string s

    }

    /***********************************************************/

    public static void PackageObfuscation() {



        // rename the folders
        FileOperation.renameDirectory(Constants.projectRootDirectory+Constants.packageName);
    }

    public static void ClassObfuscation(){
        renameAllFiles(projectDirectory + packageName);
    }

    /***********************************************************/

    private static class MethodVisitor extends VoidVisitorAdapter {
        @Override
        public void visit(MethodDeclaration n, Object arg) {
            System.out.println(n.getName());
        }
    }
}