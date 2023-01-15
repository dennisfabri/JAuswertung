package de.df.jauswertung.gui.veranstaltung;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.df.jauswertung.daten.regelwerk.GroupEvaluationMode;
import de.df.jauswertung.daten.regelwerk.Skalierungsmodus;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JLabelSeparator;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;

class JVeranstaltungseinstellungen extends JPanel {

    private JTextField name;
    private JTextField titleG;
    private JTextField titleQ;
    private JTextField location;

    JCheckBox allAGs;

    JLabelSeparator separatorGroupEvaluationMode;
    JLabelSeparator separatorPunkteanpassung;
    JLabelSeparator separatorVeranstaltung;
    JLabelSeparator separatorSonstiges;

    JRadioButton allTeams;
    JRadioButton bestTeam;
    JRadioButton best4Teams;
    JRadioButton bestTeamWithoutBlocking;
    JRadioButton bestPerDiscipline;
    JRadioButton internationalPerDiscipline;

    JRadioButton noscale;
    JRadioButton scalePoints;
    JRadioButton internationalScale;
    JRadioButton medaillen;

    private ButtonGroup groupEvaluationModes;
    private ButtonGroup groupScale;

    final JVeranstaltungswertung parent;

    public JVeranstaltungseinstellungen(JVeranstaltungswertung parent) {
        this.parent = parent;
        setBorder(BorderUtils.createLabeledBorder(I18n.get("Properties")));
        init();
        initGUI();
    }

    private void init() {
        name = new JTextField();
        titleG = new JTextField();
        titleQ = new JTextField();
        location = new JTextField();

        allAGs = new JCheckBox(I18n.get("GliederungMussAnAllenAltersklassenTeilnehmen"));
        allTeams = new JRadioButton(I18n.get("AlleTeamsWerten"));
        bestTeam = new JRadioButton(I18n.get("BestesTeamWertenMitBlockieren"));
        best4Teams = new JRadioButton(I18n.get("Besten4TeamsWertenMitBlockieren"));
        bestTeamWithoutBlocking = new JRadioButton(I18n.get("BestesTeamWertenOhneBlockieren"));
        bestPerDiscipline = new JRadioButton(I18n.get("BestenJeDisziplinWerten"));

        scalePoints = new JRadioButton(I18n.get("ScaleGroupEvaluation"));
        medaillen = new JRadioButton(I18n.get("Medaillen"));
        noscale = new JRadioButton(I18n.get("NoPointsScale"));
        internationalScale = new JRadioButton(I18n.get("InternationalPointsScale"));
        internationalPerDiscipline = new JRadioButton(I18n.get("InternationalPerDisciplineScale"));

        groupEvaluationModes = new ButtonGroup();
        groupEvaluationModes.add(allTeams);
        groupEvaluationModes.add(bestPerDiscipline);
        groupEvaluationModes.add(bestTeam);
        groupEvaluationModes.add(bestTeamWithoutBlocking);
        groupEvaluationModes.add(best4Teams);

        groupScale = new ButtonGroup();
        groupScale.add(noscale);
        groupScale.add(internationalScale);
        groupScale.add(scalePoints);
        groupScale.add(internationalPerDiscipline);
        groupScale.add(medaillen);

        ActionListener change = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                parent.setModified();

                boolean force = !bestTeamWithoutBlocking.isSelected();
                internationalPerDiscipline.setEnabled(force);
                if ((!force) && (internationalPerDiscipline.isSelected())) {
                    internationalScale.setSelected(true);
                }
            }
        };

        allAGs.addActionListener(change);
        allTeams.addActionListener(change);
        bestTeam.addActionListener(change);
        bestTeamWithoutBlocking.addActionListener(change);
        bestPerDiscipline.addActionListener(change);
        internationalPerDiscipline.addActionListener(change);
        scalePoints.addActionListener(change);
        internationalScale.addActionListener(change);
        noscale.addActionListener(change);
        medaillen.addActionListener(change);
    }

    private void initGUI() {
        SimpleFormBuilder sfm = new SimpleFormBuilder(this);
        sfm.setBorder(BorderUtils.createLabeledBorder(I18n.get("GroupEvaluation")));

        boolean indent = false;

        sfm.addSeparator(I18n.get("Veranstaltung"));
        sfm.add(new JLabel(I18n.get("Name")), name);
        sfm.add(new JLabel(I18n.get("LocationAndDate")), location);
        sfm.add(new JLabel(I18n.get("TitleForResultsByOrganization")), titleG);
        sfm.add(new JLabel(I18n.get("TitleForResultsByQuali")), titleQ);

        sfm.addSeparator(I18n.get("Pointsscale"));
        sfm.add(noscale, indent);
        sfm.add(scalePoints, indent);
        sfm.add(internationalScale, indent);
        sfm.add(internationalPerDiscipline, indent);
        sfm.add(medaillen, indent);

        sfm.addSeparator(I18n.get("GroupEvaluationMode"));
        // sfm.add(separatorGroupEvaluationMode, indent);
        sfm.add(allTeams, indent);
        sfm.add(bestTeam, indent);
        sfm.add(bestTeamWithoutBlocking, indent);
        sfm.add(bestPerDiscipline, indent);
        sfm.add(best4Teams, indent);

        sfm.addSeparator(I18n.get("Other"));
        // sfm.add(separatorSonstiges, indent);
        sfm.add(allAGs, indent);

        sfm.getPanel();
    }

    public void setProperties(Veranstaltung vs) {
        name.setText(vs.getName());
        location.setText(vs.getLocationAndDate());
        titleG.setText(vs.getTitleOrganization());
        titleQ.setText(vs.getTitleQualifikationsebene());
        allAGs.setSelected(vs.isGesamtwertungHart());
        GroupEvaluationMode mode = vs.getGesamtwertungsmodus();
        if (mode == null) {
            mode = GroupEvaluationMode.Best;
        }
        switch (mode) {
        case All:
            allTeams.setSelected(true);
            break;
        case Best:
            bestTeam.setSelected(true);
            break;
        case Best4:
            best4Teams.setSelected(true);
            break;
        case BestWithoutBlocking:
            bestTeamWithoutBlocking.setSelected(true);
            break;
        case BestInDiscipline:
            bestPerDiscipline.setSelected(true);
            break;
        }
        switch (vs.getGesamtwertungSkalieren()) {
        case KEINER:
            noscale.setSelected(true);
            break;
        case ANZAHL_DISZIPLINEN:
            scalePoints.setSelected(true);
            break;
        case INTERNATIONAL:
            internationalScale.setSelected(true);
            break;
        case INTERNATIONAL_PER_DISCIPLINE:
            internationalPerDiscipline.setSelected(true);
            break;
        case MEDAILLEN:
            medaillen.setSelected(true);
            break;
        }
    }

    public void getProperties(Veranstaltung vs) {
        vs.setName(name.getText());
        vs.setLocationAndDate(location.getText());
        vs.setTitleOrganization(titleG.getText());
        vs.setTitleQualifikationsebene(titleQ.getText());
        vs.setGesamtwertungHart(allAGs.isSelected());
        Skalierungsmodus skalierung = Skalierungsmodus.KEINER;
        if (scalePoints.isSelected()) {
            skalierung = Skalierungsmodus.ANZAHL_DISZIPLINEN;
        } else if (medaillen.isSelected()) {
            skalierung = Skalierungsmodus.MEDAILLEN;
        } else if (internationalScale.isSelected()) {
            skalierung = Skalierungsmodus.INTERNATIONAL;
        } else if (internationalPerDiscipline.isSelected()) {
            skalierung = Skalierungsmodus.INTERNATIONAL_PER_DISCIPLINE;
        }
        vs.setGesamtwertungSkalieren(skalierung);

        GroupEvaluationMode mode = GroupEvaluationMode.Best;
        if (bestPerDiscipline.isSelected()) {
            mode = GroupEvaluationMode.BestInDiscipline;
        } else if (best4Teams.isSelected()) {
            mode = GroupEvaluationMode.Best4;
        } else if (bestTeamWithoutBlocking.isSelected()) {
            mode = GroupEvaluationMode.BestWithoutBlocking;
        } else if (allTeams.isSelected()) {
            mode = GroupEvaluationMode.All;
        }
        vs.setGesamtwertungsmodus(mode);
    }
}