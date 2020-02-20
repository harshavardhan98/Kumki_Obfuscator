package obfuscator;

import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Name;
import model.*;
import utils.CommonUtils;

import static utils.Encryption.*;

public class PackageObfuscator extends Obfuscator implements Obfuscate {

    @Override
    public void obfuscate(CompilationUnit cu) {
        if (cu.getPackageDeclaration().orElse(null) != null)
            handlePackageDeclaration(cu.getPackageDeclaration().orElse(null).getName());
        handleImport(cu);
    }

    @Override
    public void handleClass(ClassOrInterfaceDeclaration clas) {

    }

    private void handleRange(TokenRange tokenRange, String identifier) {
        if (tokenRange != null && folderNameList.contains(identifier)) {
            Range range = tokenRange.getEnd().getRange().orElse(null);
            if (range != null) {
                int start_line_no = range.begin.line;
                int start_col_no = range.begin.column;
                int end_col_no = range.end.column;

                ReplacementDataNode rnode = new ReplacementDataNode();
                rnode.setLineNo(start_line_no);
                rnode.setStartColNo(start_col_no);
                rnode.setEndColNo(end_col_no);
                rnode.setReplacementString(getHexValue(identifier));
                obfuscatorConfig.setArrayList(rnode);
            }
        }
    }

    public void handleImport(CompilationUnit cu) {
        for (int i = 0; i < cu.getImports().size(); i++) {
            Name nm = cu.getImports().get(i).getName().getQualifier().orElse(null);
            boolean isAsterisk = cu.getImports().get(i).isAsterisk();
            String identifier = cu.getImports().get(i).getName().getIdentifier();

            while (nm != null) {
                String temp = nm.asString();

                if (isAsterisk)
                    temp += "." + identifier;

                if((temp + ".").equals(CommonUtils.getBasePackage()))
                    break;

                for (String str : packageNameList) {
                    if (str.trim().compareTo(temp) == 0) {
                        try {
                            if (isAsterisk) {
                                TokenRange tokenRange = cu.getImports().get(i).getName().getTokenRange().orElse(null);
                                handleRange(tokenRange, identifier);
                            } else {
                                TokenRange tokenRange = nm.getTokenRange().orElse(null);
                                handleRange(tokenRange, nm.getIdentifier());
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
                isAsterisk = false;
                nm = nm.getQualifier().orElse(null);
            }
        }
    }

    private void handlePackageDeclaration(Name name) {

        if (name != null) {
            if (name.getIdentifier() != null && folderNameList.contains(name.getIdentifier())) {
                TokenRange tokenRange = name.getTokenRange().orElse(null);
                if (tokenRange != null) {
                    Range range = tokenRange.getEnd().getRange().orElse(null);

                    if (range != null) {
                        ReplacementDataNode r = new ReplacementDataNode();
                        r.setLineNo(range.begin.line);
                        r.setStartColNo(range.begin.column);
                        r.setEndColNo(range.end.column);
                        r.setReplacementString(getHexValue(name.getIdentifier()));
                        obfuscatorConfig.setArrayList(r);
                    }
                }
            }

            handlePackageDeclaration(name.getQualifier().orElse(null));
        }
    }
}
