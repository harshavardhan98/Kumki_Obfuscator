package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class fileOperations {

    public static void rename(File sourceFolder, File destinationFolder){
        //https://www.inf.unibz.it/~calvanese/teaching/06-07-ip/lecture-notes/uni09/node12.html
        sourceFolder.renameTo(destinationFolder);
    }

    public static void copy(File sourceFolder, File destinationFolder) {
        // https://howtodoinjava.com/java/io/how-to-copy-directories-in-java/

        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then copy the file directly to new location
        if (sourceFolder.isDirectory()) {
            //Verify if destinationFolder is already present; If not then create it
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
                System.out.println("Directory created :: " + destinationFolder);
            }

            //Get all files from source directory
            String files[] = sourceFolder.list();

            //Iterate over all files and copy them to destinationFolder one by one
            for (String file : files) {
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);

                //Recursive function call
                copy(srcFile, destFile);
            }
        }
        else {
            //Copy the file content from one place to another
            try {
                Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File copied :: " + destinationFolder);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void list(String path, int level) {
        File folder = new File(path);
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                for(int i=0;i<level;i++)
                    System.out.print(" ");

                if(file.getName().charAt(0)!='.')
                    System.out.println(file.getName());
            }
            else if (file.isDirectory()) {
                System.out.println(file.getName());
                list(file.getAbsolutePath(),level+2);
            }
        }
    }
}