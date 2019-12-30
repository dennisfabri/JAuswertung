package de.df.jauswertung.misc.recupdater;

import java.util.Collection;

public class Records {

    private Record[] records;

    public Records(Collection<Record> records) {
        this.records = records.toArray(new Record[records.size()]);
    }

    public Record[] getRecords() {
        return records;
    }

    public void update(Collection<Record> times) {
        System.out.println("Updating records");
        for (Record time : times) {
            boolean fitted = false;
            for (Record record : records) {
                if (record.fits(time)) {
                    fitted = true;
                    record.update(time.getCompetition(), time.getTime(), time.getName());
                    break;
                }
            }
            if (!fitted) {
                System.out.println("Not fitting : " + time);
            }
        }
    }
}
