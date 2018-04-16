package org.bklab.sftp;

import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ColorTest {

    @Test
    public void Test() {
        GraExp2 ge = new GraExp2();

    }


    public class GraExp2 extends JFrame {
        Container c;
        JButton btn = new JButton("选背景色");
        Color color = new Color(200, 200, 200);

        public GraExp2() {
            c = getContentPane();
            c.setLayout(new FlowLayout());
            c.add(btn);
            btn.addActionListener(e -> {
                color = JColorChooser.showDialog(null, "请选择你喜欢的颜色", color);
                if (color == null) color = Color.lightGray;
                c.setBackground(color);
                c.repaint();
            });

            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            setSize(new Dimension(400, 300));
        }
    }
}
