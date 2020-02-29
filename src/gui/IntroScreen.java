package gui;


import utils.Constants;
import utils.Constants.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import static utils.Constants.projectDirectory;

public class IntroScreen extends JFrame {

    private GUIConfiguration guiConfiguration;
    private JPanel panel1;
    private JTextArea welcomeToKumkiObfuscatorTextArea;
    private JButton chooseTheFolderButton;
    private JCheckBox classObfuscationCheckBox;
    private JCheckBox packageObfusactionCheckBox;
    private JCheckBox methodObfuscationCheckBox;
    private JCheckBox variableObfuscationCheckBox;
    private JCheckBox commentObfuscationCheckBox;
    private JButton nextButton;
    final JFileChooser fc;


    public IntroScreen(GUIConfiguration g) throws HeadlessException {
        this.guiConfiguration=g;
        add(panel1);
        fc=new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        setTitle("Configuration Wizard");
        setSize(600,600);

        chooseTheFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(IntroScreen.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    chooseTheFolderButton.setText(file.getAbsolutePath());
                    guiConfiguration.setRootFolder(file.getAbsolutePath());
                    System.out.println("Opening: " + file.getName() + "." );
                } else {
                    System.out.println("Open command cancelled by user." );
                }
            }
        });

        classObfuscationCheckBox.setSelected(true);
        packageObfusactionCheckBox.setSelected(true);
        methodObfuscationCheckBox.setSelected(true);
        variableObfuscationCheckBox.setSelected(true);
        commentObfuscationCheckBox.setSelected(true);

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<Mode> selectedMode=new ArrayList<>();
                if(packageObfusactionCheckBox.isSelected())
                    selectedMode.add(Mode.PACKAGE);
                if(classObfuscationCheckBox.isSelected())
                    selectedMode.add(Mode.CLASS);
                if(methodObfuscationCheckBox.isSelected())
                    selectedMode.add(Mode.METHOD);
                if(variableObfuscationCheckBox.isSelected())
                    selectedMode.add(Mode.VARIABLE);
                if(commentObfuscationCheckBox.isSelected())
                    selectedMode.add(Mode.COMMENT);

                setVisible(false);
                updateDirectoryPath();

                guiConfiguration.setObfuscationMode(selectedMode);

                KeepDetailsScreen keepDetailsScreen=new KeepDetailsScreen(guiConfiguration);
                keepDetailsScreen.setVisible(true);

            }

        });
    }


    void updateDirectoryPath(){
        projectDirectory=guiConfiguration.getRootFolder();
        Constants.manifestPath = projectDirectory + "app" + File.separator + "src" + File.separator + "main" + File.separator + "AndroidManifest.xml";
        Constants.projectRootDirectory = projectDirectory + "app" + File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator;
        Constants.projectResDirectory = projectDirectory + "app" + File.separator + "src" + File.separator + "main" + File.separator + "res" + File.separator + "layout" + File.separator;

    }




}

/*
screen 1:
Hello,
project root directory  -> test the directory exists
Levels of obfuscation:


Screen 2:
keep classes,methods and
encryption key -> validate use only alphabetic and minimum size
finish button

Screen 3:
Progress screen
print the status of the GUI

*/
