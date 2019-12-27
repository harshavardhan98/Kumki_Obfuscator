package dummy;

import model.FileSystem;
import utils.CommonUtils;
import utils.Constants;
import utils.FileOperation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.CommonUtils.getHexValue;
import static utils.CommonUtils.parseFileStructureJson;

public class ThrashCode {

    /****************************************************************************/

    public static void list(String path, int level) {
        File folder = new File(path);
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                for(int i=0;i<level;i++)
                    System.out.print(" ");

                if(file.getName().charAt(0)!='.')
                    System.out.println(file.getName());
            }
            else if (file.isDirectory()) {
                System.out.println(file.getName());
                list(file.getAbsolutePath(),level+2);
            }
        }
    }

    /****************************************************************************/

    public static void collectMethodNameInFiles(String filePath, HashSet<String> methodList) {
        String className = CommonUtils.getClassNameFromFilePath(filePath);
        String parentClass = "";
        boolean canOverrideMethodsBeRenamed = true;
        BufferedReader reader;


        Pattern pattern = Pattern.compile("(public|private|static|protected)? ([A-Za-z0-9<>.]+) ([A-Za-z0-9]+)\\(");
        // regex logic                  ->  Access Modifier                     return type       method Name

        boolean isOverriden = false;


        try {

            reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();

            while (line != null) {

                Matcher m = pattern.matcher(line);

                // check if the method is a constructor

                if (line.trim().equals("@Override")) {
                    isOverriden = true;
                    line = reader.readLine();
                } else if (line.contains("extends")) {
                    String[] splited = line.split("\\s+");

                    int i = 0;
                    for (i = 0; i < splited.length; i++)
                        if (splited[i].equals("extends"))
                            break;
                    parentClass = splited[i + 1];

                    if (Collections.binarySearch(Constants.predefinedClassList, parentClass) >= 0)
                        canOverrideMethodsBeRenamed = false;

                    System.out.println("It extends the class : " + splited[i + 1]);
                } else if (m.find()) {

                    // do not rename the constructor
                    if (m.group(3).equals(className))
                        ;
                        // rename the methods which are override by user defined class
                    else if (isOverriden && canOverrideMethodsBeRenamed) {
                        isOverriden = false;
                        methodList.add(m.group(3));
                    }
                    // do not rename methods which override android innerclass
                    else if (isOverriden)
                        isOverriden = false;
                        // rename for all other cases
                    else {
                        //System.out.println(m.group(3));
                        methodList.add(m.group(3));
                    }

                }

                // read next line
                line = reader.readLine();
            }

            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void collectMethodNames(String projectPath, HashSet<String> methodList) {
        File folder = new File(projectPath);
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    collectMethodNames(file.getAbsolutePath(), methodList);
                } else if (file.isFile()) {
                    collectMethodNameInFiles(file.getAbsolutePath(), methodList);
                }
            }
        }
    }

    public static void renameMethods() {
        /*
         *   O.P: Renames all the method name except overridden methods of inbuilt android classes and class constructors
         *   Algorithm:
         *   Step 1: Parse all the files. When parsing a file store the name of the file and the class it extends
         *   Step 2: If it extends a class which is a part of android SDK do not rename its override methods
         *   Step 3: For each method that matches the regular expression
         *           3.1. Check if it a constructor if so do not rename it
         *           3.2. Check if the method is overridden and the parent class is a user defined class then add the method to set of methods
         *           3.3. If the method is user defined add it to the set of methods
         *   Step 4: Now generate the regular expression matcher and visit all the files and rename the matched strings accordingly
         * */
        ArrayList<FileSystem> fsTemp = parseFileStructureJson(Constants.projectDirectory + Constants.fileStructureJsonPath);
        ArrayList<String> classList = new ArrayList<>();
        //getFilesList(fsTemp, classList);

        HashSet<String> methodList = new HashSet<>();
        collectMethodNames(Constants.projectRootDirectory + Constants.packageName, methodList);

        HashMap<String, String> newNames = new HashMap<>();
        for (String s : methodList)
            newNames.put(s, getHexValue(s));

        String patternString = "(";
        for (String i : newNames.keySet()) {
            patternString += i + "|";
        }

        if (patternString.length() > 1)
            patternString = patternString.substring(0, patternString.length() - 1);
        patternString += ")";

        Pattern pattern = Pattern.compile(patternString);

        //FileOperation.renameMethodNames(Constants.projectRootDirectory + Constants.packageName, newNames, pattern);
    }
}