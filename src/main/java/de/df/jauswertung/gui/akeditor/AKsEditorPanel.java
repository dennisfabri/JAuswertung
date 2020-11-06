/*
 * Created on 13.11.2003
 */
package de.df.jauswertung.gui.akeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.blogspot.rabbithole.JSmoothList;

import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.jlist.JListUtils;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.util.DialogUtils;

/**
 * @author Dennis Mueller
 */
class AKsEditorPanel extends JPanel {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long   serialVersionUID = 3257846593079095860L;

    private JPanel              panel;
    ModifiableListModel<String> listmodel;
    JList<String>               liste;
    LinkedList<AKEditorPanel>   altersklassen;
    private int                 insertCount;
    private JMenuItem           loeschen;

    private JAKsEditor          parent           = null;
    private boolean             einzel           = false;

    private String[]            startgroups;
    private String[]            resultgroups;

    private JMenu[]             menu             = null;

    public AKsEditorPanel(JAKsEditor parent, Regelwerk currentAks, boolean einzel, boolean[] empty) {
        this.parent = parent;
        this.einzel = einzel;

        Startgruppe[] sgs = currentAks.getStartgruppen();
        startgroups = new String[sgs.length];
        for (int x = 0; x < sgs.length; x++) {
            startgroups[x] = sgs[x].getName();
        }

        Wertungsgruppe[] wgs = currentAks.getWertungsgruppen();
        resultgroups = new String[wgs.length];
        for (int x = 0; x < wgs.length; x++) {
            resultgroups[x] = wgs[x].getName();
        }

        insertCount = 1;

        altersklassen = new LinkedList<AKEditorPanel>();
        listmodel = new ModifiableListModel<String>() {

            private static final long serialVersionUID = -3302798347968933015L;

            @Override
            public void move(int from, int to) {
                super.move(from, to);
                AKEditorPanel ak = altersklassen.remove(from);
                altersklassen.add(to, ak);
                notifyChange();
            }
        };
        liste = new JSmoothList<String>(listmodel);
        JListUtils.setAlternatingListCellRenderer(liste);

        panel = new JPanel(new BorderLayout());

        JScrollPane scr = new JScrollPane(liste);
        scr.setBorder(new ShadowBorder());

        setBorder(BorderUtils.createSpaceBorder());
        setLayout(new BorderLayout(5, 5));
        add(scr, BorderLayout.WEST);
        add(panel, BorderLayout.CENTER);

        for (int x = 0; x < currentAks.size(); x++) {
            AKEditorPanel ake = new AKEditorPanel(this, currentAks.getAk(x), einzel, ((empty == null) || empty[x] ? -1 : x), startgroups, resultgroups);
            altersklassen.addLast(ake);
            listmodel.addLast(ake.getAKName());
        }

        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        liste.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                fireSelectionChanged();
            }
        });
        liste.addMouseListener(new MouseAdapter() {
            private AKPopup popup = new AKPopup();

            @Override
            public void mousePressed(MouseEvent me) {
                if (me.isPopupTrigger()) {
                    liste.setSelectedIndex(liste.locationToIndex(me.getPoint()));
                    popup.show(liste, me.getX(), me.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                mousePressed(me);
            }
        });
        liste.setPreferredSize(new Dimension(Math.max(liste.getPreferredSize().width, 100), 50));
        liste.setSelectedIndex(0);
    }

    public void getAKs(Regelwerk currentAks) {
        currentAks.setSize(altersklassen.size());
        for (int x = 0; x < altersklassen.size(); x++) {
            AKEditorPanel akepanel = altersklassen.get(x);
            currentAks.setAk(x, akepanel.getAK());
        }
    }

    public int[] getIndizes() {
        int[] result = new int[altersklassen.size()];
        for (int x = 0; x < altersklassen.size(); x++) {
            AKEditorPanel akepanel = altersklassen.get(x);
            result[x] = akepanel.getIndex();
        }
        return result;
    }

    public int getAKCount() {
        return altersklassen.size();
    }

    private JMenu[] initMenues() {
        JMenu bearbeiten = new JMenu(I18n.get("Edit"));
        loeschen = new JMenuItem(I18n.get("DeleteAG"));
        loeschen.setIcon(IconManager.getSmallIcon("delete"));
        loeschen.setEnabled(listmodel.size() > 1);
        loeschen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doDelete();
            }
        });
        JMenuItem einfuegen = new JMenuItem(I18n.get("InsertAG"));
        einfuegen.setIcon(IconManager.getSmallIcon("new"));
        einfuegen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doInsert();
            }
        });
        bearbeiten.add(loeschen);
        bearbeiten.add(einfuegen);
        return new JMenu[] { bearbeiten };
    }

    public JMenu[] getMenues() {
        if (menu == null) {
            menu = initMenues();
        }
        return menu;
    }

    void doDelete() {
        if (altersklassen.size() <= 2) {
            if (loeschen != null) {
                loeschen.setEnabled(false);
            }
        }
        if (altersklassen.size() <= 1) {
            return;
        }
        if (altersklassen.get(liste.getSelectedIndex()).getIndex() >= 0) {
            DialogUtils.wichtigeMeldung(parent, I18n.get("CannotDeleteBecauseAGNotEmpty"));
            return;
        }
        altersklassen.remove(liste.getSelectedIndex());
        listmodel.remove(listmodel.size() - 1);
        for (int x = liste.getSelectedIndex(); x < altersklassen.size(); x++) {
            listmodel.setValueAt(x, altersklassen.get(x).getAKName());
        }
        fireSelectionChanged();
        notifyChange();
    }

    void doInsert() {
        int index = liste.getSelectedIndex();
        Altersklasse ak = altersklassen.get(index).getAK();
        ak.setName(I18n.get("NameOfCopy", ak.getName(), insertCount));
        AKEditorPanel editor = new AKEditorPanel(this, ak, einzel, -1, startgroups, resultgroups);
        altersklassen.add(liste.getSelectedIndex() + 1, editor);
        listmodel.addLast("");
        for (int x = index; x < altersklassen.size(); x++) {
            listmodel.setValueAt(x, altersklassen.get(x).getAKName());
        }
        if (loeschen != null) {
            loeschen.setEnabled(true);
        }
        insertCount++;
        notifyChange();
    }

    void notifyChange() {
        parent.notifyChange();
    }

    void moveUp() {
        int x = liste.getSelectedIndex();
        AKEditorPanel ak1 = altersklassen.get(x);
        AKEditorPanel ak2 = altersklassen.get(x - 1);

        altersklassen.set(x - 1, ak1);
        altersklassen.set(x, ak2);

        listmodel.setValueAt(x - 1, ak1.getAKName());
        listmodel.setValueAt(x, ak2.getAKName());

        liste.setSelectedIndex(x - 1);
        notifyChange();
    }

    void moveDown() {
        int x = liste.getSelectedIndex();
        AKEditorPanel ak1 = altersklassen.get(x);
        AKEditorPanel ak2 = altersklassen.get(x + 1);

        altersklassen.set(x + 1, ak1);
        altersklassen.set(x, ak2);

        listmodel.setValueAt(x + 1, ak1.getAKName());
        listmodel.setValueAt(x, ak2.getAKName());

        liste.setSelectedIndex(x + 1);
        notifyChange();
    }

    void nameChanged() {
        int x = liste.getSelectedIndex();
        String name = altersklassen.get(x).getAKName();
        if (name.length() == 0) {
            name = I18n.get("NoName");
        }
        listmodel.setValueAt(x, name);
        notifyChange();
    }

    void fireSelectionChanged() {
        int x = liste.getSelectedIndex();
        if (x < 0) {
            liste.setSelectedIndex(0);
            return;
        }
        panel.removeAll();
        panel.add(altersklassen.get(x), BorderLayout.CENTER);
        panel.updateUI();

        if (loeschen != null) {
            boolean enable = (x != 0) && (getAKCount() > 1);
            loeschen.setEnabled(enable);
        }
    }

    private class AKPopup extends JPopupMenu {

        private static final long serialVersionUID = -3233163270848511987L;

        private final JMenuItem   remove;
        private final JMenuItem   add;
        private final JMenuItem   up;
        private final JMenuItem   down;

        public AKPopup() {
            remove = new JMenuItem(I18n.get("DeleteAG"), IconManager.getSmallIcon("delete"));
            add = new JMenuItem(I18n.get("InsertAG"), IconManager.getSmallIcon("new"));
            up = new JMenuItem(I18n.get("Up"), IconManager.getSmallIcon("up"));
            down = new JMenuItem(I18n.get("Down"), IconManager.getSmallIcon("down"));

            add(remove);
            add(add);
            add(new JSeparator());
            add(up);
            add(down);

            remove.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doDelete();
                }
            });
            add.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doInsert();
                }
            });
            up.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveUp();
                }
            });
            down.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveDown();
                }
            });
        }

        @Override
        public void setVisible(boolean arg0) {
            remove.setEnabled(true);
            add.setEnabled(true);
            up.setEnabled(true);
            down.setEnabled(true);

            int index = liste.getSelectedIndex();
            if (index == 0) {
                up.setEnabled(false);
            }
            if (index >= listmodel.size() - 1) {
                down.setEnabled(false);
            }
            if (listmodel.size() <= 1) {
                remove.setEnabled(false);
            }
            super.setVisible(arg0);
        }
    }

    public void updateStartgroups(String[] sgroups) {
        this.startgroups = sgroups;
        for (AKEditorPanel ake : altersklassen) {
            ake.updateStartgroups(sgroups);
        }
    }

    public void updateResultgroups(String[] rgroups) {
        this.resultgroups = rgroups;
        for (AKEditorPanel ake : altersklassen) {
            ake.updateResultgroups(rgroups);
        }
    }

}