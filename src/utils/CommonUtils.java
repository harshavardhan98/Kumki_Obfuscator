package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.FileSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;


import static utils.Constants.*;

public class CommonUtils {

    public static String getBasePackage(){
        char[] myNameChars = packageName.toCharArray();
        for (int i = 0; i < myNameChars.length; i++) {
            if(File.separator.equals(myNameChars[i] + ""))
                myNameChars[i] = '.';
        }
        return String.valueOf(myNameChars);
    }

    public static String getClassNameFromFilePath(String filePath) {
        //  usr/Desktop/file1.java -> file1
        String fileName = getFileNameFromFilePath(filePath);
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    public static String getFileNameFromFilePath(String filePath) {
        //  usr/Desktop/file1.java -> file1.java
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    }


    /****************************************************************************/

    public static ArrayList<String> loadPredefinedClassList() {
        ArrayList<String> temp = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/assets/androidClassList.txt"));
            String line = reader.readLine();

            while (line != null) {
                line = reader.readLine();
                temp.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return temp;
    }


    /****************************************************************************/

    // Build JSON file
    // Parses the JSON file
    // Stores the files & folders path into constant variables
    public static void buildJson(String path, JsonArray pkgJA, boolean flag, ArrayList<FileSystem> filesList) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (files != null) {
            for (File file : files) {
                if (!file.getName().startsWith(".")) {
                    FileSystem fs = new FileSystem();
                    if (file.isFile()) {
                        fs.setName(file.getName());
                        fs.setType("file");
                        fs.setPath(file.getParent());

                        if (!flag) {
                            JsonObject fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
                            pkgJA.add(fsJO);
                        } else
                            filesList.add(fs);
                    } else if (file.isDirectory()) {
                        fs.setName(file.getName());
                        fs.setType("directory");
                        fs.setPath(file.getParent());

                        ArrayList<FileSystem> fl = new ArrayList<>();
                        buildJson(file.getAbsolutePath(), pkgJA, true, fl);
                        fs.setFiles(fl);

                        JsonObject fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
                        pkgJA.add(fsJO);
                    }
                }
            }
        }
    }

    public static ArrayList<FileSystem> parseFileStructureJson(String path) {
        ArrayList<FileSystem> fs = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            JsonObject pkgJO = gson.fromJson(bufferedReader, JsonObject.class);
            JsonArray pkgJA = pkgJO.get("package").getAsJsonArray();

            FileSystem[] fileSystems = gson.fromJson(pkgJA, FileSystem[].class);
            Collections.addAll(fs, fileSystems);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return fs;
    }

    /****************************************************************************/



    public static void renameFile(String src, String dst) {
        //https://www.inf.unibz.it/~calvanese/teaching/06-07-ip/lecture-notes/uni09/node12.html

        File sourceFolder = new File(src);
        File destinationFolder = new File(dst);
        sourceFolder.renameTo(destinationFolder);
    }


}
