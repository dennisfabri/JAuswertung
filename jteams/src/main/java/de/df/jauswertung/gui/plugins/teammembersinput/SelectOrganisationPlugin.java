package de.df.jauswertung.gui.plugins.teammembersinput;

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

public class SelectOrganisationPlugin extends ANullPlugin {

    private CorePlugin core;

    private JComponent[] buttons;
    private JLabel label = new JLabel(I18n.get("Organisation"));
    private JComboBox<String> selection = new JComboBox<>();
    private String[] data = new String[0];
    private LinkedList<ISelectionListener> listeners = new LinkedList<>();

    public void addSelectionListener(ISelectionListener listener) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    public void removeelectionListener(ISelectionListener listener) {
        if (!listeners.contains(listener)) {
            return;
        }
        listeners.remove(listener);
    }

    private void notifyListeners() {
        String selected = (String) this.selection.getSelectedItem();
        for (ISelectionListener listener : listeners) {
            try {
                listener.selected(selected);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public SelectOrganisationPlugin() {
        buttons = new JComponent[] { label, selection };
        selection.addActionListener(e -> {
            notifyListeners();
        });
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if (due.isSource(this)) {
            return;
        }
        if (due.isReason(
                UpdateEventConstants.REASON_GLIEDERUNG_CHANGED | UpdateEventConstants.REASON_FILTER_SELECTION
                        | UpdateEventConstants.REASON_FILTERS_CHANGED
                        | UpdateEventConstants.REASON_LOAD_WK | UpdateEventConstants.REASON_NEW_LOAD_WK
                        | UpdateEventConstants.REASON_NEW_TN
                        | UpdateEventConstants.REASON_NEW_WK | UpdateEventConstants.REASON_SWIMMER_DELETED
                        | UpdateEventConstants.REASON_SWIMMER_CHANGED)) {
            String gld = (String) selection.getSelectedItem();
            AWettkampf<?> wk = core.getWettkampf();
            LinkedList<String> orgs = wk.getGliederungenMitQGliederung();
            if (orgs.size() != data.length) {
                data = orgs.toArray(new String[orgs.size()]);
                selection.setModel(new DefaultComboBoxModel<>(data));
                if (gld != null && orgs.contains(gld) && !gld.equals(selection.getSelectedItem())) {
                    selection.setSelectedItem(gld);
                }
                notifyListeners();
            }
        }
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return new ButtonInfo[] { new ButtonInfo(buttons, 500) };
    }

    public String getSelectedOrganisation() {
        if (selection == null) {
            return null;
        }
        return (String) selection.getSelectedItem();
    }

}
