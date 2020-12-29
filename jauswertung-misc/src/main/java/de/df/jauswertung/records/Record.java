package de.df.jauswertung.records;

import java.util.Date;

public class Record implements Comparable<Record> {

    public final int     Id;
    public final Date    Date;
    public final long    Time;
    public final int     Discipline;
    public final int     Agegroup;
    public final boolean Male;
    public final int     Competitor;

    public Record(int id, Date date, long time, int discipline, int agegroup, boolean male, int competitor) {
        Id = id;
        Date = date;
        Time = time;
        Discipline = discipline;
        Agegroup = agegroup;
        Male = male;
        Competitor = competitor;
    }

    @Override
    public int compareTo(Record r) {
        if (Agegroup != r.Agegroup) {
            return Agegroup < r.Agegroup ? -1 : 1;
        }
        if (Male != r.Male) {
            return Male ? 1 : -1;
        }
        if (Discipline != r.Discipline) {
            return Discipline < r.Discipline ? -1 : 1;
        }
        if (Time != r.Time) {
            return Time > r.Time ? 1 : -1;
        }
        return 0;
    }
}
