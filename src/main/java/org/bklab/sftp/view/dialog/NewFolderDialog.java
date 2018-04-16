package org.bklab.sftp.view.dialog;

import net.miginfocom.swing.MigLayout;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.hint.HintTextFieldUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author Broderick
 */
public class NewFolderDialog extends JDialog implements ActionListener, KeyListener {

    private static final long serialVersionUID = 2514495073590806949L;
    private static final int JTEXTFIELD_SIZE = 30;
    private JLabel labelName;
    private JTextField textName;
    private JButton buttonConfirm;
    private JPanel panelForm;
    private JLabel labelValidation;

    private boolean validated;

    public NewFolderDialog() {
        super();
        setIconImage(ViewUtils.createImage(DataPath.IMG_16_FOLDER_NEW));
        setTitle("New folder");
        setModal(true);
        setLayout(new BorderLayout());
        panelForm = new JPanel();
        panelForm.setLayout(new MigLayout("wrap 2"));
        labelName = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER));
        textName = new JTextField(JTEXTFIELD_SIZE);
        textName.setUI(new HintTextFieldUI("Name", false));
        textName.addKeyListener(this);
        panelForm.add(labelName);
        panelForm.add(textName);
        buttonConfirm = new JButton("Confirm", ViewUtils.createImageIcon(DataPath.IMG_16_ACCEPT));
        buttonConfirm.setActionCommand("confirm");
        buttonConfirm.addActionListener(this);

        panelForm.add(buttonConfirm, "span 2, align center");
        add(panelForm, BorderLayout.CENTER);
        labelValidation = new JLabel(" ");
        labelValidation.setForeground(Color.RED);
        labelValidation.setBorder(new EmptyBorder(5, 5, 5, 5));
        labelValidation.setFont(labelValidation.getFont().deriveFont(Font.BOLD));
        add(labelValidation, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("confirm")) {
            confirm();
        }
    }

    private void confirm() {
        if (textName.getText().trim().equals("")) {
            labelValidation.setText("Field Name is required");
        } else {
            validated = true;
            dispose();
        }
    }

    public boolean isValidated() {
        return validated;
    }

    public JTextField getTextName() {
        return textName;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == '\n') {
            confirm();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
