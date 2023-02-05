package de.df.jauswertung.gui.plugins.heats;

import java.util.function.Consumer;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.IFeature;
import de.df.jutils.plugin.IPluginManager;

public class EditHeatlistUtils {

    private static final class EditFinishedListener<T extends ASchwimmer>
            implements Consumer<JLauflisteBearbeiten<T>> {

        private final IPluginManager controller;
        private final IFeature plugin;
        private final AWettkampf<T> wk;
        private final Consumer<AWettkampf<T>> callback;

        public EditFinishedListener(IPluginManager controller, IFeature plugin, AWettkampf<T> wk,
                Consumer<AWettkampf<T>> callback) {
            this.controller = controller;
            this.plugin = plugin;
            this.wk = wk;
            this.callback = callback;
        }

        @Override
        public void accept(JLauflisteBearbeiten<T> t) {
            if (t.isChanged()) {
                notifyHeatlistChanged(controller, plugin);
                if (callback != null) {
                    callback.accept(wk);
                }
            }
        }
    }

    public static <T extends ASchwimmer> void laufliste(IPluginManager controller, IFeature plugin, AWettkampf<T> wk,
            boolean editable,
            Consumer<AWettkampf<T>> callback) {
        Consumer<JLauflisteBearbeiten<T>> sc = null;
        if (editable) {
            sc = new EditFinishedListener<T>(controller, plugin, wk, callback);
        }
        ModalFrameUtil.showAsModal(new JLauflisteBearbeiten<T>(controller.getWindow(), wk, editable, sc),
                controller.getWindow());
    }

    public static void notifyHeatlistChanged(IPluginManager controller, IFeature plugin) {
        controller.sendDataUpdateEvent("ChangeHeats", UpdateEventConstants.REASON_LAUF_LIST_CHANGED, plugin);
    }

    public static void notifyPropertyChanged(IPluginManager controller, IFeature plugin) {
        controller.sendDataUpdateEvent("ChangeProperties", UpdateEventConstants.REASON_PROPERTIES_CHANGED, plugin);
    }
}
