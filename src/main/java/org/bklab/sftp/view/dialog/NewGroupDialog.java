package org.bklab.sftp.view.dialog;

import net.miginfocom.swing.MigLayout;
import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.hint.HintTextFieldUI;
import org.bklab.sftp.view.widget.ColorChooserButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Broderick
 */
public class NewGroupDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 2848498437505472174L;
    private static final int JTEXTFIELD_SIZE = 30;
    private JLabel labelName;
    private JTextField textName;
    private JButton buttonConfirm;
    private JPanel panelForm, panelColor;
    private JLabel labelValidation;
    private ColorChooserButton buttonColor;
    private JRadioButton radioColorDefault, radioColorCustom;
    private ButtonGroup groupRadioColor;

    private boolean validated;
    private CleverTreeNode selectedNode;

    public NewGroupDialog(JFrame parent, CleverTreeNode selectedNode) {
        super(parent);
        this.selectedNode = selectedNode;
        setIconImage(ViewUtils.createImage(DataPath.IMG_16_FOLDER_NEW));
        setTitle("New group");
        setModal(true);
        setLayout(new BorderLayout());
        panelForm = new JPanel();
        panelForm.setLayout(new MigLayout("wrap 2"));
        labelName = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER));
        textName = new JTextField(JTEXTFIELD_SIZE);
        textName.setUI(new HintTextFieldUI("Name", false));
        panelForm.add(labelName);
        panelForm.add(textName);
        buttonConfirm = new JButton("Confirm", ViewUtils.createImageIcon(DataPath.IMG_16_ACCEPT));
        buttonConfirm.setActionCommand("confirm");
        buttonConfirm.addActionListener(this);


        panelForm.add(new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_COLORS)));
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

        panelForm.add(panelColor);

        panelForm.add(buttonConfirm, "span 2, align center");
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
        } else if (e.getActionCommand().equals("customColor")) {
            buttonColor.setEnabled(true);
        }
    }

    private void confirm() {
        if (textName.getText().trim().equals("")) {
            labelValidation.setText("Field Name is required");
        } else {
            validated = true;
            for (int i = 0; i < selectedNode.getChildCount(); i++) {
                if (textName.getText().equals(((CleverTreeNode) selectedNode.getChildAt(i)).getName())) {
                    validated = false;
                    break;
                }
            }
            if (validated) {
                dispose();
            } else {
                labelValidation.setText("Name already exists");
            }
        }
    }

    public boolean isValidated() {
        return validated;
    }

    public JTextField getNameText() {
        return textName;
    }

    public String getColor() {
        String toReturn = "default";
        if (radioColorCustom.isSelected()) {
            toReturn = DataUtils.colorToHex(buttonColor.getSelectedColor());
        }
        return toReturn;
    }
}
