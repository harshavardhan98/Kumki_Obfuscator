package utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Constants.classList;

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
    //rename

    public static void renameFile(String filePath,ArrayList<String> data) {
        /*
         *   Algorithm:
         *     Step 1:  Read the file to a string
         *     Step 2:  Form a hashmap with the class name as key and replacement string as value
         *     Step 3:  Create the regex pattern with all the class name to be replaced and use regex matcher to replace the patterns
         *     Step 4:  Write the string to file using fileWriter
         *     Step 5:  Rename the file (Make sure all the streams to the file are closed)
         *
         *      Reference:
         *           1. https://stackoverflow.com/a/1326962/10664312  -> regex matcher
         *           2. https://www.journaldev.com/875/java-read-file-to-string -> reading a file to a string
         * */

        // step 1:
        String fileContent = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            // delete the last new line separator
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();
            fileContent = stringBuilder.toString();
        } catch (Exception ie) {
            System.out.println(ie.getMessage());
        }

        // step 2, 3:
        Map<String, String> tokens = new HashMap<>();
        StringBuilder patternString = new StringBuilder("(");

        for (String i :data) {
            String className = CommonUtils.getClassNameFromFilePath(i);

            tokens.put(className, CommonUtils.getHexValue(className));

            patternString.append(className).append("|");
            patternString = new StringBuilder(patternString.substring(0, patternString.length() - 1));
            patternString.append(")");
        }

        Pattern pattern = Pattern.compile(patternString.toString());
        Matcher matcher = pattern.matcher(fileContent);

        StringBuffer sb = new StringBuffer();
        while (matcher.find())
            matcher.appendReplacement(sb, tokens.get(matcher.group(1)));
        matcher.appendTail(sb);

        // step 4
        try {
            PrintWriter out = new PrintWriter(filePath);
            out.println(sb.toString());
            out.close();

            // step 5
            File f = new File(filePath);
            renameFolder(filePath, f.getParent() + File.separator + CommonUtils.getHexValue(CommonUtils.getClassNameFromFilePath(filePath)) + ".java");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void renameAllFiles(String projectPath) {
        File folder = new File(projectPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.getName().startsWith("."))
                        renameFile(file.getAbsolutePath(), classList);
                }
                else if (file.isDirectory())
                    renameAllFiles(file.getAbsolutePath());
            }
        }
    }

    /**************************************/
    public static void renameDirectory(String projectPath,ArrayList<String> folderList) {
        File folder = new File(projectPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    renameDirectory(file.getAbsolutePath(),folderList);

                    if (Collections.binarySearch(Constants.predefinedClassList, file.getName()) <= -1)
                        renameFolder(file.getAbsolutePath(), file.getParent() + File.separator + CommonUtils.getHexValue(file.getName()));
                } else if (file.isFile())
                    renameFile(file.getAbsolutePath(),folderList);
            }
        }
    }

    public static void renameFolder(String src, String dst) {
        //https://www.inf.unibz.it/~calvanese/teaching/06-07-ip/lecture-notes/uni09/node12.html

        File sourceFolder = new File(src);
        File destinationFolder = new File(dst);
        sourceFolder.renameTo(destinationFolder);
    }

    /**************************************/

    public static void renameMethodNameInFiles(String filePath, HashMap<String, String> newNames, Pattern pattern) {

        String fileContent = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            // delete the last new line separator
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();
            fileContent = stringBuilder.toString();
        } catch (Exception ie) {
            System.out.println(ie.getMessage());
        }


        Matcher matcher = pattern.matcher(fileContent);
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
            matcher.appendReplacement(sb, newNames.get(matcher.group(1)));
        matcher.appendTail(sb);
        System.out.println(sb.toString());

        // step 4
        try {
            PrintWriter out = new PrintWriter(filePath);
            out.println(sb.toString());
            out.close();
            //System.out.println(filePath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public static void renameMethodNames(String projectPath, HashMap<String, String> newNames, Pattern pattern) {
        File folder = new File(projectPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    renameMethodNames(file.getAbsolutePath(), newNames, pattern);
                } else if (file.isFile()) {
                    renameMethodNameInFiles(file.getAbsolutePath(), newNames, pattern);
                }
            }
        }
    }

    /****************************************************************************/
}