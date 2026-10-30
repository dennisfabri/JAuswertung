package de.df.jauswertung.util.valueobjects;

import de.df.jauswertung.daten.Mannschaft;

public class Team {

    private final int id;
    private final boolean male;
    private final int ak;

    private final String teamname;
    private final String gliederung;
    private final String qualifikationsgliederung;
    private final String agegroup;

    private final String exportId;

    private final Teammember[] members;

    public Team(Mannschaft m, String exportId) {
        this.id = m.getStartnummer();
        this.teamname = m.getName();
        this.gliederung = m.getGliederung();
        this.qualifikationsgliederung = m.getQualifikationsebene();
        this.male = m.isMaennlich();
        this.agegroup = m.getAK().getName();
        this.ak = m.getAKNummer();
        if (exportId == null || exportId.isBlank() || exportId.equals("-")) {
            this.exportId = null;
        } else {
            this.exportId = exportId;
        }

        int count = m.getMaxMembers();
        members = new Teammember[count];
        for (int x = 0; x < count; x++) {
            members[x] = new Teammember(m, x);
        }
    }

    Team(int id, int ak, boolean male, String exportId, Teammember[] members) {
        this.id = id;
        this.ak = ak;
        this.male = male;
        this.members = members;
        this.exportId = exportId == null ? "" : exportId;

        teamname = "";
        gliederung = "";
        qualifikationsgliederung = "";
        agegroup = "";
    }

    public String getExportId() {
        return exportId;
    }

    public boolean isExportIdEmpty() {
        return exportId == null || "".equals(exportId) || "-".equals(exportId);
    }

    public boolean isMale() {
        return male;
    }

    public String getAgegroup() {
        return agegroup;
    }

    public int getId() {
        return id;
    }

    public int getAk() {
        return ak;
    }

    public String getTeamname() {
        return teamname;
    }

    public String getGliederung() {
        return gliederung;
    }

    public String getQualifikationsgliederung() {
        return qualifikationsgliederung;
    }

    public int getMemberCount() {
        return members.length;
    }

    public Teammember getMember(int x) {
        return members[x];
    }
}
