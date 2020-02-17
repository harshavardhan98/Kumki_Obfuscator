package refactor;

import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import model.FileSystem;
import obfuscator.Obfuscator;
import model.ReplacementDataNode;
import refactor.utils.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static refactor.utils.CommonUtils.*;
import static refactor.utils.Constants.*;
import static refactor.utils.FileOperation.copyFolder;

public class Main {

    public static void main(String[] args) {
        obfuscator.Obfuscator Obfuscator = new obfuscator.ClassObfuscator();
        Obfuscator.performObfuscation(obfuscator.ClassObfuscator.class);

        //backupProject();
        //analyseProjectStructure();
        //getDependencyData();

        //VariableObfuscation();
        //MethodObfuscation();
        //PackageObfuscation();
        //ClassObfuscation();
        //CommentObfuscation();
    }

    /***********************************************************/
    //Obfuscations
    public static void ClassObfuscation() {
        ClassObfuscator co = new ClassObfuscator();
        co.obfuscate();
        analyseProjectStructure();
        getDependencyData();
    }

    public static void PackageObfuscation() {
        PackageObfuscator po = new PackageObfuscator();
        po.obfuscate();
        analyseProjectStructure();
        getDependencyData();
    }

    public static void MethodObfuscation() {
        MethodObfuscator mo = new MethodObfuscator();
        mo.obfuscate();
    }

    public static void CommentObfuscation() {
        for (String s : classList) {
            try {
                File f = new File(s);
                Obfuscator obfuscator = new Obfuscator();
                obfuscator.setCurrentFile(f);

                CompilationUnit cu = JavaParser.parse(f);
                List<Comment> l = cu.getComments();

                for (Comment comment : l) {
                    if (comment.isLineComment()) {
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        Range range = comment.getRange().orElse(null);
                        if (range != null) {
                            rnode.setLineNo(range.begin.line);
                            rnode.setStartColNo(range.begin.column);
                            rnode.setEndColNo(range.end.column);
                            rnode.setReplacementString("");
                            obfuscator.setArrayList(rnode);
                        }
                    } else if (comment.isBlockComment()) {
                        ReplacementDataNode rnode = new ReplacementDataNode();
                        Range range = comment.getRange().orElse(null);
                        if (range != null) {
                            rnode.setLineNo(range.begin.line);
                            rnode.setStartColNo(range.begin.column);
                            rnode.setEndLineNo(range.end.line);
                            rnode.setEndColNo(range.end.column);
                            rnode.setReplacementString("");
                            obfuscator.setArrayList(rnode);
                        }
                    }
                }
                obfuscator.replaceComments();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void VariableObfuscation() {
        VariableObfuscation vo = new VariableObfuscation();
        vo.obfuscate();
    }

    private static void backupProject() {
        try {
            File sourceFolder = new File(projectDirectory);
            File destinationFolder = new File(sourceFolder.getAbsolutePath() + "1");

            backupProjectDirectory = destinationFolder.getAbsolutePath() + File.separator;
            copyFolder(sourceFolder, destinationFolder);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /***********************************************************/
}