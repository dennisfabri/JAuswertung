/*
 * Created on 06.06.2004
 */
package de.df.jauswertung.gui.akeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.df.jauswertung.daten.regelwerk.GroupEvaluationMode;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Skalierungsmodus;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JLabelSeparator;
import de.df.jutils.gui.layout.SimpleFormBuilder;

/**
 * @author Dennis Fabri
 * @date 06.06.2004
 */
class AKsGesamtwertungPanel extends JPanel {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257288011191498036L;

    JCheckBox                 groupEvaluation;
    JCheckBox                 allAGs;
    JRadioButton              allTeams;
    JRadioButton              bestTeam;
    JRadioButton              best4Teams;
    JRadioButton              bestTeamWithoutBlocking;
    JRadioButton              bestPerDiscipline;
    JRadioButton              internationalPerDiscipline;
    JLabelSeparator           labelSeparator;

    JLabelSeparator           punkteanpassung;
    JRadioButton              noscale;
    JRadioButton              scalePoints;
    JRadioButton              internationalScale;
    JRadioButton              medaillen;

    private ButtonGroup       groupEvaluationModes;
    private ButtonGroup       groupScale;

    JAKsEditor                parent           = null;

    public AKsGesamtwertungPanel(JAKsEditor jod) {
        initFormeln();
        initGUI();

        parent = jod;
    }

    private void initFormeln() {
        groupEvaluation = new JCheckBox(I18n.get("EnableGroupEvaluation"));
        groupEvaluation.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean selected = groupEvaluation.isSelected();
                boolean force = !bestTeamWithoutBlocking.isSelected();
                allAGs.setEnabled(selected);
                allTeams.setEnabled(selected);
                bestTeam.setEnabled(selected);
                best4Teams.setEnabled(selected);
                bestTeamWithoutBlocking.setEnabled(selected);
                bestPerDiscipline.setEnabled(selected);
                scalePoints.setEnabled(selected);
                medaillen.setEnabled(selected);
                labelSeparator.setEnabled(selected);

                noscale.setEnabled(selected);
                internationalScale.setEnabled(selected);
                internationalPerDiscipline.setEnabled(selected && force);
                punkteanpassung.setEnabled(selected);
                medaillen.setEnabled(selected);

                if ((!force) && (internationalPerDiscipline.isSelected())) {
                    internationalScale.setSelected(true);
                }
            }
        });
        allAGs = new JCheckBox(I18n.get("GliederungMussAnAllenAltersklassenTeilnehmen"));
        allAGs.setEnabled(false);
        allTeams = new JRadioButton(I18n.get("AlleTeamsWerten"));
        allTeams.setEnabled(false);
        bestTeam = new JRadioButton(I18n.get("BestesTeamWertenMitBlockieren"));
        bestTeam.setEnabled(false);
        best4Teams = new JRadioButton(I18n.get("Besten4TeamsWertenMitBlockieren"));
        best4Teams.setEnabled(false);
        bestTeamWithoutBlocking = new JRadioButton(I18n.get("BestesTeamWertenOhneBlockieren"));
        bestTeamWithoutBlocking.setEnabled(false);
        bestPerDiscipline = new JRadioButton(I18n.get("BestenJeDisziplinWerten"));
        bestPerDiscipline.setEnabled(false);

        scalePoints = new JRadioButton(I18n.get("ScaleGroupEvaluation"));
        scalePoints.setEnabled(false);
        medaillen = new JRadioButton(I18n.get("Medaillen"));
        medaillen.setEnabled(false);
        noscale = new JRadioButton(I18n.get("NoPointsScale"));
        noscale.setEnabled(false);
        internationalScale = new JRadioButton(I18n.get("InternationalPointsScale"));
        internationalScale.setEnabled(false);
        internationalPerDiscipline = new JRadioButton(I18n.get("InternationalPerDisciplineScale"));
        internationalPerDiscipline.setEnabled(false);

        labelSeparator = new JLabelSeparator(I18n.get("GroupEvaluationMode"));
        labelSeparator.setEnabled(false);
        punkteanpassung = new JLabelSeparator(I18n.get("Pointsscale"));
        punkteanpassung.setEnabled(false);

        groupEvaluationModes = new ButtonGroup();
        groupEvaluationModes.add(allTeams);
        groupEvaluationModes.add(bestPerDiscipline);
        groupEvaluationModes.add(bestTeam);
        groupEvaluationModes.add(best4Teams);
        groupEvaluationModes.add(bestTeamWithoutBlocking);

        groupScale = new ButtonGroup();
        groupScale.add(noscale);
        groupScale.add(internationalScale);
        groupScale.add(scalePoints);
        groupScale.add(internationalPerDiscipline);
        groupScale.add(medaillen);

        ActionListener change = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                parent.notifyChange();

                boolean force = !bestTeamWithoutBlocking.isSelected();
                internationalPerDiscipline.setEnabled(force);
                if ((!force) && (internationalPerDiscipline.isSelected())) {
                    internationalScale.setSelected(true);
                }
            }
        };

        groupEvaluation.addActionListener(change);
        allAGs.addActionListener(change);
        allTeams.addActionListener(change);
        bestTeam.addActionListener(change);
        best4Teams.addActionListener(change);
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
        // sfm.setBorder(BorderUtils.createLabeledBorder(I18n
        // .get("GroupEvaluation")));

        sfm.add(groupEvaluation);
        sfm.add(allAGs, true);

        sfm.add(punkteanpassung, true);
        sfm.add(noscale, true);
        sfm.add(scalePoints, true);
        sfm.add(internationalScale, true);
        sfm.add(internationalPerDiscipline, true);
        sfm.add(medaillen, true);

        sfm.add(labelSeparator, true);

        sfm.add(allTeams, true);
        sfm.add(bestTeam, true);
        sfm.add(bestTeamWithoutBlocking, true);
        sfm.add(bestPerDiscipline, true);
        sfm.add(best4Teams, true);

        sfm.getPanel();
    }

    public void setSettings(Regelwerk aks) {
        groupEvaluation.setSelected(aks.hasGesamtwertung());
        allAGs.setSelected(aks.isGesamtwertungHart());
        GroupEvaluationMode mode = aks.getGesamtwertungsmodus();
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
        case BestWithoutBlocking:
            bestTeamWithoutBlocking.setSelected(true);
            break;
        case BestInDiscipline:
            bestPerDiscipline.setSelected(true);
            break;
        case Best4:
            best4Teams.setSelected(true);
            break;
        }
        switch (aks.getGesamtwertungSkalieren()) {
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

    public void getSettings(Regelwerk aks) {
        aks.setGesamtwertung(groupEvaluation.isSelected());
        aks.setGesamtwertungHart(allAGs.isSelected());
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
        aks.setGesamtwertungSkalieren(skalierung);

        GroupEvaluationMode mode = GroupEvaluationMode.Best;
        if (bestTeamWithoutBlocking.isSelected()) {
            mode = GroupEvaluationMode.BestWithoutBlocking;
        } else if (bestPerDiscipline.isSelected()) {
            mode = GroupEvaluationMode.BestInDiscipline;
        } else if (allTeams.isSelected()) {
            mode = GroupEvaluationMode.All;
        } else if (best4Teams.isSelected()) {
            mode = GroupEvaluationMode.Best4;
        }
        aks.setGesamtwertungsmodus(mode);
    }
}