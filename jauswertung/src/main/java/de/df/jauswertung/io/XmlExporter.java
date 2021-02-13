/*
 * Created on 14.01.2005
 */
package de.df.jauswertung.io;

import static de.df.jauswertung.daten.PropertyConstants.DATE;
import static de.df.jauswertung.daten.PropertyConstants.LOCATION;
import static de.df.jauswertung.daten.PropertyConstants.NAME;
import static de.df.jauswertung.io.ExportManager.NAMES;
import static de.df.jauswertung.io.ExportManager.PENALTIES;
import static de.df.jauswertung.io.ExportManager.REGISTRATION;
import static de.df.jauswertung.io.ExportManager.RESULTS;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.regelwerk.StrafenKapitel;
import de.df.jauswertung.daten.regelwerk.StrafenParagraph;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.GesamtwertungSchwimmer;
import de.df.jauswertung.util.GesamtwertungWettkampf;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.ergebnis.ResultUtils;
import de.df.jauswertung.util.ergebnis.SchwimmerData;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.exception.ParserException;
import de.df.jutils.io.Transform;
import de.df.jutils.util.Feedback;
import de.df.jutils.util.StringTools;

/**
 * @author Fabri
 */
public class XmlExporter implements IExporter {

    private static DecimalFormat doubleFormat;
    private static DecimalFormat integerFormat;

    static {
        doubleFormat = new DecimalFormat();
        doubleFormat.setMaximumFractionDigits(2);
        doubleFormat.setMinimumFractionDigits(2);

        integerFormat = new DecimalFormat();
        integerFormat.setMaximumFractionDigits(0);
        integerFormat.setMinimumFractionDigits(0);
        integerFormat.setDecimalSeparatorAlwaysShown(false);
    }

    public static <T extends ASchwimmer> Document generateHeats(AWettkampf<T> wk) throws ParserConfigurationException {
        throw new ParserConfigurationException();
    }

    public static <T extends ASchwimmer> Document generateZW(AWettkampf<T> wk) throws ParserConfigurationException {
        throw new ParserConfigurationException();
    }

    public static <T extends ASchwimmer> Document generateCompetition(AWettkampf<T> wk) throws ParserConfigurationException {
        throw new ParserConfigurationException();
    }

    public static <T extends ASchwimmer> Document generateResults(AWettkampf<T> wk) throws ParserConfigurationException {
        return generateResults(wk, 0);
    }

    public static <T extends ASchwimmer> Document generateResults(AWettkampf<T> wk, int quali) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        Document xmldoc = impl.createDocument(null, "results", null);
        Element root = xmldoc.getDocumentElement();
        root.setAttribute("name", I18n.get("Results"));

        addTitles(xmldoc, wk);
        addInfos(xmldoc);
        addNames(xmldoc, wk);

        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;

        Element competition = xmldoc.createElementNS(null, "competition");
        competition.setAttribute("type", (w instanceof MannschaftWettkampf ? "Team" : "Single"));
        root.appendChild(competition);

        for (int x = 0; x < wk.getRegelwerk().size(); x++) {
            for (int y = 0; y < 2; y++) {
                Element e = createAgegroupResults(wk, xmldoc, wk.getRegelwerk().getAk(x), y == 1, quali);
                if (e != null) {
                    competition.appendChild(e);
                }
            }
        }
        Element e = createGesamtwertungResults(wk, xmldoc);
        if (e != null) {
            competition.appendChild(e);
        }
        return xmldoc;
    }

    public static <T extends ASchwimmer> Document generateGesamtwertungResults(AWettkampf<T> wk) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        Document xmldoc = impl.createDocument(null, "results", null);
        Element root = xmldoc.getDocumentElement();
        root.setAttribute("name", I18n.get("Results"));

        addTitles(xmldoc, wk);
        addInfos(xmldoc);
        addNames(xmldoc, wk);

        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;

        Element competition = xmldoc.createElementNS(null, "competition");
        competition.setAttribute("type", (w instanceof MannschaftWettkampf ? "Team" : "Single"));
        root.appendChild(competition);

        Element e = createGesamtwertungResults(wk, xmldoc);
        if (e != null) {
            competition.appendChild(e);
        }
        return xmldoc;
    }

    public static <T extends ASchwimmer> Document generateResults(AWettkampf<T> wk, int agegroup, boolean male)
            throws ParserConfigurationException, ParserException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        Document xmldoc = impl.createDocument(null, "results", null);
        Element root = xmldoc.getDocumentElement();
        root.setAttribute("name", I18n.get("Results"));

        if (agegroup >= wk.getRegelwerk().size()) {
            throw new ParserException("Agegroup does not exist!");
        }

        addTitles(xmldoc, wk);
        addInfos(xmldoc);
        Element e = addNames(xmldoc, wk);
        e.setAttributeNS(null, "agegroup", wk.getRegelwerk().getAk(agegroup).getName() + " " + I18n.geschlechtToString(wk.getRegelwerk(), male));

        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;

        Element competition = xmldoc.createElementNS(null, "competition");
        competition.setAttribute("type", (w instanceof MannschaftWettkampf ? "Team" : "Single"));
        root.appendChild(competition);

        e = createAgegroupResults(wk, xmldoc, wk.getRegelwerk().getAk(agegroup), male, 0);
        if (e != null) {
            competition.appendChild(e);
        }
        return xmldoc;
    }

    public static <T extends ASchwimmer> Document generateStartkarten(AWettkampf<T> wk) throws ParserConfigurationException {
        throw new ParserConfigurationException();
    }

    public static <T extends ASchwimmer> Document generateRegistration(AWettkampf<T> wk) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        Document xmldoc = impl.createDocument(null, "registrations", null);
        Element root = xmldoc.getDocumentElement();
        root.setAttribute("name", I18n.get("Registrations"));

        addTitles(xmldoc, wk);
        addInfos(xmldoc);
        addNames(xmldoc, wk);

        root.appendChild(createRegistrations(wk, xmldoc));
        return xmldoc;
    }

    public static <T extends ASchwimmer> Document generatePenalties(AWettkampf<T> wk) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        Document xmldoc = impl.createDocument(null, "penalties", null);
        Element root = xmldoc.getDocumentElement();
        root.setAttribute("name", I18n.get("PenaltyCatalog"));

        addTitles(xmldoc, wk);
        addInfos(xmldoc);
        addNames(xmldoc, wk);

        root.appendChild(createPenalties(wk, xmldoc));
        return xmldoc;
    }

    public static <T extends ASchwimmer> Document generateIndex(AWettkampf<T> wk) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        Document xmldoc = impl.createDocument(null, "index", null);
        Element root = xmldoc.getDocumentElement();
        root.setAttribute("name", I18n.get("Index"));

        addTitles(xmldoc, wk);
        addInfos(xmldoc);
        addNames(xmldoc, wk);

        root.appendChild(createIndex(wk, xmldoc));
        return xmldoc;
    }

    public static <T extends ASchwimmer> Document generateZipIndex(AWettkampf<T> wk) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        Document xmldoc = impl.createDocument(null, "index", null);
        Element root = xmldoc.getDocumentElement();
        root.setAttribute("name", I18n.get("Index"));

        addTitles(xmldoc, wk);
        addInfos(xmldoc);
        addNames(xmldoc, wk);

        root.appendChild(createZipIndex(wk, xmldoc));
        return xmldoc;
    }

    @Override
    public boolean isSupported(int type) {
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
    public String getName() {
        return "XML";
    }

    @Override
    public String[] getSuffixes() {
        return new String[] { "xml" };
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#registration(java.lang.String, de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean registration(OutputStream out, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateRegistration(wk);
            fb.showFeedback(I18n.get("WritingXml"));
            Transform.transformDocument2XML(out, null, d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#heats(java.lang.String, de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean heats(OutputStream out, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateHeats(wk);
            fb.showFeedback(I18n.get("WritingXml"));
            Transform.transformDocument2XML(out, null, d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#hlw(java.lang.String, de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean zusatzwertung(OutputStream out, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateZW(wk);
            fb.showFeedback(I18n.get("WritingXml"));
            Transform.transformDocument2XML(out, null, d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#results(java.lang.String, de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean results(OutputStream out, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateResults(wk);
            fb.showFeedback(I18n.get("WritingXml"));
            Transform.transformDocument2XML(out, null, d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.io.Exporter#startkarten(java.lang.String, de.df.jauswertung.daten.Wettkampf)
     */
    @Override
    public <T extends ASchwimmer> boolean startkarten(OutputStream out, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generateStartkarten(wk);
            fb.showFeedback(I18n.get("WritingXml"));
            Transform.transformDocument2XML(out, null, d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public <T extends ASchwimmer> boolean penalties(OutputStream out, AWettkampf<T> wk, Feedback fb) {
        try {
            fb.showFeedback(I18n.get("CollectingData"));
            Document d = XmlExporter.generatePenalties(wk);
            fb.showFeedback(I18n.get("WritingXml"));
            Transform.transformDocument2XML(out, null, d);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static <T extends ASchwimmer> Element createAgegroupResults(AWettkampf<T> wk, Document xmldoc, Altersklasse ak, boolean male, int quali) {

        SchwimmerResult<T>[] results = ResultUtils.getResults(wk, ak, male);

        if (results.length > 0) {
            double maxpoints = results[0].getPoints();

            Element agegroup = xmldoc.createElementNS(null, "agegroup");
            agegroup.setAttributeNS(null, "name", ak.getName());
            agegroup.setAttributeNS(null, "male", "" + male);
            agegroup.setAttributeNS(null, "sex", I18n.geschlechtToString(wk.getRegelwerk(), male));

            for (int i = 0; i < ak.getDiszAnzahl(); i++) {
                Disziplin d = ak.getDisziplin(i, male);
                Element e = xmldoc.createElementNS(null, "discipline");
                e.setAttributeNS(null, "name", d.getName());
                e.setAttributeNS(null, "number", "" + (i + 1));
                agegroup.appendChild(e);
            }
            if (ak.hasHLW()) {
                Element e = xmldoc.createElementNS(null, "hlw");
                e.setAttributeNS(null, "enabled", "true");
                agegroup.appendChild(e);
            }
            // double points = Double.MAX_VALUE;
            int rank = 0;
            for (int i = 0; i < results.length; i++) {
                T s = results[i].getSchwimmer();
                Element e = xmldoc.createElementNS(null, "registration");
                e.setAttributeNS(null, "name", s.getName());
                if (s instanceof Teilnehmer) {
                    Teilnehmer t = (Teilnehmer) s;

                    e.setAttributeNS(null, "year", I18n.yearToString(t.getJahrgang()));
                    e.setAttributeNS(null, "yearshort", I18n.yearToShortString(t.getJahrgang()));
                    e.setAttributeNS(null, "forename", t.getVorname());
                    e.setAttributeNS(null, "surname", t.getNachname());
                }
                // if (results[i].getPoints() < points) {
                if (results[i].getPlace() > rank) {
                    rank = results[i].getPlace();
                    // points = results[i].getPoints() - 0.005;
                    e.setAttributeNS(null, "rank", integerFormat.format(rank));
                } else {
                    e.setAttributeNS(null, "rank", "");
                }

                double diff = maxpoints - results[i].getPoints();

                String pointsText = doubleFormat.format(results[i].getPoints());
                if (quali > 0) {
                    pointsText = rank <= quali ? "Q" : "";
                }

                e.setAttributeNS(null, "points", pointsText);
                e.setAttributeNS(null, "difference", diff >= 0 ? doubleFormat.format(diff) : "");
                e.setAttributeNS(null, "penalty", I18n.getPenaltyShort(results[i].getStrafe()));
                e.setAttributeNS(null, "number", StartnumberFormatManager.format(s));
                e.setAttributeNS(null, "regpoints", doubleFormat.format(s.getMeldepunkte(0)));
                e.setAttributeNS(null, "comment", s.getBemerkung());
                e.setAttributeNS(null, "organisation", s.getGliederung());
                e.setAttributeNS(null, "qualificationorganisation", s.getQualifikationsebene());

                SchwimmerData<T>[] daten = results[i].getResults();
                for (int z = 0; z < ak.getDiszAnzahl(); z++) {
                    Element dis = xmldoc.createElementNS(null, "result");
                    if (s.isDisciplineChosen(z)) {
                        dis.setAttributeNS(null, "chosen", "1");

                        dis.setAttributeNS(null, "number", integerFormat.format(z + 1));
                        dis.setAttributeNS(null, "penalty", PenaltyUtils.getPenaltyMediumText(daten[z].getStrafe(), ak));
                        switch (daten[z].getStrafart()) {
                        case AUSSCHLUSS:
                            dis.setAttributeNS(null, "points", "");
                            dis.setAttributeNS(null, "pointsshort", "");
                            dis.setAttributeNS(null, "time", "");
                            dis.setAttributeNS(null, "rank", "");
                            break;
                        case DISQUALIFIKATION:
                            dis.setAttributeNS(null, "points", doubleFormat.format(daten[z].getPoints()));
                            dis.setAttributeNS(null, "pointsshort", integerFormat.format(Math.floor(daten[z].getPoints())));
                            dis.setAttributeNS(null, "time", StringTools.zeitString(daten[z].getTime()));
                            dis.setAttributeNS(null, "rank", "");
                            break;
                        case NICHT_ANGETRETEN:
                            dis.setAttributeNS(null, "points", doubleFormat.format(daten[z].getPoints()));
                            dis.setAttributeNS(null, "pointsshort", integerFormat.format(Math.floor(daten[z].getPoints())));

                            dis.setAttributeNS(null, "time", "");
                            dis.setAttributeNS(null, "rank", "");
                            break;
                        default:
                            dis.setAttributeNS(null, "points", doubleFormat.format(daten[z].getPoints()));
                            dis.setAttributeNS(null, "pointsshort", integerFormat.format(Math.floor(daten[z].getPoints())));
                            dis.setAttributeNS(null, "time", StringTools.zeitString(daten[z].getTime()));
                            dis.setAttributeNS(null, "rank", integerFormat.format(daten[z].getRank()));
                            break;
                        }
                    } else {
                        dis.setAttributeNS(null, "chosen", "0");

                        dis.setAttributeNS(null, "number", "");
                        dis.setAttributeNS(null, "time", "");
                        dis.setAttributeNS(null, "rank", "");

                        dis.setAttributeNS(null, "penalty", "");
                        dis.setAttributeNS(null, "points", "");
                    }
                    e.appendChild(dis);
                }
                if (ak.hasHLW()) {
                    Element zw = xmldoc.createElementNS(null, "hlwresult");
                    zw.setAttributeNS(null, "points", integerFormat.format(s.getHLWPunkte()));
                    e.appendChild(zw);
                }
                agegroup.appendChild(e);
            }
            return agegroup;
        }
        return null;

    }

    private static <T extends ASchwimmer> Element createGesamtwertungResults(AWettkampf<T> w, Document xmldoc) {
        if (!w.getRegelwerk().hasGesamtwertung()) {
            return null;
        }
        GesamtwertungWettkampf wk = new GesamtwertungWettkampf(w);
        GesamtwertungSchwimmer[] reg = wk.getResult();
        if (reg.length > 0) {
            Element agegroup = xmldoc.createElementNS(null, "groupevaluation");

            double points = Double.MAX_VALUE;
            int rank = 0;
            for (int i = 0; i < reg.length; i++) {
                Element e = xmldoc.createElementNS(null, "registration");
                if (reg[i].getPunkte() < points) {
                    rank = i + 1;
                    points = reg[i].getPunkte() - 0.005;
                    e.setAttributeNS(null, "rank", "" + rank);
                } else {
                    e.setAttributeNS(null, "rank", "");
                }
                e.setAttributeNS(null, "points", doubleFormat.format(reg[i].getPunkte()));
                e.setAttributeNS(null, "organisation", reg[i].getGliederung());
                agegroup.appendChild(e);
            }
            return agegroup;
        }
        return null;

    }

    /**
     * @param wk
     * @param xmldoc
     */
    private static <T extends ASchwimmer> Element createRegistrations(AWettkampf<T> wk, Document xmldoc) {
        @SuppressWarnings("rawtypes")
        AWettkampf w = wk;

        Element root = xmldoc.createElementNS(null, "competition");
        root.setAttribute("type", (w instanceof MannschaftWettkampf ? "Team" : "Single"));
        LinkedList<T> s = wk.getSchwimmer();
        ASchwimmer[] reg = s.toArray(new ASchwimmer[s.size()]);
        for (ASchwimmer aReg : reg) {
            Element e = xmldoc.createElementNS(null, "registration");
            e.setAttributeNS(null, "name", aReg.getName());
            if (aReg instanceof Teilnehmer) {
                Teilnehmer t = (Teilnehmer) aReg;
                int jahr = t.getJahrgang();
                e.setAttributeNS(null, "year", jahr == 0 ? "" : "" + jahr);
                e.setAttributeNS(null, "forename", t.getVorname());
                e.setAttributeNS(null, "surname", t.getNachname());
            }
            e.setAttributeNS(null, "male", "" + aReg.isMaennlich());
            e.setAttributeNS(null, "sex", I18n.geschlechtToString(wk.getRegelwerk(), aReg.isMaennlich()));
            e.setAttributeNS(null, "agegroup", aReg.getAK().toString());
            e.setAttributeNS(null, "number", StartnumberFormatManager.format(aReg));
            e.setAttributeNS(null, "regpoints", doubleFormat.format(aReg.getMeldepunkte(0)));
            e.setAttributeNS(null, "comment", aReg.getBemerkung());
            e.setAttributeNS(null, "organisation", aReg.getGliederung());
            e.setAttributeNS(null, "ak", "" + aReg.isAusserKonkurrenz());
            root.appendChild(e);
        }
        return root;
    }

    private static <T extends ASchwimmer> Element createPenalties(AWettkampf<T> wk, Document xmldoc) {
        Element root = xmldoc.createElementNS(null, "list");

        Strafen strafen = wk.getStrafen();
        ListIterator<StrafenKapitel> chapters = strafen.getKapitel().listIterator();

        while (chapters.hasNext()) {
            StrafenKapitel chapter = chapters.next();
            if (!chapter.getName().equals("Sonstiges")) {
                Element c = xmldoc.createElementNS(null, "chapter");
                c.setAttributeNS(null, "name", chapter.getName());

                ListIterator<StrafenParagraph> paragraphs = chapter.getParagraphen().listIterator();
                while (paragraphs.hasNext()) {
                    StrafenParagraph paragraph = paragraphs.next();
                    Element p = xmldoc.createElementNS(null, "paragraph");
                    p.setAttributeNS(null, "name", paragraph.getName());

                    ListIterator<Strafe> penalties = paragraph.getStrafen().listIterator();
                    while (penalties.hasNext()) {
                        Strafe s = penalties.next();
                        Element t = xmldoc.createElementNS(null, "penalty");
                        t.setAttributeNS(null, "code", (s.getShortname().length() == 0 ? " " : s.getShortname()));
                        t.setAttributeNS(null, "text", s.getName());
                        t.setAttributeNS(null, "penalty", PenaltyUtils.getPenaltyValue(s, null));

                        p.appendChild(t);
                    }

                    c.appendChild(p);
                }

                root.appendChild(c);
            }
        }
        return root;
    }

    /**
     * @param wk
     * @param xmldoc
     */
    @SuppressWarnings("deprecation")
    private static <T extends ASchwimmer> Element createIndex(AWettkampf<T> wk, Document xmldoc) {

        Element root = xmldoc.createElementNS(null, "index");

        Regelwerk aks = wk.getRegelwerk();
        for (int i = 0; i < aks.size(); i++) {
            if (SearchUtils.hasSchwimmer(wk, aks.getAk(i))) {
                Element e = xmldoc.createElementNS(null, "agegroup");
                e.setAttributeNS(null, "name", aks.getAk(i).getName());

                boolean supported = SearchUtils.getSchwimmer(wk, aks.getAk(i), true).size() > 0;

                e.setAttributeNS(null, "malesupported", "" + supported);
                e.setAttributeNS(null, "malename", aks.getAk(i) + " " + I18n.geschlechtToString(wk.getRegelwerk(), true));
                if (supported) {
                    e.setAttributeNS(null, "malelink", "male" + i + ".html");
                }

                supported = SearchUtils.getSchwimmer(wk, aks.getAk(i), false).size() > 0;

                e.setAttributeNS(null, "femalesupported", "" + supported);
                e.setAttributeNS(null, "femalename", aks.getAk(i) + " " + I18n.geschlechtToString(wk.getRegelwerk(), false));
                if (supported) {
                    e.setAttributeNS(null, "femalelink", "female" + i + ".html");
                }

                root.appendChild(e);
            }
        }

        if (wk.getRegelwerk().hasGesamtwertung()) {
            GesamtwertungWettkampf w = new GesamtwertungWettkampf(wk);
            GesamtwertungSchwimmer[] reg = w.getResult();
            if (reg.length > 0) {
                Element e = xmldoc.createElementNS(null, "full");
                root.appendChild(e);
            }
        }

        String[] formats = ExportManager.getSupportedFormats();
        for (String format1 : formats) {
            Element e = xmldoc.createElementNS(null, "format");
            e.setAttributeNS(null, "name", format1);
            root.appendChild(e);
        }

        for (int i = 0; i < NAMES.length; i++) {
            Element e = xmldoc.createElementNS(null, "type");

            String name = "";
            try {
                name = URLEncoder.encode(NAMES[i], "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                // try best guess
                name = URLEncoder.encode(NAMES[i]);
            }

            e.setAttributeNS(null, "name", NAMES[i]);
            for (String format : formats) {
                boolean supported = ExportManager.isSupported(format, i) && ExportManager.isEnabled(wk, i);
                Element f = xmldoc.createElementNS(null, "format");
                f.setAttributeNS(null, "supported", "" + supported);
                f.setAttributeNS(null, "name", ExportManager.getName(format));
                if (supported) {
                    f.setAttributeNS(null, "link", "/export/" + format.replace(" ", "+") + "/" + name + "." + ExportManager.getSuffixes(format)[0]);
                }
                e.appendChild(f);
            }
            root.appendChild(e);
        }

        if (wk.hasSchwimmer()) {
            Element f = xmldoc.createElementNS(null, "graphics");
            f.setAttribute("name", I18n.get("ResultsAsGraphics"));

            Element e = xmldoc.createElementNS(null, "image");
            e.setAttribute("name1", "800x600");
            e.setAttribute("link1", "/images-0800x0600.zip");
            e.setAttribute("name2", "600x800");
            e.setAttribute("link2", "/images-0600x0800.zip");
            f.appendChild(e);

            e = xmldoc.createElementNS(null, "image");
            e.setAttribute("name1", "1024x768");
            e.setAttribute("link1", "/images-1024x0768.zip");
            e.setAttribute("name2", "768x1024");
            e.setAttribute("link2", "/images-0768x1024.zip");
            f.appendChild(e);

            e = xmldoc.createElementNS(null, "image");
            e.setAttribute("name1", "1280x1024");
            e.setAttribute("link1", "/images-1280x1024.zip");
            e.setAttribute("name2", "1024x1280");
            e.setAttribute("link2", "/images-1024x1280.zip");
            f.appendChild(e);

            // e = xmldoc.createElementNS(null, "image");
            // e.setAttribute("name1", "1600x1200");
            // e.setAttribute("link1", "/images-1600x1200.zip");
            // e.setAttribute("name2", "1600x1200");
            // e.setAttribute("link2", "/images-1600x1200.zip");
            // f.appendChild(e);

            root.appendChild(f);
        }
        return root;
    }

    private static <T extends ASchwimmer> Element createZipIndex(AWettkampf<T> wk, Document xmldoc) {

        Element root = xmldoc.createElementNS(null, "index");

        Regelwerk aks = wk.getRegelwerk();
        for (int i = 0; i < aks.size(); i++) {
            Element e = xmldoc.createElementNS(null, "agegroup");
            e.setAttributeNS(null, "name", aks.getAk(i).getName());

            boolean supported = SearchUtils.getSchwimmer(wk, aks.getAk(i), true).size() > 0;

            e.setAttributeNS(null, "malesupported", "" + supported);
            e.setAttributeNS(null, "malename", aks.getAk(i) + " " + I18n.geschlechtToString(wk.getRegelwerk(), true));
            if (supported) {
                e.setAttributeNS(null, "malelink", "male" + i + ".html");
            }

            supported = SearchUtils.getSchwimmer(wk, aks.getAk(i), false).size() > 0;

            e.setAttributeNS(null, "femalesupported", "" + supported);
            e.setAttributeNS(null, "femalename", aks.getAk(i) + " " + I18n.geschlechtToString(wk.getRegelwerk(), false));
            if (supported) {
                e.setAttributeNS(null, "femalelink", "female" + i + ".html");
            }

            root.appendChild(e);
        }

        if (wk.getRegelwerk().hasGesamtwertung()) {
            GesamtwertungWettkampf w = new GesamtwertungWettkampf(wk);
            GesamtwertungSchwimmer[] reg = w.getResult();
            if (reg.length > 0) {
                Element e = xmldoc.createElementNS(null, "full");
                root.appendChild(e);
            }
        }

        return root;
    }

    /**
     * @param xmldoc
     */
    private static <T extends ASchwimmer> Element addTitles(Document xmldoc, AWettkampf<T> wk) {
        // Spaltentitel eintragen
        Element e = xmldoc.createElementNS(null, "titles");
        Element root = xmldoc.getDocumentElement();
        e.setAttributeNS(null, "download", I18n.get("Download"));
        e.setAttributeNS(null, "export", I18n.get("Export"));
        e.setAttributeNS(null, "name", I18n.get("Name"));
        e.setAttributeNS(null, "forename", I18n.get("Forename"));
        e.setAttributeNS(null, "surname", I18n.get("Surname"));
        e.setAttributeNS(null, "sex", I18n.get("Sex"));
        e.setAttributeNS(null, "male", I18n.get("male"));
        e.setAttributeNS(null, "female", I18n.get("female"));
        e.setAttributeNS(null, "agegroup", I18n.get("AgeGroup"));
        e.setAttributeNS(null, "rank", I18n.get("Rank"));
        e.setAttributeNS(null, "rankshort", I18n.get("RankShort"));
        e.setAttributeNS(null, "number", I18n.get("StartnumberShort"));
        e.setAttributeNS(null, "points", I18n.get("Points"));
        e.setAttributeNS(null, "time", I18n.get("Time"));
        e.setAttributeNS(null, "groupevaluation", I18n.get("GroupEvaluation"));
        e.setAttributeNS(null, "year", I18n.get("YearOfBirth"));
        e.setAttributeNS(null, "yearshort", I18n.get("YearOfBirthShort"));
        e.setAttributeNS(null, "regpoints", I18n.get("ReportedPoints"));
        e.setAttributeNS(null, "results", I18n.get("Results"));
        e.setAttributeNS(null, "comment", I18n.get("Comment"));
        e.setAttributeNS(null, "organisation", I18n.get("Organisation"));
        e.setAttributeNS(null, "penalty", I18n.get("Penalty"));
        e.setAttributeNS(null, "penaltyshort", I18n.get("PenaltyShort"));
        e.setAttributeNS(null, "hlwname", wk.getRegelwerk().getZusatzwertungShort());
        e.setAttributeNS(null, "groupevaluation", I18n.get("GroupEvaluation"));
        root.appendChild(e);
        return e;
    }

    /**
     * @param xmldoc
     * @param root
     */
    private static Element addInfos(Document xmldoc) {
        // Spaltentitel eintragen
        Element e = xmldoc.createElementNS(null, "infos");
        Element root = xmldoc.getDocumentElement();
        e.setAttributeNS(null, "name", I18n.get("ProgramInfo"));
        e.setAttributeNS(null, "copyright", I18n.get("CopyrightInfo"));
        e.setAttributeNS(null, "url", I18n.get("ProgramURLInfo"));
        e.setAttributeNS(null, "homepage", I18n.get("HomepageInfo"));
        root.appendChild(e);
        return e;
    }

    /**
     * @param xmldoc
     * @param root
     */
    @SuppressWarnings("rawtypes")
    private static Element addNames(Document xmldoc, AWettkampf wk) {
        // Spaltentitel eintragen
        Element e = xmldoc.createElementNS(null, "competitioninfos");
        Element root = xmldoc.getDocumentElement();
        e.setAttributeNS(null, "name", wk.getStringProperty(NAME));
        e.setAttributeNS(null, "location", wk.getStringProperty(LOCATION));
        e.setAttributeNS(null, "date", wk.getStringProperty(DATE));

        Date date = wk.getLastChangedDate();
        e.setAttributeNS(null, "dateoflastchange", DateFormat.getDateInstance().format(date));
        e.setAttributeNS(null, "timeoflastchange", DateFormat.getTimeInstance().format(date));
        e.setAttributeNS(null, "single", wk instanceof EinzelWettkampf ? "true" : "false");
        root.appendChild(e);
        return e;
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
     *            Name der Datei @param wk Wettkampf @return Erfolgsmeldung
     */
    @Override
    public synchronized <T extends ASchwimmer> boolean referees(OutputStream name, AWettkampf<T> wk, Feedback fb) {
        return false;
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