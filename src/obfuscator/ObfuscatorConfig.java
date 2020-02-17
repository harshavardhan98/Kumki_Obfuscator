package obfuscator;

import model.ReplacementDataNode;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ObfuscatorConfig {

    private ArrayList<ReplacementDataNode> arrayList;

    public ObfuscatorConfig() {
        arrayList = new ArrayList<ReplacementDataNode>();
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

    public void replaceInFiles(File file) {

        String temp;
        ReplacementDataNode r1 = new ReplacementDataNode();

        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(file.toPath()));

            Collections.sort(arrayList, new Comparator<ReplacementDataNode>() {
                @Override
                public int compare(ReplacementDataNode lhs, ReplacementDataNode rhs) {
                    if (lhs.getLineNo() != rhs.getLineNo())
                        return (lhs.getLineNo() - rhs.getLineNo());
                    else
                        return rhs.getStartColNo() - lhs.getStartColNo();
                }
            });

            for (ReplacementDataNode r : arrayList) {
                r1 = r;
                temp = fileContent.get(r.getLineNo() - 1);
                temp = temp.substring(0, r.getStartColNo() - 1) + r.getReplacementString() + temp.substring(r.getEndColNo());
                fileContent.set(r.getLineNo() - 1, temp);
            }
            Files.write(file.toPath(), fileContent);
        } catch (Exception e) {
            System.out.print(r1.getLineNo());
            System.out.println(e.getMessage());
        }
    }
}
