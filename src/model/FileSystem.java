package model;

import java.util.List;

public class FileSystem {
    private String name;
    private String type;
    private String path;
    private List<FileSystem> files;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FileSystem> getFiles() {
        return files;
    }

    public void setFiles(List<FileSystem> files) {
        this.files = files;
    }
}
