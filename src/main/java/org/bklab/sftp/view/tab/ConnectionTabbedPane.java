package org.bklab.sftp.view.tab;

import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.core.Broderick;
import org.bklab.sftp.view.panel.ConnectionsPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Broderick
 */
public class ConnectionTabbedPane extends JTabbedPane implements MouseListener, ChangeListener {

    private static final long serialVersionUID = 2603361136910005851L;
    private List<List<GroupedTabPanel>> panelGroups;
    private Broderick frame;

    public ConnectionTabbedPane(Broderick frame) {
        super();
        this.frame = frame;
        panelGroups = new ArrayList<>();
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        addMouseListener(this);
        addChangeListener(this);
    }

    public List<List<GroupedTabPanel>> getPanelGroups() {
        return panelGroups;
    }

    public TabPanel getSelectedComponent() {
        return (TabPanel) super.getSelectedComponent();
    }

    public TabPanel getComponentAt(int index) {
        return (TabPanel) super.getComponentAt(index);
    }

    public Broderick getFrame() {
        return frame;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            if (getSelectedIndex() > 0) {
                TabPanel selected = getSelectedComponent();
                selected.close();
            }
        }
    }

    public void dispose(GroupedTabPanel selected) {
        boolean found = false;
        String uuid = selected.getNode().getUuid();
        for (int i = 1; i < getTabCount(); i++) {
            AtomicReference<TabPanel> tab = new AtomicReference<>(getComponentAt(i));
            if (tab.get().getType().equals(TabPanel.TYPE_CONNECTIONS) == false && tab.get().getNode().getUuid().equals(uuid)) {
                found = true;
                break;
            }
        }
        if (!found) {
            ((ConnectionsPanel) getComponentAt(0)).getTree().getNodeByUuid(uuid).setIcon(DataPath.IMG_16_TERMINAL_OFF, false);
        }

        List<GroupedTabPanel> group = selected.getGroup();
        group.remove(this);
        if (group.size() == 0) {
            getPanelGroups().remove(group);
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

    @Override
    public void stateChanged(ChangeEvent e) {
        getSelectedComponent().focus();
    }
}
