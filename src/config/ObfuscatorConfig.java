package config;

import model.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class ObfuscatorConfig {

    private ArrayList<ReplacementDataNode> arrayList;

    public ObfuscatorConfig() {
        arrayList = new ArrayList<>();
    }

    public void setArrayList(ReplacementDataNode rnode) {
        this.arrayList.add(rnode);
    }

    public void replaceInFiles(File file) {

        String currentLine;
        ReplacementDataNode debugReplacementDataNode = new ReplacementDataNode();

        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(file.toPath()));

            Collections.sort(arrayList, (lhs, rhs) -> {
                if (lhs.getLineNo() != rhs.getLineNo())
                    return (lhs.getLineNo() - rhs.getLineNo());
                else
                    return rhs.getStartColNo() - lhs.getStartColNo();
            });

            for (ReplacementDataNode r : arrayList) {
                debugReplacementDataNode = r;
                currentLine = fileContent.get(r.getLineNo() - 1);
                currentLine = currentLine.substring(0, r.getStartColNo() - 1) + r.getReplacementString() + currentLine.substring(r.getEndColNo());
                fileContent.set(r.getLineNo() - 1, currentLine);
            }
            Files.write(file.toPath(), fileContent);
        } catch (Exception e) {
            System.out.print(debugReplacementDataNode.getLineNo());
            System.out.println(e.getMessage());
        }

        arrayList.clear();
    }
}
