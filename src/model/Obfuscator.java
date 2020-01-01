package model;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Obfuscator {
    private File currentFile;
    private ArrayList<ReplacementDataNode> arrayList;

    public Obfuscator(){
        arrayList = new ArrayList<ReplacementDataNode>();
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    public ArrayList<ReplacementDataNode> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<ReplacementDataNode> arrayList) {
        this.arrayList = arrayList;
    }

    public void setArrayList(ReplacementDataNode rnode) {
        this.arrayList.add(rnode);
    }

    /*****************************************************************/

    public void replaceInFiles() {
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(currentFile.toPath()));
            for (ReplacementDataNode r : arrayList) {
                String temp = fileContent.get(r.getLineNo() - 1);
                temp = temp.substring(0, r.getStartColNo() - 1) + r.getReplacementString() + temp.substring(r.getEndColNo());
                fileContent.set(r.getLineNo() - 1, temp);
            }
            Files.write(currentFile.toPath(), fileContent);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
