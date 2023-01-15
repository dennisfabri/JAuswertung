/*
 * Created on 14.01.2005
 */
package de.df.jauswertung.io;

import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.io.Transform;
import de.df.jutils.util.Feedback;

/**
 * @author Fabri
 */
public class HtmlZipExporter extends EmptyExporter {

    @Override
    public String[] getSuffixes() {
        return new String[] { "zip" };
    }

    @Override
    public String getName() {
        return "Zipped HTML";
    }

    @Override
    public boolean isSupported(ImportExportTypes type) {
        switch (type) {
        // case HEATLIST:
        // case HLWLIST:
        // case RESULTS_AG:
        case REGISTRATION:
        case RESULTS:
            return true;
        default:
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.io.Exporter#registration(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean registration(OutputStream out, AWettkampf<T> wk, Feedback fb) {
        try {
            ZipOutputStream zos = new ZipOutputStream(out);
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(9);
            zos.putNextEntry(new ZipEntry(I18n.get("Registrations") + ".html"));

            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateRegistration(wk);
            fb.showFeedback(I18n.get("WritingHtml"));
            Transform.writeHtmlDocument(zos, "xsl/registrations.xsl", d);

            zos.closeEntry();
            zos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.io.Exporter#results(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean results(OutputStream out, AWettkampf<T> wk, Feedback fb) {
        try {
            ZipOutputStream zos = new ZipOutputStream(out);
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(9);
            zos.putNextEntry(new ZipEntry("index.html"));

            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateZipIndex(wk);
            fb.showFeedback(I18n.get("WritingIndex"));
            Transform.writeHtmlDocument(zos, "xsl/multiindex.xsl", d);

            zos.closeEntry();

            Regelwerk aks = wk.getRegelwerk();
            for (int x = 0; x < aks.size(); x++) {
                fb.showFeedback(aks.getAk(x).getName());
                for (int y = 0; y < 2; y++) {
                    if (SearchUtils.hasSchwimmer(wk, aks.getAk(x), y == 1)) {
                        zos.putNextEntry(new ZipEntry((y == 1 ? "male" : "female") + x + ".html"));
                        Transform.writeHtmlDocument(zos, "xsl/results.xsl", XmlExporter.generateResults(wk, x, y == 1));
                        zos.closeEntry();
                    }
                }
            }
            if (wk.getRegelwerk().hasGesamtwertung()) {
                fb.showFeedback(I18n.get("GroupEvaluation"));
                zos.putNextEntry(new ZipEntry("groupevaluation.html"));
                Transform.writeHtmlDocument(zos, "xsl/results.xsl", XmlExporter.generateGesamtwertungResults(wk));
                zos.closeEntry();
            }
            zos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}