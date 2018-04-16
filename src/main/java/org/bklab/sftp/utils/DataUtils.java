package org.bklab.sftp.utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Broderick
 */
public class DataUtils {
    private static DecimalFormat secondRound = new DecimalFormat("#.##");
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static boolean isInteger(String value) {
        boolean toReturn = false;
        try {
            Integer.parseInt(value);
            toReturn = true;
        } catch (NumberFormatException e) {
            toReturn = false;
        }
        return toReturn;
    }

    public static String colorToHex(Color color) {
        String toReturn = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        return toReturn;
    }

    public static Color hexToColor(String color) {
        return Color.decode(color);
    }

    public static String getExtension(String fileName) {
        String toReturn = "";
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf >= 0) {
            toReturn = fileName.substring(lastIndexOf + 1);
        }
        return toReturn;
    }

    public static String getHumanReadableFileSize(long size) {
        String toReturn = "";
        if (size > Math.pow(1024, 4)) {
            toReturn = secondRound.format(((double) size) / Math.pow(1024, 4)) + " TB";
        } else if (size > Math.pow(1024, 3)) {
            toReturn = secondRound.format(((double) size) / Math.pow(1024, 3)) + " GB";
        } else if (size > Math.pow(1024, 2)) {
            toReturn = secondRound.format(((double) size) / Math.pow(1024, 2)) + " MB";
        } else if (size > Math.pow(1024, 1)) {
            toReturn = secondRound.format(((double) size) / Math.pow(1024, 1)) + " KB";
        } else {
            toReturn = size + " B";
        }
        return toReturn;
    }

    public static String dateFormat(Date date) {
        return dateFormatter.format(date);
    }

    public static boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return !Character.isISOControl(c)
                && c != KeyEvent.CHAR_UNDEFINED
                && block != null
                && block != Character.UnicodeBlock.SPECIALS;
    }
}
