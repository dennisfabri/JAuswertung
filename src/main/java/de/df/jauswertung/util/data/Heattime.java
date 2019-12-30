package de.df.jauswertung.util.data;

import java.util.Date;

public class Heattime {

    public Heattime() {
        Country = "GER";
        Kreisname = "";
        KreisId = "";
        Abschnitt = 3;
        Lsv = "";
        Count = 1;

        Bahnseite = "";
        DsvId = "";
        OrganizationId = "";

        DisciplineChar = ' ';
    }

    public int     Heat;
    public int     Lane;

    public int     Time;
    public String  Penalty;

    public String  Agegroup;
    public String  Discipline;

    public String  Name;
    public String  Organization;
    public String  Sex;

    public String  CompetitionName;
    public int     Lanecount;
    public String  CompetitionType;
    public int     Length;
    public char    DisciplineChar;
    public String  Surname;
    public Date    Date;
    public int     YearOfBirth;
    public String  Firstname;
    public boolean IsRelay;
    public int     Startnumber;
    public String  Timetype;
    public int     CompetitionId;
    public String  Status;

    public String  OrganizationId;
    public String  Bahnseite;
    public String  DsvId;
    public int     Count;
    public String  Lsv;
    public int     Abschnitt;
    public String  Kreisname;
    public String  Country;
    public String  KreisId;

}
