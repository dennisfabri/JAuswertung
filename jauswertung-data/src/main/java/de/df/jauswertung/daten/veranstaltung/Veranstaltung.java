package de.df.jauswertung.daten.veranstaltung;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.df.jauswertung.daten.regelwerk.GroupEvaluationMode;
import de.df.jauswertung.daten.regelwerk.Skalierungsmodus;

public class Veranstaltung {

    private Map<String, Object> properties = new HashMap<>();
    private List<CompetitionContainer> competitions = new ArrayList<>();
    private String name = "";
    private String titleOrganization = "";
    private String titleQualifikationsebene = "";
    private String locationAndDate = "";

    public Veranstaltung() {
    }

    private Object getProperty(String id) {
        return properties.get(id);
    }

    private void setProperty(String id, Object value) {
        properties.put(id, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name must no be null.");
        }
        this.name = name;
    }

    public String getTitleOrganization() {
        if (titleOrganization == null) {
            titleOrganization = "";
        }
        return titleOrganization;
    }

    public void setTitleOrganization(String titleOrganization) {
        this.titleOrganization = titleOrganization;
    }

    public String getTitleQualifikationsebene() {
        if (titleQualifikationsebene == null) {
            titleQualifikationsebene = "";
        }
        return titleQualifikationsebene;
    }

    public void setTitleQualifikationsebene(String titleQualifikationsebene) {
        this.titleQualifikationsebene = titleQualifikationsebene;
    }

    public void setCompetitions(List<CompetitionContainer> competitions) {
        this.competitions = new ArrayList<>(competitions);
    }

    public List<CompetitionContainer> getCompetitions() {
        return new ArrayList<>(competitions);
    }

    public String[] getCompetitionNames() {
        String[] titles = new String[competitions.size()];
        int x = 0;
        for (CompetitionContainer cc : competitions) {
            titles[x] = cc.getName();
            x++;
        }
        return titles;
    }

    public boolean isGesamtwertungHart() {
        Object o = getProperty("hard");
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean b) {
            return b;
        }
        return false;
    }

    public void setGesamtwertungHart(boolean gesamtwertungHart) {
        setProperty("hard", gesamtwertungHart);
    }

    public Skalierungsmodus getGesamtwertungSkalieren() {
        Object o = getProperty("scale");
        if (o == null) {
            return Skalierungsmodus.KEINER;
        }
        return (Skalierungsmodus) o;
    }

    public void setGesamtwertungSkalieren(Skalierungsmodus gesamtwertungSkalieren) {
        setProperty("scale", gesamtwertungSkalieren);
    }

    public GroupEvaluationMode getGesamtwertungsmodus() {
        Object o = getProperty("mode");
        if (o == null) {
            return GroupEvaluationMode.All;
        }
        return (GroupEvaluationMode) o;
    }

    public void setGesamtwertungsmodus(GroupEvaluationMode gesamtwertungsmodus) {
        setProperty("mode", gesamtwertungsmodus);
    }

    public String getLocationAndDate() {
        if (locationAndDate == null) {
            locationAndDate = "";
        }
        return locationAndDate;
    }

    public void setLocationAndDate(String locationAndDate) {
        this.locationAndDate = locationAndDate;
    }

    public void verweiseRelativieren(String veranstaltungsdatei) {
        String verzeichnis = verzeichnis(veranstaltungsdatei);
        for (CompetitionContainer cc : competitions) {
            String datei = cc.getFilename();
            int trenner = letzterTrenner(datei);
            if (trenner >= 0 && verzeichnis(datei).equals(verzeichnis)) {
                cc.setFilename(datei.substring(trenner + 1));
            }
        }
    }

    public void verweiseAufloesen(String veranstaltungsdatei) {
        Path verzeichnis = Path.of(veranstaltungsdatei).getParent();
        for (CompetitionContainer cc : competitions) {
            String datei = cc.getFilename();
            if (!datei.isEmpty() && letzterTrenner(datei) < 0) {
                cc.setFilename(verzeichnis.resolve(datei).toString());
            }
        }
    }

    private static String verzeichnis(String datei) {
        return datei.substring(0, Math.max(letzterTrenner(datei), 0));
    }

    private static int letzterTrenner(String datei) {
        return Math.max(datei.lastIndexOf('/'), datei.lastIndexOf('\\'));
    }
}
