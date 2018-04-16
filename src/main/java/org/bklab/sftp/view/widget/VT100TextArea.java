package org.bklab.sftp.view.widget;

import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ExceptionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Broderick
 */
@SuppressWarnings("unused")
public class VT100TextArea extends JPanel implements MouseListener, ActionListener, KeyListener {

    private static final long serialVersionUID = 5621159880953331554L;
    private static final int FPS = 24;
    private static boolean debugVT = false;
    private static boolean debugAscii = false;
    private static boolean debugData = false;
    private static int chWidt = 8;
    private static int chHeight = 20;
    private Font font;
    private int rows, columns;
    private Dimension size;
    private char[][] data;
    private int[][] foregroundColors;
    private int[][] backGroundColors;
    private boolean[] scrollEnabled;
    private int[] newLines;
    private boolean mousePressed;
    private Timer timer;
    private Point startSelPoint, endSelPoint;
    private int posX, posY;
    private OutputStream out;
    private Toolkit toolkit;
    private Clipboard clipboard;

    private Color[] colors;
    private Color foregroundColor, backgroundColor;
    private boolean negativeColor;

    public VT100TextArea(int rows, int columns, OutputStream out) throws Exception {
        super();
        if (debugData || debugVT) {
            System.out.println("START VT100 with " + rows + " rows and " + columns + " colums");
        }
        this.font = Font.createFont(Font.PLAIN, new File(DataPath.FONT_SOURCECODEPRO_MEDIUM_TTF)).deriveFont(Font.PLAIN, 14);
        this.rows = rows;
        this.columns = columns;
        this.out = out;
        toolkit = Toolkit.getDefaultToolkit();
        clipboard = toolkit.getSystemClipboard();
        colors = new Color[8];
        colors[0] = Color.BLACK;
        colors[1] = Color.RED;
        colors[2] = Color.GREEN;
        colors[3] = Color.YELLOW;
        colors[4] = Color.BLUE;
        colors[5] = Color.MAGENTA;
        colors[6] = Color.CYAN;
        colors[7] = Color.LIGHT_GRAY;
        foregroundColor = Color.LIGHT_GRAY;
        backgroundColor = Color.BLACK;
        negativeColor = false;

        addMouseListener(this);
        //Key Listeners need focusable components
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);
        posX = 0;
        posY = 0;
        size = new Dimension(columns * chWidt, rows * chHeight);
        setBackground(Color.BLACK);
        data = new char[rows][columns];
        foregroundColors = new int[rows][columns];
        backGroundColors = new int[rows][columns];
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                data[y][x] = ' ';
                foregroundColors[y][x] = 0;
                backGroundColors[y][x] = 0;
            }
        }
        scrollEnabled = new boolean[rows];
        newLines = new int[rows];
        for (int i = 0; i < rows; i++) {
            scrollEnabled[i] = true;
            newLines[i] = 0;
        }
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        startSelPoint = null;
        endSelPoint = null;
        timer = new Timer(1000 / FPS, this);
        //timer.start();
    }

    public static void main(String[] args) {
        try {
            debugData = true;
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame frame = new JFrame("Test VT100 Panel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            VT100TextArea vt100 = new VT100TextArea(10, 10, null);
            frame.add(vt100);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            vt100.setData("Broderick Labs\n布克约森实验室\nbkLab.org".getBytes("UTF-8"));
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    @Override
    public Dimension getMaximumSize() {
        return size;
    }

    @Override
    public Dimension getMinimumSize() {
        return size;
    }

    @Override
    public Dimension getSize() {
        return size;
    }

    @Override
    public int getWidth() {
        return (int) size.getWidth();
    }

    @Override
    public int getHeight() {
        return (int) size.getHeight();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setFont(font);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                if (backGroundColors[y][x] != 0) {
                    if (backGroundColors[y][x] == -1) {
                        backgroundColor = Color.BLACK;
                        negativeColor = false;
                    } else if (backGroundColors[y][x] == -2) {
                        negativeColor = true;
                    } else if (backGroundColors[y][x] == -3) {
                        negativeColor = false;
                    } else {
                        backgroundColor = colors[backGroundColors[y][x] - 40];
                    }
                }
                g.setColor(negativeColor ? negativeColor(backgroundColor) : backgroundColor);
                g.fillRect(chWidt * x, chHeight * y, chWidt, chHeight);

                if (foregroundColors[y][x] != 0) {
                    if (foregroundColors[y][x] == -1) {
                        foregroundColor = Color.LIGHT_GRAY;
                        negativeColor = false;
                    } else if (foregroundColors[y][x] == -2) {
                        negativeColor = true;
                    } else if (foregroundColors[y][x] == -3) {
                        negativeColor = false;
                    } else {
                        foregroundColor = colors[foregroundColors[y][x] - 30];
                    }
                }
                g.setColor(negativeColor ? negativeColor(foregroundColor) : foregroundColor);
                g.drawChars(data[y], x, 1, chWidt * x, chHeight * (y + 1) - 5);
            }
        }
        if (mousePressed) {
            Point point = getMouseChar();
            if (startSelPoint == null) {
                startSelPoint = point;
            }
            endSelPoint = point;
        }
        if (startSelPoint != null && endSelPoint != null) {
            Point firstPoint = new Point(startSelPoint.x < endSelPoint.x ? startSelPoint.x : endSelPoint.x, startSelPoint.y < endSelPoint.y ? startSelPoint.y : endSelPoint.y);
            Point secondPoint = new Point(startSelPoint.x > endSelPoint.x ? startSelPoint.x : endSelPoint.x, startSelPoint.y > endSelPoint.y ? startSelPoint.y : endSelPoint.y);
            for (int y = firstPoint.y; y <= secondPoint.y; y++) {
                for (int x = firstPoint.x; x <= secondPoint.x; x++) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(chWidt * x, chHeight * y, chWidt, chHeight);
                    g.setColor(Color.BLACK);
                    g.drawChars(data[y], x, 1, chWidt * x, chHeight * (y + 1) - 5);
                }
            }
        }
        //Set cursor
        g.setColor(Color.GREEN);
        g.fillRect(chWidt * posX, chHeight * posY, chWidt, chHeight);
        g.setColor(Color.BLACK);
        g.drawChars(data[posY], posX, 1, chWidt * posX, chHeight * (posY + 1) - 5);
    }

    private Color negativeColor(Color color) {
        if (color.equals(Color.LIGHT_GRAY)) {
            return Color.BLACK;
        } else if (color.equals(Color.BLACK)) {
            return Color.LIGHT_GRAY;
        } else if (color.equals(Color.RED)) {
            return Color.BLUE;
        } else if (color.equals(Color.BLUE)) {
            return Color.RED;
        } else if (color.equals(Color.MAGENTA)) {
            return Color.CYAN;
        } else if (color.equals(Color.CYAN)) {
            return Color.MAGENTA;
        } else if (color.equals(Color.GREEN)) {
            return Color.YELLOW;
        } else if (color.equals(Color.YELLOW)) {
            return Color.GREEN;
        }
        return Color.BLACK;
    }

    public void setData(byte[] data) {
        setData(data, data.length);
    }

    public void setData(byte[] data, int offset) {
        String debugData1 = "";
        String debugData2 = "";
        if (debugData) {
            System.out.println("setData " + data.length + " offset " + offset);
        }
        for (int i = 0; i < offset; i++) {
            char c = (char) (data[i] & 0xff);
            if (debugData) {
                debugData1 += c + " ";
                debugData2 += ((int) data[i]) + " ";
            }
            if (c == 27) { //Escape VT-100
                String parameters = "";
                char secondCommand = ' ';
                i++;
                char firstCommand = (char) (data[i] & 0xff);
                //censire tutti i comandi senza second parameter
                if (firstCommand == '=' || firstCommand == '>' || firstCommand == '<' || firstCommand == 'c' || firstCommand == 'N' || firstCommand == 'O') {
                    //no parameters and second command
                } else if (firstCommand == 'M') {
                    scrollUp();
                } else if (firstCommand == 'D') {
                    scrollDown();
                } else if (firstCommand == 'E') {
                    if (debugVT) {
                        System.out.println("Move to the next line");
                    }
                    posY++;
                } else if (firstCommand == '7') {
                    if (debugVT) {
                        System.out.println("Save");
                    }
                } else if (firstCommand == '8') {
                    if (debugVT) {
                        System.out.println("Restore");
                    }
                } else if (firstCommand == 'H') {
                    if (debugVT) {
                        System.out.println("TAB");
                    }
                } else if (firstCommand == 'c') {
                    if (debugVT) {
                        System.out.println("RESET");
                    }
                } else {
                    //Take parameters
                    i++;
                    char c3 = (char) (data[i] & 0xff);
                    while (isParameter(c3)) {
                        parameters += c3;
                        i++;
                        c3 = (char) (data[i] & 0xff);
                    }
                    secondCommand = c3;
                    if (debugVT) {
                        System.out.println("VT-100: " + firstCommand + parameters + secondCommand);
                    }
                }
                if (firstCommand == '[' && secondCommand == 'm') {
                    ArrayList<String> parametersList = new ArrayList<String>(Arrays.asList(parameters.split(";")));
                    for (String parameter : parametersList) {
                        if (DataUtils.isInteger(parameter)) {
                            int parameterNumber = Integer.parseInt(parameter);
                            if (parameterNumber >= 30 && parameterNumber <= 37) {
                                foregroundColors[posY][posX] = parameterNumber;
                            } else if (parameterNumber >= 40 && parameterNumber <= 47) {
                                backGroundColors[posY][posX] = parameterNumber;
                            } else if (parameterNumber == 0) {
                                foregroundColors[posY][posX] = -1;
                                backGroundColors[posY][posX] = -1;
                            } else if (parameterNumber == 7) {
                                foregroundColors[posY][posX] = -2;
                                backGroundColors[posY][posX] = -2;
                            } else if (parameterNumber == 27) {
                                foregroundColors[posY][posX] = -3;
                                backGroundColors[posY][posX] = -3;
                            } else {
                                if (debugVT) {
                                    System.out.println("Number not matching: " + parameterNumber);
                                }
                            }
                        } else {
                            if (!parameter.trim().equals("")) {
                                if (debugVT) {
                                    System.out.println("Formatting parameter not found: " + parameter);
                                }
                            } else {
                                foregroundColors[posY][posX] = -1;
                                backGroundColors[posY][posX] = -1;
                            }
                        }
                    }
                } else if (firstCommand == '[' && secondCommand == 'K') {
                    if (parameters.equals("")) {
                        //Erased from the current cursor position to the end of the current line.
                        for (int j = posX; j < columns; j++) {
                            this.data[posY][j] = ' ';
                            this.foregroundColors[posY][j] = 0;
                            this.backGroundColors[posY][j] = 0;
                        }
                    } else {
                        if (debugVT) {
                            System.out.println("BUK");
                        }
                    }
                } else if (firstCommand == '[' && secondCommand == 'A') {
                    int n = 1;
                    if (!parameters.equals("")) {
                        n = Integer.parseInt(parameters);
                    }
                    posY = posY - n;
                } else if (firstCommand == '[' && secondCommand == 'B') {
                    int n = 1;
                    if (!parameters.equals("")) {
                        n = Integer.parseInt(parameters);
                    }
                    posY = posY + n;
                } else if (firstCommand == '[' && secondCommand == 'C') {
                    int n = 1;
                    if (!parameters.equals("")) {
                        n = Integer.parseInt(parameters);
                    }
                    posX = posX + n;
                } else if (firstCommand == '[' && secondCommand == 'D') {
                    int n = 1;
                    if (!parameters.equals("")) {
                        n = Integer.parseInt(parameters);
                    }
                    posX = posX - n;
                } else if (firstCommand == '[' && secondCommand == 'H') {
                    //Set cursor position
                    if (parameters.equals("")) {
                        //the cursor will move to the home position
                        posX = 0;
                        posY = 0;
                    } else {
                        String[] elements = parameters.split(";");
                        int[] intParameters = new int[]{Integer.parseInt(elements[0]) - 1, Integer.parseInt(elements[1]) - 1};
                        posX = intParameters[1];
                        if (posX >= columns) {
                            if (debugVT) {
                                System.err.println("Wrong posX request: " + posX + " - setted to " + (columns - 1));
                            }
                            posX = columns - 1;
                        }
                        posY = intParameters[0];
                        if (posY >= rows) {
                            if (debugVT) {
                                System.err.println("Wrong posY request: " + posY + " - setted to " + (rows - 1));
                            }
                            posY = rows - 1;
                        }
                    }
                } else if (firstCommand == '[' && secondCommand == 'J') {
                    //Erases the screen from the current line down to the bottom of the screen.
                    switch (parameters) {
                        case "":
                        case "0":
                            for (int j = posY; j < rows; j++) {
                                for (int k = 0; k < columns; k++) {
                                    this.data[j][k] = ' ';
                                    this.foregroundColors[j][k] = 0;
                                    this.backGroundColors[j][k] = 0;
                                }
                            }
                            break;
                        case "1":
                            //Clear screen from cursor up
                            for (int j = 0; j < posY + 1; j++) {
                                for (int k = 0; k < columns; k++) {
                                    this.data[j][k] = ' ';
                                    this.foregroundColors[j][k] = 0;
                                    this.backGroundColors[j][k] = 0;
                                }
                            }
                            break;
                        case "2":
                            //Clear entire screen
                            for (int j = 0; j < rows; j++) {
                                for (int k = 0; k < columns; k++) {
                                    this.data[j][k] = ' ';
                                    this.foregroundColors[j][k] = 0;
                                    this.backGroundColors[j][k] = 0;
                                }
                            }
                            break;
                        default:
                            if (debugVT) {
                                System.err.println("Parameter [J not valid: " + parameters);
                            }
                            break;
                    }
                } else if (firstCommand == '[' && secondCommand == 'r') {
                    //set scrolling
                    if (parameters.equals("")) {
                        if (debugVT) {
                            System.out.println("BO");
                        }
                    } else {
                        String[] elements = parameters.split(";");
                        int[] intParameters = new int[]{Integer.parseInt(elements[0]) - 1, Integer.parseInt(elements[1])};
                        for (int j = 0; j < intParameters[0]; j++) {
                            scrollEnabled[j] = false;
                        }
                        for (int j = intParameters[0]; j < intParameters[1]; j++) {
                            scrollEnabled[j] = true;
                        }
                        for (int j = intParameters[1]; j < rows; j++) {
                            scrollEnabled[j] = false;
                        }
                    }
                } else {
                    if (!debugVT) {
                        System.out.println("VT-100: " + firstCommand + parameters + secondCommand);
                    }
                }
            } else if (c == 7) {
                //BELL
            } else if (c == 8) {
                //this.data[posY][posX]=' ';
                if (posX == 0) {
                    posY--;
                    posX = columns - 1;
                } else {
                    posX--;
                }
            } else if (c == 10) {
                if (debugData) {
                    System.out.println(debugData1);
                    System.out.println(debugData2);
                }
                newLines[posY] = 1;
                if (debugVT) {
                    System.out.println("new line in row " + posY);
                }
                newLine();
            } else if (c == 13) {
                posX = 0;
            } else if (c == 15) {
                //NOOP
            } else if (c < 32) {
                if (!debugAscii) {
                    System.out.println("Found non printable ASCII " + (int) c);
                }
                continue;
            } else {
                if (debugData) {
                    System.out.println("data[" + posX + "][" + posY + "]=" + c + " " + data[i]);
                }
                this.data[posY][posX] = c;
                posX++;
                if (posX == columns) {
                    //posX=0;
                    //needs new line?
                    boolean needsNewLine = false;
                    for (int j = i + 1; j < offset; j++) {
                        char c2 = (char) (data[j] & 0xff);
                        if (c2 == 27) {
                            j++;
                            char firstCommand2 = (char) (data[j] & 0xff);
                            j++;
                            if (firstCommand2 != '=' && firstCommand2 != '>') {
                                c2 = (char) (data[j] & 0xff);
                                while (isParameter(c2)) {
                                    j++;
                                    c2 = (char) (data[j] & 0xff);
                                }
                                char secondCommand2 = c2;
                                if (firstCommand2 == '[' && secondCommand2 == 'H') {
                                    break;
                                }
                            }
                        } else if (c2 == 10) {
                            break;
                        } else if (c2 >= 32) {
                            needsNewLine = true;
                            break;
                        }
                    }
                    if (needsNewLine) {
                        if (debugData) {
                            System.out.println(debugData1);
                            System.out.println(debugData2);
                        }
                        newLine();
                    } else {
                        posX--;
                    }
                }
            }
            if (c < 32) {
                if (debugAscii) {
                    System.out.println("Found non printable ASCII " + (int) c);
                }
            }
        }
        repaint();
    }

    private void newLine() {
        posX = 0;
        posY++;
        int max = getMaxScrollingRow();
        if (posY > max) {
            posY--;
            scrollDown();
        }
    }

    private void scrollDown() {
        int min = getMinScrollingRow() + 1;
        int max = getMaxScrollingRow() + 1;
        for (int k = min; k < max; k++) {
            this.data[k - 1] = this.data[k];
            this.foregroundColors[k - 1] = this.foregroundColors[k];
            this.backGroundColors[k - 1] = this.backGroundColors[k];
            this.newLines[k - 1] = this.newLines[k];
        }
        this.data[max - 1] = new char[columns];
        this.foregroundColors[max - 1] = new int[columns];
        this.backGroundColors[max - 1] = new int[columns];
        this.newLines[max - 1] = 0;
        for (int k = 0; k < columns; k++) {
            this.data[max - 1][k] = ' ';
            this.foregroundColors[max - 1][k] = 0;
            this.backGroundColors[max - 1][k] = 0;
        }
    }

    private void scrollUp() {
        int min = getMinScrollingRow();
        int max = getMaxScrollingRow();
        for (int k = max; k > min; k--) {
            this.data[k] = this.data[k - 1];
            this.foregroundColors[k] = this.foregroundColors[k - 1];
            this.backGroundColors[k] = this.backGroundColors[k - 1];
            this.newLines[k] = this.newLines[k - 1];
        }
        this.data[min] = new char[columns];
        this.foregroundColors[min] = new int[columns];
        this.backGroundColors[min] = new int[columns];
        this.newLines[min] = 0;
        for (int k = 0; k < columns; k++) {
            this.data[min][k] = ' ';
            this.foregroundColors[min][k] = 0;
            this.backGroundColors[min][k] = 0;
        }
    }

    private int getMinScrollingRow() {
        for (int i = 0; i < scrollEnabled.length; i++) {
            if (scrollEnabled[i]) {
                return i;
            }
        }
        return rows;
    }

    private int getMaxScrollingRow() {
        for (int i = scrollEnabled.length - 1; i >= 0; i--) {
            if (scrollEnabled[i]) {
                return i;
            }
        }
        return 0;
    }

    private boolean isParameter(char c3) {
        return c3 == '0' || c3 == '1' || c3 == '2' || c3 == '3' || c3 == '4' || c3 == '5' || c3 == '6' || c3 == '7' || c3 == '8' || c3 == '9' || c3 == ';' || c3 == '?';
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    private Point getMouseChar() {
        Point toReturn = null;
        Point point = getMousePosition();
        if (point != null) {
            toReturn = new Point();
            toReturn.x = point.x / chWidt;
            toReturn.y = point.y / chHeight;
        }
        return toReturn;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startSelPoint = null;
        endSelPoint = null;
        mousePressed = true;
        timer.start();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;
        timer.stop();
        copySelection();
    }

    private void copySelection() {
        StringBuilder sb = new StringBuilder();
        StringBuffer sbLine = new StringBuffer();
        if (startSelPoint != null && endSelPoint != null) {
            Point firstPoint = new Point(startSelPoint.x < endSelPoint.x ? startSelPoint.x : endSelPoint.x, startSelPoint.y < endSelPoint.y ? startSelPoint.y : endSelPoint.y);
            Point secondPoint = new Point(startSelPoint.x > endSelPoint.x ? startSelPoint.x : endSelPoint.x, startSelPoint.y > endSelPoint.y ? startSelPoint.y : endSelPoint.y);
            if (debugVT) {
                System.out.println("F " + firstPoint);
                System.out.println("S " + firstPoint);
            }
            for (int y = firstPoint.y; y <= secondPoint.y; y++) {
                for (int x = firstPoint.x; x <= secondPoint.x; x++) {
                    sbLine.append(data[y][x]);
                }
                String line = sbLine.toString().replaceAll("\\s+$", "");
                sb.append(line);
                sbLine = new StringBuffer();
                if (secondPoint.x == (columns - 1)) {
                    if (newLines[y] != 0) {
                        sb.append("\n");
                    } else {
                        if (debugVT) {
                            System.out.println("Skipped on row " + y);
                        }
                    }
                } else {
                    sb.append("\n");
                }
            }
        }
        StringSelection selection = new StringSelection(sb.toString());
        clipboard.setContents(selection, selection);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        requestFocus();
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() == timer) {
            repaint();// this will call at every 1 second
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            startSelPoint = null;
            endSelPoint = null;
            repaint();
        } else if (code == KeyEvent.VK_CONTROL || code == KeyEvent.VK_ALT || code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_ALT_GRAPH) {

        } else if (code == KeyEvent.VK_F4 && e.isAltDown()) {

        } else if (code == KeyEvent.VK_UP) {
            try {
                out.write(27);
                out.write('[');
                out.write('A');
            } catch (IOException exception) {
                ExceptionManager.manageException(exception);
            }
        } else if (code == KeyEvent.VK_DOWN) {
            try {
                out.write(27);
                out.write('[');
                out.write('B');
            } catch (IOException exception) {
                ExceptionManager.manageException(exception);
            }
        } else if (code == KeyEvent.VK_LEFT) {
            try {
                out.write(27);
                out.write('[');
                out.write('D');
            } catch (IOException exception) {
                ExceptionManager.manageException(exception);
            }
        } else if (code == KeyEvent.VK_RIGHT) {
            try {
                out.write(27);
                out.write('[');
                out.write('C');
            } catch (IOException exception) {
                ExceptionManager.manageException(exception);
            }
        } else {
            if (out != null) {
                try {
                    out.write(e.getKeyChar());
                } catch (IOException exception) {
                    ExceptionManager.manageException(exception);
                }
            }
        }

        if (code != KeyEvent.VK_ESCAPE && (code != KeyEvent.VK_F4 || !e.isAltDown())) {
            e.consume();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
