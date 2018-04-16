package org.bklab.sftp.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bklab.sftp.core.Broderick;
import org.bklab.sftp.locale.i18n;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Broderick
 */
public class ExceptionManager {
    private static boolean debug = false;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
    private static Desktop desktop = Desktop.getDesktop();

    public static void manageException(Exception e) {
        if (debug) {
            e.printStackTrace();
        } else {
            Date date = new Date();
            String stackTrace = ExceptionUtils.getStackTrace(e);
            Properties systemProperties = System.getProperties();
            Map<String, String> envProperites = System.getenv();
            StringBuffer buffer = new StringBuffer();
            buffer.append(Broderick.PROGRAM_NAME + " " + Broderick.PROGRAM_VERSION + " Exception Report\n\n");
            buffer.append(date.getTime() + "\n");
            buffer.append(date + "\n");
            buffer.append(dateFormat.format(date) + "\n\n");
            buffer.append(e.getClass().getName() + "\n");
            buffer.append(e.getMessage() + "\n\n");
            buffer.append(stackTrace + "\n\n");
            buffer.append("SYSTEM PROPERTIES\n");
            Set<String> keys = systemProperties.stringPropertyNames();
            ArrayList<String> keysArray = new ArrayList<String>(keys);
            Collections.sort(keysArray);
            for (String key : keysArray) {
                buffer.append(key + ": " + systemProperties.get(key) + "\n");
            }
            buffer.append("\nENV PROPERTIES\n");
            keys = envProperites.keySet();
            keysArray = new ArrayList<String>(keys);
            Collections.sort(keysArray);
            for (String key : keysArray) {
                buffer.append(key + ": " + envProperites.get(key) + "\n");
            }
            try {
                File file = new File("exception_reports/" + Broderick.PROGRAM_NAME_SHORT + "/" + "ExceptionReport_" + date.getTime() + ".txt");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fw = new FileWriter(file);
                fw.write(buffer.toString());
                fw.close();
                int answer = JOptionPane.showConfirmDialog(null, i18n.Exception + e.getLocalizedMessage() + "\n" + i18n.Exception  + file.getAbsolutePath() + i18n.submitExceptionRequest, i18n.ExceptionManagerTitle, JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    desktop.browse(new URI("https://broderick.cn"));
                    desktop.open(new File("exception_reports"));
                }
            } catch (Exception exception) {
                e.printStackTrace();
                exception.printStackTrace();
            }
        }
    }
}
