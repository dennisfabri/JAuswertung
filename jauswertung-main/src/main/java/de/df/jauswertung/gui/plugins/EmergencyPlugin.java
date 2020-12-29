/*
 * Created on 18.04.2005
 */
package de.df.jauswertung.gui.plugins;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.SystemUtils;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.AltersklassenUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class EmergencyPlugin extends ANullPlugin {

    private CorePlugin    core = null;
    private WarningPlugin warn = null;

    public EmergencyPlugin() {
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        warn = (WarningPlugin) plugincontroller.getFeature("de.df.jauswertung.warning", pluginuid);
    }

    @Override
    public MenuInfo[] getMenues() {
        return null;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (due.isReason(UpdateEventConstants.REASON_STARTUP)) {
            // warn32Bit();
        }
        if (due.isReason(UpdateEventConstants.REASON_LOAD_WK)) {
            try {
                // @SuppressWarnings("rawtypes")
                // AWettkampf wk = core.getWettkampf();

                // new Check("2013").check(wk);
                // new Check("2014").check(wk);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unused")
    private void warn32Bit() {
        String arch = SystemUtils.OS_ARCH;
        if (arch == null) {
            // Architecture unkown therefore print the warning
            arch = "32";
        }
        if (SystemUtils.IS_OS_WINDOWS && !(arch.contains("32") || arch.contains("86"))) {
            warn.warn(getController().getWindow(), I18n.get("32BitWarning.Title"), I18n.get("32BitWarning.Text"), I18n.get("32BitWarning.Note"),
                    "32BitWarning");
        }
    }

    private class RecWert {

        public RecWert(String ak, String d, boolean m, int z) {
            Altersklasse = ak;
            Disziplin = d;
            Maennlich = m;
            Zeit = z;
        }

        public String  Altersklasse;
        public String  Disziplin;
        public boolean Maennlich;
        public int     Zeit;
    }

    @SuppressWarnings("unused")
    private class Check {

        protected String            jahr;
        private LinkedList<RecWert> recs = new LinkedList<RecWert>();

        public Check(String j) {
            jahr = j;
        }

        @SuppressWarnings("rawtypes")
        void check(AWettkampf wk) {
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
                                System.out.println(rec.Altersklasse + " " + (rec.Maennlich ? "m" : "w") + " " + rec.Disziplin);
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
            Regelwerk aks = AltersklassenUtils.getAKs("DLRG " + jahr + suffix);

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
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    showRecWarningI();
                }
            });
        }

        void showRecWarningI() {
            StringBuilder sb = new StringBuilder();
            for (RecWert rec : recs) {
                sb.append(" - " + rec.Altersklasse + " " + (rec.Maennlich ? "m" : "w") + " - " + rec.Disziplin + "");
                sb.append("\n");
            }
            boolean ok = DialogUtils.ask(getController().getWindow(), "Fehler in den Rec-Werten " + jahr,
                    "In den Rec-Werten ist leider ein Fehler aufgefallen:\n\n" + sb.toString() + "\nSollen die Werte jetzt korrigiert werden?");
            if (ok) {
                FixRecWarning();
            }
        }

        private void FixRecWarning() {
            @SuppressWarnings("rawtypes")
            AWettkampf wk = core.getWettkampf();
            Regelwerk aks = wk.getRegelwerk();
            if (aks.getBeschreibung().contains(jahr)) {
                boolean changed = false;
                for (RecWert rec : recs) {
                    int index = aks.getIndex(rec.Altersklasse);
                    if (index < 0) {
                        continue;
                    }
                    Altersklasse ak = aks.getAk(index);
                    if (ak == null) {
                        continue;
                    }
                    Disziplin d = ak.getDisziplin(rec.Disziplin, rec.Maennlich);
                    if (d == null) {
                        continue;
                    }
                    if (d.getRec() != rec.Zeit) {
                        d.setRec(rec.Zeit);
                        changed = true;
                    }
                }
                if (changed) {
                    getController().sendDataUpdateEvent("ChangeRB", UpdateEventConstants.REASON_AKS_CHANGED, EmergencyPlugin.this);
                }
            }
        }
    }
}
