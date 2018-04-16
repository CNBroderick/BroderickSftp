package org.bklab.sftp.view.dialog;

import net.miginfocom.swing.MigLayout;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.ViewUtils;

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
public class DeleteDialog extends JDialog implements ActionListener, KeyListener {
    public static final String ANSWER_YES = "yes";
    public static final String ANSWER_NO = "no";

    private static final long serialVersionUID = 2307823245040721040L;
    private static final int BUTTON_WIDTH = 75;
    private JLabel labelIcon, labelMessage;
    private JButton buttonYes, buttonNo;
    private JPanel panelMain, panelButtons;

    private String answer;


    public DeleteDialog() {
        super();
        answer = ANSWER_NO;
        panelMain = new JPanel(new BorderLayout());
        panelMain.setBorder(new EmptyBorder(5, 5, 0, 5));
        setIconImage(ViewUtils.createImage(DataPath.IMG_16_DELETE));
        setTitle("Delete");
        setModal(true);
        setLayout(new BorderLayout());
        labelIcon = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_32_QUESTION));
        labelMessage = new JLabel("Are you sure to want to delete the selected item(s)?");
        labelMessage.setBorder(new EmptyBorder(0, 5, 0, 0));
        buttonYes = new JButton("Yes");
        buttonYes.setActionCommand("yes");
        buttonYes.addActionListener(this);
        buttonYes.setPreferredSize(new Dimension(BUTTON_WIDTH, 10));
        buttonYes.addKeyListener(this);

        buttonNo = new JButton("No");
        buttonNo.setActionCommand("no");
        buttonNo.addActionListener(this);
        buttonNo.setPreferredSize(new Dimension(BUTTON_WIDTH, 10));
        buttonNo.addKeyListener(this);

        panelButtons = new JPanel();
        panelButtons.setLayout(new MigLayout("wrap 2,align center"));
        panelButtons.add(buttonYes);
        panelButtons.add(buttonNo);

        panelMain.add(labelIcon, BorderLayout.WEST);
        panelMain.add(labelMessage, BorderLayout.CENTER);
        panelMain.add(panelButtons, BorderLayout.SOUTH);
        add(panelMain, BorderLayout.CENTER);
        addKeyListener(this);
        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getLastComponent(Container aContainer) {
                return buttonYes;
            }

            @Override
            public Component getFirstComponent(Container aContainer) {
                return buttonNo;
            }

            @Override
            public Component getDefaultComponent(Container aContainer) {
                return buttonNo;
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                if (aComponent == buttonYes) {
                    return buttonNo;
                } else {
                    return buttonYes;
                }
            }

            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                if (aComponent == buttonYes) {
                    return buttonNo;
                } else {
                    return buttonYes;
                }
            }
        });
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == buttonYes) {
            answer = ANSWER_YES;
            dispose();
        } else if (e.getSource() == buttonNo) {
            answer = ANSWER_NO;
            dispose();
        }
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (buttonYes.hasFocus()) {
                buttonNo.requestFocus();
            } else {
                buttonYes.requestFocus();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
