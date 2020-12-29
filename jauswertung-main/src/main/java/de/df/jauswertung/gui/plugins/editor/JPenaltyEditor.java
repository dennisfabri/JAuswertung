/*
 * Created on 16.06.2007
 */
package de.df.jauswertung.gui.plugins.editor;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PENALTY;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_POINTS_CHANGED;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.PenaltyImageListCellRenderer;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IPluginManager;

public class JPenaltyEditor<T extends ASchwimmer> extends JFrame {

    private static final long       serialVersionUID = 3879690726009652858L;

    final Window                    parent;
    final T                         swimmer;
    final boolean                   printpenalties;
    final IPluginManager            controller;
    final AWettkampf<T>             wk;

    private final ArrayList<String> ids              = new ArrayList<String>();

    private final JTabbedPane       penaltytabs;

    public JPenaltyEditor(Window parent, IPluginManager controller, AWettkampf<T> wk, T swimmer, int discipline, boolean print) {
        this(parent, controller, wk, swimmer, print);
        if (discipline >= 0) {
            penaltytabs.setSelectedIndex(discipline + 1);
        } else {
            penaltytabs.setSelectedIndex(0);
        }
    }

    private JPenaltyEditor(Window parent, IPluginManager controller, AWettkampf<T> wk, T swimmer, boolean print) {
        super(I18n.get("PenaltyEditor"));
        this.controller = controller;
        this.parent = parent;
        this.swimmer = swimmer;
        this.wk = wk;
        this.printpenalties = print;

        penaltytabs = new JTabbedPane();

        boolean alwaysDisq = swimmer.getAK().isStrafeIstDisqualifikation();

        init(alwaysDisq);
        addActions();

        pack();
        WindowUtils.center(this, parent);
        UIStateUtils.uistatemanage(this, "JPenaltyEditor");
    }

    public JPenaltyEditor(Window parent, IPluginManager controller, AWettkampf<T> wk, T swimmer, String discipline, boolean print) {
        this(parent, controller, wk, swimmer, print);
        if (discipline != null) {
            for (int x = 0; x < ids.size(); x++) {
                if (ids.get(x).equals(discipline)) {
                    penaltytabs.setSelectedIndex(x + 1);
                    break;

                }
            }
        } else {
            penaltytabs.setSelectedIndex(0);
        }
    }

    private void init(boolean alwaysDisq) {
        FormLayout layout = new FormLayout("4px,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        add(createUserInfo(), CC.xy(2, 2));
        add(createPenaltyPanels(alwaysDisq), CC.xy(2, 4));
        add(createButtons(), CC.xy(2, 6));
    }

    private JPanel createUserInfo() {
        JPanel p = new JPanel();
        p.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information")));

        SimpleFormBuilder sfm = new SimpleFormBuilder(p);

        sfm.add(I18n.get("Startnumber"), StartnumberFormatManager.format(swimmer));
        sfm.add(I18n.get("Name"), swimmer.getName());
        sfm.add(I18n.get("Organisation"), swimmer.getGliederung());
        sfm.add(I18n.get("AgeGroup"), I18n.get("AgeGroupSex", swimmer.getAK().toString(), I18n.geschlechtToString(swimmer)));

        p = sfm.getPanel();

        return p;
    }

    private JTabbedPane createPenaltyPanels(boolean alwaysDisq) {
        JPanel p = new JPenaltyDisciplineEditor(ASchwimmer.DISCIPLINE_NUMBER_SELF, alwaysDisq);
        penaltytabs.addTab(p.getName(), p);
        if (wk.isHeatBased()) {
            OWDisziplin<T>[] dx = wk.getLauflisteOW().getDisziplinen();
            Arrays.sort(dx);
            for (OWDisziplin<T> d : dx) {
                if (d.Schwimmer.contains(swimmer)) {
                    p = new JPenaltyDisciplineEditor(d.Id, alwaysDisq);
                    penaltytabs.addTab(p.getName(), p);
                    ids.add(d.Id);
                }
            }
        } else {
            for (int x = 0; x < swimmer.getAK().getDiszAnzahl(); x++) {
                p = new JPenaltyDisciplineEditor(x, alwaysDisq);
                penaltytabs.addTab(p.getName(), p);
            }
        }
        return penaltytabs;
    }

    private JPanel createButtons() {
        JButton close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.setEnabled(true);
                setVisible(false);
            }
        });

        JPanel p = new JPanel(new FlowLayout(SwingConstants.RIGHT, 0, 0));
        p.add(close);
        return p;
    }

    private interface IPenaltyDisciplineEditorStrategy {

        String getName();

        ActionListener getAddActionListener();

        ActionListener getDeleteActionListener();

        ActionListener getEditActionListener();

        boolean isEnabled();

        void edit();

        void updatePenalties();

        String getId();

    }

    class JPenaltyDisciplineEditor extends JPanel {

        private class PenaltyDisciplineEditorByTime implements IPenaltyDisciplineEditorStrategy {

            private int discipline;

            public PenaltyDisciplineEditorByTime(int discipline) {
                this.discipline = discipline;
            }

            @Override
            public String getName() {
                if (discipline == ASchwimmer.DISCIPLINE_NUMBER_SELF) {
                    return I18n.get("General");
                }
                return swimmer.getAK().getDisziplin(discipline, swimmer.isMaennlich()).getName();
            }

            @Override
            public ActionListener getAddActionListener() {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JPenaltyWizard npw = new JPenaltyWizard(JPenaltyEditor.this, controller, wk, swimmer, printpenalties, false);
                        npw.setSelectedDisziplin(discipline);
                        npw.start();
                        updatePenalties();
                    }
                };
            }

            @Override
            public boolean isEnabled() {
                return (discipline < 0) || swimmer.isDisciplineChosen(discipline);
            }

            @Override
            public ActionListener getDeleteActionListener() {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int index = penalties.getSelectedIndex();
                        swimmer.removeStrafe(discipline, index);
                        updatePenalties();
                        controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY, swimmer, discipline, null);
                    }
                };
            }

            @Override
            public ActionListener getEditActionListener() {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        edit();
                    }
                };
            }

            @Override
            public void edit() {
                JPenaltyWizard npw = new JPenaltyWizard(JPenaltyEditor.this, controller, wk, swimmer, printpenalties, false);
                npw.setSelectedDisziplin(discipline);
                npw.setPenalty(penalties.getSelectedIndex());
                npw.start();
                updatePenalties();
            }

            @Override
            public void updatePenalties() {
                int index = penalties.getSelectedIndex();

                LinkedList<Strafe> strafen = swimmer.getStrafen(discipline);
                penalties.setListData(strafen.toArray(new Strafe[strafen.size()]));

                if ((index >= 0) && (strafen.size() > 0)) {
                    index = Math.min(strafen.size() - 1, index);
                    penalties.setSelectedIndex(index);
                }
            }

            @Override
            public String getId() {
                return "" + discipline;
            }
        }

        private class PenaltyDisciplineEditorByHeat implements IPenaltyDisciplineEditorStrategy {

            private final String id;
            private final String name;

            public PenaltyDisciplineEditorByHeat(String id) {
                this.id = id;
                this.name = I18n.getDisciplineName(wk, id);
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public ActionListener getAddActionListener() {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JPenaltyWizard npw = new JPenaltyWizard(JPenaltyEditor.this, controller, wk, swimmer, printpenalties, false);
                        npw.setSelectedDisziplin(id);
                        npw.start();
                        updatePenalties();
                    }
                };
            }

            @Override
            public void edit() {
                JPenaltyWizard npw = new JPenaltyWizard(JPenaltyEditor.this, controller, wk, swimmer, printpenalties, false);
                npw.setSelectedDisziplin(id);
                npw.setPenalty(penalties.getSelectedIndex());
                npw.start();
                updatePenalties();
            }

            @Override
            public ActionListener getDeleteActionListener() {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int index = penalties.getSelectedIndex();
                        swimmer.removeStrafe(id, index);
                        updatePenalties();
                        controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY, swimmer, id, null);
                    }
                };
            }

            @Override
            public ActionListener getEditActionListener() {
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        edit();
                    }
                };
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void updatePenalties() {
                int index = penalties.getSelectedIndex();

                LinkedList<Strafe> strafen = swimmer.getStrafen(id);
                penalties.setListData(strafen.toArray(new Strafe[strafen.size()]));

                if ((index >= 0) && (strafen.size() > 0)) {
                    index = Math.min(strafen.size() - 1, index);
                    penalties.setSelectedIndex(index);
                }
            }

            @Override
            public String getId() {
                return id;
            }
        }

        private static final long              serialVersionUID = 6959364812083918875L;

        final IPenaltyDisciplineEditorStrategy editor;

        final JList<Strafe>                    penalties        = new JList<Strafe>();

        final JButton                          delete           = new JButton(IconManager.getSmallIcon("delete"));
        final JButton                          edit             = new JButton(IconManager.getSmallIcon("edit"));
        private final JButton                  add              = new JButton(IconManager.getSmallIcon("new"));

        JPenaltyDisciplineEditor(int discipline, boolean alwaysDisq) {
            editor = new PenaltyDisciplineEditorByTime(discipline);
            init(alwaysDisq);
        }

        public String getId() {
            return editor.getId();
        }

        public JPenaltyDisciplineEditor(String id, boolean alwaysDisq) {
            editor = new PenaltyDisciplineEditorByHeat(id);
            init(alwaysDisq);
        }

        @SuppressWarnings("unchecked")
        private void init(boolean alwaysDisq) {
            setName(editor.getName());

            add.setToolTipText(I18n.getToolTip("NewPenalty"));
            edit.setToolTipText(I18n.getToolTip("EditPenalty"));
            delete.setToolTipText(I18n.getToolTip("DeletePenalty"));

            JToolBar tools = new JToolBar();
            tools.setFloatable(false);
            tools.add(add);
            tools.add(edit);
            tools.add(delete);

            edit.setEnabled(false);
            delete.setEnabled(false);

            if (editor.isEnabled()) {
                add.addActionListener(editor.getAddActionListener());
                delete.addActionListener(editor.getDeleteActionListener());
                edit.addActionListener(editor.getEditActionListener());

                penalties.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                penalties.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        boolean b = penalties.getSelectedIndex() >= 0;
                        edit.setEnabled(b);
                        delete.setEnabled(b);
                    }
                });
                penalties.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if ((e.getClickCount() == 2) && (!e.isPopupTrigger())) {
                            penalties.setSelectedIndex(penalties.locationToIndex(e.getPoint()));
                            editor.edit();
                        }
                    }

                    @Override
                    public void mousePressed(MouseEvent e) {
                        mouseClicked(e);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        mouseClicked(e);
                    }
                });

                if (alwaysDisq) {
                    tools.add(new JLabel(I18n.get("PenaltyInformation")));
                }
            } else {
                add.setEnabled(false);
                penalties.setEnabled(false);

                tools.add(new JLabel(I18n.get("DisciplineNotChosen")));
            }

            penalties.setCellRenderer(new PenaltyImageListCellRenderer(alwaysDisq));
            editor.updatePenalties();

            FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default,4dlu,fill:default:grow,4dlu");
            setLayout(layout);

            add(tools, CC.xy(2, 2));
            add(new JScrollPane(penalties), CC.xy(2, 4));
        }
    }

    private void addActions() {
        WindowUtils.addEscapeAction(this, new Runnable() {
            @Override
            public void run() {
                parent.setEnabled(true);
                setVisible(false);
            }
        });
        WindowUtils.addEnterAction(this, new Runnable() {
            @Override
            public void run() {
                parent.setEnabled(true);
                setVisible(false);
            }
        });
    }
}