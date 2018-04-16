package org.bklab.sftp.core.connection;

import org.bklab.sftp.locale.i18n;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.ssh2.KnownHosts;
import org.bklab.ssh2.ServerHostKeyVerifier;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * This ServerHostKeyVerifier asks the user on how to proceed if a key cannot be found in the in-memory database.
 *
 * @author Broderick
 */
public class AdvancedVerifier implements ServerHostKeyVerifier {
    private KnownHosts database;
    private String knownHostPath;
    private JFrame frame;

    public AdvancedVerifier(KnownHosts database, JFrame frame, String knownHostPath) {
        super();
        this.database = database;
        this.knownHostPath = knownHostPath;
        this.frame = frame;
    }

    public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey) throws Exception {
        final String host = hostname;
        final String algo = serverHostKeyAlgorithm;
        String message;
        /* Check database */
        int result = database.verifyHostkey(hostname, serverHostKeyAlgorithm, serverHostKey);

        switch (result) {
            case KnownHosts.HOSTKEY_IS_OK:
                return true;
            case KnownHosts.HOSTKEY_IS_NEW:
                message = i18n.requestConfirmAcceptHostkey1 + algo + i18n.requestConfirmAcceptHostkey2 + host + i18n.requestConfirmAcceptHostkey3;
                break;
            case KnownHosts.HOSTKEY_HAS_CHANGED:
                message = i18n.WarningHostkeyChanged1 + host + i18n.WarningHostkeyChanged2;
                break;
            default:
                throw new IllegalStateException();
        }

        /* Include the fingerprints in the message */
        String hexFingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey);
        String bubblebabbleFingerprint = KnownHosts.createBubblebabbleFingerprint(serverHostKeyAlgorithm, serverHostKey);
        message += i18n.hexFingerprint + hexFingerprint + "\nBubblebabble Fingerprint: " + bubblebabbleFingerprint;

        /* Now ask the user */
        int choice = JOptionPane.showConfirmDialog(frame, message);
        if (choice == JOptionPane.YES_OPTION) {
            /* Be really paranoid. We use a hashed hostname entry */
            String hashedHostname = KnownHosts.createHashedHostname(hostname);

            /* Add the hostkey to the in-memory database */
            database.addHostkey(new String[]{hashedHostname}, serverHostKeyAlgorithm, serverHostKey);

            /* Also try to add the key to a known_host file */
            try {
                KnownHosts.addHostkeyToFile(new File(knownHostPath), new String[]{hashedHostname}, serverHostKeyAlgorithm, serverHostKey);
            } catch (IOException e) {
                ExceptionManager.manageException(e);
            }
            return true;
        } else if (choice == JOptionPane.CANCEL_OPTION) {
            throw new Exception(i18n.abortedServerHostkey);
        }
        return false;
    }
}