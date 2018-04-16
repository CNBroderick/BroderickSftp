package org.bklab.sftp.locale;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Broderick
 */
public class i18n {

    private static Locale defaultLocale = Locale.getDefault();
    private static ResourceBundle bundle = ResourceBundle.getBundle("org.bklab.sftp.locale.broderick", defaultLocale);
//    debug english frame
//    private static ResourceBundle bundle = ResourceBundle.getBundle("org.bklab.sftp.locale.broderick", Locale.ROOT);

    public static final String programName;
    public static final String programNameShort;
    public static final String useDefaultConnection;
    public static final String useDefaultConnectionFolderPath;
    public static final String chooseConnectionFolder;
    public static final String connectionFolderSetDefaultPath;
    public static final String inputConnectionFolderPassword;
    public static final String reinputConnectionFolderPassword;
    public static final String inputPasswordForConnectionFile;
    public static final String incorrectPassword;

    public static final String requestConfirmAcceptHostkey1;
    public static final String requestConfirmAcceptHostkey2;
    public static final String requestConfirmAcceptHostkey3;
    public static final String WarningHostkeyChanged1;
    public static final String WarningHostkeyChanged2;
    public static final String hexFingerprint;
    public static final String abortedServerHostkey;
    public static final String DSAAuthentication;
    public static final String RSAAuthentication;
    public static final String RSAAuthenticationFailed;
    public static final String EnterDsaPassword;
    public static final String EnterRsaPassword;
    public static final String KeyboardInteractiveNotWork;
    public static final String KeyboardInteractiveAuthFailed;
    public static final String PasswordAuthenticationFailed;
    public static final String NoSupportedAuthenticationMethodsAvailable;
    public static final String Exception;
    public static final String submitExceptionRequest;
    public static final String ExceptionManagerTitle;

    static {
        programName = bundle.getString("programName");
        programNameShort = bundle.getString("programNameShort");
        useDefaultConnection = bundle.getString("useDefaultConnection");
        useDefaultConnectionFolderPath = bundle.getString("useDefaultConnectionFolderPath");
        chooseConnectionFolder = bundle.getString("chooseConnectionFolder");
        connectionFolderSetDefaultPath = bundle.getString("connectionFolderSetDefaultPath");
        inputConnectionFolderPassword = bundle.getString("inputConnectionFolderPassword");
        reinputConnectionFolderPassword = bundle.getString("reInputConnectionFolderPassword");
        inputPasswordForConnectionFile = bundle.getString("inputPasswordForConnectionFile");
        incorrectPassword = bundle.getString("incorrectPassword");

        requestConfirmAcceptHostkey1 = bundle.getString("requestConfirmAcceptHostkey1");
        requestConfirmAcceptHostkey2 = bundle.getString("requestConfirmAcceptHostkey2");
        requestConfirmAcceptHostkey3 = bundle.getString("requestConfirmAcceptHostkey3");
        WarningHostkeyChanged1 = bundle.getString("WarningHostkeyChanged1");
        WarningHostkeyChanged2 = bundle.getString("WarningHostkeyChanged2");
        hexFingerprint = bundle.getString("hexFingerprint");
        abortedServerHostkey = bundle.getString("abortedServerHostkey");
        DSAAuthentication = bundle.getString("DSAAuthentication");
        RSAAuthentication = bundle.getString("RSAAuthentication");
        RSAAuthenticationFailed = bundle.getString("RSAAuthenticationFailed");
        EnterDsaPassword = bundle.getString("EnterDsaPassword");
        EnterRsaPassword = bundle.getString("EnterRsaPassword");
        KeyboardInteractiveNotWork = bundle.getString("KeyboardInteractiveNotWork");
        KeyboardInteractiveAuthFailed = bundle.getString("KeyboardInteractiveAuthFailed");
        PasswordAuthenticationFailed = bundle.getString("PasswordAuthenticationFailed");
        NoSupportedAuthenticationMethodsAvailable = bundle.getString("NoSupportedAuthenticationMethodsAvailable");
        Exception = bundle.getString("Exception");
        submitExceptionRequest = bundle.getString("submitExceptionRequest");
        ExceptionManagerTitle = bundle.getString("ExceptionManagerTitle");

    }

}
