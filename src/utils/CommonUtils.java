package utils;

import model.FileSystem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class CommonUtils {

    public static void buildJson(String path, JsonArray pkgJA, boolean flag, List<FileSystem> filesList) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if(files != null) {
            for (File file : files) {
                if(!file.getName().startsWith(".")) {
                    FileSystem fs = new FileSystem();
                    if (file.isFile()) {
                        if(!flag) {
                            fs.setName(file.getName());
                            fs.setType("file");
                            fs.setPath(file.getParent());

                            JsonObject fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
                            pkgJA.add(fsJO);
                        }
                        else{
                            fs.setName(file.getName());
                            fs.setType("file");
                            fs.setPath(file.getParent());
                            filesList.add(fs);
                        }
                    }
                    else if (file.isDirectory()) {
                        fs.setName(file.getName());
                        fs.setType("directory");
                        fs.setPath(file.getParent());

                        List<FileSystem> fl = new ArrayList<>();
                        buildJson(file.getAbsolutePath(), pkgJA, true, fl);
                        fs.setFiles(fl);

                        JsonObject fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
                        pkgJA.add(fsJO);
                    }
                }
            }
        }
    }

    /****************************************************************************/
    //Vignere Cipher

    public static String getHexValue(String value) {
        value = value.toLowerCase();
        String keyValue = Constants.keyValue;
        String tempKey = keyValue;

        int len1 = value.length();
        int len2 = keyValue.length();
        int len3 = (int)Math.ceil(len1/(double)len2);

        for(int i=1; i < len3; i++)
            keyValue += tempKey;

        String text = "";
        for(int i=0; i < value.length() ; i++) {
            char c = value.charAt(i);
            char k = keyValue.charAt(i);
            int cix = c - 97;
            int key = k - 97;
            cix = (cix + key) % 26 + 97;
            text += (char)(cix);
        }

        return text;
    }

    /****************************************************************************/

    public static ArrayList<String> getMethods(Object object){
        ArrayList<String> list = new ArrayList<>();
        Class cls = object.getClass();
        Method[] methods = cls.getDeclaredMethods();
        for (Method method:methods)
            list.add(method.getName());

        return list;
    }

    public static ArrayList<String> getIdentifiers(Object object){
        ArrayList<String> list = new ArrayList<>();
        Class cls = object.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields)
            list.add(field.getName());

        return list;
    }

    /****************************************************************************/
    //Parses the Json file and returns the list of files and folders in the project

    public static ArrayList<FileSystem> parseFileStructureJson(String path) {

        ArrayList<FileSystem> fs = new ArrayList<>();
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            JsonObject pkgJO = gson.fromJson(bufferedReader, JsonObject.class);
            JsonArray pkgJA = pkgJO.get("package").getAsJsonArray();

            FileSystem[] fileSystems = gson.fromJson(pkgJA, FileSystem[].class);
            Collections.addAll(fs, fileSystems);

            /*for(FileSystem f:fileSystems)
                System.out.println(f.getName());*/
        }
        catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
        }

        return fs;
    }

    /****************************************************************************/
    //Returns the list of files in the project
    public static void getFilesList(ArrayList<FileSystem> fs, ArrayList<String> classList){
        for(FileSystem f : fs) {
            if(f.getType().equals("file") && f.getName().endsWith(".java")){
                classList.add(f.getPath() + File.separator + f.getName());
            }
            else
                getFilesList(new ArrayList<>(f.getFiles()), classList);
        }
    }


    // returns the list of folders in the project
    public static void getFolderList(ArrayList<FileSystem> fs,ArrayList<String> folderList){

        for(FileSystem f:fs){
            if(f.getType().equals("directory")){
                folderList.add(f.getPath()+File.separator+f.getName());
                getFolderList(new ArrayList<>(f.getFiles()),folderList);
            }
        }
    }


    /****************************************************************************/




    public static String getFileNameFromFilePath(String filePath){
        //  usr/Desktop/file1.java -> file1.java
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        return fileName;
    }


    public static String getClassNameFromFilePath(String filePath){
        //  usr/Desktop/file1.java -> file1
        String fileName = getFileNameFromFilePath(filePath);
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /****************************************************************************/

    public static void loadClassList(){

        System.out.println("I am here");
        ArrayList<String> temp=new ArrayList<>();
        BufferedReader reader;

        try{
            reader = new BufferedReader(new FileReader("src/utils/androidClassList.txt"));
            String line = reader.readLine();

            while (line != null) {
                System.out.println(line);
                // read next line
                line = reader.readLine();
                temp.add(line);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Constants.classList.addAll(temp);

    }


}