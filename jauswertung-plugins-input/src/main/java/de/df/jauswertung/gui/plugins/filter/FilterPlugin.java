/*
 * Created on 15.03.2006
 */
package de.df.jauswertung.gui.plugins.filter;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_FILTERS_CHANGED;

import java.awt.event.ItemListener;
import java.util.function.Consumer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import de.df.jauswertung.daten.Filter;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.WarningPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class FilterPlugin extends ANullPlugin {

    private JComboBox<Filter> filters = null;
    private ButtonInfo[] bi = null;
    private ItemListener il = null;

    private JMenuItem menu = null;
    private MenuInfo[] mi = null;

    private CorePlugin core = null;
    private WarningPlugin warn = null;

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        warn = (WarningPlugin) plugincontroller.getFeature("de.df.jauswertung.warning", pluginuid);
    }

    private void init() {
        if (mi != null) {
            return;
        }
        menu = new JMenuItem(I18n.get("OutputFilter"));
        menu.setToolTipText(I18n.getToolTip("DefineOutputFilter"));
        menu.addActionListener(arg0 -> {
            changeFilters();
        });

        mi = new MenuInfo[1];
        mi[0] = new MenuInfo(I18n.get("Execute"), 520, menu, 5000);
    }

    private void initButton() {
        if (bi != null) {
            return;
        }
        il = arg0 -> {
            updateButtonSelection();
        };

        filters = new JComboBox<>();
        filters.setToolTipText(I18n.getToolTip("SelectOutputFilter"));
        filters.setEnabled(false);

        JLabel label = new JLabel(I18n.get("OutputFilter") + "  ");
        JComponent[] cs = new JComponent[] { label, filters };

        bi = new ButtonInfo[1];
        bi[0] = new ButtonInfo(cs, 5000);
    }

    @Override
    public MenuInfo[] getMenues() {
        init();
        return mi;
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        initButton();
        return bi;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        boolean enabled = core.getWettkampf().hasSchwimmer();
        if ((due.getChangeReason() & REASON_FILTERS_CHANGED) > 0) {
            filters.removeItemListener(il);
            filters.removeAllItems();

            filters.setModel(new DefaultComboBoxModel<>(core.getWettkampf().getFilter()));
            if (core.getWettkampf().isHeatBased()) {
                filters.setSelectedIndex(0);
                filters.addItemListener(il);
                enabled = false;
                if (core.getWettkampf().getCurrentFilterIndex() > 0) {
                    new Thread() {
                        @Override
                        public void run() {
                            core.getWettkampf().setCurrentFilterIndex(0);
                            getController().sendDataUpdateEvent("FiltersChanged",
                                    UpdateEventConstants.REASON_FILTERS_CHANGED, FilterPlugin.this);
                        }
                    }.start();
                }
            } else {
                int index = core.getWettkampf().getCurrentFilterIndex();
                if (index < filters.getItemCount()) {
                    filters.setSelectedIndex(index);
                } else {
                    filters.setSelectedIndex(filters.getItemCount() - 1);
                }

                filters.addItemListener(il);
            }
            filters.setEnabled(filters.getItemCount() > 1);
        }
        menu.setEnabled(enabled);
    }

    void selectFilter(Filter filter) {
        if (filters.getModel().getSize() == 0) {
            return;
        }
        if (filter == null) {
            if (filters.getSelectedIndex() != 0) {
                filters.setSelectedIndex(0);
            }
        } else {
            if (filters.getSelectedItem() != filter) {
                filters.setSelectedItem(filter);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void changeFilters() {
        Consumer<JFilterFrame> sc = this::internalCallback;
        JFilterFrame jfd = new JFilterFrame(getController().getWindow(), core.getWettkampf(), sc);
        ModalFrameUtil.showAsModal(jfd, getController().getWindow());
    }

    /**
     * @param jfd
     */
    @SuppressWarnings("rawtypes")
    void internalCallback(JFilterFrame jfd) {
        if (jfd.hasChanged()) {
            core.getWettkampf().setFilter(jfd.getFilter());
            getController().sendDataUpdateEvent("FiltersChanged", UpdateEventConstants.REASON_FILTERS_CHANGED, this);
        }
        selectFilter(jfd.getSelectedFilter());
    }

    void updateButtonSelection() {
        int index = Math.max(0, filters.getSelectedIndex());
        if (index != core.getWettkampf().getCurrentFilterIndex()) {
            core.getWettkampf().setCurrentFilterIndex(index);
            getController().sendDataUpdateEvent("FilterSelection", UpdateEventConstants.REASON_FILTER_SELECTION, this);
            if (index > 0) {
                warn.information(getController().getWindow(), null, I18n.get("Filter.Information"),
                        I18n.get("Filter.Note"), "FilterInfo");
            }
        }
    }
}