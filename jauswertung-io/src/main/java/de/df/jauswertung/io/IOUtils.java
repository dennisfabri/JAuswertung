/*
 * Created on 22.01.2005
 */
package de.df.jauswertung.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.text.StringEscapeUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.xmt.VersionedDocument;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Eingabe;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Filter;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Mannschaftsmitglied;
import de.df.jauswertung.daten.Mannschaftsmitgliedermeldung;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.Timelimit;
import de.df.jauswertung.daten.Timelimitchecktype;
import de.df.jauswertung.daten.Timelimits;
import de.df.jauswertung.daten.TimelimitsContainer;
import de.df.jauswertung.daten.Wettkampfart;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.daten.event.PropertyChangeListener;
import de.df.jauswertung.daten.event.PropertyChangeManager;
import de.df.jauswertung.daten.kampfrichter.Kampfrichter;
import de.df.jauswertung.daten.kampfrichter.KampfrichterEinheit;
import de.df.jauswertung.daten.kampfrichter.KampfrichterPosition;
import de.df.jauswertung.daten.kampfrichter.KampfrichterVerwaltung;
import de.df.jauswertung.daten.laufliste.Duration;
import de.df.jauswertung.daten.laufliste.HLWLauf;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.daten.laufliste.Lauf;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWLaufliste;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.laufliste.Time;
import de.df.jauswertung.daten.misc.BugReport;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Einspruch;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.daten.regelwerk.Startgruppe;
import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.daten.regelwerk.StrafenKapitel;
import de.df.jauswertung.daten.regelwerk.StrafenParagraph;
import de.df.jauswertung.daten.regelwerk.Wertungsgruppe;
import de.df.jauswertung.util.Utils;
import de.dm.ares.data.Heat;
import de.dm.ares.data.Lane;
import de.dm.ares.data.LaneStatus;
import de.dm.ares.data.util.XStreamUtil;

/**
 * @author Dennis Fabri @date 22.01.2005
 */
public final class IOUtils {

    private static Logger log = LoggerFactory.getLogger(IOUtils.class);

    private IOUtils() {
        // Hide constructor
    }

    static Object fromXML(InputStream is) throws IOException {
        byte[] data = is.readAllBytes();

        UniversalDetector detector = new UniversalDetector();
        detector.handleData(data);
        detector.dataEnd();
        String charset = detector.getDetectedCharset();
        log.debug("Detected charset '{}'", charset);

        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                InputStreamReader reader = new InputStreamReader(bis, charset)) {
            return fromXML(reader, charset);
        }
    }

    private static Object fromXML(Reader is, String charset) throws IOException {
        VersionedDocument.xstream = getXStream();
        String xml = readText(is).replace("\n\n", "\n");
        if (xml.startsWith("PK")) {
            return null;
        }
        if (!xml.startsWith("<?xml")) {
            try {
                String prefix = "<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\n";
                String xml2 = xml;
                int index = xml2.indexOf('>');
                if (index > 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(prefix);
                    sb.append(xml.substring(0, index));
                    sb.append(" version=\"0\"");
                    sb.append(xml.substring(index));
                    Object o = VersionedDocument.fromXML(sb.toString()).toBean();
                    if (o != null) {
                        return o;
                    }
                }
            } catch (com.thoughtworks.xstream.mapper.CannotResolveClassException crce) {
                crce.printStackTrace();
            } catch (RuntimeException re) {
                re.printStackTrace();
            }
        }
        try {
            Object o = VersionedDocument.fromXML(xml).toBean();
            if (o != null) {
                return o;
            }
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
        return VersionedDocument.xstream.fromXML(xml);
    }

    static String readText(Reader is) throws IOException {
        BufferedReader br = new BufferedReader(is);
        StringBuilder text = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            text.append(line);
            text.append("\n");

            line = br.readLine();
        }
        return text.toString();
    }

    private static void writeText(String text, Writer is) throws IOException {
        BufferedWriter br = new BufferedWriter(is);
        br.write(text);
        br.flush();
    }

    static void toXML(Object o, OutputStream os) throws IOException {
        toXML(o, new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }

    static void toXML(Object o, Writer os) throws IOException {
        VersionedDocument.xstream = getXStream();
        String xml = VersionedDocument.fromBean(o).toXML();
        writeText(xml, os);
    }

    private static XStream instance = null;

    public static XStream getXStream() {
        if (instance == null) {
            instance = XStreamUtil.getXStream();
            setupPermissions(instance);

            instance.aliasType("de.dm.auswertung.daten.regelwerk.Startgruppe", Startgruppe.class);
            instance.aliasType("de.dm.auswertung.daten.regelwerk.Wertungsgruppe", Wertungsgruppe.class);
            instance.aliasType("de.dm.auswertung.daten.Eingabe", Eingabe.class);
            instance.aliasType("de.dm.auswertung.daten.laufliste.HLWListe$1", HLWListe.WettkampfChangeListener.class);
            instance.aliasType("de.dm.auswertung.daten.laufliste.HLWListe$Einteilung", HLWListe.Einteilung.class);
            instance.aliasType("de.dm.auswertung.daten.laufliste.Laufliste$1", Laufliste.WettkampfChangeListener.class);
            instance.aliasType("de.dm.auswertung.daten.laufliste.Laufliste$Einteilung", Laufliste.Einteilung.class);
            instance.aliasType("de.dm.auswertung.daten.laufliste.Laufliste$BlockEinteilung",
                    Laufliste.BlockEinteilung.class);
            instance.aliasType("de.dm.auswertung.daten.Timelimits", Timelimits.class);
            instance.aliasType("de.dm.auswertung.daten.Timelimit", Timelimit.class);
            instance.aliasType("de.dm.auswertung.daten.Timelimitchecktype", Timelimitchecktype.class);
            instance.aliasType("de.dm.auswertung.daten.TimelimitsContainer", TimelimitsContainer.class);

            instance.alias("EinzelWettkampf", EinzelWettkampf.class);
            instance.alias("Mannschaft", Mannschaft.class);
            instance.alias("MannschaftWettkampf", MannschaftWettkampf.class);
            instance.alias("Schwimmer", ASchwimmer.class);
            instance.alias("Teilnehmer", Teilnehmer.class);
            instance.alias("Wettkampf", AWettkampf.class);
            instance.alias("Zielrichterentscheid", Zielrichterentscheid.class);
            instance.alias("Eingabe", Eingabe.class);

            instance.alias("PropertyChangeListener", PropertyChangeListener.class);
            instance.alias("PropertyChangeManager", PropertyChangeManager.class);

            instance.alias("HLW", HLWLauf.class);
            instance.alias("HLWListe", HLWListe.class);
            instance.alias("HLWListeWettkampfChangeListener", HLWListe.WettkampfChangeListener.class);
            instance.alias("HLWListeEinteilung", HLWListe.Einteilung.class);
            instance.alias("Lauf", Lauf.class);
            instance.alias("Laufliste", Laufliste.class);
            instance.alias("LauflisteWettkampfChangeListener", Laufliste.WettkampfChangeListener.class);
            instance.alias("LauflisteEinteilung", Laufliste.Einteilung.class);
            instance.alias("LauflisteBlockEinteilung", Laufliste.BlockEinteilung.class);
            instance.alias("Time", Time.class);
            instance.alias("Duration", Duration.class);

            instance.alias("BugReport", BugReport.class);

            instance.alias("Altersklasse", Altersklasse.class);
            instance.alias("Altersklassen", Regelwerk.class);
            instance.alias("Disziplin", Disziplin.class);
            instance.alias("Startgruppe", Startgruppe.class);
            instance.alias("Wertungsgruppe", Wertungsgruppe.class);

            instance.alias("Timelimits", Timelimits.class);
            instance.alias("Timelimit", Timelimit.class);
            instance.alias("Timelimitchecktype", Timelimitchecktype.class);
            instance.alias("TimelimitsContainer", TimelimitsContainer.class);

            instance.alias("OWDisziplin", OWDisziplin.class);
            instance.alias("OWSelection", OWSelection.class);
            instance.alias("OWLauf", OWLauf.class);
            instance.alias("OWLaufliste", OWLaufliste.class);

            instance.alias("Einspruch", Einspruch.class);
            instance.alias("Strafe", Strafe.class);
            instance.alias("Strafen", Strafen.class);
            instance.alias("StrafenKapitel", StrafenKapitel.class);
            instance.alias("StrafenParagraph", StrafenParagraph.class);

            instance.alias("Kampfrichterteam", KampfrichterVerwaltung.class);
            instance.alias("Kampfrichterverwaltung", KampfrichterVerwaltung.class);
            instance.alias("Kampfrichtereinheit", KampfrichterEinheit.class);
            instance.alias("KampfrichterPosition", KampfrichterPosition.class);
            instance.alias("Kampfrichter", Kampfrichter.class);

            instance.alias("Filter", Filter.class);
            instance.alias("Wettkampfart", Wettkampfart.class);
            instance.alias("HLWStates", HLWStates.class);

            instance.alias("Mannschaftsmitglied", Mannschaftsmitglied.class);
            instance.alias("Mannschaftsmitgliedermeldung", Mannschaftsmitgliedermeldung.class);

            instance.useAttributeFor(ASchwimmer.class, "gliederung");
            instance.useAttributeFor(ASchwimmer.class, "maennlich");
            instance.useAttributeFor(ASchwimmer.class, "bemerkung");
            instance.useAttributeFor(ASchwimmer.class, "aknummer");
            instance.useAttributeFor(ASchwimmer.class, "ausserkonkurrenz");
            instance.useAttributeFor(ASchwimmer.class, "startnummer");
            instance.useAttributeFor(ASchwimmer.class, "startunterlagen");

            instance.useAttributeFor(Mannschaft.class, "name");
            instance.useAttributeFor(Mannschaft.class, "mitglieder");

            instance.useAttributeFor(Teilnehmer.class, "jahrgang");
            instance.useAttributeFor(Teilnehmer.class, "vorname");
            instance.useAttributeFor(Teilnehmer.class, "nachname");

            instance.useAttributeFor(Zielrichterentscheid.class, "disziplin");

            instance.aliasAttribute(ASchwimmer.class, "hlwState", "hlwState2");
            instance.aliasAttribute(ASchwimmer.class, "punktehlw", "punktehlw2");
            instance.aliasAttribute(Laufliste.class, "verteilung", "verteilung2");

            instance.registerConverter(new StringConverter());
        }

        return instance;
    }

    private static void setupPermissions(XStream xstream) {
        xstream.allowTypes(new Class[] { Heat.class, Lane.class, LaneStatus.class });
        xstream.allowTypesByWildcard(new String[] { "de.df.jauswertung.daten.**", "java.util.*", "java.lang.*",
                "de.df.jutils.print.PageSetting" });
    }

    private static class StringConverter extends AbstractSingleValueConverter {

        public StringConverter() {
            // Nothing to do
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean canConvert(Class clazz) {
            return clazz.equals(String.class);
        }

        @Override
        public Object fromString(String value) {
            return StringEscapeUtils.unescapeXml(value);
        }

        @Override
        public String toString(Object value) {
            return StringEscapeUtils.escapeXml11(value.toString());
        }
    }

    public static void writeToPreferences(String name, Object data) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputManager.speichereObject(bos, data);
        Utils.getPreferences().putByteArray(name, bos.toByteArray());
    }

    public static Object readFromPreferences(String name) {
        byte[] bytes = Utils.getPreferences().getByteArray(name, null);
        if (bytes == null) {
            return null;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return InputManager.ladeObject(bis);
    }
}