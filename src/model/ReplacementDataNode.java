package model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ReplacementDataNode {

    int LineNo;
    int startColNo;
    int endColNo;
    String replacementString;

    public int getLineNo() {
        return LineNo;
    }

    public int getStartColNo() {
        return startColNo;
    }

    public int getEndColNo() {
        return endColNo;
    }

    public String getReplacementString() {
        return replacementString;
    }

    public void setLineNo(int lineNo) {
        LineNo = lineNo;
    }

    public void setStartColNo(int startColNo) {
        this.startColNo = startColNo;
    }

    public void setEndColNo(int endColNo) {
        this.endColNo = endColNo;
    }

    public void setReplacementString(String replacementString) {
        this.replacementString = replacementString;
    }



    public static void replaceInFiles(ArrayList<ReplacementDataNode> replacementData, File file) {

        try {

            List<String> fileContent = new ArrayList<>(Files.readAllLines(file.toPath()));

            for (ReplacementDataNode r : replacementData) {
                String temp = fileContent.get(r.getLineNo()-1);
                temp = temp.substring(0, r.getStartColNo()-1) + r.replacementString + temp.substring(r.getEndColNo());
                fileContent.set(r.getLineNo()-1, temp);
            }

            Files.write(file.toPath(), fileContent);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


    public static void testReplacement(){

        ReplacementDataNode temp=new ReplacementDataNode();
        temp.setLineNo(37);
        temp.setStartColNo(9);
        temp.setEndColNo(23);
        temp.setReplacementString("xxx");

        ArrayList<ReplacementDataNode> ar=new ArrayList<>();
        ar.add(temp);

        File f=new File("/Users/harshavardhanp/final_year_project/fyp/kumkiTest/app/src/main/java/com/example/dsc_onboarding/MainActivity.java");

        ReplacementDataNode.replaceInFiles(ar,f);

    }
};






