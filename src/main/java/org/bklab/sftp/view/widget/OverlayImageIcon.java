package org.bklab.sftp.view.widget;

import javax.swing.*;
import java.awt.*;

/**
 * @author Broderick
 */
public class OverlayImageIcon extends ImageIcon {

    private static final long serialVersionUID = 7752878811533901824L;

    private ImageIcon iconBottom;
    private ImageIcon iconTop;

    public OverlayImageIcon(ImageIcon iconBottom, ImageIcon iconTop) {
        super(iconBottom.getImage());
        this.iconBottom = iconBottom;
        this.iconTop = iconTop;
    }

    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        iconBottom.paintIcon(c, g, x, y);
        iconTop.paintIcon(c, g, x, y);
    }
}
