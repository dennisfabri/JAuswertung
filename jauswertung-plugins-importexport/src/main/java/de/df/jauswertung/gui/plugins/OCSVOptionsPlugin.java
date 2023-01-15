/*
 * Created on 05.12.2004
 */
package de.df.jauswertung.gui.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.CsvExporter;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

public class OCSVOptionsPlugin extends AFeature implements MOptionenPlugin.OptionsPlugin {

    JPanel optionsPanel = null;
    JRadioButton excel = null;
    JRadioButton correct = null;

    MOptionenPlugin options;

    public OCSVOptionsPlugin() {
        CsvExporter.excelmode = Utils.getPreferences().getBoolean("ExcelMode", true);
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        options = (MOptionenPlugin) getController().getFeature("de.df.jauswertung.options", getUid());
        options.addOptionsPlugin(this);
        Thread t = new Thread("OCSVOptionsPlugin.PanelInitialization") {
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
            EDTUtils.executeOnEDT(new Runnable() {
                @Override
                public void run() {
                    createPanel();
                }
            });
        }
        return new JPanel[] { optionsPanel };
    }

    void createPanel() {
        excel = new JRadioButton(I18n.get("CSVExcelCompatible"));
        excel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                options.notifyChange();
            }
        });
        correct = new JRadioButton(I18n.get("CSVCorrect"));
        correct.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                options.notifyChange();
            }
        });

        ButtonGroup bg = new ButtonGroup();
        bg.add(excel);
        bg.add(correct);

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu", FormLayoutUtils.createLayoutString(3));
        optionsPanel = new JPanel(layout);
        optionsPanel.setName(I18n.get("CSV"));
        optionsPanel.setToolTipText(I18n.getToolTip("CSVOptions"));

        optionsPanel.add(correct, CC.xy(2, 2));
        optionsPanel.add(excel, CC.xy(2, 4));

        excel.setSelected(CsvExporter.excelmode);
        correct.setSelected(!CsvExporter.excelmode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#ok()
     */
    @Override
    public void apply() {
        apply(excel.isSelected());
    }

    private static void apply(boolean excelmode) {
        CsvExporter.excelmode = excelmode;
        Utils.getPreferences().putBoolean("ExcelMode", excelmode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#cancel()
     */
    @Override
    public void cancel() {
        excel.setSelected(CsvExporter.excelmode);
        correct.setSelected(!CsvExporter.excelmode);
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