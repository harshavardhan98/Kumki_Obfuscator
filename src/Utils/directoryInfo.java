package Utils;

import Model.Tree;

import java.io.File;

public class directoryInfo {


    public static void listFiles(String path, int level)
    {

        File folder = new File(path);


        File[] files = folder.listFiles();

        for (File file : files)
        {
            if (file.isFile())
            {
                for(int i=0;i<level;i++)
                    System.out.print(" ");

                if(file.getName().charAt(0)!='.')
                    System.out.println(file.getName());
            }
            else if (file.isDirectory())
            {
                System.out.println(file.getName());
                listFiles(file.getAbsolutePath(),level+2);
            }
        }

    }


    public static void buildTree(String path, Tree<String> t){

        File folder=new File(path);
        File[] files=folder.listFiles();

        for(File file:files){
            if(file.isFile() && file.getName().charAt(0)!='.'){
                t.addLeaf(file.getName());
            }
            else if(file.isDirectory()){
                t.addLeaf(file.getName());
                buildTree(file.getAbsolutePath(),t.getTree(file.getName()));
            }
        }
    }


}
