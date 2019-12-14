import Model.FileSystem;
import Utils.directoryInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args){
        String projectDirectory,packageName;
        projectDirectory = "/Users/harshavardhanp/Downloads/kumkiTest";
        //projectDirectory = "C:\\Users\\Logesh Dinakaran\\OneDrive\\Desktop\\kumkiTest";

        projectDirectory += "/app/src/main/java/";
        //projectDirectory += "\\app\\src\\main\\java\\";

        packageName = "com/example/dsc_onboarding";
        //packageName = "com\\example\\dsc_onboarding";

        File f=new File(projectDirectory + packageName);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootJO = new JsonObject();
        JsonArray pkgJA = new JsonArray();

        directoryInfo.buildJson(f.getAbsolutePath(), pkgJA,false, null);

        rootJO.add("package", pkgJA);
        System.out.println(gson.toJson(rootJO));
    }
}
