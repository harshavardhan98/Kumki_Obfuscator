import obfuscator.ClassObfuscator;
import obfuscator.Obfuscator;
import static utils.Backup.*;

public class Main {
    public static void main(String[] args) {

        init();

        Obfuscator obfuscator = new ClassObfuscator();
        obfuscator.init();
        obfuscator.performObfuscation(obfuscator);
    }

    public static void init(){
        backupProject();
    }
}
