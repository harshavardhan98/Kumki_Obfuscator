package utils;

import java.io.File;

import static refactor.utils.FileOperation.getClassNameFromFilePath;
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

}
