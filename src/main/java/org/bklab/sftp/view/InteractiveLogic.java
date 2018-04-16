package org.bklab.sftp.view;

import org.bklab.sftp.view.dialog.EnterSomethingDialog;
import org.bklab.ssh2.InteractiveCallback;

import javax.swing.*;
import java.io.IOException;

/**
 * The logic that one has to implement if "keyboard-interactive" authentication shall be supported
 *
 * @author Broderick
 */
public class InteractiveLogic implements InteractiveCallback {
    private int promptCount = 0;
    private String lastError;
    private JFrame loginFrame;

    public InteractiveLogic(String lastError, JFrame loginFrame) {
        this.lastError = lastError;
        this.loginFrame = loginFrame;
    }

    /**
     *  the callback may be invoked several times, depending on how many questions-sets the server sends
     */
    public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt, boolean[] echo) throws IOException {
        String[] result = new String[numPrompts];
        for (int i = 0; i < numPrompts; i++) {
            /* Often, servers just send empty strings for "name" and "instruction" */
            String[] content = new String[]{lastError, name, instruction, prompt[i]};

            if (lastError != null) {
                /* show lastError only once */
                lastError = null;
            }

            EnterSomethingDialog esd = new EnterSomethingDialog(loginFrame, "Keyboard Interactive Authentication", content, !echo[i]);
            esd.setVisible(true);
            if (esd.getAnswer() == null) {
                throw new IOException("Login aborted by user");
            }
            result[i] = esd.getAnswer();
            promptCount++;
        }
        return result;
    }

    /**
     * We maintain a prompt counter - this enables the detection of situations where the ssh
     * server is signaling "authentication failed" even though it did not send a single prompt.
     */
    public int getPromptCount() {
        return promptCount;
    }
}
