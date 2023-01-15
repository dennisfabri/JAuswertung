package de.df.jauswertung.gui.plugins.timelimit;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.Timelimits;
import de.df.jauswertung.daten.TimelimitsContainer;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

class JTimelimitsEditor extends JFrame {

    private JFrame parent;

    private JList<Timelimits> entries;
    private ModifiableListModel<Timelimits> model;

    private JButton ok;
    private JButton cancel;

    private JTimelimitsPanel panel;

    private JPopupMenu menu;
    private JMenuItem add;
    private JMenuItem remove;

    private ISimpleCallback<TimelimitsContainer> callback;

    JTimelimitsEditor(JFrame parent, TimelimitsContainer timelimits, ISimpleCallback<TimelimitsContainer> callback,
            Sex female, Sex male) {
        this.parent = parent;

        FormLayout layout = new FormLayout("4dlu,fill:max(200dlu;default),4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        setTitle(I18n.get("Timelimits"));
        setIconImages(IconManager.getTitleImages());

        ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                doOk();
            }
        });
        cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                doCancel();
            }
        });

        Timelimits[] limits = timelimits.getTimelimits();
        this.callback = callback;

        model = new ModifiableListModel<>(limits);
        entries = new JList<Timelimits>(model);
        entries.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent evt) {
                showEntriesPopup(evt);
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                showEntriesPopup(evt);
            }
        });
        entries.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent evt) {
                int selected = entries.getSelectedIndex();
                int previous = selected == evt.getFirstIndex() ? evt.getLastIndex() : evt.getFirstIndex();
                selectEntry(previous, selected);
            }
        });

        panel = new JTimelimitsPanel(this, female, male);

        add(new JScrollPane(entries), CC.xy(2, 2));
        add(panel, CC.xy(4, 2));

        add(FormLayoutUtils.createButtonsPanel(ok, cancel), CC.xy(4, 4, "right,fill"));

        setMinimumSize(new Dimension(600, 400));

        pack();
        UIStateUtils.uistatemanage(this);

        initMenu();
    }

    private void doClose() {
        parent.setEnabled(true);
        JTimelimitsEditor.this.setVisible(false);
        JTimelimitsEditor.this.dispose();
    }

    private void doCancel() {
        doClose();
    }

    private void doOk() {
        if (save()) {
            doClose();
        } else {
            DialogUtils.warn(this, I18n.get("TimelimitsNotValid"), I18n.get("TimelimitsNotValid.Note"));
        }
    }

    private void initMenu() {
        add = new JMenuItem(I18n.get("Add"), IconManager.getSmallIcon("new"));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                addEntry();
            }
        });
        remove = new JMenuItem(I18n.get("Remove"), IconManager.getSmallIcon("remove"));
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                removeEntry();
            }
        });

        menu = new JPopupMenu();
        menu.add(add);
        menu.add(remove);
    }

    private void addEntry() {
        model.addLast(new Timelimits());
        entries.setSelectedIndex(model.getSize() - 1);
    }

    private void removeEntry() {
        int index = entries.getSelectedIndex();
        if (index >= 0) {
            panel.setData(null);
            model.remove(index);
        }

        if (model.getSize() >= index) {
            entries.setSelectedIndex(index);
        } else if (model.getSize() > 0) {
            entries.setSelectedIndex(model.getSize() - 1);
        }
    }

    private void selectEntry(int previous, int index) {
        panel.save();
        if (index >= 0) {
            panel.setData(model.getElementAt(index));
        } else {
            panel.setData(null);
        }
    }

    private void showEntriesPopup(MouseEvent evt) {
        if (!SwingUtilities.isRightMouseButton(evt)) {
            return;
        }

        int clicked = entries.locationToIndex(evt.getPoint());
        if (clicked != -1 && entries.getCellBounds(clicked, clicked).contains(evt.getPoint())) {
            entries.setSelectedIndex(clicked);
        }
        menu.show(entries, evt.getX(), evt.getY());
    }

    private boolean save() {
        boolean saved = panel.save();
        if (saved) {
            callback.callback(new TimelimitsContainer(model.getAllElements()));
        }
        return saved;
    }
}