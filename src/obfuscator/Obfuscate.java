package obfuscator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public interface Obfuscate {
    void obfuscate(CompilationUnit cu);

    void handleClass(ClassOrInterfaceDeclaration clas);
}
