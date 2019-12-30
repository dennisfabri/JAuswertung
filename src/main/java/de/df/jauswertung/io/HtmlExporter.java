/*
 * Created on 14.01.2005
 */
package de.df.jauswertung.io;

import static de.df.jauswertung.io.ExportManager.PENALTIES;
import static de.df.jauswertung.io.ExportManager.REGISTRATION;
import static de.df.jauswertung.io.ExportManager.RESULTS;

import java.io.OutputStream;

import org.w3c.dom.Document;

import de.df.jauswertung.daten.*;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.io.Transform;
import de.df.jutils.util.Feedback;

/**
 * @author Fabri
 */
public class HtmlExporter implements IExporter {

    @Override
    public String[] getSuffixes() {
        return new String[] { "html", "htm" };
    }

    @Override
    public String getName() {
        return "HTML";
    }

    @Override
    public boolean isSupported(int type) {
        switch (type) {
        // case HEATLIST:
        // case HLWLIST:
        // case RESULTS_AG:
        case REGISTRATION:
        case RESULTS:
        case PENALTIES:
            return true;
        default:
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#registration(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean registration(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateRegistration(wk);
            fb.showFeedback(I18n.get("WritingHtml"));
            Transform.writeHtmlDocument(name, "xsl/registrations.xsl", d);
            name.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#heats(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean heats(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateHeats(wk);
            fb.showFeedback(I18n.get("WritingHtml"));
            Transform.writeHtmlDocument(name, "xsl/heats.xsl", d);
            name.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#hlw(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean zusatzwertung(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateZW(wk);
            fb.showFeedback(I18n.get("WritingHtml"));
            Transform.writeHtmlDocument(name, "xsl/zw.xsl", d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#results(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean results(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateResults(wk);
            fb.showFeedback(I18n.get("WritingHtml"));
            Transform.writeHtmlDocument(name, "xsl/results.xsl", d);
            name.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#startkarten(java.lang.String,
     * de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean startkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateStartkarten(wk);
            fb.showFeedback(I18n.get("WritingHtml"));
            Transform.writeHtmlDocument(name, "xsl/startkarten.xsl", d);
            name.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungStartkarten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean protocol(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    /**
     * Exportiert die Kampfrichter eines Wettkampfes in eine CSV-Datei.
     * 
     * @param name
     *            Name der Datei
     * @param wk
     *            Wettkampf
     * @return Erfolgsmeldung
     */
    @Override
    public synchronized <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean penalties(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generatePenalties(wk);
            fb.showFeedback(I18n.get("WritingHtml"));
            Transform.writeHtmlDocument(name, "xsl/penalties.xsl", d);
            name.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean teammembers(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean zusatzwertungResults(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean bestezeiten(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean heatsoverview(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }

    @Override
    public <T extends ASchwimmer> boolean heattimes(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
    }
}