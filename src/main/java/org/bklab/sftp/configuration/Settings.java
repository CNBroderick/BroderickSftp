package org.bklab.sftp.configuration;

import org.bklab.sftp.core.Broderick;
import org.bklab.sftp.utils.ExceptionManager;

import java.io.*;
import java.util.Date;
import java.util.Properties;

/**
 * @author Broderick
 */
public class Settings {
    private static Properties properties;

    public static String get(String key) {
        return getProperties().getProperty(key);
    }

    public static Integer getInteger(String key) {
        Integer toReturn = null;
        String value = get(key);
        try {
            toReturn = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            ExceptionManager.manageException(e);
        }
        return toReturn;
    }

    public static Boolean getBoolean(String key) {
        Boolean toReturn = null;
        String value = get(key);
        try {
            toReturn = Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    private static Properties getProperties() {
        try {
            if (properties == null) {
                properties = new Properties();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream("conf/settings.properties");
                } catch (FileNotFoundException e) {
                    File file = new File("conf/settings.properties");
                    file.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    Date date = new Date();
                    Properties newPropertiesFile = new Properties();
                    newPropertiesFile.setProperty("startMaximized", "true");
                    newPropertiesFile.setProperty("terminalFontSizePt", "14");
                    newPropertiesFile.setProperty("defaultHeight", "768");
                    newPropertiesFile.setProperty("defaultWidth", "1024");
                    newPropertiesFile.store(fos, Broderick.PROGRAM_NAME + " " + Broderick.PROGRAM_VERSION + "\n" + date.getTime());
                    fos.close();
                    fis = new FileInputStream("conf/settings.properties");
                }
                properties.load(fis);
                fis.close();
            }
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
        return properties;
    }

    public static void addProperty(String key, String value) {
        try {
            getProperties().setProperty(key, value);
            Date date = new Date();
            FileOutputStream fos = new FileOutputStream("conf/settings.properties");
            getProperties().store(fos, Broderick.PROGRAM_NAME + " " + Broderick.PROGRAM_VERSION + "\n" + date.getTime());
            fos.close();
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
    }
}
