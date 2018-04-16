package org.bklab.sftp.core.connection;

import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.sftp.core.Broderick;
import org.bklab.sftp.configuration.Settings;
import org.bklab.sftp.locale.i18n;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.view.InteractiveLogic;
import org.bklab.sftp.view.dialog.EnterSomethingDialog;
import org.bklab.sftp.view.panel.SshPanel;
import org.bklab.ssh2.Connection;
import org.bklab.ssh2.KnownHosts;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * The SSH-2 connection is established in this thread.
 * If we would not use a separate thread (e.g., put this code in the event handler of the "Login" button) then the GUI
 * would not be responsive (missing window repaints if you move the window etc.)
 *
 * @author Broderick
 */
public class ConnectionManager extends Thread {
    private static final String connectionsDir = Settings.get("connectionsDir");
    private static String knownHostPath = connectionsDir + "/known_hosts";
    private static String idDSAPath = connectionsDir + "/id_dsa";
    private static String idRSAPath = connectionsDir + "/id_rsa";

    private static KnownHosts database = new KnownHosts();

    private CleverTreeNode node;
    private Broderick frame;
    private Connection connection;
    private SshPanel terminalPanel;
    private Boolean result;

    public ConnectionManager(Broderick frame, CleverTreeNode node) {
        super();
        this.frame = frame;
        this.node = node;
        this.result = null;
        File knownHostFile = new File(knownHostPath);
        if (knownHostFile.exists()) {
            try {
                database.addHostkeys(knownHostFile);
            } catch (IOException e) {
                ExceptionManager.manageException(e);
            }
        }
    }

    public void run() {
        result = connect();
    }

    public Boolean connect() {
        boolean toReturn = true;
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String hostname = node.getPropertyValue("host");
        int port = Integer.parseInt(node.getPropertyValue("sshPort"));
        String username = node.getPropertyValue("username");
        String password = node.getPropertyValue("password");
        connection = new Connection(hostname, port);

        try {
            /*
             * CONNECT AND VERIFY SERVER HOST KEY (with callback)
             */
            String[] hostkeyAlgos = database.getPreferredServerHostkeyAlgorithmOrder(hostname);

            if (hostkeyAlgos != null) {
                connection.setServerHostKeyAlgorithms(hostkeyAlgos);
            }
            connection.connect(new AdvancedVerifier(database, frame, knownHostPath));

            /*
             * AUTHENTICATION PHASE
             */

            boolean enableKeyboardInteractive = true;
            boolean enableDSA = true;
            boolean enableRSA = true;
            String lastError = null;
            while (true) {
                if ((enableDSA || enableRSA) && connection.isAuthMethodAvailable(username, "publickey")) {
                    if (enableDSA) {
                        File key = new File(idDSAPath);
                        if (key.exists()) {
                            EnterSomethingDialog esd = new EnterSomethingDialog(frame, i18n.DSAAuthentication, new String[]{lastError, i18n.EnterDsaPassword}, true);
                            esd.setVisible(true);
                            boolean res = connection.authenticateWithPublicKey(username, key, esd.getAnswer());
                            if (res == true) {
                                break;
                            }
                            lastError = "DSA authentication failed.";
                        }
                        enableDSA = false; // do not try again
                    }

                    if (enableRSA) {
                        File key = new File(idRSAPath);
                        if (key.exists()) {
                            EnterSomethingDialog esd = new EnterSomethingDialog(frame, i18n.RSAAuthentication, new String[]{lastError, i18n.EnterRsaPassword}, true);
                            esd.setVisible(true);
                            boolean res = connection.authenticateWithPublicKey(username, key, esd.getAnswer());
                            if (res == true) {
                                break;
                            }
                            lastError = i18n.RSAAuthenticationFailed;
                        }
                        enableRSA = false; // do not try again
                    }
                    continue;
                }

                if (enableKeyboardInteractive && connection.isAuthMethodAvailable(username, "keyboard-interactive")) {
                    InteractiveLogic il = new InteractiveLogic(lastError, frame);
                    boolean res = connection.authenticateWithKeyboardInteractive(username, il);
                    if (res == true) {
                        break;
                    }
                    if (il.getPromptCount() == 0) {
                        // aha. the server announced that it supports "keyboard-interactive", but when
                        // we asked for it, it just denied the request without sending us any prompt.
                        // That happens with some server versions/configurations.
                        // We just disable the "keyboard-interactive" method and notify the user.
                        lastError = i18n.KeyboardInteractiveNotWork;
                        enableKeyboardInteractive = false; // do not try this again
                    } else {
                        lastError = i18n.KeyboardInteractiveAuthFailed; // try again, if possible
                    }
                    continue;
                }

                if (connection.isAuthMethodAvailable(username, "password")) {
                    boolean res = connection.authenticateWithPassword(username, password);
                    if (res == true) {
                        break;
                    }
                    lastError = i18n.PasswordAuthenticationFailed; // try again, if possible
                    continue;
                }
                throw new IOException(i18n.NoSupportedAuthenticationMethodsAvailable);
            }
            //AUTH OK
        } catch (Exception e) {
            toReturn = false;
            try {
                connection.close();
            } catch (Exception ex) {
            }
            JOptionPane.showMessageDialog(frame, i18n.Exception + e.getMessage());
        }
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        return toReturn;
    }

    public SshPanel getTerminalPanel() {
        return terminalPanel;
    }

    public Connection getConnection() {
        return connection;
    }

    public Boolean getResult() {
        return result;
    }
}
