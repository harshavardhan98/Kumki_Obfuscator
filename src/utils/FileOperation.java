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
    //rename
    public static void renameFile(String filePath, ArrayList<String> data, int opCode) {
        /*
         *   Algorithm:
         *     Step 1:  Read the file to a string
         *     Step 2:  Form a hashmap with the class name as key and replacement string as value
         *     Step 3:  Create the regex pattern with all the class name to be replaced and use regex matcher to replace the patterns
         *     Step 4:  Write the string to file using fileWriter
         *     Step 5:  Rename the file (Make sure all the streams to the file are closed)
         *
         *     Regex: (?:[^a-zA-Z0-9])(OnClickListener)(?:[^a-zA-Z0-9])
         *
         *      Reference:
         *           1. https://stackoverflow.com/a/1326962/10664312  -> regex matcher
         *           2. https://www.journaldev.com/875/java-read-file-to-string -> reading a file to a string
         *           3. https://stackoverflow.com/questions/3512471/what-is-a-non-capturing-group-in-regular-expressions
         *           4. https://stackoverflow.com/a/38296697 -> replacement of regex patter in group-1
         *
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
        StringBuilder patternString = new StringBuilder("(?:[^a-zA-Z0-9])(");

        for (String i : data) {
            String className = i;
            if(opCode == isFile)
                className = CommonUtils.getClassNameFromFilePath(i);

            tokens.put(className, CommonUtils.getHexValue(className));

            patternString.append(className).append("|");
        }

        patternString = new StringBuilder(patternString.substring(0, patternString.length() - 1));
        patternString.append(")(?:[^a-zA-Z0-9])");

        Pattern pattern = Pattern.compile(patternString.toString());
        Matcher matcher = pattern.matcher(fileContent);

        StringBuffer sb = new StringBuffer();
        while (matcher.find())
            matcher.appendReplacement(sb, matcher.group(0).replaceFirst(Pattern.quote(matcher.group(1)), tokens.get(matcher.group(1))));
        matcher.appendTail(sb);

        // step 4
        try {
            PrintWriter out = new PrintWriter(filePath);
            out.println(sb.toString());
            out.close();

            // step 5
            if(opCode == isFile) {
                File f = new File(filePath);
                renameDirectory(filePath, f.getParent() + File.separator + CommonUtils.getHexValue(CommonUtils.getClassNameFromFilePath(filePath)) + ".java");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void renameAllFiles(String projectPath, int opCode) {

        /*
            opCode = 0 (ClassFile)
            opCode = 1 (Methods)
        */

        File folder = new File(projectPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (!file.getName().startsWith(".")) {
                        if(opCode == isFile)
                            renameFile(file.getAbsolutePath(), classList, opCode);
                        //else if(opCode == isMethod)
                            //renameFile(file.getAbsolutePath(), new ArrayList<>(methodMap), opCode);
                    }
                } else if (file.isDirectory())
                    renameAllFiles(file.getAbsolutePath(), opCode);
            }
        }
    }

    /**************************************/

    public static void renameDirectory(String src, String dst) {
        //https://www.inf.unibz.it/~calvanese/teaching/06-07-ip/lecture-notes/uni09/node12.html

        File sourceFolder = new File(src);
        File destinationFolder = new File(dst);
        sourceFolder.renameTo(destinationFolder);
    }

    public static void renameAllDirectories(String projectPath) {
        File folder = new File(projectPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    renameAllDirectories(file.getAbsolutePath());

                    if (Collections.binarySearch(Constants.predefinedClassList, file.getName()) <= -1)
                        renameDirectory(file.getAbsolutePath(), file.getParent() + File.separator + CommonUtils.getHexValue(file.getName()));
                } else if (file.isFile())
                    renameFile(file.getAbsolutePath(), Constants.folderList,0);
            }
        }
    }

    /****************************************************************************/
}