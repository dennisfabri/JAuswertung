/*
 * Created on 02.05.2005
 */
package de.df.jauswertung.gui.plugins.zielrichterentscheid;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.ZielrichterentscheidUtils;
import de.df.jauswertung.util.vergleicher.ZielrichterentscheidVergleicher;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.window.JOptionsFrame;

class JZielrichterentscheidFrame<T extends ASchwimmer> extends JOptionsFrame {

    private static final long serialVersionUID = 4051325639070265655L;

    private final JFrame parent;

    JList liste;
    ModifiableListModel<Zielrichterentscheid<T>> model;
    JZielrichterentscheidPanel<T> editor;
    JButton add;
    JButton remove;

    LinkedList<Zielrichterentscheid<T>> possible;

    AWettkampf<T> wk;

    public JZielrichterentscheidFrame(JFrame parent) {
        super(parent, I18n.get("Zielrichterentscheid"), IconManager.getIconBundle());
        this.parent = parent;

        setIconImages(IconManager.getTitleImages());

        possible = new LinkedList<>();

        addOptionsListener(new OptionsListener() {
            @Override
            public void apply() {
                JZielrichterentscheidFrame.this.apply();
            }

            @Override
            public void cancel() {
                update(wk);
            }
        });

        initGUI();
        pack();
        setSize(Math.max(getWidth(), 500), Math.max(getHeight(), 400));
        UIStateUtils.uistatemanage(parent, this, "JZielrichterentscheidDialog");
    }

    private void initGUI() {
        model = new ModifiableListModel<>();
        liste = new JList(model);
        editor = new JZielrichterentscheidPanel<>(this);

        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        liste.setCellRenderer(new ZielrichterentscheidListCellRenderer());

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu");

        JPanel panel = new JPanel(layout);

        JScrollPane scr = new JScrollPane(liste);
        scr.setBorder(BorderUtils.createLabeledBorder(I18n.get("Zielrichterentscheide")));
        scr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(getButtons(), CC.xyw(2, 2, 3, "fill,fill"));
        panel.add(scr, CC.xy(2, 4));
        panel.add(editor, CC.xy(4, 4));

        liste.addListSelectionListener(e -> {
            if (liste.getSelectedIndex() < 0) {
                if (model.getSize() > 0) {
                    liste.setSelectedIndex(0);
                }
            } else {
                editor.setEntscheid((Zielrichterentscheid<T>) liste.getSelectedValue());
            }
            remove.setEnabled(liste.getSelectedIndex() >= 0);
        });

        setContent(panel);
    }

    private JPanel getButtons() {
        add = new JTransparentButton(IconManager.getSmallIcon("new"));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Zielrichterentscheid<T> ze = JZESelectionDialog.getZielrichterentscheid(JZielrichterentscheidFrame.this,
                        possible);
                if (ze != null) {
                    possible.remove(ze);
                    model.add(ze, 0);
                    model.sort(new ZielrichterentscheidVergleicher<T>());
                    liste.setSelectedValue(ze, true);
                    editor.setEntscheid(ze);

                    add.setEnabled(!possible.isEmpty());

                    setChanged(true);
                }
            }
        });
        remove = new JTransparentButton(IconManager.getSmallIcon("remove"));
        remove.addActionListener(e -> {
            int index = liste.getSelectedIndex();
            if (index >= 0) {
                Zielrichterentscheid<T> ze = model.remove(index);
                possible.addLast(ze);
                Collections.sort(possible, new ZielrichterentscheidVergleicher<T>());

                add.setEnabled(!possible.isEmpty());
                setChanged(true);
            }
            if (model.getSize() <= index) {
                index = model.getSize() - 1;
            }
            if (index >= 0) {
                liste.setSelectedIndex(index);
                editor.setEntscheid((Zielrichterentscheid<T>) liste.getSelectedValue());
            } else {
                remove.setEnabled(false);
                editor.setEntscheid(null);
            }
        });

        add.setEnabled(!possible.isEmpty());
        remove.setEnabled((model.getSize() > 0) && (liste.getSelectedIndex() >= 0));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.add(add);
        p.add(remove);
        return p;
    }

    @Override
    public void setVisible(boolean visible) {
        if (parent != null) {
            parent.setEnabled(!visible);
        }
        super.setVisible(visible);
    }

    public void start(AWettkampf<T> w) {
        update(w);
        ModalFrameUtil.showAsModal(this, parent);
    }

    public void apply() {
        LinkedList<Zielrichterentscheid<T>> zes = new LinkedList<>();
        for (int x = 0; x < model.getSize(); x++) {
            zes.addLast(model.getElementAt(x));
        }
        wk.setZielrichterentscheide(ZielrichterentscheidUtils.createCopy(zes));
    }

    @Override
    public synchronized void setChanged(boolean changed) {
        super.setChanged(changed);
        if (liste != null) {
            liste.repaint();
        }
    }

    @SuppressWarnings("rawtypes")
    public void update(AWettkampf<T> w) {
        this.wk = w;

        possible = ZielrichterentscheidUtils.generateZielrichterentscheide(w);
        if (possible == null) {
            possible = new LinkedList<>();
        }

        LinkedList<Zielrichterentscheid<T>> copy = ZielrichterentscheidUtils.createCopy(wk.getZielrichterentscheide());

        for (Zielrichterentscheid ze : copy) {
            possible.remove(ze);
        }

        add.setEnabled(!possible.isEmpty());

        model.removeAll();
        model.addAll(copy);
        if (model.getSize() > 0) {
            liste.setSelectedIndex(0);
        } else {
            editor.setEntscheid(null);
        }

        setChanged(false);
    }
}