package de.df.jauswertung.gui.plugins.upload;

import javax.swing.JPanel;

import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.plugins.MOptionenPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

@SuppressWarnings("unused")
public class OUploadOptionsPlugin extends AFeature implements MOptionenPlugin.OptionsPlugin {

    JPanel optionsPanel = null;
    JWarningTextField url = null;

    MOptionenPlugin options;

    public OUploadOptionsPlugin() {
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        options = (MOptionenPlugin) getController().getFeature("de.df.jauswertung.options", getUid());
        options.addOptionsPlugin(this);
        Thread t = new Thread("OUploadOptionsPlugin.PanelInitialization") {
            @Override
            public void run() {
                getPanels();
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#getPanel()
     */
    @Override
    public synchronized JPanel[] getPanels() {
        if (optionsPanel == null) {
            EDTUtils.executeOnEDT(this::createPanel);
        }
        return new JPanel[] { optionsPanel };
    }

    void createPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu", FormLayoutUtils.createLayoutString(3));
        optionsPanel = new JPanel(layout);
        optionsPanel.setName(I18n.get("CSV"));
        optionsPanel.setToolTipText(I18n.getToolTip("CSVOptions"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#ok()
     */
    @Override
    public void apply() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#cancel()
     */
    @Override
    public void cancel() {
    }

    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // Nothing to do here
    }
}