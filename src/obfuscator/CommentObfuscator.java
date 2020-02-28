package obfuscator;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.comments.Comment;
import model.ReplacementDataNode;
import model.Scope;

import java.util.List;

public class CommentObfuscator extends Obfuscator implements Obfuscate {
    public CommentObfuscator() {
        super();
    }

    @Override
    public void obfuscate(CompilationUnit cu) {
        List<Comment> l = cu.getComments();

        for (Comment comment : l) {
            handleLineComment(comment);
            handleBlockComment(comment);
        }
    }

    @Override
    public void handleClass(ClassOrInterfaceDeclaration clas, Scope scope) {
    }

    public void handleLineComment(Comment comment) {
        if (comment == null || !comment.isLineComment())
            return;

        ReplacementDataNode rnode = new ReplacementDataNode();
        Range range = comment.getRange().orElse(null);
        if (range != null) {
            rnode.setLineNo(range.begin.line);
            rnode.setStartColNo(range.begin.column);
            rnode.setEndColNo(range.end.column);
            rnode.setReplacementString("");
            obfuscatorConfig.setArrayList(rnode);
        }
    }

    public void handleBlockComment(Comment comment) {
        if (comment == null || !comment.isBlockComment())
            return;

        ReplacementDataNode rnode = new ReplacementDataNode();
        Range range = comment.getRange().orElse(null);
        if (range != null) {
            rnode.setLineNo(range.begin.line);
            rnode.setStartColNo(range.begin.column);
            rnode.setEndLineNo(range.end.line);
            rnode.setEndColNo(range.end.column);
            rnode.setReplacementString("");
            obfuscatorConfig.setArrayList(rnode);
        }
    }
}
