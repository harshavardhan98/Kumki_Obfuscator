import Model.Tree;
import Utils.directoryInfo;

import java.io.File;

public class Main {
    public static void main(String[] args){

        String projectDirectory="/Users/harshavardhanp/Downloads/dsc3";
        projectDirectory+="/app/src/main/java/";
        String packageName="in/cocomo/listview";

        //directoryInfo.listFiles(projectDirectory+packageName,0);

        File f=new File(projectDirectory+packageName);
        Tree<String> t=new Tree<>("");
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
        System.out.println("\n\n");


    }
}
