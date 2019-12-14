package src;

import Model.FileSystem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args){

        //String projectDirectory = "/Users/harshavardhanp/Downloads/dsc3";
        String projectDirectory = "C:\\Users\\Logesh Dinakaran\\OneDrive\\Desktop\\kumkiTest";

        //projectDirectory += "/app/src/main/java/";
        projectDirectory += "\\app\\src\\main\\java\\";

        //String packageName = "com/example/dsc_onboarding";
        String packageName = "com\\example\\dsc_onboarding";

        //directoryInfo.listFiles(projectDirectory+packageName,0);

        File f=new File(projectDirectory + packageName);
        /*Tree<String> t=new Tree<>("");
        directoryInfo.buildTree(f.getAbsolutePath(),t);

        System.out.println("\n\n");
        System.out.println(t.toString());
        System.out.println("\n\n");

        for(Tree<String> i:t.getSubTrees()){
            System.out.println(i.getHead());
        }

        System.out.println("\n\n");
        Tree<String> temp= t.getTree("adapter");
        for(Tree<String> i:temp.getSubTrees()){
            System.out.println(i.getHead());
        }
        System.out.println("\n\n");*/
        //directoryInfo.buildJson(f.getAbsolutePath(), fs);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootJO = new JsonObject();
        JsonArray pkgJA = new JsonArray();
        FileSystem fs = new FileSystem();

        /*********************************************************/
        //Directory

        fs.setName("Model");
        fs.setType("directory");

        List<FileSystem> filesList = new ArrayList<>();

        FileSystem fileFS = new FileSystem();
        fileFS.setName("Tree.java");
        fileFS.setType("file");
        filesList.add(fileFS);

        fileFS = new FileSystem();
        fileFS.setName("Graph.java");
        fileFS.setType("file");
        filesList.add(fileFS);

        fs.setFiles(filesList);
        JsonObject fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
        pkgJA.add(fsJO);

        /*********************************************************/
        //File

        fs = new FileSystem();
        fs.setName("Main.java");
        fs.setType("file");

        fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
        pkgJA.add(fsJO);

        /*********************************************************/

        rootJO.add("package", pkgJA);
        System.out.println(gson.toJson(rootJO));
    }
}
