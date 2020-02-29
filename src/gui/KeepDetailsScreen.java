package gui;

import utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

public class KeepDetailsScreen extends JFrame {
    private GUIConfiguration guiConfiguration;
    private JPanel panel1;
    private JTextArea keepClasses;
    private JTextArea keepMethods;
    private JTextArea keepFields;
    private JButton nextButton;
    private JButton backButton;


    public KeepDetailsScreen(GUIConfiguration gui) throws HeadlessException {
        guiConfiguration=gui;
        add(panel1);
        setTitle("Configuration Wizard");
        setSize(600,600);

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guiConfiguration.setKeepClasses(extractClassName(keepClasses.getText()));
                guiConfiguration.setKeepMethods(extractMethodName(keepMethods.getText()));
                guiConfiguration.setKeepFields(extractFieldName(keepFields.getText()));
                System.out.println("testing the code");
                ObfusactionScreen obfusactionScreen=new ObfusactionScreen(guiConfiguration);
                obfusactionScreen.setVisible(true);
                setVisible(false);
            }
        });
    }

    ArrayList<String> extractClassName(String data){

        ArrayList<String> keepClasses=new ArrayList<>();

        if(data!=null && data.length()<=0)
            return keepClasses;

        String[] classes=data.split(",");
        Collections.addAll(keepClasses,classes);
        return keepClasses;
    }


    ArrayList<String> extractMethodName(String data){
        ArrayList<String> keepMethods=new ArrayList<>();

        if(data!=null && data.length()<=0)
            return keepMethods;

        String[] classes=data.split(",");
        Collections.addAll(keepMethods,classes);
        return keepMethods;
    }


    ArrayList<String> extractFieldName(String data){

        ArrayList<String> keepFields=new ArrayList<>();

        if(data!=null && data.length()<=0)
            return keepFields;

        String[] classes=data.split(",");
        Collections.addAll(keepFields,classes);
        return keepFields;

    }

}
