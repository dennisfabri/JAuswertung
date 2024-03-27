package de.df.jauswertung.gui.akeditor;

import java.awt.Toolkit;
import java.util.Comparator;
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

import de.df.jauswertung.daten.laufliste.Reihenfolge;
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

import static de.df.jauswertung.daten.laufliste.Reihenfolge.fromValue;
import static java.util.Arrays.stream;

public class Startgroupspanel extends JPanel {

    private final ModifiableListModel<Startgruppe> model = new ModifiableListModel<>();
    private final JList<Startgruppe> liste = new JList<>(model);
    private final JAKsEditor parent;
    private final JButton add = new JTransparentButton(IconManager.getSmallIcon("new"));
    private final JButton edit = new JTransparentButton(IconManager.getSmallIcon("edit"));
    private final JButton delete = new JTransparentButton(IconManager.getSmallIcon("delete"));

    private boolean updating = false;

    public Startgroupspanel(JAKsEditor editor) {
        parent = editor;
        setBorder(BorderUtils.createLabeledBorder(I18n.get("Startgroups")));

        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JListUtils.setAlternatingListCellRenderer(liste);

        add.addActionListener(e -> {
            add();
        });
        edit.addActionListener(e -> {
            edit();
        });
        delete.addActionListener(e -> {
            delete();
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

    private static class Editor extends JDialog {

        private Startgruppe result = null;

        private final JWarningTextField text = new JWarningTextField(true, false);
        private final JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        private final JComboBox<String> laufsortierung;
        private final JCheckBox laufrotation;

        public Editor(JFrame parent, Startgruppe initial) {
            super(parent, I18n.get("Startgroup"), true);

            laufsortierung = new JComboBox<>(
                    stream(Reihenfolge.values()).sorted(Comparator.comparingInt(Reihenfolge::getValue))
                            .filter(r -> !r.equals(Reihenfolge.Regelwerk)).map(r -> I18n.get("Sorting." + r.name()))
                            .toArray(String[]::new));

            laufsortierung.setSelectedIndex(Reihenfolge.Meldezeiten.getValue());
            laufsortierung.setToolTipText(I18n.getToolTip("Laufsortierung"));
            laufsortierung.addItemListener(e -> {
                updateLaufrotation();
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
            JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
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

            ok.addActionListener(e -> {
                doOk();
            });
            cancel.addActionListener(e -> {
                doCancel();
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

            WindowUtils.addEnterAction(this, this::doOk);
            WindowUtils.addEscapeAction(this, this::doCancel);

            checkOk();
            pack();
            WindowUtils.center(this);
        }

        void updateLaufrotation() {
            laufrotation.setEnabled(fromValue(laufsortierung.getSelectedIndex()).isRotatable());
        }

        void checkOk() {
            ok.setEnabled(!text.getText().isEmpty());
        }

        void doOk() {
            if (!ok.isEnabled()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            result = new Startgruppe(text.getText());
            result.setLaufrotation(laufrotation.isSelected() && laufrotation.isEnabled());
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
        aks.setStartgruppen(result.toArray(Startgruppe[]::new));
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
