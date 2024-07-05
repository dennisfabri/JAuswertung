package de.df.jauswertung.gui.veranstaltung;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.dp.displaytool.data.Competition;
import de.df.jauswertung.dp.displaytool.data.Competitor;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.GesamtwertungSchwimmer;
import de.df.jauswertung.util.GesamtwertungWettkampf;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.DialogUtils;

class ResultsExporter implements Printer {

    private JButton resultprint;
    private JComboBox<String> resultmodus;
    private JPanel panel;

    private final JVeranstaltungswertung parent;

    public ResultsExporter(JVeranstaltungswertung parent) {
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        resultprint = new JButton(I18n.get("Export"));
        resultprint.addActionListener(e -> {
            print();
        });

        resultmodus = new JComboBox<>(new String[] { I18n.get("Organisation"), I18n.get("Qualifikationsebene") });

        panel = new JPanel(new FormLayout(
                "4dlu:grow,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu,fill:default,4dlu",
                FormLayoutUtils.createLayoutString(1, 4, 0)));

        panel.add(new JLabel(I18n.get("Print.WertungNach")), CC.xy(4, 2));
        panel.add(resultmodus, CC.xy(6, 2));
        panel.add(resultprint, CC.xy(10, 2));
    }

    @SuppressWarnings("rawtypes")
    private AWettkampf[] getResult() {
        Veranstaltung vs = parent.getVeranstaltung();
        boolean gliederungen = resultmodus.getSelectedIndex() == 0;
        return VeranstaltungsUtils.veranstaltung2Wettkampf(vs, gliederungen,false);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void dataUpdated(AWettkampf wk) {
        // Nothing to do
    }

    @Override
    public String getName() {
        return I18n.get("Results");
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    private void print() {
        AWettkampf[] wks = getResult();
        if (wks == null || wks.length == 0) {
            DialogUtils.inform(parent, I18n.get("NoDataToPrint"), I18n.get("NoDataToPrint.Note"));
            return;
        }

        AWettkampf p = wks[0];
        String filename = FileChooserUtils.saveFile(parent, new SimpleFileFilter("Competition-Datei", ".competition"));
        if (filename != null) {
            Competition c = getGesamtwertung(p, parent.getVeranstaltung(), resultmodus.getSelectedIndex() == 0);
            OutputManager.speichereObject(filename, c);
        }
    }

    @SuppressWarnings({ "unchecked", "null", "rawtypes" })
    private static <T extends ASchwimmer> Competition getGesamtwertung(AWettkampf<T> wk, Veranstaltung vs,
            boolean gliederung) {
        AWettkampf[] wks = VeranstaltungsUtils.getWettkaempfe(vs.getCompetitions());
        String[] nx = vs.getCompetitionNames();
        LinkedList<Integer> sizes = new LinkedList<>();
        LinkedList<String> names = new LinkedList<>();
        LinkedList<String> sexes = new LinkedList<>();
        for (int x = 0; x < wks.length; x++) {
            int amount = 0;
            if (wks[x] != null) {
                if (!gliederung) {
                    ListIterator<ASchwimmer> li = wks[x].getSchwimmer().listIterator();
                    while (li.hasNext()) {
                        ASchwimmer s = li.next();
                        if (s.getQualifikationsebene().trim().length() == 0) {
                            li.remove();
                        } else {
                            s.setGliederung(s.getQualifikationsebene());
                        }
                    }
                }
                for (int y = 0; y < wks[x].getRegelwerk().size(); y++) {
                    if (SearchUtils.hasSchwimmer(wks[x], wks[x].getRegelwerk().getAk(y))) {
                        amount++;
                        sexes.add(wks[x].getRegelwerk().getTranslation("femaleShort", I18n.get("sex1Short")));
                        sexes.add(wks[x].getRegelwerk().getTranslation("maleShort", I18n.get("sex2Short")));
                    }
                }
            }
            if (amount > 0) {
                sizes.addLast(amount);
                names.addLast(nx[x]);
            }
        }
        if (sizes.isEmpty()) {
            return null;
        }

        Competition c = new Competition(gliederung ? "National" : "LV");
        GesamtwertungSchwimmer[] result = buildGesamtwertungsergebnis(wk, false,
                sexes.toArray(new String[sexes.size()]));
        if (result == null || result.length == 0) {
            return c;
        }
        c.setCompetitors(
                Arrays.stream(result).map(m -> new Competitor(m.getName(), m.getPunkte())).toArray(Competitor[]::new));
        return c;
    }

    @SuppressWarnings("rawtypes")
    private static GesamtwertungSchwimmer[] buildGesamtwertungsergebnis(AWettkampf wettkampf, boolean nachkomma,
            String[] sexes) {
        if (!wettkampf.getRegelwerk().hasGesamtwertung()) {
            return null;
        }
        return new GesamtwertungWettkampf(wettkampf).getResult();
    }

}