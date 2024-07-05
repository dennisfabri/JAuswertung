package de.df.jauswertung.gui.akeditor;

import java.awt.Toolkit;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.ResultgroupsComparator;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.jlist.JListUtils;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.WindowUtils;

public class ResultgroupsPanel extends JPanel {

    private ModifiableListModel<Wertungsgruppe> model = new ModifiableListModel<>();
    private JList<Wertungsgruppe> liste = new JList<>(model);

    private JAKsEditor parent;

    private JButton add = new JTransparentButton(IconManager.getSmallIcon("new"));
    private JButton edit = new JTransparentButton(IconManager.getSmallIcon("edit"));
    private JButton delete = new JTransparentButton(IconManager.getSmallIcon("delete"));

    private boolean updating = false;

    public ResultgroupsPanel(JAKsEditor editor) {
        parent = editor;
        setBorder(BorderUtils.createLabeledBorder(I18n.get("Resultgroups")));
        // setBorder(null);

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

        private Wertungsgruppe result = null;

        private JCheckBox disziplin = new JCheckBox();
        private JCheckBox mehrkampf = new JCheckBox();
        private JCheckBox requiresZW = new JCheckBox();
        private JCheckBox penaltyIsDisq = new JCheckBox();
        private JWarningTextField text = new JWarningTextField(true, false);
        private JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        private JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));

        public Editor(JFrame parent, Wertungsgruppe initial) {
            super(parent, I18n.get("Resultgroup"), true);

            disziplin.addChangeListener(e -> {
                updateDisziplin();
            });
            requiresZW.setEnabled(false);

            if (initial != null) {
                text.setText(initial.getName());
                disziplin.setSelected(initial.isProtokollMitEinzelwertung());
                mehrkampf.setSelected(initial.isProtokollMitMehrkampfwertung());
                penaltyIsDisq.setSelected(initial.isStrafeIstDisqualifikation());
                requiresZW.setSelected(initial.isEinzelwertungHlw());
            }

            text.setColumns(24);

            FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                    FormLayoutUtils.createLayoutString(6));
            setLayout(layout);

            JPanel p = new JPanel(
                    new FormLayout("0dlu:grow,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu"));
            p.add(ok, CC.xy(2, 2));
            p.add(cancel, CC.xy(4, 2));

            add(new JLabel(I18n.get("Name")), CC.xy(2, 2));
            add(text, CC.xy(4, 2));
            add(new JLabel(I18n.get("Mehrkampf")), CC.xy(2, 4));
            add(mehrkampf, CC.xy(4, 4));
            add(new JLabel(I18n.get("Einzelwertung")), CC.xy(2, 6));
            add(disziplin, CC.xy(4, 6));
            add(new JLabel(I18n.get("EinzelwertungZW")), CC.xy(2, 8));
            add(requiresZW, CC.xy(4, 8));
            add(new JLabel(I18n.get("PenaltyIsDisqualification")), CC.xy(2, 10));
            add(penaltyIsDisq, CC.xy(4, 10));
            add(p, CC.xyw(2, 12, 3));

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

        void updateDisziplin() {
            requiresZW.setEnabled(disziplin.isSelected());
        }

        void checkOk() {
            ok.setEnabled(text.getText().length() > 0);
        }

        void doOk() {
            if (!ok.isEnabled()) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            result = new Wertungsgruppe(text.getText());
            result.setProtokollMitMehrkampfwertung(mehrkampf.isSelected());
            result.setProtokollMitEinzelwertung(disziplin.isSelected());
            result.setEinzelwertungHlw(requiresZW.isSelected() && disziplin.isSelected());
            result.setStrafeIstDisqualifikation(penaltyIsDisq.isSelected());
            setVisible(false);
        }

        void doCancel() {
            result = null;
            setVisible(false);
        }

        public Wertungsgruppe getValue() {
            return result;
        }
    }

    void add() {
        Editor editor = new Editor(parent, null);
        editor.setVisible(true);
        Wertungsgruppe value = editor.getValue();
        if (value != null) {
            model.addLast(value);
            model.sort(new ResultgroupsComparator());
            if (liste.isSelectionEmpty()) {
                liste.setSelectedIndex(0);
            }
            notifyResultgroupsChange();
            notifyChange();
        }
    }

    void edit() {
        if (liste.isSelectionEmpty()) {
            return;
        }
        int index = liste.getSelectedIndex();
        Editor editor = new Editor(parent, liste.getSelectedValue());
        editor.setVisible(true);
        Wertungsgruppe value = editor.getValue();
        if (value != null) {
            model.setValueAt(index, value);
            model.sort(new ResultgroupsComparator());
            notifyResultgroupsChange();
            notifyChange();
        }
    }

    void delete() {
        if (liste.isSelectionEmpty()) {
            return;
        }
        model.remove(liste.getSelectedIndex());
        notifyResultgroupsChange();
        notifyChange();
    }

    private void notifyResultgroupsChange() {
        LinkedList<Wertungsgruppe> wgs = model.getAllElements();
        String[] s = new String[wgs.size()];
        int pos = 0;
        for (Wertungsgruppe wg : wgs) {
            s[pos] = wg.getName();
            pos++;
        }
        parent.updateResultgroups(s);
    }

    private void notifyChange() {
        if (updating) {
            return;
        }
        parent.notifyChange();
    }

    public void getSettings(Regelwerk aks) {
        aks.setWertungsgruppen(model.getAllElements().toArray(new Wertungsgruppe[0]));
    }

    public void setSettings(Regelwerk aks) {
        model.clear();
        model.addAll(aks.getWertungsgruppen());
        liste.setModel(model);
    }

}
