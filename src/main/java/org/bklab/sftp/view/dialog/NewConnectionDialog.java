package org.bklab.sftp.view.dialog;

import net.miginfocom.swing.MigLayout;
import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.hint.HintPasswordFieldUI;
import org.bklab.sftp.view.hint.HintTextFieldUI;
import org.bklab.sftp.view.widget.ColorChooserButton;
import org.bklab.sftp.view.widget.InputFileChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Broderick
 */
public class NewConnectionDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 2848498437505472174L;
    private static final int JTEXTFIELD_SIZE = 30;
    private JLabel labelName, labelHost, labelSshPort, labelUsername, labelPassword, labelRepeatPassword, labelCommands, labelColor;
    private JTextField textName, textHost, textSshPort, textUsername;
    private JPasswordField textPassword, textRepeatPassword;
    private JButton confirmButton;
    private JPanel panelForm, panelColor, panelCommand;
    private JLabel labelValidation;
    private ColorChooserButton buttonColor;
    private JRadioButton radioColorDefault, radioColorCustom;
    private ButtonGroup groupRadioColor;
    private JFileChooser fileChooser;
    private InputFileChooser inputFileChooser;
    private JCheckBox checkCommand;

    private boolean validated;
    private CleverTreeNode selectedNode;

    public NewConnectionDialog(JFrame parent, CleverTreeNode selectedNode) {
        super(parent);
        this.selectedNode = selectedNode;
        setIconImage(ViewUtils.createImage(DataPath.IMG_16_CONNECTION_NEW));
        setTitle("New connection");
        setModal(true);
        setLayout(new BorderLayout());
        panelForm = new JPanel();
        panelForm.setLayout(new MigLayout("wrap 2"));
        labelName = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_TERMINAL_ON));
        labelHost = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_SERVER));
        labelSshPort = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_PORT_SSH));
        labelUsername = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_USER));
        labelPassword = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_KEY));
        labelRepeatPassword = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_ACCEPT));
        textName = new JTextField(JTEXTFIELD_SIZE);
        textName.setUI(new HintTextFieldUI("Name", false));
        textHost = new JTextField(JTEXTFIELD_SIZE);
        textHost.setUI(new HintTextFieldUI("Host", false));
        textSshPort = new JTextField(JTEXTFIELD_SIZE);
        textSshPort.setUI(new HintTextFieldUI("SSH/SFTP Port", false));
        textSshPort.setText("22");
        textUsername = new JTextField(JTEXTFIELD_SIZE);
        textUsername.setUI(new HintTextFieldUI("Username", false));
        textPassword = new JPasswordField(JTEXTFIELD_SIZE);
        textPassword.setUI(new HintPasswordFieldUI("Password", false));
        textRepeatPassword = new JPasswordField(JTEXTFIELD_SIZE);
        textRepeatPassword.setUI(new HintPasswordFieldUI("Repeat password", false));
        panelForm.add(labelName);
        panelForm.add(textName);
        panelForm.add(labelHost);
        panelForm.add(textHost);
        panelForm.add(labelSshPort);
        panelForm.add(textSshPort);
        panelForm.add(labelUsername);
        panelForm.add(textUsername);
        panelForm.add(labelPassword);
        panelForm.add(textPassword);
        panelForm.add(labelRepeatPassword);
        panelForm.add(textRepeatPassword);


        buttonColor = new ColorChooserButton(Color.WHITE);
        buttonColor.setEnabled(false);
        panelColor = new JPanel();
        panelColor.setBorder(new TitledBorder("Connection color"));
        radioColorDefault = new JRadioButton("Default");
        radioColorDefault.setActionCommand("defaultColor");
        radioColorDefault.addActionListener(this);
        radioColorCustom = new JRadioButton("Custom");
        radioColorCustom.setActionCommand("customColor");
        radioColorCustom.addActionListener(this);
        groupRadioColor = new ButtonGroup();
        groupRadioColor.add(radioColorDefault);
        groupRadioColor.add(radioColorCustom);
        radioColorDefault.setSelected(true);
        panelColor.add(radioColorDefault);
        panelColor.add(radioColorCustom);
        panelColor.add(buttonColor);

        fileChooser = new JFileChooser("commands");
        fileChooser.setDialogTitle("Open text file...");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "Text file (*.txt)";
            }

            @Override
            public boolean accept(File file) {
                boolean toReturn = false;
                if (file.isDirectory() || (file.isFile() && file.getName().endsWith(".txt"))) {
                    toReturn = true;
                }
                return toReturn;
            }
        });

        checkCommand = new JCheckBox("Execute custom commands after login");
        checkCommand.setActionCommand("customCommand");
        checkCommand.addActionListener(this);
        inputFileChooser = new InputFileChooser(fileChooser, ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER));
        inputFileChooser.setEnabled(false);
        labelCommands = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_TERMINAL));

        panelCommand = new JPanel(new MigLayout("wrap 1"));
        panelCommand.setBorder(new TitledBorder("Custom commands"));
        panelCommand.add(checkCommand);
        panelCommand.add(inputFileChooser, "width 200!");

        panelForm.add(labelCommands);
        panelForm.add(panelCommand);

        labelColor = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_COLORS));
        panelForm.add(labelColor);
        panelForm.add(panelColor);

        confirmButton = new JButton("Confirm", ViewUtils.createImageIcon(DataPath.IMG_16_ACCEPT));
        confirmButton.setActionCommand("confirm");
        confirmButton.addActionListener(this);
        panelForm.add(confirmButton, "span 2, align center");
        add(panelForm, BorderLayout.CENTER);
        labelValidation = new JLabel(" ");
        labelValidation.setForeground(Color.RED);
        labelValidation.setBorder(new EmptyBorder(5, 5, 5, 5));
        labelValidation.setFont(labelValidation.getFont().deriveFont(Font.BOLD));
        add(labelValidation, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("confirm")) {
            confirm();
        } else if (e.getActionCommand().equals("defaultColor")) {
            buttonColor.setEnabled(false);
        } else if (e.getActionCommand().equals("customCommand")) {
            inputFileChooser.setEnabled(checkCommand.isSelected());
        } else if (e.getActionCommand().equals("customColor")) {
            buttonColor.setEnabled(true);
        }
    }

    private void confirm() {
        if (textName.getText().trim().equals("")) {
            labelValidation.setText("Field Name is required");
        } else if (textHost.getText().trim().equals("")) {
            labelValidation.setText("Field Host is required");
        } else if (new String(textSshPort.getText()).trim().equals("")) {
            labelValidation.setText("Field SSH/SFTP port is required");
        } else if (textUsername.getText().trim().equals("")) {
            labelValidation.setText("Field Username is required");
        } else if (new String(textPassword.getPassword()).trim().equals("")) {
            labelValidation.setText("Field password is required");
        } else if (new String(textRepeatPassword.getPassword()).trim().equals("")) {
            labelValidation.setText("Field repeat password is required");
        } else if (checkCommand.isSelected() && inputFileChooser.getFile() == null) {
            labelValidation.setText("Select a custom commands file");
        } else {
            validated = true;
            if (!new String(textPassword.getPassword()).equals(new String(textRepeatPassword.getPassword()))) {
                validated = false;
                labelValidation.setText("Passwords doesn't match");
            } else {
                for (int i = 0; i < selectedNode.getChildCount(); i++) {
                    if (textName.getText().equals(((CleverTreeNode) selectedNode.getChildAt(i)).getName())) {
                        validated = false;
                        labelValidation.setText("Name already exists");
                        break;
                    }
                }
            }
            if (!DataUtils.isInteger(textSshPort.getText())) {
                validated = false;
                labelValidation.setText("SSH/SFTP port must be a integer");
            } else {
                int value = Integer.parseInt(textSshPort.getText());
                if (value < 0 || value > 65535) {
                    validated = false;
                    labelValidation.setText("SSH/SFTP port must be a integer between 0 and 65535");
                }
            }
            if (validated) {
                dispose();
            }
        }
    }

    public boolean isValidated() {
        return validated;
    }

    public JTextField getTextName() {
        return textName;
    }

    public JTextField getTextUsername() {
        return textUsername;
    }

    public JPasswordField getTextPassword() {
        return textPassword;
    }

    public JTextField getTextHost() {
        return textHost;
    }

    public String getColor() {
        String toReturn = "default";
        if (radioColorCustom.isSelected()) {
            toReturn = DataUtils.colorToHex(buttonColor.getSelectedColor());
        }
        return toReturn;
    }

    public File getCustomCommandFile() {
        return checkCommand.isSelected() ? inputFileChooser.getFile() : null;
    }

    public JTextField getTextSshPort() {
        return textSshPort;
    }

}
