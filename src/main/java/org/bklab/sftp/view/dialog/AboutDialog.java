package org.bklab.sftp.view.dialog;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.core.Broderick;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.utils.ViewUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author Broderick
 */
public class AboutDialog extends JDialog implements HyperlinkListener, MouseListener {
    private static final long serialVersionUID = 4113056938268212718L;
    private static Desktop desktop = Desktop.getDesktop();
    private JPanel mainPanel, topPanel, bottomPanel;
    private JLabel labelName, labelLogo, broderickLabsLabel1, broderickLabsLabel2, labelSpace, blogLabE11, blogLabE12;
    private String bkLabOrgHtml;
    private JEditorPane paneBkLabOrgHtml;

    public AboutDialog(Broderick frame) {
        super(frame);
        setTitle("About " + Broderick.PROGRAM_NAME);
        setIconImage(ViewUtils.createImage(DataPath.IMG_16_INFORMATIONS));
        setResizable(false);
        setModal(true);
        setLayout(new BorderLayout());
        bkLabOrgHtml = "";
        try {
            bkLabOrgHtml = FileUtils.readFileToString(new File(DataPath.BKLAB_ORG_HTML), Charset.forName("UTF-8"));
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(mainPanel, BorderLayout.CENTER);
        labelName = new JLabel(Broderick.PROGRAM_NAME + " " + Broderick.PROGRAM_VERSION, JLabel.CENTER);
        Font font = labelName.getFont();
        font = font.deriveFont(12.0f);
        labelName.setFont(labelName.getFont().deriveFont(Font.BOLD, 20));
        labelLogo = new JLabel(ViewUtils.createImageIcon(DataPath.ICON_LOGO_256));
        paneBkLabOrgHtml = new JEditorPane("text/html", bkLabOrgHtml);
        paneBkLabOrgHtml.setBackground(new Color(240, 240, 240));
        paneBkLabOrgHtml.setEditable(false);
        paneBkLabOrgHtml.addHyperlinkListener(this);
        topPanel = new JPanel(new BorderLayout());
        topPanel.add(labelName, BorderLayout.SOUTH);
        topPanel.add(labelLogo, BorderLayout.CENTER);
        topPanel.setOpaque(false);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.setBackground(new Color(240, 240, 240));
        mainPanel.setOpaque(true);
        mainPanel.add(paneBkLabOrgHtml, BorderLayout.CENTER);

        broderickLabsLabel1 = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_BLABS));
        broderickLabsLabel1.addMouseListener(this);
        String blank = "       ";
        broderickLabsLabel2 = new JLabel(blank + "Broderick Labs -- broderick.cn, bkLab.org, bz8.org");
        broderickLabsLabel2.setFont(font);
        broderickLabsLabel2.addMouseListener(this);
        labelSpace = new JLabel(" ");
        blogLabE11 = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_BLOG_CENTER));
        blogLabE11.addMouseListener(this);
        blogLabE12 = new JLabel(blank + "Broderick Labs Blog Center --Broderick Personal Blog.");
        blogLabE12.setFont(font);
        blogLabE12.addMouseListener(this);
        bottomPanel = new JPanel(new MigLayout("wrap 1"));
        bottomPanel.setOpaque(false);
        bottomPanel.add(broderickLabsLabel1, "align center");
        bottomPanel.add(broderickLabsLabel2, "align center");
        bottomPanel.add(labelSpace, "align center");
        bottomPanel.add(blogLabE11, "align center");
        bottomPanel.add(blogLabE12, "align center");
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
            browse(e.getURL());
        }
    }

    private void browse(String string) {
        try {
            browse(new URI(string));
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    private void browse(URL url) {
        try {
            browse(url.toURI());
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    private void browse(URI uri) {
        try {
            desktop.browse(uri);
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == broderickLabsLabel1 || e.getSource() == broderickLabsLabel2) {
            browse("https://broderick.cn");
            e.consume();
        } else if (e.getSource() == blogLabE11 || e.getSource() == blogLabE12) {
            browse("https://broderick.cn/blog");
            e.consume();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (e.getSource() == broderickLabsLabel1 || e.getSource() == broderickLabsLabel2 || e.getSource() == blogLabE11 || e.getSource() == blogLabE12) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (e.getSource() == broderickLabsLabel1 || e.getSource() == broderickLabsLabel2 || e.getSource() == blogLabE11 || e.getSource() == blogLabE12) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

    }
}
