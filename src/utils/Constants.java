package utils;

import java.io.File;

public class Constants {
    public static String projectDirectory = "/Users/harshavardhanp/final_year_project/bkp/ssn-app/";
    //public static final String projectDirectory = "/Users/logesh/Documents/SSN/kumkiTest/";
    //public static final String projectDirectory = "/Users/logesh/Documents/SSN/kumkiTestSSN/";

    public static String manifestPath = projectDirectory + "app" + File.separator + "src" + File.separator + "main" + File.separator + "AndroidManifest.xml";
    public static String projectRootDirectory = projectDirectory + "app" + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;
    public static String projectResDirectory = projectDirectory + "app" + File.separator + "src" + File.separator + "main" + File.separator + "res" + File.separator + "layout" + File.separator;

    public static String packageName = "in" + File.separator + "edu" + File.separator + "ssn" + File.separator + "ssnapp" + File.separator;
    //public static String packageName = "com" + File.separator + "example" + File.separator + "dsc_onboarding" + File.separator;

    public static final String fileStructureJsonPath = "originalFileStructure.json";

    /**************************************************************************/
    //Vignere cipher key

    public static String keyValue = "givemearupee";

    public enum Mode {
        CLASS, METHOD,PACKAGE,COMMENT,VARIABLE, STRING_CONSTANT_OBFUSCATION, INTEGER_CONSTANT_OBFUSCATION, DOUBLE_CONSTANT_OBFUSCATION, CHAR_CONSTANT_OBFUSCATION
    }
}
