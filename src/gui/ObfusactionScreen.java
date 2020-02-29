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
    private JLabel textField1;
    private JButton obfuscateButton;


    public ObfusactionScreen(GUIConfiguration gui) throws HeadlessException {

        add(panel1);
        guiConfiguration=gui;


        obfuscateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setSize(600,600);

                Obfuscator obfuscator=new Obfuscator();
                obfuscator.setKeepClass(guiConfiguration.getKeepClasses());
                obfuscator.setKeepField(guiConfiguration.getKeepFields());
                obfuscator.setKeepMethod(guiConfiguration.getKeepMethods());


                if(guiConfiguration.getObfuscationMode().contains(Mode.CLASS)){
                    obfuscator = new ClassObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.CLASS);
                }

                if(guiConfiguration.getObfuscationMode().contains(Mode.PACKAGE)){
                    obfuscator = new PackageObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.PACKAGE);
                }

                if(guiConfiguration.getObfuscationMode().contains(Mode.METHOD)){
                    obfuscator = new MethodObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.METHOD);
                }

                if(guiConfiguration.getObfuscationMode().contains(Mode.VARIABLE)){
                    obfuscator = new VariableObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.VARIABLE);
                }

                if(guiConfiguration.getObfuscationMode().contains(Mode.COMMENT)){
                    obfuscator = new CommentObfuscator();
                    postObfuscationRoutine(obfuscator,Mode.COMMENT);
                }


                setVisible(false);
            }
        });

    }

    void postObfuscationRoutine(Obfuscator obfuscator, Mode m){
        obfuscator.init();
        obfuscator.performObfuscation(obfuscator);

        if(m.equals(Mode.PACKAGE))
            textField1.setText("Package Obfuscation Completed");
        else if(m.equals(Mode.CLASS))
            textField1.setText("Class Obfuscation Completed");
        else if(m.equals(Mode.METHOD))
            textField1.setText("Method Obfuscation Completed");
        else if(m.equals(Mode.VARIABLE))
            textField1.setText("Variable Obfuscation Completed");
        else if(m.equals(Mode.COMMENT))
            textField1.setText("Comment Obfuscation Completed");

    }



}
