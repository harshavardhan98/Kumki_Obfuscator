package model;

public class ReplacementDataNode {

    private int LineNo;
    private int startColNo;
    private int endLineNo;
    private int endColNo;
    private String replacementString;

    public ReplacementDataNode() {
        endLineNo=-1;
    }

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

    public void setEndLineNo(int endLineNo){
        this.endLineNo=endLineNo;
    }
    public int getEndLineNo(){
        return this.endLineNo;
    }
};






