package de.df.jauswertung.dp.splitter;

import java.util.ArrayList;
import java.util.List;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.Utils;

public class DPSplitter {

    private final String sourcefile;
    private final String nationsfile;
    private final String lvsfile;

    private DPSplitter(String sourcefile, String nationsfile, String lvsfile) {
        this.sourcefile = sourcefile;
        this.nationsfile = nationsfile;
        this.lvsfile = lvsfile;
    }

    private void execute() {
        AWettkampf<?> wk = InputManager.ladeWettkampf(sourcefile);
        OutputManager.speichereWettkampf(nationsfile, filterNations(Utils.copy(wk)));
        OutputManager.speichereWettkampf(lvsfile, filterLVs(Utils.copy(wk)));
    }

    private <T extends ASchwimmer> AWettkampf<T> filterLVs(AWettkampf<T> wk) {
        List<T> toRemove = new ArrayList<>();
        for (T s : wk.getSchwimmer()) {
            if (!s.getQualifikationsebene().isEmpty()) {
                if (s.getQualifikationsebene().equalsIgnoreCase("GER")) {
                    if (s.getGliederung().equalsIgnoreCase("Germany")) {
                        // Startet nur für die Nationalmannschaft => nicht LV
                        toRemove.add(s);
                    } else {
                        s.setQualifikationsebene("");
                    }
                } else {
                    // Nicht in der deutschen Nationalmannschaft => nicht LV
                    toRemove.add(s);
                }
            } else {
                // Nicht in einer Nationalmannschaft => ok
            }
        }
        wk.removeSchwimmer(toRemove);
        return wk;
    }

    private <T extends ASchwimmer> AWettkampf<T> filterNations(AWettkampf<T> wk) {
        List<T> toRemove = new ArrayList<>();
        for (T s : wk.getSchwimmer()) {
            if (s.getQualifikationsebene().isEmpty()) {
                toRemove.add(s);
            } else {
                if (s.getQualifikationsebene().equalsIgnoreCase("GER")) {
                    s.setGliederung("Germany");
                }
            }
        }
        wk.removeSchwimmer(toRemove);
        return wk;
    }

    public static void main(String[] args) {
        new DPSplitter(args[0], args[1], args[2]).execute();
    }
}
