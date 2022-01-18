package de.df.jauswertung.gui.plugins.emergency;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.SwingUtilities;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.EmergencyPlugin;
import de.df.jauswertung.io.AgeGroupIOUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.IPlugin;
import de.df.jutils.plugin.IPluginManager;

public class Check {

    private final IPlugin parent;
    private final IPluginManager controller;
    private final CorePlugin core;
    
    protected String jahr;
    private LinkedList<RecWert> recs = new LinkedList<>();

    public Check(EmergencyPlugin emergencyPlugin, IPluginManager controller, CorePlugin core, String j) {
        parent = emergencyPlugin;
        this.controller = controller;
        this.core = core;
        
        jahr = j;
    }

    @SuppressWarnings("rawtypes")
    public void check(AWettkampf wk) {
        Regelwerk aks = wk.getRegelwerk();
        if (aks.getBeschreibung().contains(jahr)) {
            initRecs(wk);
            ListIterator<RecWert> li = recs.listIterator();
            while (li.hasNext()) {
                RecWert rec = li.next();
                try {
                    int aki = aks.getIndex(rec.Altersklasse);
                    if (aki < 0) {
                        li.remove();
                    } else {
                        Altersklasse ak = aks.getAk(aki);
                        Disziplin d = ak.getDisziplin(rec.Disziplin, rec.Maennlich);
                        if (d == null) {
                            System.out.println(
                                    rec.Altersklasse + " " + (rec.Maennlich ? "m" : "w") + " " + rec.Disziplin);
                        }
                        if (d == null || d.getRec() == rec.Zeit) {
                            li.remove();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (!recs.isEmpty()) {
                showRecWarning();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void initRecs(AWettkampf wk) {
        String suffix = wk instanceof MannschaftWettkampf ? ".rwm" : ".rwe";
        Regelwerk aks = AgeGroupIOUtils.getAKs("DLRG " + jahr + suffix);

        for (int i = 0; i < aks.size(); i++) {
            Altersklasse ak = aks.getAk(i);
            for (int x = 0; x < ak.getDiszAnzahl(); x++) {
                for (int y = 0; y < 2; y++) {
                    Disziplin d = ak.getDisziplin(x, y == 1);
                    recs.add(new RecWert(ak.getName(), d.getName(), y == 1, d.getRec()));
                }
            }
        }
    }

    void showRecWarning() {
        SwingUtilities.invokeLater(() -> showRecWarningI());
    }

    void showRecWarningI() {
        StringBuilder sb = new StringBuilder();
        for (RecWert rec : recs) {
            sb.append(" - " + rec.Altersklasse + " " + (rec.Maennlich ? "m" : "w") + " - " + rec.Disziplin + "");
            sb.append("\n");
        }
        boolean ok = DialogUtils.ask(controller.getWindow(), "Fehler in den Rec-Werten " + jahr,
                "In den Rec-Werten ist leider ein Fehler aufgefallen:\n\n" + sb.toString()
                        + "\nSollen die Werte jetzt korrigiert werden?");
        if (ok) {
            boolean changed = korrigiereWettkampf();
            if (changed) {
                controller.sendDataUpdateEvent("ChangeRB", UpdateEventConstants.REASON_AKS_CHANGED,
                        parent);
            }
        }
    }

    private boolean korrigiereWettkampf() {
        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();
        Regelwerk aks = wk.getRegelwerk();
        if (!aks.getBeschreibung().contains(jahr)) {
            return false;
        }
        return korrigiereRecWerte(aks);
    }

    private boolean korrigiereRecWerte(Regelwerk aks) {
        boolean changed = false;
        for (RecWert rec : recs) {
            Disziplin d = findeDisziplin(aks, rec);
            if (d != null && d.getRec() != rec.Zeit) {
                d.setRec(rec.Zeit);
                changed = true;
            }
        }
        return changed;
    }
    
    private Disziplin findeDisziplin(Regelwerk aks, RecWert rec) {
        int index = aks.getIndex(rec.Altersklasse);
        if (index < 0) {
            return null;
        }
        Altersklasse ak = aks.getAk(index);
        if (ak == null) {
            return null;
        }
        return ak.getDisziplin(rec.Disziplin, rec.Maennlich);
        
    }
}