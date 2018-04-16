package org.bklab.sftp.view.widget;

import org.bklab.sftp.utils.ExceptionManager;
import org.junit.Test;

import javax.swing.*;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

public class VT100TextAreaTest {
    @Test
    public void createDeployment() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame frame=new JFrame("Test VT100 Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            VT100TextArea vt100=new VT100TextArea(10,10,null);
            frame.add(vt100);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            vt100.setData("1234567890\n1234567890\n1234567890\n1234567890".getBytes());
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Test
    public void testUtf8() throws IOException {

    }

}
