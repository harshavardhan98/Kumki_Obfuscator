package obfuscator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.*;
import utils.visitor.MethodVisitor;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static utils.CommonUtils.*;
import static utils.Constants.*;
import static utils.Encryption.*;

public class Obfuscator {

    public static ObfuscatorConfig obfuscatorConfig;

    public ArrayList<String> classList;
    public static ArrayList<String> classNameList;
    public ArrayList<String> packageNameList;
    public ArrayList<String> folderList;
    public static HashSet<String> folderNameList;
    public static Map<String, ArrayList<MethodModel>> methodMap;

    // keep data
    public ArrayList<String> keepClass;
    public ArrayList<String> keepMethod;

    private static int jsonFileNameCount = 0;

    public Obfuscator() {
        obfuscatorConfig = new ObfuscatorConfig();
    }

    public void init() {
        initialiseKeepClass();
        initialiseKeepMethod();
        analyseProjectStructure();
        getDependencyData();
    }

    public void performObfuscation(Obfuscator object) {
        for (String s : classList) {
            File file = new File(s);
            String className = getClassNameFromFilePath(file.getName());

            try {
                CompilationUnit cu = JavaParser.parse(file);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(className).orElse(null);
                if (clas == null)
                    clas = cu.getInterfaceByName(className).orElse(null);

                if (object instanceof ClassObfuscator) {
                    ((ClassObfuscator) object).obfuscate(cu);
                    ((ClassObfuscator) object).handleClass(clas);
                } else if (object instanceof PackageObfuscator) {
                    ((PackageObfuscator) object).obfuscate(cu);
                } else if (object instanceof MethodObfuscator) {
                    ((MethodObfuscator) object).obfuscate(cu);
                    ((MethodObfuscator) object).handleClass(clas);
                }
                obfuscatorConfig.replaceInFiles(file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (object instanceof ClassObfuscator)
                renameFile(file.getAbsolutePath(), file.getParent() + File.separator + getHexValue(className) + ".java");
        }

        if (object instanceof PackageObfuscator) {
            for (String s : folderList) {
                File f = new File(s);
                if (!folderNameList.contains(f.getName()))
                    continue;
                String className = getFileNameFromFilePath(f.getAbsolutePath());
                renameFile(f.getAbsolutePath(), f.getParent() + File.separator + getHexValue(className));
            }
        }
    }

    /***********************************************************/

    public void analyseProjectStructure() {
        File f = new File(projectRootDirectory + packageName);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootJO = new JsonObject();
        JsonArray pkgJA = new JsonArray();

        buildJson(f.getAbsolutePath(), pkgJA, false, null);
        rootJO.add("package", pkgJA);
        String fileStructureJS = gson.toJson(rootJO);

        File file = new File(projectDirectory + (jsonFileNameCount++) + fileStructureJsonPath);
        try {
            FileWriter fr = new FileWriter(file);
            fr.write(fileStructureJS);
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getDependencyData() {
        ArrayList<String> predefinedClassList = loadFromFile("androidClassList.txt");
        ArrayList<String> predefinedMethodList = loadFromFile("androidMethodList.txt");

        ArrayList<FileSystem> fsTemp = parseFileStructureJson(projectDirectory + (jsonFileNameCount - 1) + fileStructureJsonPath);
        folderList = new ArrayList<>();
        folderNameList = new HashSet<>();
        classList = new ArrayList<>();
        packageNameList = new ArrayList<>();
        classNameList = new ArrayList<>();
        methodMap = new HashMap<>();
        getFilesList(fsTemp);

        for (int i = 0; i < classNameList.size(); i++) {
            if (predefinedClassList.contains(classNameList.get(i)) || keepClass.contains(classNameList.get(i)))
                classNameList.remove(i--);
        }

        // in.edu.ssn.ssnapp.
        String basePackage = getBasePackage();
        String[] arr = basePackage.substring(0, basePackage.length() - 1).split("[.]");

        for (String s : arr)
            folderNameList.remove(s);

        for (String s : predefinedClassList)
            folderNameList.remove(s);

        for (Map.Entry<String, ArrayList<MethodModel>> entry : methodMap.entrySet()) {
            ArrayList<MethodModel> temp = entry.getValue();
            for (int i = 0; i < temp.size(); i++) {
                MethodModel m = temp.get(i);
                if (predefinedMethodList.contains(m.getName()) || keepMethod.contains(m.getName()))
                    temp.remove(i--);
            }
            methodMap.put(entry.getKey(), temp);
        }
    }

    public void getFilesList(ArrayList<FileSystem> fs) {
        for (FileSystem f : fs) {
            if (f.getType().equals("file") && f.getName().endsWith(".java")) {
                classList.add(f.getPath() + File.separator + f.getName());
                classNameList.add(getClassNameFromFilePath(f.getName()));

                try {
                    File file = new File(f.getPath() + File.separator + f.getName());
                    CompilationUnit cu = JavaParser.parse(file);

                    //Don't visit methods in keep class
                    if (!keepClass.contains(getClassNameFromFilePath(file.getAbsolutePath())))
                        cu.accept(new MethodVisitor(methodMap), file.getAbsolutePath());

                    ClassOrInterfaceDeclaration clas = cu.getClassByName(getClassNameFromFilePath(f.getName())).orElse(null);
                    if (clas == null)
                        clas = cu.getInterfaceByName(getClassNameFromFilePath(f.getName())).orElse(null);

                    List<BodyDeclaration<?>> members = clas.getMembers();
                    if (!members.isEmpty()) {
                        for (BodyDeclaration<?> bd : members) {
                            if (bd.isClassOrInterfaceDeclaration()) {
                                getClassNameFromFilePath(file.getAbsolutePath());
                                classNameList.add(bd.asClassOrInterfaceDeclaration().getName().asString());
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (f.getType().equals("directory")) {
                String path = f.getPath() + File.separator + f.getName();
                folderList.add(path);
                packageNameList.add(getPackageNameFromPath(path));
                String[] arr = (getPackageNameFromPath(path)).split("[.]");
                folderNameList.add(arr[arr.length - 1]);
            }

            if (f.getFiles() != null)
                getFilesList(f.getFiles());
        }
    }

    public static void updateObfuscatorConfig(ReplacementDataNode r) {
        obfuscatorConfig.setArrayList(r);
    }

    public static Boolean verifyUserDefinedClass(String ObjType) {
        // todo write as lambda
        for (int x = 0; x < classNameList.size(); x++) {
            if (classNameList.get(x).equals(ObjType))
                return true;
        }
        return false;
    }

    public static Boolean verifyUserDefinedMethod(MethodModel input) {

        // TODO write a comparator
        for (Map.Entry<String, ArrayList<MethodModel>> entry : methodMap.entrySet()) {
            ArrayList<MethodModel> temp = entry.getValue();
            for (int i = 0; i < temp.size(); i++) {
                MethodModel m = temp.get(i);
                if (m.getName().equals(input.getName()) && m.getNoOfParameters() == input.getNoOfParameters())
                    return true;
            }
        }
        return false;
    }

    public static Boolean verifyUserDefinedMethodbyName(String input) {

        // TODO write a comparator
        for (Map.Entry<String, ArrayList<MethodModel>> entry : methodMap.entrySet()) {
            ArrayList<MethodModel> temp = entry.getValue();
            for (int i = 0; i < temp.size(); i++) {
                MethodModel m = temp.get(i);
                if (m.getName().equals(input))
                    return true;
            }
        }
        return false;
    }

    public void initialiseKeepClass() {
        keepClass = new ArrayList<>();
    }

    public void initialiseKeepMethod() {
        keepMethod = new ArrayList<>();
    }
}
