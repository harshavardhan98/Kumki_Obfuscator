package utils;

import model.FileSystem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

                            JsonObject fsJO = gson.fromJson(gson.toJson(fs), JsonObject.class);
                            pkgJA.add(fsJO);
                        }
                        else{
                            fs.setName(file.getName());
                            fs.setType("file");
                            filesList.add(fs);
                        }
                    }
                    else if (file.isDirectory()) {
                        fs.setName(file.getName());
                        fs.setType("directory");

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
}
