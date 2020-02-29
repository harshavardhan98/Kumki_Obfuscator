package gui;

import obfuscator.*;
import utils.Constants.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ObfusactionScreen extends JFrame{

    GUIConfiguration guiConfiguration;
    private JPanel panel1;
    //private JLabel textField1;
    private JButton obfuscateButton;
    private JTextArea textField1;


    public ObfusactionScreen(GUIConfiguration gui) throws HeadlessException {

        add(panel1);
        guiConfiguration=gui;
        setSize(600,600);

        obfuscateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Obfuscator obfuscator;

                if(guiConfiguration.getObfuscationMode().contains(Mode.COMMENT)){
                    obfuscator = new CommentObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.COMMENT);
                }


                if(guiConfiguration.getObfuscationMode().contains(Mode.PACKAGE)){
                    obfuscator = new PackageObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.PACKAGE);
                }

                if(guiConfiguration.getObfuscationMode().contains(Mode.CLASS)){
                    obfuscator = new ClassObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.CLASS);
                }


                if(guiConfiguration.getObfuscationMode().contains(Mode.METHOD)){
                    obfuscator = new MethodObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.METHOD);
                }

                if(guiConfiguration.getObfuscationMode().contains(Mode.VARIABLE)){
                    obfuscator = new VariableObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.VARIABLE);
                }

                textField1.setText(textField1.getText()+"\n"+"Completed the process");
            }
        });

    }

    void postObfuscationRoutine(Obfuscator obfuscator, Mode m){

        obfuscator.setKeepClass(guiConfiguration.getKeepClasses());
        obfuscator.setKeepField(guiConfiguration.getKeepFields());
        obfuscator.setKeepMethod(guiConfiguration.getKeepMethods());

        obfuscator.init();
        obfuscator.performObfuscation(obfuscator);

        if(m.equals(Mode.PACKAGE))
            textField1.setText(textField1.getText()+"\n"+"Package Obfuscation Completed\n");
        else if(m.equals(Mode.CLASS))
            textField1.setText(textField1.getText()+"\n"+"Class Obfuscation Completed\n");
        else if(m.equals(Mode.METHOD))
            textField1.setText(textField1.getText()+"\n"+"Method Obfuscation Completed\n");
        else if(m.equals(Mode.VARIABLE))
            textField1.setText(textField1.getText()+"\n"+"Variable Obfuscation Completed\n");
        else if(m.equals(Mode.COMMENT))
            textField1.setText(textField1.getText()+"\n"+"Comment Obfuscation Completed\n");

    }



}
