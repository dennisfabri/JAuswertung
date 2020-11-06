package de.df.jauswertung.print;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.ergebnis.Formel;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.print.ComponentListPrintable2;
import de.df.jutils.print.HeaderFooterPrintable;
import de.df.jutils.print.PrintManager;
import de.df.jutils.util.StringTools;

public class RulebookSettingsPrintable extends HeaderFooterPrintable {
    public RulebookSettingsPrintable(Regelwerk aks, boolean header) {
        super(new ComponentListPrintable2(false, generateEntries(aks)), header ? new MessageFormat(I18n.get("RulebookSettings")) : null, null, null);
    }

    private static Component[] generateEntries(Regelwerk aks) {
        if (aks == null) {
            throw new NullPointerException("aks must not be null");
        }
        JPanel[] entries = new JPanel[aks.size() + 1];

        entries[0] = createPropertiesPanel(aks);

        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);

            TitledBorder border = new TitledBorder(ak.getName());
            border.setTitleColor(Color.BLACK);
            if (PrintManager.getFont() != null) {
                border.setTitleFont(PrintManager.getFont());
            }

            JPanel p = new JPanel(new BorderLayout(0, 0));
            p.setOpaque(false);
            p.setBackground(Color.WHITE);

            p.add(createTop(aks, ak, aks.hasGesamtwertung()), BorderLayout.NORTH);
            p.add(createBottom(aks, ak, aks.getZusatzwertung()), BorderLayout.CENTER);

            entries[x + 1] = p;
        }
        return entries;
    }

    @SuppressWarnings("rawtypes")
    private static JPanel createPropertiesPanel(Regelwerk aks) {
        SimpleFormBuilder sfm = new SimpleFormBuilder(false, true, 1);
        sfm.setFont(PrintManager.getFont());

        sfm.add(I18n.get("Description") + ": ", aks.getBeschreibung());
        Formel formel = FormelManager.getInstance().get(aks.getFormelID());
        sfm.add(I18n.get("Formula") + ": ", formel.getName());

        StringBuffer gesamtwertung1 = new StringBuffer();
        StringBuffer gesamtwertung2 = new StringBuffer();
        StringBuffer gesamtwertung3 = new StringBuffer();
        StringBuffer gesamtwertung4 = new StringBuffer();
        if (aks.hasGesamtwertung()) {
            gesamtwertung1.append(I18n.get("Yes"));
            switch (aks.getGesamtwertungsmodus()) {
            case All:
                gesamtwertung2.append(I18n.get("AlleTeamsWerten"));
                break;
            case Best:
                gesamtwertung2.append(I18n.get("BestesTeamWertenMitBlockieren"));
                break;
            case Best4:
                gesamtwertung2.append(I18n.get("Besten4TeamsWertenMitBlockieren"));
                break;
            case BestWithoutBlocking:
                gesamtwertung2.append(I18n.get("BestesTeamWertenOhneBlocken"));
                break;
            case BestInDiscipline:
                gesamtwertung2.append(I18n.get("BestenJeDisziplinWerten"));
                break;
            }

            if (aks.isGesamtwertungHart()) {
                gesamtwertung3.append(I18n.get("GliederungMussAnAllenAltersklassenTeilnehmen"));
            }
            switch (aks.getGesamtwertungSkalieren()) {
            case ANZAHL_DISZIPLINEN:
                gesamtwertung4.append(I18n.get("ScalePoint"));
                break;
            case INTERNATIONAL:
                gesamtwertung4.append(I18n.get("InternationalPointsScale"));
                break;
            case INTERNATIONAL_PER_DISCIPLINE:
                gesamtwertung4.append(I18n.get("BestenJeDisziplinInternationalWerten"));
                break;
            case MEDAILLEN:
                gesamtwertung4.append(I18n.get("MedaillenWerten"));
                break;
            case KEINER:
                break;
            }
        } else {
            gesamtwertung1.append(I18n.get("No"));
        }
        sfm.add(I18n.get("GroupEvaluation") + ": ", gesamtwertung1.toString());
        if (gesamtwertung2.length() > 0) {
            sfm.add("", gesamtwertung2.toString(), true);
        }
        if (gesamtwertung3.length() > 0) {
            sfm.add("", gesamtwertung3.toString(), true);
        }
        if (gesamtwertung4.length() > 0) {
            sfm.add("", gesamtwertung4.toString(), true);
        }

        return sfm.getPanel();
    }

    private static JPanel createTop(Regelwerk aks, Altersklasse ak, boolean gesamtwertung) {
        SimpleFormBuilder sfb = new SimpleFormBuilder(false, false, 1);
        sfb.setFont(PrintManager.getFont());

        sfb.addSeparator(ak.getName());

        boolean first = true;

        StringBuffer properties = new StringBuffer();

        if (ak.hasEinzelwertung()) {
            first = false;
            properties.append(I18n.get("Einzelwertung")).append(ak.isEinzelwertungHlw() ? " " + I18n.get("RequiresZW") : "");
        }

        if (gesamtwertung) {
            if (first) {
                first = false;
            } else {
                properties.append(", ");
            }

            String gw = I18n.get("No");
            if (ak.getGesamtwertung(true) && ak.getGesamtwertung(false)) {
                gw = I18n.geschlechtToString(aks, false) + " & " + I18n.geschlechtToString(aks, true);
            } else if (!ak.getGesamtwertung(true) && ak.getGesamtwertung(false)) {
                gw = I18n.geschlechtToString(aks, false);
            } else if (ak.getGesamtwertung(true) && !ak.getGesamtwertung(false)) {
                gw = I18n.geschlechtToString(aks, true);
            } else {
                gw = I18n.geschlechtToString(aks, true);
            }
            properties.append(I18n.get("GroupEvaluation")).append(" (").append(gw).append(")");
        }

        if (properties.length() > 0) {
            sfb.addText(properties.toString());
        }

        if (ak.isDisciplineChoiceAllowed()) {
            properties.setLength(0);
            properties.append(I18n.get("AllowChoiceOfDisciplines")).append(" ")
                    .append(I18n.get("MinAndMax", ak.getMinimalChosenDisciplines(), ak.getMaximalChosenDisciplines()));
            sfb.addText(properties.toString());
        }

        return sfb.getPanel();
    }

    private static JLabel createLabel(String id) {
        JLabel l = new JLabel(id);
        if (PrintManager.getFont() != null) {
            l.setFont(PrintManager.getFont());
        }
        l.setForeground(Color.black);
        return l;
    }

    private static JPanel createBottom(Regelwerk aks, Altersklasse ak, String zwtext) {
        JPanel p = new JPanel(new FormLayout("1dlu,fill:default:grow,1dlu,right:default," + "1dlu,right:default,1dlu",
                FormLayoutUtils.createLayoutString(2 + ak.getDiszAnzahl() + (ak.hasHLW() ? 1 : 0), 1) + ",1dlu"));
        p.setOpaque(false);

        p.add(createLabel(I18n.get("Discipline")), CC.xywh(2, 2, 1, 3, "left,center"));
        p.add(createLabel(I18n.get("Rec-Value")), CC.xyw(4, 2, 3, "center,center"));
        p.add(createLabel(I18n.geschlechtToStringSubject(aks, false)), CC.xy(4, 4, "center,center"));
        p.add(createLabel(I18n.geschlechtToStringSubject(aks, true)), CC.xy(6, 4, "center,center"));

        for (int x = 0; x < ak.getDiszAnzahl(); x++) {
            p.add(createLabel(ak.getDisziplin(x, true).getName()), CC.xy(2, 6 + 2 * x));
            p.add(createLabel(StringTools.zeitString(ak.getDisziplin(x, false).getRec())), CC.xy(4, 6 + 2 * x));
            p.add(createLabel(StringTools.zeitString(ak.getDisziplin(x, true).getRec())), CC.xy(6, 6 + 2 * x));
        }
        if (ak.hasHLW()) {
            p.add(createLabel(zwtext), CC.xy(2, 6 + 2 * ak.getDiszAnzahl()));
        }

        return p;
    }
}
