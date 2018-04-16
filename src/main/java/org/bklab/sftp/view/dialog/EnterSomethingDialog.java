package org.bklab.sftp.view.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author Broderick
 */
public class EnterSomethingDialog extends JDialog {
    private static final long serialVersionUID = 2548971711823349064L;

    private JTextField answerField;
    private JPasswordField passwordField;
    private boolean isPassword;
    private String answer;

    public EnterSomethingDialog(JFrame parent, String title, String content, boolean isPassword) {
        this(parent, title, new String[]{content}, isPassword);
    }

    public EnterSomethingDialog(JFrame parent, String title, String[] content, boolean isPassword) {
        super(parent, title, true);
        this.isPassword = isPassword;
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));

        for (int i = 0; i < content.length; i++) {
            if ((content[i] == null) || (content[i] == "")) {
                continue;
            }
            JLabel contentLabel = new JLabel(content[i]);
            pan.add(contentLabel);
        }

        answerField = new JTextField(20);
        passwordField = new JPasswordField(20);

        if (isPassword) {
            pan.add(passwordField);
        } else {
            pan.add(answerField);
        }

        KeyAdapter kl = new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == '\n') {
                    finish();
                }
            }
        };

        answerField.addKeyListener(kl);
        passwordField.addKeyListener(kl);
        getContentPane().add(BorderLayout.CENTER, pan);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void finish() {
        if (isPassword) {
            answer = new String(passwordField.getPassword());
        } else {
            answer = answerField.getText();
        }
        dispose();
    }

    public String getAnswer() {
        return answer;
    }
}