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

    public void replaceComments(){
        try{
            List<String> fileContent = new ArrayList<>(Files.readAllLines(currentFile.toPath()));
            for(ReplacementDataNode r:arrayList){
                if(r.getEndLineNo()==-1){
                    String temp = fileContent.get(r.getLineNo() - 1);
                    temp = temp.substring(0, r.getStartColNo() - 1);
                    fileContent.set(r.getLineNo() - 1, temp);
                }
                else{
                    String temp = fileContent.get(r.getLineNo() - 1);
                    temp = temp.substring(0,r.getStartColNo()-1);
                    fileContent.set(r.getLineNo() - 1, temp);

                    for(int i=r.getLineNo();i<r.getEndLineNo()-1;i++){
                        fileContent.set(i,"");
                    }

                    temp = fileContent.get(r.getEndLineNo() - 1).substring(r.getEndColNo());
                    fileContent.set(r.getEndLineNo()-1,temp);
                }
            }

            Files.write(currentFile.toPath(), fileContent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
