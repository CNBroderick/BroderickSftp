package org.bklab.sftp.view.widget;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * @author Broderick
 */
public class InputFileChooser extends JPanel implements ActionListener, MouseListener {

    private static final long serialVersionUID = 7241869144790496312L;
    private JFileChooser fileChooser;
    private JTextField text;
    private JButton button;
    private File file;

    public InputFileChooser(JFileChooser fileChooser, Icon icon) {
        super();
        this.fileChooser = fileChooser;
        setLayout(new BorderLayout());
        setOpaque(false);
        JPanel panelText = new JPanel(new MigLayout("insets 0,fill"));
        JPanel panelButton = new JPanel(new MigLayout("insets 0"));
        text = new JTextField();
        text.setEditable(false);
        text.addMouseListener(this);
        button = new JButton(icon);
        button.setSize(new Dimension(22, 22));
        button.setPreferredSize(new Dimension(22, 22));
        button.setMaximumSize(new Dimension(22, 22));
        button.addActionListener(this);
        panelText.add(text, "growx");
        panelButton.add(button);
        add(panelText, BorderLayout.CENTER);
        add(panelButton, BorderLayout.EAST);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showOpen();
    }

    private void showOpen() {
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            text.setText(file.getAbsolutePath());
        }
    }

    public File getFile() {
        return file;
    }

    public void reset() {
        text.setText("");
        file = null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        text.setEnabled(enabled);
        button.setEnabled(enabled);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (isEnabled()) {
            showOpen();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
