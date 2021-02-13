/*
 * Created on 05.12.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleTableLayout;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

public class OBetaPlugin extends AFeature implements MOptionenPlugin.OptionsPlugin {

    JPanel                           optionsPanel;

    JCheckBox                        oldCheckBox;
    JCheckBox                        betaCheckBox;

    boolean                          betaEnabled = false;
    boolean                          oldEnabled  = false;

    MOptionenPlugin                  optionen;

    private LinkedList<BetaListener> betaListeners;
    private LinkedList<BetaListener> oldListeners;

    public OBetaPlugin() {
        betaListeners = new LinkedList<BetaListener>();
        oldListeners = new LinkedList<BetaListener>();

        optionsPanel = new JPanel(new SimpleTableLayout(1, 5, 5));
        optionsPanel.setName(I18n.get("Beta"));
        optionsPanel.setName(I18n.getToolTip("BetaOptions"));
        optionsPanel.setBorder(BorderUtils.createSpaceBorder());

        oldCheckBox = new JCheckBox(I18n.get("EnableOldFeatures"));
        oldCheckBox.setToolTipText("EnableOldFeatures");
        oldCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optionen.notifyChange();
            }
        });
        oldCheckBox.setEnabled(false);
        oldCheckBox.setSelected(oldEnabled);

        betaCheckBox = new JCheckBox(I18n.get("EnableBetaFeatures"));
        betaCheckBox.setToolTipText("EnableBetaFeatures");
        betaCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optionen.notifyChange();
            }
        });
        betaCheckBox.setEnabled(false);
        betaCheckBox.setSelected(betaEnabled);

        optionsPanel.add(oldCheckBox);
        optionsPanel.add(betaCheckBox);
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);

        optionen = (MOptionenPlugin) plugincontroller.getFeature("de.df.jauswertung.options", pluginuid);
    }

    /*
     * (non-Javadoc)
     * @see
     * de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#getPanel()
     */
    @Override
    public JPanel[] getPanels() {
        if (!Utils.isInDevelopmentMode()) {
            return new JPanel[0];
        }
        return new JPanel[] { optionsPanel };
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#ok()
     */
    @Override
    public void apply() {
        if (betaEnabled != betaCheckBox.isSelected()) {
            betaEnabled = betaCheckBox.isSelected();
            for (BetaListener listener : betaListeners) {
                try {
                    listener.stateChanged(betaEnabled);
                } catch (Exception t) {
                    t.printStackTrace();
                }
            }
        }
        if (oldEnabled != oldCheckBox.isSelected()) {
            oldEnabled = oldCheckBox.isSelected();
            for (BetaListener listener : oldListeners) {
                try {
                    listener.stateChanged(oldEnabled);
                } catch (Exception t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public boolean getBetaState() {
        return betaEnabled;
    }

    public boolean getOldState() {
        return oldEnabled;
    }

    /*
     * (non-Javadoc)
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#cancel()
     */
    @Override
    public void cancel() {
        betaCheckBox.setSelected(betaEnabled);
        oldCheckBox.setSelected(oldEnabled);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // Nothing to do here
    }

    public void addBetaListener(BetaListener listener) {
        betaListeners.add(listener);
        betaCheckBox.setEnabled(true);
        update();
    }

    public void addOldListener(BetaListener listener) {
        oldListeners.add(listener);
        oldCheckBox.setEnabled(true);
        update();
    }

    public static interface BetaListener {
        void stateChanged(boolean enabled);
    }

    private void update() {
        switch (betaListeners.size() + oldListeners.size()) {
        case 0:
            optionen.removeOptionsPlugin(this);
            break;
        case 1:
            optionen.addOptionsPlugin(this);
            break;
        default:
            break;
        }
    }

    @Override
    public boolean isOk() {
        return true;
    }
}