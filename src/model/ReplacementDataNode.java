package model;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ReplacementDataNode {

    private int LineNo;
    private int startColNo;
    private int endColNo;
    private String replacementString;

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
};






