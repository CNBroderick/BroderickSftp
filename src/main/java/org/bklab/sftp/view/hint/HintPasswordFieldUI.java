package org.bklab.sftp.view.hint;

import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author Broderick
 */
public class HintPasswordFieldUI extends BasicPasswordFieldUI implements FocusListener {

    private String hint;
    private boolean hideOnFocus;
    private Color color;

    public HintPasswordFieldUI(String hint) {
        this(hint, false);
    }

    public HintPasswordFieldUI(String hint, boolean hideOnFocus) {
        this(hint, hideOnFocus, null);
    }

    public HintPasswordFieldUI(String hint, boolean hideOnFocus, Color color) {
        this.hint = hint;
        this.hideOnFocus = hideOnFocus;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    private void repaint() {
        if (getComponent() != null) {
            getComponent().repaint();
        }
    }

    public boolean isHideOnFocus() {
        return hideOnFocus;
    }

    public void setHideOnFocus(boolean hideOnFocus) {
        this.hideOnFocus = hideOnFocus;
        repaint();
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
        repaint();
    }

    @Override
    protected void paintSafely(Graphics g) {
        super.paintSafely(g);
        JTextComponent comp = getComponent();
        HintTextFieldUI.manageColor(g, comp, hint, hideOnFocus, color);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (hideOnFocus) repaint();

    }

    @Override
    public void focusLost(FocusEvent e) {
        if (hideOnFocus) repaint();
    }

    @Override
    protected void installListeners() {
        super.installListeners();
        getComponent().addFocusListener(this);
    }

    @Override
    protected void uninstallListeners() {
        super.uninstallListeners();
        getComponent().removeFocusListener(this);
    }
}
