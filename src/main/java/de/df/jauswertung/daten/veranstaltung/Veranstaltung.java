package de.df.jauswertung.daten.veranstaltung;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;

import de.df.jauswertung.daten.regelwerk.*;

public class Veranstaltung implements Serializable {

    private Hashtable<String, Object>        properties               = new Hashtable<String, Object>();
    private LinkedList<CompetitionContainer> competitions             = new LinkedList<CompetitionContainer>();
    private String                           name                     = "";
    private String                           titleOrganization        = "";
    private String                           titleQualifikationsebene = "";
    private String                           locationAndDate          = "";

    public Veranstaltung() {
        // Nothing to do
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

    public void setCompetitions(LinkedList<CompetitionContainer> competitions) {
        this.competitions = new LinkedList<CompetitionContainer>(competitions);
    }

    public LinkedList<CompetitionContainer> getCompetitions() {
        return new LinkedList<CompetitionContainer>(competitions);
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
        if (o instanceof Boolean) {
            return (Boolean) o;
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
}