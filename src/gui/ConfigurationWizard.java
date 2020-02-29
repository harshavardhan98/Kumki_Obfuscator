package gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ConfigurationWizard extends JFrame {
    private JPanel panel1;
    private JTextField textField1;
    private JTextField textField2;
    private JTextArea textArea1;
    private JButton obfuscateButton;
    private JButton chooseFileButton;
    final JFileChooser fc;

    public ConfigurationWizard() {

        add(panel1);
        fc=new JFileChooser();


        setTitle("Configuration Wizard");
        setSize(400,500);

        obfuscateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("textField1 "+textField1.getText());
                System.out.println("textField2 "+textField2.getText());
                System.out.println("textArea1 "+textArea1.getText());
            }
        });

        chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(ConfigurationWizard.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    System.out.println("Opening: " + file.getName() + "." );
                } else {
                    System.out.println("Open command cancelled by user." );
                }

            }
        });

    }
}
