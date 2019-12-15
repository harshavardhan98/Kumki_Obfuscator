package utils;

import org.unix4j.Unix4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class FileOperation {

    /****************************************************************************/
    //copy

    public static void copyFolder(File sourceFolder, File destinationFolder) {
        // https://howtodoinjava.com/java/io/how-to-copy-directories-in-java/

        //Check if sourceFolder is a directory or file
        //If sourceFolder is file; then copy the file directly to new location
        if (sourceFolder.isDirectory()) {
            //Verify if destinationFolder is already present; If not then create it
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
                //System.out.println("Directory created :: " + destinationFolder);
            }

            //Get all files from source directory
            String files[] = sourceFolder.list();

            //Iterate over all files and copy them to destinationFolder one by one
            if(files != null) {
                for (String file : files) {
                    File srcFile = new File(sourceFolder, file);
                    File destFile = new File(destinationFolder, file);

                    //Recursive function call
                    copyFolder(srcFile, destFile);
                }
            }
        }
        else {
            //Copy the file content from one place to another
            try {
                Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                //System.out.println("File copied :: " + destinationFolder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /****************************************************************************/
    //rename

    public static void renameFile(ArrayList<String> classList, String filePath) {
        File f = new File(filePath);
        File temp = new File(f.getParent() + File.separator + "kumkiTemp.java");

        for (String path : classList) {
            // getting the fileName(frag1.java) from the entire classPath
            String fileName = path.substring(path.lastIndexOf(File.separator) + 1);

            // getting the frag1 from frag1.java
            String className = fileName.substring(0, fileName.lastIndexOf("."));

            String replaceString = "s/" + className + File.separator + CommonUtils.getHexValue(fileName) + "/g";
            Unix4j.cat(filePath).sed(replaceString).toFile(temp);

            try {
                FileInputStream fis = new FileInputStream(temp.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(f.getAbsolutePath());

                int b;
                while ((b = fis.read()) != -1)
                    fos.write(b);

                fis.close();
                fos.close();

                temp.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        renameFolder(f.getAbsolutePath(), f.getParent() + File.separator + CommonUtils.getHexValue(f.getName()) + ".java");
    }

    public static void renameAllFiles(ArrayList<String> classList, String projectPath) {
        File folder = new File(projectPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.getName().startsWith("."))
                        renameFile(classList, file.getAbsolutePath());
                } else if (file.isDirectory())
                    renameAllFiles(classList, file.getAbsolutePath());
            }
        }
    }

    public static void renameFolder(String src, String dst) {
        //https://www.inf.unibz.it/~calvanese/teaching/06-07-ip/lecture-notes/uni09/node12.html

        File sourceFolder = new File(src);
        File destinationFolder = new File(dst);

        sourceFolder.renameTo(destinationFolder);
    }
}