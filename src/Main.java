import gui.ConfigurationWizard;
import gui.GUIConfiguration;
import gui.IntroScreen;
import obfuscator.*;
import javax.swing.*;
import static utils.Backup.*;

public class Main {

    static GUIConfiguration guiConfiguration;

    public static void main(String[] args) {
        init();
    }

    public static void init() {
        backupProject();
        initUI();
        //initCLIObfuscation();
    }

    public static void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            guiConfiguration=new GUIConfiguration();
            IntroScreen introScreen=new IntroScreen(guiConfiguration);
            introScreen.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initCLIObfuscation(){

        Obfuscator obfuscator;
        obfuscator = new ClassObfuscator();
        obfuscator.init();
        obfuscator.performObfuscation(obfuscator);

        obfuscator = new PackageObfuscator();
        obfuscator.init();
        obfuscator.performObfuscation(obfuscator);

        obfuscator = new MethodObfuscator();
        obfuscator.init();
        obfuscator.performObfuscation(obfuscator);

        obfuscator = new VariableObfuscator();
        obfuscator.init();
        obfuscator.performObfuscation(obfuscator);

        obfuscator = new CommentObfuscator();
        obfuscator.init();
        obfuscator.performObfuscation(obfuscator);
    }
}
