package de.df.jauswertung.util;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.laufliste.HLWListe;
import de.df.jauswertung.daten.laufliste.Laufliste;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jutils.plugin.IFeature;
import de.df.jutils.plugin.IPluginManager;

public final class PluginUtils {

    public static <T extends ASchwimmer> void erzeugeHlwliste(IPluginManager controller, IFeature feature, AWettkampf<T> wk) {
        wk.getHLWListe().erzeugen(null);
        controller.sendDataUpdateEvent("NewZWList", UpdateEventConstants.REASON_ZW_LIST_CHANGED, feature);
    }

    public static <T extends ASchwimmer> void erzeugeHlwliste(IPluginManager controller, IFeature feature, AWettkampf<T> wk, HLWListe.Einteilung[] aufteilung) {
        wk.getHLWListe().erzeugen(aufteilung);
        controller.sendDataUpdateEvent("NewZWList", UpdateEventConstants.REASON_ZW_LIST_CHANGED, feature);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.beta.plugin.Controller#erzeugeLaufliste()
     */
    public static <T extends ASchwimmer> void erzeugeLaufliste(IPluginManager controller, IFeature feature, AWettkampf<T> wk) {
        wk.getLaufliste().erzeugen();
        controller.sendDataUpdateEvent("NewHeatlist", UpdateEventConstants.REASON_LAUF_LIST_CHANGED, feature);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.beta.plugin.Controller#erzeugeLaufliste()
     */
    public static <T extends ASchwimmer> void erzeugeLaufliste(IPluginManager controller, IFeature feature, AWettkampf<T> wk,
            Laufliste.BlockEinteilung[] data) {
        boolean reordered = wk.getLaufliste().erzeugen(data);
        controller.sendDataUpdateEvent("NewHeatlist", UpdateEventConstants.REASON_LAUF_LIST_CHANGED
                | (reordered
                        ? UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_MELDEZEITEN_CHANGED | UpdateEventConstants.REASON_PENALTY
                                | UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED
                        : UpdateEventConstants.NOTHING),
                feature);
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.beta.plugin.Controller#erzeugeLaufliste()
     */
    public static <T extends ASchwimmer> void erzeugeLaufliste(IPluginManager controller, IFeature feature, AWettkampf<T> wk, Laufliste.Einteilung[] data) {
        boolean reordered = wk.getLaufliste().erzeugen(data);
        controller.sendDataUpdateEvent("NewHeatlist", UpdateEventConstants.REASON_LAUF_LIST_CHANGED
                | (reordered
                        ? UpdateEventConstants.REASON_AKS_CHANGED | UpdateEventConstants.REASON_MELDEZEITEN_CHANGED | UpdateEventConstants.REASON_PENALTY
                                | UpdateEventConstants.REASON_POINTS_CHANGED | UpdateEventConstants.REASON_SWIMMER_CHANGED
                        : UpdateEventConstants.NOTHING),
                feature);
    }

}
