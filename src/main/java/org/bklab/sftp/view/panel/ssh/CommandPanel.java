package org.bklab.sftp.view.panel.ssh;

import net.miginfocom.swing.MigLayout;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.panel.SshPanel;
import org.bklab.sftp.view.widget.InputFileChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Broderick
 */
public class CommandPanel extends JPanel implements KeyListener, ActionListener {

    private static final long serialVersionUID = 7567479247257469656L;
    private JTextField textCommand;
    private SshPanel sshPanel;
    private JCheckBox checkboxGroup;
    private InputFileChooser inputFile;

    public CommandPanel(SshPanel sshPanel) {
        super();
        this.sshPanel = sshPanel;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 0, 0, 0));
        JPanel panelTitle = new JPanel(new MigLayout("wrap 2,fill"));
        panelTitle.setOpaque(false);
        panelTitle.setBorder(new TitledBorder("Execute command"));
        JLabel labelCommand = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_TERMINAL));
        textCommand = new JTextField(400);
        textCommand.addKeyListener(this);
        JLabel labelFile = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_FILE));
        JFileChooser fileChooser = new JFileChooser("commands");
        fileChooser.setDialogTitle("Open text file...");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return "Text file (*.txt)";
            }

            @Override
            public boolean accept(File file) {
                boolean toReturn = false;
                if (file.isDirectory() || (file.isFile() && file.getName().endsWith(".txt"))) {
                    toReturn = true;
                }
                return toReturn;
            }
        });
        inputFile = new InputFileChooser(fileChooser, ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER));
        checkboxGroup = new JCheckBox("Execute on group");
        checkboxGroup.setSelected(true);
        checkboxGroup.setOpaque(false);
        JButton buttonExcec = new JButton("Execute command", ViewUtils.createImageIcon(DataPath.IMG_16_GEARS));
        buttonExcec.addActionListener(this);
        panelTitle.add(labelCommand);
        panelTitle.add(textCommand, "growx");
        panelTitle.add(labelFile);
        panelTitle.add(inputFile, "growx");
        panelTitle.add(checkboxGroup, "span 2");
        panelTitle.add(buttonExcec, "span 2, align center");
        add(panelTitle, BorderLayout.CENTER);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        inputFile.reset();
        if (e.getKeyChar() == '\n') {
            String command = textCommand.getText();
            if (!command.trim().equals("")) {
                singleCommand();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File file = inputFile.getFile();
        if (file != null) {
            command(file, checkboxGroup.isSelected());
        } else {
            singleCommand();
        }
    }

    private void command(File file, boolean executeOnGroup) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            while (line != null) {
                sshPanel.command(line, executeOnGroup);
                line = br.readLine();
            }
            br.close();
        } catch (IOException exception) {
            ExceptionManager.manageException(exception);
        }
    }

    private void singleCommand() {
        String command = textCommand.getText();
        if (!command.trim().equals("")) {
            sshPanel.command(command, checkboxGroup.isSelected());
            textCommand.setText("");
        }
    }
}
