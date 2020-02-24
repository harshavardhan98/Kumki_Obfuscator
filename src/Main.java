import gui.ConfigurationWizard;
import obfuscator.*;
import javax.swing.*;

import static utils.Backup.*;

public class Main {
    public static void main(String[] args) {

        init();
        initUI();

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
//
//        obfuscator = new VariableObfuscator();
//        obfuscator.init();
//        obfuscator.performObfuscation(obfuscator);

    }

    public static void init() {
        backupProject();
    }

    public static void initUI(){
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            ConfigurationWizard configurationWizard=new ConfigurationWizard();
            configurationWizard.setVisible(true);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
