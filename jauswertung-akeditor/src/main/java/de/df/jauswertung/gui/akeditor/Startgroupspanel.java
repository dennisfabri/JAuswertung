package de.df.jauswertung.gui.akeditor;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.StartgroupsComparator;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.jlist.JListUtils;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.WindowUtils;

public class Startgroupspanel extends JPanel {

    private ModifiableListModel<Startgruppe> model = new ModifiableListModel<Startgruppe>();
    private JList<Startgruppe> liste = new JList<Startgruppe>(model);

    private JAKsEditor parent;

    private JButton add = new JTransparentButton(IconManager.getSmallIcon("new"));
    private JButton edit = new JTransparentButton(IconManager.getSmallIcon("edit"));
    private JButton delete = new JTransparentButton(IconManager.getSmallIcon("delete"));

    private boolean updating = false;

    public Startgroupspanel(JAKsEditor editor) {
        parent = editor;
        setBorder(BorderUtils.createLabeledBorder(I18n.get("Startgroups")));

        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JListUtils.setAlternatingListCellRenderer(liste);

        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                add();
            }
        });
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete();
            }
        });

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,0dlu,0dlu:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu");
        setLayout(layout);

        JScrollPane scr = new JScrollPane(liste);
        scr.setBorder(new ShadowBorder());

        add(add, CC.xy(2, 2));
        add(edit, CC.xy(4, 2));
        add(delete, CC.xy(6, 2));
        add(scr, CC.xyw(2, 4, 7));
    }

    private class Editor extends JDialog {

        private Startgruppe result = null;

        private JWarningTextField text = new JWarningTextField(true, false);
        private JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        private JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        private JComboBox<String> laufsortierung;
        private JCheckBox laufrotation;

        public Editor(JFrame parent, Startgruppe initial) {
            super(parent, I18n.get("Startgroup"), true);

            laufsortierung = new JComboBox<String>(
                    new String[] { I18n.get("Randomly"), I18n.get("SameOrganisationSameHeat"),
                            I18n.get("SameOrganisationDifferentHeats"), I18n.get("SortByAnouncedPoints"),
                            I18n.get("SortByAnouncedTimes") });
            laufsortierung.setSelectedIndex(4);
            laufsortierung.setToolTipText(I18n.getToolTip("Laufsortierung"));
            laufsortierung.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    updateLaufrotation();
                }
            });

            laufrotation = new JCheckBox();
            laufrotation.setSelected(true);
            laufrotation.setToolTipText(I18n.getToolTip("Laufrotation"));

            text.setColumns(24);
            updateLaufrotation();

            if (initial != null) {
                text.setText(initial.getName());
                laufsortierung.setSelectedIndex(Math.max(0, initial.getLaufsortierung()));
                laufrotation.setSelected(initial.hasLaufrotation());
            }

            JPanel p = new JPanel(
                    new FormLayout("0dlu:grow,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu"));
            p.add(ok, CC.xy(2, 2));
            p.add(cancel, CC.xy(4, 2));

            FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                    FormLayoutUtils.createLayoutString(4));
            setLayout(layout);

            add(new JLabel(I18n.get("Name")), CC.xy(2, 2));
            add(text, CC.xy(4, 2));
            add(new JLabel(I18n.get("SortingOfSwimmers")), CC.xy(2, 4));
            add(laufsortierung, CC.xy(4, 4));
            add(new JLabel(I18n.get("RotateLanes")), CC.xy(2, 6));
            add(laufrotation, CC.xy(4, 6));
            add(p, CC.xyw(2, 8, 3));

            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doOk();
                }
            });
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doCancel();
                }
            });
            text.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void removeUpdate(DocumentEvent e) {
                    checkOk();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkOk();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    checkOk();
                }
            });

            WindowUtils.addEnterAction(this, new Runnable() {
                @Override
                public void run() {
                    doOk();
                }
            });
            WindowUtils.addEscapeAction(this, new Runnable() {
                @Override
                public void run() {
                    doCancel();
                }
            });

            checkOk();
            pack();
            WindowUtils.center(this);
        }

        void updateLaufrotation() {
            laufrotation.setEnabled(laufsortierung.getSelectedIndex() != 4);
        }

        void checkOk() {
            ok.setEnabled(text.getText().length() > 0);
        }

        void doOk() {
            if (!ok.isEnabled()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            result = new Startgruppe(text.getText());
            result.setLaufrotation(laufrotation.isSelected());
            result.setLaufsortierung(laufsortierung.getSelectedIndex());
            setVisible(false);
        }

        void doCancel() {
            result = null;
            setVisible(false);
        }

        public Startgruppe getValue() {
            return result;
        }
    }

    void add() {
        Editor editor = new Editor(parent, null);
        editor.setVisible(true);
        Startgruppe value = editor.getValue();
        if (value != null) {
            model.addLast(value);
            model.sort(new StartgroupsComparator());
            if (liste.isSelectionEmpty()) {
                liste.setSelectedIndex(0);
            }
            notifyStartgroupsChange();
        }
    }

    void edit() {
        if (liste.isSelectionEmpty()) {
            return;
        }
        int index = liste.getSelectedIndex();
        Editor editor = new Editor(parent, liste.getSelectedValue());
        editor.setVisible(true);
        Startgruppe value = editor.getValue();
        if (value != null) {
            model.setValueAt(index, value);
            model.sort(new StartgroupsComparator());
            notifyStartgroupsChange();
        }
    }

    private void notifyStartgroupsChange() {
        LinkedList<Startgruppe> wgs = model.getAllElements();
        String[] s = new String[wgs.size()];
        int pos = 0;
        for (Startgruppe wg : wgs) {
            s[pos] = wg.getName();
            pos++;
        }
        parent.updateStartgroups(s);
        notifyChange();
    }

    void delete() {
        if (liste.isSelectionEmpty()) {
            return;
        }
        model.remove(liste.getSelectedIndex());
        notifyStartgroupsChange();
    }

    private void notifyChange() {
        if (updating) {
            return;
        }
        parent.notifyChange();
    }

    public void getSettings(Regelwerk aks) {
        LinkedList<Startgruppe> result = model.getAllElements();
        aks.setStartgruppen(result.toArray(new Startgruppe[result.size()]));
    }

    public void setSettings(Regelwerk aks) {
        updating = true;
        model.clear();
        model.addAll(aks.getStartgruppen());
        if (model.size() > 0) {
            liste.setSelectedIndex(0);
        }
        updating = false;
    }
}
