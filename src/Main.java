import obfuscator.*;

import static utils.Backup.*;

public class Main {
    public static void main(String[] args) {

        init();

        Obfuscator obfuscator;

//        obfuscator = new ClassObfuscator();
//        obfuscator.init();
//        obfuscator.performObfuscation(obfuscator);
//
//        obfuscator = new PackageObfuscator();
//        obfuscator.init();
//        obfuscator.performObfuscation(obfuscator);
//
//        obfuscator = new MethodObfuscator();
//        obfuscator.init();
//        obfuscator.performObfuscation(obfuscator);

        obfuscator = new VariableObfuscator();
        obfuscator.init();
        obfuscator.performObfuscation(obfuscator);

    }

    public static void init() {
        backupProject();
    }
}
