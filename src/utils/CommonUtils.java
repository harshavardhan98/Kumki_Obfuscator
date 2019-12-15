package utils;

import model.FileSystem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.IdentifierDependency;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
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
                            System.out.println(file.getName());
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

    /***************************************************/
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

    /***************************************************/

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

    /***************************************************/

    public static ArrayList<FileSystem> parseFileStructureJson(String path) {

        /*
        *  Parses the Json file and returns the list of java classes in the project
        * */

        ArrayList<FileSystem> fs=new ArrayList<>();

        try{

            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            JsonObject body = gson.fromJson(bufferedReader, JsonObject.class);
            JsonArray results = body.get("package").getAsJsonArray();

            System.out.println("\n\n");
            System.out.println(results);
            System.out.println("\n\n");

            FileSystem[] fileSystems=gson.fromJson(results,FileSystem[].class);

            for(FileSystem f:fileSystems)
                System.out.println(f.getName());

            Collections.addAll(fs,fileSystems);


        }catch (IOException ie){
                System.out.println("Result of the parsing is "+ie.getMessage());
        }

        return fs;
    }


    public static void getClassList(ArrayList<FileSystem> fs,ArrayList<String> classList){
        /*
        *   Returns the list of classes in the Array of filesystem
        * */
        for(FileSystem f:fs){

            if(f.getType().equals("file") && f.getName().endsWith(".java")){
                classList.add(f.getPath()+"/"+f.getName());
            }
            else{
                getClassList(new ArrayList<>(f.getFiles()),classList);
            }
        }
    }


    public static HashMap<String,IdentifierDependency> getDependencyData(String jsonStructurePath){

        /*
        *
        *   Algorithm:
        *   Step 1: Parses the json file and gets the list of java class files
        *   Step 2: Dependency data is stored as HashMap<String,IdentifierDependency>
                    key="Frag1.java"  value="HashMap<String,ArrayList<String>> hm"
                                               |->  key="file name in the project"  value="List of lines where Frag1.java occurs"
        *   Step 3: Call the renameClasses Method to populate the dependency data for each file and renames the classes
        *   Step 4: Use the printDependencyDataOfIdentifier function to print the dependency data of all the identifiers
        *
        * */

        // get the list of class files
        ArrayList<FileSystem> fsTemp=parseFileStructureJson(jsonStructurePath);
        ArrayList<String> classList=new ArrayList<>();
        getClassList(fsTemp,classList);

        // Initialisation of dependency data
        HashMap<String, IdentifierDependency> dependencyData=new HashMap<>();
        for(String i:classList){
            File f=new File(i);
            dependencyData.put(f.getName(),new IdentifierDependency(f.getName()));
        }

        // parsing the project to get the dependency data
        fileOperations.renameClasses(classList,Constants.projectRootDirectory + Constants.packageName,dependencyData);

        // printing the dependency data for each identifier
        for(String i:classList){
            File f=new File(i);
            printDependencyDataOfIdentifier(f.getName(),dependencyData);
        }

        return dependencyData;
    }

    public static void printDependencyDataOfIdentifier(String identifierName, HashMap<String, IdentifierDependency> dependencyData){

        HashMap<String,ArrayList<String>> data=dependencyData.get(identifierName).getEntries();

        System.out.println("Dependencies of "+identifierName);
        for(String i:data.keySet()){
            ArrayList<String> lines=data.get(i);
            for(String j:lines)
                System.out.println("   "+j);
        }
    }
}