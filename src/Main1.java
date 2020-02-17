import obfuscator.ClassObfuscator;
import obfuscator.Obfuscator;
import static utils.Backup.*;

public class Main1 {
    public static void main(String[] args) {

        init();

        Obfuscator obfuscator = new ClassObfuscator();
        obfuscator.init();
        obfuscator.performObfuscation(ClassObfuscator.class);
    }

    public static void init(){
        backupProject();
    }
}
