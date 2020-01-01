package utils;

import model.ReplacementDataNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Constants.*;

public class FileOperation {

    /****************************************************************************/
    //copy
    public static void copyFolder(File sourceFolder, File destinationFolder) {
        // https://howtodoinjava.com/java/io/how-to-copy-directories-in-java/

        //Check if sourceFolder is a directory or file
        if (sourceFolder.isDirectory()) {
            //Verify if destinationFolder is already present; If not then create it
            if (!destinationFolder.exists()) {
                destinationFolder.mkdir();
            }

            //Get all files from source directory
            String files[] = sourceFolder.list();

            //Iterate over all files and copy them to destinationFolder one by one
            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(sourceFolder, file);
                    File destFile = new File(destinationFolder, file);

                    //Recursive function call
                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            //Copy the file content from one place to another
            try {
                Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /****************************************************************************/

    public static void replaceInFiles(File file, ArrayList<ReplacementDataNode> arrayList) {
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(file.toPath()));
            for (ReplacementDataNode r : arrayList) {
                String temp = fileContent.get(r.getLineNo() - 1);
                temp = temp.substring(0, r.getStartColNo() - 1) + r.getReplacementString() + temp.substring(r.getEndColNo());
                fileContent.set(r.getLineNo() - 1, temp);
            }
            Files.write(file.toPath(), fileContent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /****************************************************************************/
}