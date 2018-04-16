package org.bklab.sftp.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author Broderick
 */
public class ViewUtils {
    private static Toolkit toolkit = Toolkit.getDefaultToolkit();

    public static Toolkit getToolkit() {
        return toolkit;
    }

    public static ImageIcon createImageIcon(String path) {
        ImageIcon toReturn = null;
        try {
            Image image = toolkit.getImage(path);
            toReturn = new ImageIcon(image);
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
        return toReturn;
    }

    public static Image createImage(String path) {
        Image toReturn = null;
        try {
            toReturn = toolkit.getImage(path);
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
        return toReturn;
    }


    public static ImageIcon createImageIcon(String path, int height) {
        ImageIcon toReturn = null;
        try {
            BufferedImage bimg = ImageIO.read(new File(path));
            double currentWidth = bimg.getWidth();
            double currentHeight = bimg.getHeight();
            Image image = toolkit.getImage(path);
            Image scaledImage = image.getScaledInstance((int) Math.round((height / currentHeight) * currentWidth), height, java.awt.Image.SCALE_SMOOTH);
            toReturn = new ImageIcon(scaledImage);
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
        return toReturn;
    }

    public static Border createBorder(Color color) {
        return BorderFactory.createMatteBorder(5, 5, 5, 5, color);
    }

    public static BufferedImage rotate(BufferedImage image, double angle) {
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = image.getWidth(), h = image.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
        GraphicsConfiguration gc = getDefaultConfiguration();
        BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
        Graphics2D g = result.createGraphics();
        g.translate((neww - w) / 2, (newh - h) / 2);
        g.rotate(angle, w / 2, h / 2);
        g.drawRenderedImage(image, null);
        g.dispose();
        return result;
    }

    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }
}
