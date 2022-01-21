/*
 * Created on 14.01.2005
 */
package de.df.jauswertung.io;

import java.io.OutputStream;

import org.w3c.dom.Document;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.io.Transform;
import de.df.jutils.util.Feedback;

/**
 * @author Fabri
 */
public class HtmlExporter extends EmptyExporter {

    @Override
    public String[] getSuffixes() {
        return new String[] { "html", "htm" };
    }

    @Override
    public String getName() {
        return "HTML";
    }

    @Override
    public boolean isSupported(ImportExportTypes type) {
        switch (type) {
        case REGISTRATION:
        case RESULTS:
        case PENALTIES:
            return true;
        default:
            return false;
        }
    }

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
}