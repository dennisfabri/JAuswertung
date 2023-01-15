package de.df.jauswertung.io.massexport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.OWUtils;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.io.ImportExportTypes;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.io.XmlExporter;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.ResultUtils;
import de.df.jauswertung.util.ergebnis.DataType;
import de.df.jauswertung.util.ergebnis.FormelILS;
import de.df.jauswertung.util.ergebnis.FormelILSOutdoor;
import de.df.jutils.io.Transform;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.NullFeedback;
import de.df.jutils.util.StringTools;

class CompetitionExporter {

    private CompetitionExporter() {
        // TODO Auto-generated constructor stub
    }

    private static DateFormat df = DateFormat.getTimeInstance();

    private static NullFeedback nf = new NullFeedback();

    static void export(String destination, AWettkampf<?> wk, String[] formats) throws IOException {
        System.out.print("Export timestamp " + df.format(new Date()) + ": ");

        wk = CompetitionUtils.createCompetitionWithCompleteDisciplines(wk);

        OutputManager.speichereWettkampf(destination + "wettkampf-" + StringTools.getCompactDateTimeString() + ".wk",
                wk);

        boolean exportAll = formats != null && formats.length > 0;
        if (exportAll) {
            String[] formats2 = ExportManager.getSupportedFormats();
            ArrayList<String> fx = new ArrayList<>();
            for (String f : formats) {
                for (String f2 : formats2) {
                    if (f2.equalsIgnoreCase(f)) {
                        fx.add(f2);
                    }
                }
            }
            formats = fx.toArray(new String[fx.size()]);
            for (ImportExportTypes type : ImportExportTypes.values()) {
                if (ExportManager.isEnabled(wk, type)) {
                    if (!ExportManager.NAMES[type.getValue()].equals(I18n.get("Weitermeldung"))) {
                        for (String format : formats) {
                            if (!("PDF".equals(format) || "Zipped HTML".equals(format))) {
                                if (ExportManager.isSupported(format, type)) {
                                    String name = destination + ExportManager.NAMES[type.getValue()] + "."
                                            + ExportManager.getSuffixes(format)[0];
                                    String tmp = destination + ExportManager.NAMES[type.getValue()] + "."
                                            + ExportManager.getSuffixes(format)[0] + ".tmp";
                                    System.out.print(".");
                                    ExportManager.export(type, tmp, format, wk, nf);
                                    File dest = new File(name);
                                    while (dest.exists()) {
                                        dest.delete();
                                    }
                                    File temp = new File(tmp);
                                    temp.renameTo(dest);
                                }
                            }
                        }
                    }
                }
            }
            System.out.println();
        }
        exportRounds(destination, wk);
    }

    private static <T extends ASchwimmer> AWettkampf<T> createCompetitionFor(AWettkampf<T> wk, OWSelection t) {
        AWettkampf<T> wkx = ResultUtils.createCompetitionFor(wk, t);
        for (int x = 0; x < wkx.getRegelwerk().size(); x++) {
            wkx.getRegelwerk().getAk(x).setHLW(false);
        }
        if (wkx instanceof MannschaftWettkampf) {
            MannschaftWettkampf mwk = (MannschaftWettkampf) wkx;
            for (Mannschaft m : mwk.getSchwimmer()) {
                m.setName(m.getStarterShort(0, ", "));
            }
        }

        Altersklasse ak = wkx.getRegelwerk().getAk(0);
        Disziplin d = ak.getDisziplin(0, t.male);
        StringBuilder sb = new StringBuilder();
        sb.append(d.getName());
        sb.append(" - ");
        if (t.isFinal) {
            sb.append("Finale");
        } else if (t.round == 0) {
            sb.append("Vorlauf");
        } else {
            sb.append("Zwischenlauf " + t.round);
        }
        d.setName(sb.toString());

        if (!t.isFinal) {
            wkx.getRegelwerk().setFormelID(wk.getDataType() == DataType.RANK ? FormelILSOutdoor.ID : FormelILS.ID);
        }
        return wkx;
    }

    private static <T extends ASchwimmer> boolean results(String name, AWettkampf<T> wk, Feedback fb, int quali,
            boolean includeTimes) throws IOException {
        FileOutputStream fos = new FileOutputStream(name);
        try {
            System.out.print(".");
            return results(fos, wk, fb, quali, includeTimes);
        } finally {
            fos.close();
        }
    }

    private static <T extends ASchwimmer> boolean results(OutputStream name, AWettkampf<T> wk, Feedback fb, int quali,
            boolean includeTimes) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateResults(wk, quali);
            fb.showFeedback(I18n.get("WritingHtml"));
            Transform.writeHtmlDocument(name, includeTimes ? "xsl/resultsjrppool.xsl" : "xsl/resultsjrpow.xsl", d);
            name.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void exportRounds(String destination, AWettkampf<?> wk) throws IOException {
        if (!wk.isHeatBased() || wk.getLauflisteOW() == null || wk.getLauflisteOW().isEmpty()) {
            return;
        }
        OWSelection[] ows = OWUtils.getCreatedRounds(wk, false);
        for (OWSelection t : ows) {
            AWettkampf<?> wkt = createCompetitionFor(wk, t);
            boolean hasTimes = false;
            for (ASchwimmer s : wkt.getSchwimmer()) {
                if (s.hasInput(0)) {
                    hasTimes = true;
                    break;
                }
            }
            if (hasTimes) {
                String filename = destination + "Result-" + wkt.getRegelwerk().getAk(0).getName() + "-"
                        + (t.male ? 'm' : 'w') + "-" + (t.isFinal ? 9 : t.round)
                        + "-" + wkt.getRegelwerk().getAk(0).getDisziplin(0, t.male).getName() + ".html";
                filename = filename.replace(" ", "_");
                int quali = t.isFinal ? 0
                        : wk.getRegelwerk().getAk(t.akNummer).getDisziplin(t.discipline, t.male).getRunden()[t.round];
                results(filename, wkt, nf, quali, !wk.isOpenWater());
            }
        }

    }
}
