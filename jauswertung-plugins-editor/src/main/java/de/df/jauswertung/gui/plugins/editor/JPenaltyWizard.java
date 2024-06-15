/*
 * Created on 01.02.2005
 */
package de.df.jauswertung.gui.plugins.editor;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PENALTY;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_POINTS_CHANGED;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.regelwerk.Strafarten;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.gui.penalties.PenaltyUIUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.PenaltyShortTextListRenderer;
import de.df.jauswertung.gui.util.WizardUIElementsProvider;
import de.df.jauswertung.print.StrafpunktePrintable;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.wizard.AWizardPage;
import de.df.jutils.gui.wizard.CancelListener;
import de.df.jutils.gui.wizard.FinishListener;
import de.df.jutils.gui.wizard.JWizard;
import de.df.jutils.gui.wizard.JWizardDialog;
import de.df.jutils.gui.wizard.PageSwitchListener;
import de.df.jutils.gui.wizard.UpdateListener;
import de.df.jutils.gui.wizard.WizardInfoPage;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.PrintManager;

/**
 * @author Fabri
 */
class JPenaltyWizard implements FinishListener, CancelListener {

    interface IPenaltyWizardStrategy {
        LinkedList<Strafe> getStrafen();

        void setStrafen(LinkedList<Strafe> str);

        void addStrafe(Strafe strafe);

        Printable getPrintable(Strafe s);

        Object getEventInfo();
    }

    class PenaltyWizardByTime implements IPenaltyWizardStrategy {

        private final int disziplin;

        public PenaltyWizardByTime(int d) {
            disziplin = d;
        }

        @Override
        public LinkedList<Strafe> getStrafen() {
            return schwimmer.getStrafen(disziplin);
        }

        @Override
        public void setStrafen(LinkedList<Strafe> str) {
            schwimmer.setStrafen(disziplin, str);
        }

        @Override
        public void addStrafe(Strafe strafe) {
            schwimmer.addStrafe(disziplin, strafe);
        }

        @Override
        public Printable getPrintable(Strafe s) {
            @SuppressWarnings("unchecked")
            Printable p = new StrafpunktePrintable<ASchwimmer>(wk, schwimmer, disziplin, s);
            return PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), true, I18n.get("Penalty"));
        }

        @Override
        public Object getEventInfo() {
            return (disziplin == ASchwimmer.DISCIPLINE_NUMBER_SELF ? null : disziplin);
        }
    }

    class PenaltyWizardByHeat implements IPenaltyWizardStrategy {

        private final String id;

        public PenaltyWizardByHeat(String id) {
            this.id = id;
        }

        @Override
        public LinkedList<Strafe> getStrafen() {
            return schwimmer.getStrafen(id);
        }

        @Override
        public void setStrafen(LinkedList<Strafe> strafen) {
            schwimmer.setStrafen(id, strafen);
        }

        @Override
        public void addStrafe(Strafe strafe) {
            schwimmer.addStrafe(id, strafe);
        }

        @Override
        public Printable getPrintable(Strafe s) {
            @SuppressWarnings("unchecked")
            Printable p = new StrafpunktePrintable<ASchwimmer>(wk, schwimmer, id, s);
            return PrintManager.getFinalPrintable(p, wk.getLastChangedDate(), true, I18n.get("Penalty"));
        }

        @Override
        public Object getEventInfo() {
            return id;
        }
    }

    private StrafenPanel strafen = null;
    private CodePanel code = null;
    private ASchwimmer schwimmer = null;
    @SuppressWarnings("rawtypes")
    private AWettkampf wk = null;

    private boolean printOnFinish = false;

    private IPluginManager controller = null;
    private JWizardDialog window = null;
    private JWizard wizard = null;
    private IPenaltyWizardStrategy strategy = null;

    private int steps = 0;

    private int penaltyindex = -1;

    @SuppressWarnings("rawtypes")
    public JPenaltyWizard(JFrame parent, IPluginManager c, AWettkampf wk, ASchwimmer s, boolean printOnFinish,
            boolean fullmode) {
        super();
        wizard = new JWizard(WizardUIElementsProvider.getInstance());
        window = new JWizardDialog(parent, I18n.get("Penalty"), wizard, false);
        window.setAnimated(false);
        init(c, wk, s, printOnFinish, fullmode);
    }

    @SuppressWarnings("rawtypes")
    private void init(IPluginManager c, AWettkampf wettkampf, ASchwimmer s, boolean print, boolean full) {
        if (s == null) {
            throw new NullPointerException();
        }

        strategy = new PenaltyWizardByTime(ASchwimmer.DISCIPLINE_NUMBER_SELF);

        controller = c;

        schwimmer = s;
        this.wk = wettkampf;
        this.printOnFinish = print;

        strafen = new StrafenPanel();
        code = new CodePanel();

        if (full) {
            wizard.addPage(new InfoPanel());
        }
        wizard.addPage(code);
        wizard.addPage(strafen);

        wizard.addListener(this);
        window.pack();
        window.setResizable(false);
        UIStateUtils.uistatemanage(c.getWindow(), window, "PenaltyWizard");
        window.pack();
        window.setResizable(false);
    }

    void setSelectedDisziplin(String id) {
        if (id.isEmpty()) {
            setSelectedDisziplin(ASchwimmer.DISCIPLINE_NUMBER_SELF);
        } else {
            @SuppressWarnings("rawtypes")
            OWDisziplin d = wk.getLauflisteOW().getDisziplin(id);
            if (d == null) {
                throw new IllegalArgumentException("Discipline does not exist.");
            }
            if (!schwimmer.isDisciplineChosen(d.disziplin)) {
                throw new IllegalArgumentException("Discipline not chosen.");
            }
        }
        strategy = new PenaltyWizardByHeat(id);
    }

    void setSelectedDisziplin(int x) {
        if (x < 0) {
            if (x != ASchwimmer.DISCIPLINE_NUMBER_SELF) {
                throw new IllegalArgumentException("Discipline does not exist.");
            }
        }
        if (x != ASchwimmer.DISCIPLINE_NUMBER_SELF) {
            if (!schwimmer.isDisciplineChosen(x)) {
                throw new IllegalArgumentException("Discipline not chosen.");
            }
        }
        strategy = new PenaltyWizardByTime(x);
    }

    public void setPenalty(int index) {
        if (index < -1) {
            throw new IllegalArgumentException("Index out of bounds (" + index + " < -1).");
        }
        if (index >= strategy.getStrafen().size()) {
            throw new IllegalArgumentException(
                    "Index out of bounds (" + index + " >= " + strategy.getStrafen().size() + ").");
        }
        penaltyindex = index;

        if (index >= 0) {
            Strafe s = strategy.getStrafen().get(index);

            boolean found = false;
            LinkedList<Strafe> penalties = wk.getStrafen().getStrafenListe();
            ListIterator<Strafe> li = penalties.listIterator();
            while (li.hasNext()) {
                Strafe c = li.next();
                if (c.equals(s)) {
                    code.setSelectedIndex(li.previousIndex());
                    found = true;
                    break;
                }
            }
            if (found) {
                strafen.setIgnoreUpdate(false);
                steps = wizard.getPageCount() - 2;
            } else {
                strafen.update(s);
                strafen.setIgnoreUpdate(true);
                steps = wizard.getPageCount() - 1;
            }
        } else {
            strafen.setIgnoreUpdate(false);
            steps = 0;
        }
    }

    public void start() {
        window.start(steps);
    }

    @Override
    public void finish() {
        Strafe strafe = null;
        if (wizard.isCurrentPage(code)) {
            strafe = code.getStrafe();
        } else {
            strafe = strafen.getStrafe();
        }
        if (penaltyindex >= 0) {
            LinkedList<Strafe> str = strategy.getStrafen();
            str.set(penaltyindex, strafe);
            strategy.setStrafen(str);
        } else {
            strategy.addStrafe(strafe);
        }
        if (printOnFinish) {
            PrintExecutor.print(strategy.getPrintable(strafe), I18n.get("Penalty"), true, controller.getWindow());
        }
        controller.sendDataUpdateEvent("SetPenalty", REASON_POINTS_CHANGED | REASON_PENALTY, schwimmer,
                strategy.getEventInfo(), null);
        window.setVisible(false);
    }

    String[] getDisziplinen() {
        String[] temp = schwimmer.getAK().getDisziplinenNamen();
        String[] result = new String[temp.length + 1];
        System.arraycopy(temp, 0, result, 1, temp.length);
        result[0] = I18n.get("General");
        return result;
    }

    private class InfoPanel extends WizardInfoPage {
        public InfoPanel() {
            super(I18n.get("Penalty.Information"), I18n.get("Penalty.Information.Information"),
                    new String[] { I18n.get("Startnumber"), I18n.get("Name"), I18n.get("Organisation"),
                            I18n.get("AgeGroup") },
                    new String[] { StartnumberFormatManager.format(schwimmer), schwimmer.getName(),
                            schwimmer.getGliederung(),
                            schwimmer.getAK().toString() + " " + I18n.geschlechtToString(schwimmer) });
        }
    }

    private class CodePanel extends AWizardPage implements UpdateListener, PageSwitchListener {

        private JComboBox<Strafe> penalties = null;
        private JPanel page = null;

        public CodePanel() {
            // Nothing to do
        }

        @Override
        public JComponent getPage() {
            if (page == null) {
                penalties = new JComboBox<>(wk.getStrafen().getStrafenListe().toArray(new Strafe[0]));
                penalties.setRenderer(new PenaltyShortTextListRenderer());
                penalties.setSelectedIndex(0);

                FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                        "4dlu:grow,fill:default,4dlu:grow");
                page = new JPanel(layout);

                page.add(new JLabel(I18n.get("Penalty")), CC.xy(2, 2));
                page.add(penalties, CC.xy(4, 2));
            }
            return page;
        }

        public void setSelectedIndex(int index) {
            penalties.setSelectedIndex(index);
        }

        public Strafe getStrafe() {
            return (Strafe) penalties.getSelectedItem();
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (wizard.isCurrentPage(this)) {
                penalties.requestFocus();
            }
            wizard.setFinishButtonEnabled(true);
            wizard.setNextButtonEnabled(true);
        }

        @Override
        public void update() {
            if (wizard.isCurrentPage(this)) {
                wizard.setFinishButtonEnabled(true);
                wizard.setNextButtonEnabled(true);
            }
        }
    }

    private class StrafenPanel extends AWizardPage implements UpdateListener, PageSwitchListener {

        final class StrafenKatalogListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                Strafe s = PenaltyUIUtils.showPenalties(window, wk.getStrafen(), getStrafe());
                if (s != null) {
                    update(s);
                }
            }
        }

        JComboBox<String> type = null;
        JIntegerField points = null;
        JTextField paragraph = null;
        JTextPane description = null;
        JButton catalogue = null;

        private int ignore = 0;

        private JPanel panel = null;

        public void setIgnoreUpdate(boolean b) {
            if (b) {
                ignore = 2;
            } else {
                ignore = 0;
            }
        }

        public StrafenPanel() {
            super(I18n.get("ChoosePenalty"), I18n.get("ChoosePenalty.Information"));
            type = new JComboBox<>(
                    new String[] { I18n.get("None"), I18n.get("Points"), I18n.get("DidNotStart"),
                            I18n.get("Disqualification"), I18n.get("Debarment") });
            points = new JIntegerField();
            paragraph = new JTextField();
            description = new JTextPane();
            catalogue = new JButton(I18n.get("PenaltyCatalogue"));

            type.addItemListener(arg0 -> {
                update();
            });
            points.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent arg0) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent arg0) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent arg0) {
                    update();
                }
            });
            catalogue.addActionListener(new StrafenKatalogListener());

            FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                    "4dlu,fill:default,4dlu,fill:default,4dlu," + "fill:default,4dlu,fill:default,4dlu,"
                            + "fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,"
                            + "center:default,4dlu,fill:default,4dlu");
            panel = new JPanel(layout);
            layout.setRowGroups(new int[][] { { 2, 4, 6, 8, 10, 12, 16 } });

            panel.add(new JLabel(I18n.get("TypeOfPenalty")), CC.xy(2, 2));
            panel.add(new JLabel(I18n.get("Points")), CC.xy(2, 4));
            panel.add(new JLabel(I18n.get("Paragraph")), CC.xy(2, 6));
            panel.add(new JLabel(I18n.get("Description")), CC.xy(2, 8));
            JSeparator js = new JSeparator();
            js.setAlignmentY(Component.CENTER_ALIGNMENT);
            panel.add(new JSeparator(), CC.xyw(2, 14, 3));

            panel.add(type, CC.xy(4, 2));
            panel.add(points, CC.xy(4, 4));
            panel.add(paragraph, CC.xy(4, 6));
            panel.add(new JScrollPane(description), CC.xywh(4, 8, 1, 5));

            panel.add(catalogue, CC.xy(4, 16));

            update();
        }

        private Strafarten getType() {
            return switch (type.getSelectedIndex()) {
            case 0 -> Strafarten.NICHTS;
            case 1 -> Strafarten.STRAFPUNKTE;
            case 2 -> Strafarten.NICHT_ANGETRETEN;
            case 3 -> Strafarten.DISQUALIFIKATION;
            case 4 -> Strafarten.AUSSCHLUSS;
            default -> Strafarten.NICHTS;
            };
        }

        public Strafe getStrafe() {
            Strafe s = null;
            if ((getType() == Strafarten.NICHTS)
                    && (paragraph.getText().length() + description.getText().length() == 0)) {
                s = new Strafe(Strafe.NICHTS);
            } else {
                s = new Strafe(description.getText(), paragraph.getText(), getType(), points.getInt());
            }
            return s;
        }

        public void update(Strafe strafe) {
            if (ignore > 0) {
                ignore--;
                return;
            }
            EDTUtils.executeOnEDT(new PenaltyUpdate(strafe));
        }

        private class PenaltyUpdate implements Runnable {

            private final Strafe strafe;

            public PenaltyUpdate(Strafe s) {
                strafe = s;
            }

            @Override
            public void run() {
                if (strafe == null) {
                    type.setSelectedIndex(0);
                    points.setText("");
                    paragraph.setText("");
                    description.setText("");
                } else {
                    switch (strafe.getArt()) {
                    case DISQUALIFIKATION:
                        type.setSelectedIndex(3);
                        break;
                    case AUSSCHLUSS:
                        type.setSelectedIndex(4);
                        break;
                    case NICHT_ANGETRETEN:
                        type.setSelectedIndex(2);
                        break;
                    case STRAFPUNKTE:
                        type.setSelectedIndex(1);
                        break;
                    case NICHTS:
                    default:
                        type.setSelectedIndex(0);
                        break;
                    }
                    if (strafe.getArt() == Strafarten.STRAFPUNKTE) {
                        points.setInt(strafe.getStrafpunkte());
                    }
                    paragraph.setText(strafe.getShortname());
                    description.setText(strafe.getName());
                }
            }
        }

        @Override
        public void update() {
            if (wizard.isCurrentPage(this)) {
                boolean isPointsSelected = type.getSelectedIndex() == 1;
                paragraph.setEnabled(true);
                description.setEnabled(true);

                if (isPointsSelected) {
                    points.setEnabled(true);
                    wizard.setFinishButtonEnabled(points.isValidInt());
                    wizard.setPrevioustButtonEnabled(points.isValidInt());
                } else {
                    points.setEnabled(false);
                    wizard.setFinishButtonEnabled(true);
                    wizard.setPrevioustButtonEnabled(true);
                }
                wizard.setNextButtonEnabled(true);
            }
        }

        @Override
        public JComponent getPage() {
            return panel;
        }

        @Override
        public void pageSwitch(boolean forward) {
            if (wizard.isCurrentPage(this)) {
                update(code.getStrafe());
            }
            update();
        }
    }

    @Override
    public void cancel() {
        window.setVisible(false);
    }
}