package de.df.jauswertung.gui.plugins;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.util.StringTools;

public class HighTimesPlugin extends ANullPlugin {

    private CorePlugin core = null;

    public HighTimesPlugin() {
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    public boolean isEnabled() {
        return SchwimmerUtils.EnableHighTimesWarning;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (!isEnabled()) {
            return;
        }
        if (!core.getWettkampf().isDLRGBased()) {
            return;
        }
        if ((due.getChangeReason() & UpdateEventConstants.REASON_POINTS_CHANGED) != 0) {
            if (due.getData() instanceof ASchwimmer s && due.getAdditionalInformation() instanceof Integer disziplin
                    && disziplin >= 0) {
                Altersklasse ak = s.getAK();
                long zeit = s.getZeit(disziplin);
                long rec = ak.getDisziplin(disziplin, s.isMaennlich()).getRec();
                if ((rec > 0) && (zeit >= rec * 5)) {
                    if (s instanceof Mannschaft) {
                        DialogUtils.inform(getController().getWindow(),
                                I18n.get("TimeTooHighTeam", s.getName(),
                                        ak.getDisziplin(disziplin, s.isMaennlich()).getName(),
                                        StringTools.zeitString((int) zeit)),
                                I18n.get("TimeTooHighTeam.Note", s.getName(),
                                        ak.getDisziplin(disziplin, s.isMaennlich()).getName(),
                                        StringTools.zeitString((int) zeit)));
                    } else {
                        DialogUtils.inform(getController().getWindow(),
                                I18n.get("TimeTooHighSingle", s.getName(),
                                        ak.getDisziplin(disziplin, s.isMaennlich()).getName(),
                                        StringTools.zeitString((int) zeit)),
                                I18n.get("TimeTooHighSingle.Note", s.getName(),
                                        ak.getDisziplin(disziplin, s.isMaennlich()).getName(),
                                        StringTools.zeitString((int) zeit)));
                    }
                }
            }
        }
    }
}