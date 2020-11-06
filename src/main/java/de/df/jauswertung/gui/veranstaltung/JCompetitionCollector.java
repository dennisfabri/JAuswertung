package de.df.jauswertung.gui.veranstaltung;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.blogspot.rabbithole.JSmoothList;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.veranstaltung.CompetitionContainer;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.InputManager;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.filefilter.SimpleFileFilter;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.FileChooserUtils;
import de.df.jutils.gui.util.WindowUtils;

class JCompetitionCollector extends JPanel {

    static final SimpleFileFilter             wkff = new SimpleFileFilter(I18n.get("Competition"), "wk");

    JSmoothList<CompetitionContainer>         competitions;
    ModifiableListModel<CompetitionContainer> data;

    private JTransparentButton                add;
    private JTransparentButton                edit;
    private JTransparentButton                remove;
    private JTransparentButton                check;

    JFrame                                    parent;

    public JCompetitionCollector(JFrame parent) {
        this.parent = parent;

        setBorder(BorderUtils.createLabeledBorder(I18n.get("Competitions")));
        data = new ModifiableListModel<CompetitionContainer>();
        competitions = new JSmoothList<CompetitionContainer>(data);
        competitions.setCellRenderer(new CompetitionContainerRenderer());
        competitions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(competitions);
        scroll.setBorder(new ShadowBorder());

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default:grow,4dlu");
        setLayout(layout);

        add(getButtons(), CC.xy(2, 2));
        add(scroll, CC.xy(2, 4));
    }

    private JPanel getButtons() {
        add = new JTransparentButton(IconManager.getSmallIcon("new"));
        add.setToolTipText(I18n.getToolTip("AddCompetition"));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCompetition();
            }
        });
        edit = new JTransparentButton(IconManager.getSmallIcon("edit"));
        edit.setToolTipText(I18n.getToolTip("Edit"));
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editCompetition();
            }
        });
        remove = new JTransparentButton(IconManager.getSmallIcon("delete"));
        remove.setToolTipText(I18n.getToolTip("DeleteCompetition"));
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCompetition();
            }
        });
        check = new JTransparentButton(IconManager.getSmallIcon("check"));
        check.setToolTipText(I18n.getToolTip("CheckCompetitionsForSimilarOrganizations"));
        check.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                check();
            }
        });

        FormLayout buttonslayout = new FormLayout("0dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
        JPanel buttons = new JPanel(buttonslayout);

        buttons.add(add, CC.xy(2, 2));
        buttons.add(edit, CC.xy(4, 2));
        buttons.add(remove, CC.xy(6, 2));
        buttons.add(check, CC.xy(8, 2));

        return buttons;
    }

    public void setProperties(Veranstaltung vs) {
        setCompetitions(vs.getCompetitions());
    }

    public void getProperties(Veranstaltung vs) {
        vs.setCompetitions(getCompetitions());
    }

    public void setCompetitions(LinkedList<CompetitionContainer> container) {
        data.removeAll();
        data.addAll(container);
    }

    public LinkedList<CompetitionContainer> getCompetitions() {
        return data.getAllElements();
    }

    private static final class CompetitionContainerRenderer extends DefaultListCellRenderer {

        private JPanel panel;
        private JLabel name;
        private JLabel file;
        private JLabel image;

        public CompetitionContainerRenderer() {
            name = new JLabel();
            file = new JLabel();
            image = new JLabel();

            panel = new JPanel(new FormLayout("1dlu,fill:default,1dlu,fill:default:grow", "1dlu,fill:default,1dlu,fill:default,1dlu"));
            panel.add(image, CC.xywh(2, 2, 1, 3, "fill,fill"));
            panel.add(name, CC.xy(4, 2));
            panel.add(file, CC.xy(4, 4));
            panel.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!(value instanceof CompetitionContainer)) {
                return c;
            }

            panel.setBackground(c.getBackground());
            panel.setForeground(c.getForeground());
            panel.setFont(c.getFont());
            name.setBackground(c.getBackground());
            name.setForeground(c.getForeground());
            name.setFont(c.getFont());
            file.setBackground(c.getBackground());
            file.setForeground(c.getForeground());
            file.setFont(c.getFont());
            image.setBackground(c.getBackground());
            image.setForeground(c.getForeground());
            image.setFont(c.getFont());

            CompetitionContainer cc = (CompetitionContainer) value;
            name.setText(cc.getName().length() == 0 ? " " : cc.getName());
            String fn = cc.getFilename();
            if (fn.length() == 0) {
                fn = " ";
            } else {
                File f = new File(fn);
                fn = f.getName();
                try {
                    fn = f.getCanonicalPath();
                } catch (IOException e) {
                    // Nothing to do
                }
                if (fn.lastIndexOf(File.separator) >= 0) {
                    fn = fn.substring(fn.lastIndexOf(File.separator) + 1);
                }
                if (fn.lastIndexOf("/") >= 0) {
                    fn = fn.substring(fn.lastIndexOf("/") + 1);
                }
            }
            file.setText(fn);
            image.setIcon(cc.exists() ? IconManager.getBigIcon("ok") : IconManager.getBigIcon("warn"));
            return panel;
        }
    }

    void addCompetition() {
        data.addLast(new CompetitionContainer("", ""));
        editiere(data.getSize() - 1);
        updateButtons();
    }

    void editCompetition() {
        if (competitions.getSelectedIndex() >= 0) {
            editiere(competitions.getSelectedIndex());
        }
        updateButtons();
    }

    void deleteCompetition() {
        if (competitions.getSelectedIndex() >= 0) {
            data.remove(competitions.getSelectedIndex());
        }
        updateButtons();
    }

    private void updateButtons() {
        remove.setEnabled(competitions.getModel().getSize() > 0);
        edit.setEnabled(competitions.getModel().getSize() > 0);
        add.setEnabled(competitions.getModel().getSize() < 8);
        check.setEnabled(competitions.getModel().getSize() > 1);
    }

    void editiere(int index) {
        new JCompetitionEditor(index).setVisible(true);
    }

    @SuppressWarnings({ "cast", "rawtypes" })
    void check() {
        LinkedList<CompetitionContainer> containers = getCompetitions();
        LinkedList<AWettkampf> wks = new LinkedList<AWettkampf>();
        LinkedList<String> names = new LinkedList<String>();
        for (CompetitionContainer c : containers) {
            AWettkampf wk = InputManager.ladeWettkampf(c.getFilename());
            if (wk != null) {
                wks.addLast(wk);
                names.addLast(c.getName());
            }
        }
        new JSimilarityCheck(parent, (AWettkampf[]) wks.toArray(new AWettkampf[wks.size()]), (String[]) names.toArray(new String[names.size()]))
                .setVisible(true);
    }

    private class JCompetitionEditor extends JDialog {

        private JTextField                 name = new JWarningTextField();
        private JTextField                 file = new JWarningTextField();
        private final CompetitionContainer container;

        public JCompetitionEditor(int index) {
            super(parent, I18n.get("Edit"), true);

            container = data.getElementAt(index);
            name.setText(container.getName());
            file.setText(container.getFilename());

            JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ok();
                }
            });
            JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancel();
                }
            });

            JButton browse = new JButton("...");
            browse.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    browse();
                }
            });

            FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                    "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu");
            setLayout(layout);

            add(new JLabel(I18n.get("Name")), CC.xy(2, 2));
            add(name, CC.xyw(4, 2, 3));
            add(new JLabel(I18n.get("File")), CC.xy(2, 4));
            add(file, CC.xy(4, 4));
            add(browse, CC.xy(6, 4));

            FormLayout layout2 = new FormLayout("0dlu:grow,fill:default,4dlu,fill:default,0dlu", "0dlu,fill:default,0dlu");
            JPanel buttons = new JPanel(layout2);
            buttons.add(ok, CC.xy(2, 2));
            buttons.add(cancel, CC.xy(4, 2));

            add(buttons, CC.xyw(2, 6, 5));

            pack();
            WindowUtils.center(this, parent);
        }

        void ok() {
            container.setName(name.getText());
            container.setFilename(file.getText());
            EDTUtils.repaintLater(competitions);
            setVisible(false);
        }

        void cancel() {
            setVisible(false);
        }

        void browse() {
            String fn = FileChooserUtils.chooseFile(I18n.get("Open"), I18n.get("Open"), wkff, parent);
            if (fn != null) {
                file.setText(fn);
            }
        }
    }
}