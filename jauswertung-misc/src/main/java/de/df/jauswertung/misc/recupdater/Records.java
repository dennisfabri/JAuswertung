package de.df.jauswertung.misc.recupdater;

import java.util.Collection;

import de.df.jauswertung.misc.times.Time;

public class Records {

    private Record[] records;

    public Records(Collection<Record> records) {
        this.records = records.toArray(new Record[records.size()]);
    }

    public Record[] getRecords() {
        return records;
    }

    public void update(Collection<Time> times) {
        System.out.println("Updating records");
        for (Time time : times) {
            System.out.println("" + time.toString());
            boolean fitted = false;
            for (Record record : records) {
                if (record.fits(time)) {
                    fitted = true;
                    record.update(time.competition(), time.timeInHundrets(), time.name());
                    break;
                }
            }
            if (!fitted && !time.hasRealPenalty()) {
                System.out.println("Not fitting : " + time);
            }
        }
    }

    public void print() {
        for (Record record : records) {
            System.out.println(record);
        }
    }
}
