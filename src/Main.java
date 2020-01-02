import com.github.javaparser.JavaParser;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.*;
import utils.*;
import static utils.CommonUtils.*;
import static utils.Constants.*;
import static utils.FileOperation.*;

public class Main {

    private static int jsonFileNameCount = 0;

    public static void main(String[] args) {
        //backupProject();
        analyseProjectStructure();
        getDependencyData();

        //removeComments();
        //MethodObfuscation();
        //ClassObfuscation();
        //PackageObfuscation();
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
    public static void ClassObfuscation() {
        ClassObfuscator co = new ClassObfuscator();
        co.obfuscate();
        analyseProjectStructure();
        getDependencyData();
    }

    public static void PackageObfuscation() {
        PackageObfuscator po = new PackageObfuscator();
        po.obfuscate();
        analyseProjectStructure();
        getDependencyData();
    }

    public static void MethodObfuscation() {
        MethodObfuscator mo = new MethodObfuscator();
        mo.obfuscate();
    }

    public static void removeComments(){

        for(String s:classList){

            try{
                File f=new File(s);
                Obfuscator obfuscator=new Obfuscator();
                obfuscator.setCurrentFile(f);

                CompilationUnit cu=JavaParser.parse(f);
                cu.getComments();
                List<Comment> l=cu.getComments();

                for(Comment comment:l){

                    if(comment.isLineComment()){
                        ReplacementDataNode r=new ReplacementDataNode();
                        Position begin=comment.getRange().orElse(null).begin;
                        Position end=comment.getRange().orElse(null).end;
                        r.setLineNo(begin.line);
                        r.setStartColNo(begin.column);
                        r.setEndColNo(end.column);
                        r.setReplacementString("");
                        obfuscator.setArrayList(r);
                    }
                    else if(comment.isBlockComment()){
                        ReplacementDataNode r=new ReplacementDataNode();
                        Position begin=comment.getRange().orElse(null).begin;
                        Position end=comment.getRange().orElse(null).end;
                        r.setLineNo(begin.line);
                        r.setStartColNo(begin.column);
                        r.setEndLineNo(end.line);
                        r.setEndColNo(end.column);
                        obfuscator.setArrayList(r);
                    }
                }

                obfuscator.replaceComments();

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    /***********************************************************/
}