package obfuscator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import handler.StatementHandler;
import model.FileSystem;
import refactor.utils.CommonUtils;
import refactor.utils.FileOperation;
import utils.visitor.MethodVisitor;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import static refactor.utils.CommonUtils.*;
import static utils.CommonUtils.getClassNameFromFilePath;
import static utils.Constants.*;
import static refactor.utils.FileOperation.*;

public class Obfuscator {

    ObfuscatorConfig obfuscatorConfig;
    StatementHandler statementHandler;

    public ArrayList<String> classList;
    public ArrayList<String> classNameList;
    public ArrayList<String> folderList;
    public Map<String, ArrayList<String>> methodMap;
    public ArrayList<String> keepClass;

    private static int jsonFileNameCount = 0;

    public Obfuscator() {
        obfuscatorConfig = new ObfuscatorConfig();
    }

    public void init() {
        analyseProjectStructure();
        getDependencyData();
    }

    public void performObfuscation(Object p) {

        for (String s : classList) {
            File file = new File(s);
            String className = getClassNameFromFilePath(file.getName());

            try {
                CompilationUnit cu = JavaParser.parse(file);
                ClassOrInterfaceDeclaration clas = cu.getClassByName(className).orElse(null);
                if (clas == null)
                    clas = cu.getInterfaceByName(className).orElse(null);

                if (p instanceof ClassObfuscator) {
                    ((ClassObfuscator) p).Obfuscate(cu);
                    ((ClassObfuscator) p).handleClass(clas);
                }

                obfuscatorConfig.replaceInFiles(file);
            } catch (Exception e) {
                e.printStackTrace();
            }

            renameFile(file.getAbsolutePath(), file.getParent() + File.separator + CommonUtils.getHexValue(className) + ".java");
        }
    }

    /***********************************************************/

    public void analyseProjectStructure() {
        keepClass.add("NewMessageEvent");
        keepClass.add("Comments");
        keepClass.add("ClubPost");

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
        ArrayList<String> predefinedClassList = loadPredefinedClassList();

        ArrayList<FileSystem> fsTemp = parseFileStructureJson(projectDirectory + (jsonFileNameCount - 1) + fileStructureJsonPath);
        folderList = new ArrayList<>();
        classList = new ArrayList<>();
        classNameList = new ArrayList<>();
        methodMap = new HashMap<>();
        keepClass = new ArrayList<>();
        getFilesList(fsTemp);

        for (int i = 0; i < classList.size(); i++) {
            if (Collections.binarySearch(predefinedClassList, classList.get(i)) >= 0)
                classList.remove(i--);
        }

        for (int i = 0; i < folderList.size(); i++) {
            if (Collections.binarySearch(predefinedClassList, folderList.get(i)) >= 0)
                folderList.remove(i--);
        }
    }

    public void getFilesList(ArrayList<FileSystem> fs) {
        for (FileSystem f : fs) {
            if (f.getType().equals("file") && f.getName().endsWith(".java")) {
                classList.add(f.getPath() + File.separator + f.getName());
                //TODO: CHECK (2)
                classNameList.add(getClassNameFromFilePath(f.getName()));

                try {
                    File file = new File(f.getPath() + File.separator + f.getName());
                    CompilationUnit cu = JavaParser.parse(file);

                    if (!keepClass.contains(getClassNameFromFilePath(file.getAbsolutePath()))) {
                        cu.accept(new MethodVisitor(methodMap), file.getAbsolutePath());
                        //TODO: CHECK (1)
                        System.out.println(methodMap);
                    }

                    ClassOrInterfaceDeclaration clas = cu.getClassByName(FileOperation.getClassNameFromFilePath(f.getName())).orElse(null);
                    if (clas == null)
                        clas = cu.getInterfaceByName(FileOperation.getClassNameFromFilePath(f.getName())).orElse(null);

                    List<BodyDeclaration<?>> members = clas.getMembers();
                    if (!members.isEmpty()) {
                        for (BodyDeclaration<?> bd : members) {
                            if (bd.isClassOrInterfaceDeclaration()) {
                                FileOperation.getClassNameFromFilePath(file.getAbsolutePath());
                                classNameList.add(bd.asClassOrInterfaceDeclaration().getName().asString());
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (f.getType().equals("directory"))
                folderList.add(f.getPath() + File.separator + f.getName());

            if (f.getFiles() != null)
                getFilesList(f.getFiles());
        }
    }
}