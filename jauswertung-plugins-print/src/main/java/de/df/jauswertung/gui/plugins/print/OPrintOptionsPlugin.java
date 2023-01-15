/*
 * Created on 05.12.2004
 */
package de.df.jauswertung.gui.plugins.print;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.fontchooser.JFontChooser;

import de.df.jauswertung.gui.plugins.MOptionenPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.JResultTable;
import de.df.jauswertung.print.PrintUtils;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.jtable.JPrintTable;
import de.df.jutils.gui.layout.ListLayout;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintManager;

public class OPrintOptionsPlugin extends AFeature implements MOptionenPlugin.OptionsPlugin {

    private static final String RESULTS_KEY = "ResultsHorizontalInPrototcol";
    private static final String EMPTY_LANES_KEY = "PrintEmptyLanes";
    private static final String EMPTY_CARDS_KEY = "PrintEmptyCards";
    private static final String POINTS_IN_DISCIPLINE_RESULTS = "PrintPointsInDisciplineResults";
    private static final String CHECKSUM = "PrintChecksum";
    private static final String ROWMARKER = "PrintRowMarker";
    private static final String PRINT_DNS = "PrintDidNotStart";
    private static final String PRINT_RANKS = "PrintRanksInResults";
    private static final String PRINT_HLW_NAMES = "PrintZWnames";
    private static final String OMIT_ORGANISATION_FOR_TEAM = "PrintOmitOrganisationForTeams";

    JFontChooser fontChooser = null;
    JPanel fontPanel = null;

    JCheckBox results = null;
    JCheckBox emptylanes = null;
    JCheckBox emptycards = null;
    JCheckBox points = null;
    JCheckBox checksum = null;
    JCheckBox printdns = null;
    JCheckBox printranks = null;
    JCheckBox hlwNames = null;
    JCheckBox omitOrganisationForTeams = null;

    JLabel rowmarkercolor = null;
    BufferedImage rowmarkerimage = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
    ImageIcon rowmarkericon = new ImageIcon(rowmarkerimage);
    JSlider rowmarker = null;

    JPanel printPanel = null;

    MOptionenPlugin options;

    public OPrintOptionsPlugin() {
        PrintUtils.printProtocolResultsHorizontal = Utils.getPreferences().getBoolean(RESULTS_KEY, false);
        PrintUtils.printEmptyLanes = Utils.getPreferences().getBoolean(EMPTY_LANES_KEY, true);
        PrintUtils.printEmptyCards = Utils.getPreferences().getBoolean(EMPTY_CARDS_KEY, false);
        PrintUtils.printPointsInDisciplineResults = Utils.getPreferences().getBoolean(POINTS_IN_DISCIPLINE_RESULTS,
                true);
        PrintUtils.printChecksum = Utils.getPreferences().getBoolean(CHECKSUM, true);
        PrintUtils.printDidNotStart = Utils.getPreferences().getBoolean(PRINT_DNS, false);
        JResultTable.printRanksInResults = Utils.getPreferences().getBoolean(PRINT_RANKS, false);
        JPrintTable.setRowMarker(Utils.getPreferences().getInt(ROWMARKER, 240));
        PrintUtils.printZWnames = Utils.getPreferences().getBoolean(PRINT_HLW_NAMES, false);
        PrintUtils.printOmitOrganisationForTeams = Utils.getPreferences().getBoolean(OMIT_ORGANISATION_FOR_TEAM, false);
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        options = (MOptionenPlugin) getController().getFeature("de.df.jauswertung.options", getUid());
        options.addOptionsPlugin(this);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getPanels();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#getPanel()
     */
    @Override
    public synchronized JPanel[] getPanels() {
        if (fontPanel == null) {
            {
                fontPanel = new JPanel(new FormLayout("0dlu,fill:default:grow,0dlu", "0dlu,fill:default:grow,0dlu"));
                fontPanel.setName(I18n.get("PrintFont"));

                fontChooser = new JFontChooser();
                fontChooser.setSelectedFont(PrintManager.getFont());
                fontChooser.addPropertyChangeListener(JFontChooser.SELECTED_FONT_CHANGED_KEY,
                        new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent evt) {
                                options.notifyChange();
                            }
                        });
                JScrollPane scroll = new JScrollPane(fontChooser);
                scroll.setBorder(null);
                fontPanel.add(scroll, CC.xy(2, 2));
            }

            results = new JCheckBox(I18n.get("PrintResultsHorizontallyInProtocol"));
            results.setSelected(PrintUtils.printProtocolResultsHorizontal);
            results.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    options.notifyChange();
                }
            });

            emptylanes = new JCheckBox(I18n.get("PrintEmptyLanes"));
            emptylanes.setSelected(PrintUtils.printEmptyLanes);
            emptylanes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    options.notifyChange();
                }
            });

            printdns = new JCheckBox(I18n.get("PrintDidNotStart"));
            printdns.setSelected(PrintUtils.printDidNotStart);
            printdns.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    options.notifyChange();
                }
            });

            emptycards = new JCheckBox(I18n.get("PrintEmptyCards"));
            emptycards.setSelected(PrintUtils.printEmptyCards);
            emptycards.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    options.notifyChange();
                }
            });

            points = new JCheckBox(I18n.get("PrintPointsInDisciplineResults"));
            points.setSelected(PrintUtils.printPointsInDisciplineResults);
            points.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    options.notifyChange();
                }
            });

            checksum = new JCheckBox(I18n.get("PrintChecksum"));
            checksum.setSelected(PrintUtils.printChecksum);
            checksum.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    options.notifyChange();
                }
            });

            printranks = new JCheckBox(I18n.get("PrintRanksInResults"));
            printranks.setSelected(JResultTable.printRanksInResults);
            printranks.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    options.notifyChange();
                }
            });

            hlwNames = new JCheckBox(I18n.get("PrintZWnames"));
            hlwNames.setSelected(PrintUtils.printZWnames);
            hlwNames.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    options.notifyChange();
                }
            });

            omitOrganisationForTeams = new JCheckBox(I18n.get("PrintOmitOrganisationForTeams"));
            omitOrganisationForTeams.setSelected(PrintUtils.printOmitOrganisationForTeams);
            omitOrganisationForTeams.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    options.notifyChange();
                }
            });

            rowmarker = new JSlider(0, 255, JPrintTable.getRowMarker());
            rowmarker.setMajorTickSpacing(32);
            rowmarker.setMinorTickSpacing(16);
            // rowmarker.setPaintLabels(true);
            // rowmarker.setPaintTrack(true);
            // rowmarker.setPaintTicks(true);
            rowmarker.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    options.notifyChange();
                    updateRowMarkerColor();
                }
            });
            rowmarkercolor = new JLabel(new ImageIcon(rowmarkerimage));
            JPanel rowmarkerpanel = new JPanel(new BorderLayout(5, 5));
            rowmarkerpanel.add(rowmarker, BorderLayout.CENTER);
            rowmarkerpanel.add(rowmarkercolor, BorderLayout.WEST);
            updateRowMarkerColor();

            printPanel = new JPanel(new ListLayout(8));
            printPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
            printPanel.setName(I18n.get("Print"));
            printPanel.add(results);
            printPanel.add(emptylanes);
            printPanel.add(emptycards);
            printPanel.add(points);
            printPanel.add(checksum);
            printPanel.add(printdns);
            printPanel.add(printranks);
            printPanel.add(hlwNames);
            printPanel.add(omitOrganisationForTeams);
            printPanel.add(new JLabel(I18n.get("PrintRowMarker")));
            printPanel.add(rowmarkerpanel);

            {
                JPanel p = new JPanel(new BorderLayout());
                JScrollPane scroll = new JScrollPane(printPanel);
                scroll.setBorder(null);
                p.add(scroll);
                p.setName(printPanel.getName());
                printPanel = p;
            }
        }
        return new JPanel[] { fontPanel, printPanel };
    }

    void updateRowMarkerColor() {
        int value = rowmarker.getValue();
        Graphics g = rowmarkerimage.getGraphics();
        g.setColor(new Color(value, value, value));
        g.fillRect(0, 0, rowmarkerimage.getWidth(), rowmarkerimage.getHeight());
        rowmarkericon.setImage(rowmarkerimage);
        rowmarkercolor.setIcon(rowmarkericon);
        rowmarkercolor.updateUI();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#ok()
     */
    @Override
    public void apply() {
        PrintManager.setFont(fontChooser.getSelectedFont());

        PrintUtils.printProtocolResultsHorizontal = results.isSelected();
        PrintUtils.printEmptyLanes = emptylanes.isSelected();
        PrintUtils.printEmptyCards = emptycards.isSelected();
        PrintUtils.printPointsInDisciplineResults = points.isSelected();
        PrintUtils.printChecksum = checksum.isSelected();
        PrintUtils.printDidNotStart = printdns.isSelected();
        JResultTable.printRanksInResults = printranks.isSelected();
        PrintUtils.printZWnames = hlwNames.isSelected();
        PrintUtils.printOmitOrganisationForTeams = omitOrganisationForTeams.isSelected();
        JPrintTable.setRowMarker(rowmarker.getValue());

        Utils.getPreferences().putBoolean(RESULTS_KEY, results.isSelected());
        Utils.getPreferences().putBoolean(EMPTY_LANES_KEY, emptylanes.isSelected());
        Utils.getPreferences().putBoolean(EMPTY_CARDS_KEY, emptycards.isSelected());
        Utils.getPreferences().putBoolean(POINTS_IN_DISCIPLINE_RESULTS, points.isSelected());
        Utils.getPreferences().putBoolean(CHECKSUM, checksum.isSelected());
        Utils.getPreferences().putBoolean(PRINT_DNS, printdns.isSelected());
        Utils.getPreferences().putBoolean(PRINT_RANKS, printranks.isSelected());
        Utils.getPreferences().putInt(ROWMARKER, rowmarker.getValue());
        Utils.getPreferences().putBoolean(PRINT_HLW_NAMES, PrintUtils.printZWnames);
        Utils.getPreferences().putBoolean(OMIT_ORGANISATION_FOR_TEAM, PrintUtils.printOmitOrganisationForTeams);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.df.jauswertung.gui.plugins.MOptionenPlugin.OptionsPlugin#cancel()
     */
    @Override
    public void cancel() {
        fontChooser.setSelectedFont(PrintManager.getFont());

        results.setSelected(PrintUtils.printProtocolResultsHorizontal);
        emptylanes.setSelected(PrintUtils.printEmptyLanes);
        emptycards.setSelected(PrintUtils.printEmptyCards);
        points.setSelected(PrintUtils.printPointsInDisciplineResults);
        checksum.setSelected(PrintUtils.printChecksum);
        printdns.setSelected(PrintUtils.printDidNotStart);
        printranks.setSelected(JResultTable.printRanksInResults);
        rowmarker.setValue(JPrintTable.getRowMarker());
        hlwNames.setSelected(PrintUtils.printZWnames);
        omitOrganisationForTeams.setSelected(PrintUtils.printOmitOrganisationForTeams);
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // Nothing to do here
    }

    @Override
    public boolean isOk() {
        return true;
    }
}