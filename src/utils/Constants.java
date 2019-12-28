package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Constants {
    //public static String projectDirectory = "/Users/harshavardhanp/final_year_project/fyp/kumkiTest/";
    //public static String projectDirectory ="D:\\Users\\Desktop\\fyp\\kumkiTest\\";
    public static String projectDirectory = "C:\\Users\\Logesh Dinakaran\\OneDrive\\Desktop\\kumkiTest\\";

    public static String backupProjectDirectory = "";
    public static String packageName = "com" + File.separator + "example" + File.separator + "dsc_onboarding" + File.separator;
    public static String projectRootDirectory = projectDirectory + "app" + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;

    public static String fileStructureJsonPath = "originalFileStructure.json";

    /**************************************************************************/
    //Vignere cipher key

    public static String keyValue = "givemearupee";
    public static int isFile = 0;
    public static int isMethod = 1;

    /**************************************************************************/
    //Android predefined classes

    public static ArrayList<String> predefinedClassList = new ArrayList<>();

    /**************************************************************************/
    //Obtained from the projectRootDirectory

    public static ArrayList<String> classList = new ArrayList<>();
    public static ArrayList<String> folderList = new ArrayList<>();
    public static Map<String, ArrayList<String>> methodMap = new HashMap<String, ArrayList<String>>();

    /**************************************************************************/
}