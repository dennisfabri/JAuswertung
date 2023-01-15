/**
 * 
 */
package de.df.jauswertung.gui.plugins.http;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.plugins.MOptionenPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.layout.FormLayoutUtils;

class HttpOptionsPlugin implements MOptionenPlugin.OptionsPlugin {

    final HttpServerPlugin parent;

    /**
     * @param httpServerPlugin
     */
    HttpOptionsPlugin(HttpServerPlugin httpServerPlugin) {
        parent = httpServerPlugin;
    }

    private JPanel panel;
    JIntegerField portField;
    JCheckBox defaultPort;

    JRadioButton exportEverything;
    JRadioButton exportOnlyComplete;
    JRadioButton exportFiltered;
    ButtonGroup exportGroup;

    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public void apply() {
        if (defaultPort.isSelected()) {
            parent.setPort(80);
            portField.setInt(80);
        } else {
            parent.setPort(portField.getInt());
        }
        ExportMode mode = ExportMode.Everything;
        if (exportOnlyComplete.isSelected()) {
            mode = ExportMode.Completed;
        } else if (exportFiltered.isSelected()) {
            mode = ExportMode.Filtered;
        }
        parent.setExportMode(mode);
    }

    @Override
    public void cancel() {
        portField.setInt(parent.getPort());
        defaultPort.setSelected(parent.getPort() == 80);
        ExportMode mode = parent.getExportMode();
        switch (mode) {
        default:
            exportEverything.setSelected(true);
            break;
        case Completed:
            exportOnlyComplete.setSelected(true);
            break;
        case Filtered:
            exportFiltered.setSelected(true);
            break;
        }
    }

    public void setEnabled(boolean b) {
        if (panel != null) {
            defaultPort.setEnabled(b);
            if (b) {
                portField.setEnabled(!defaultPort.isSelected());
            } else {
                portField.setEnabled(false);
            }
        }
    }

    @Override
    public JPanel[] getPanels() {
        if (panel == null) {
            FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu",
                    FormLayoutUtils.createLayoutString(5));

            panel = new JPanel(layout);
            panel.setName(I18n.get("HttpServer"));
            panel.setToolTipText(I18n.getToolTip("HttpServer"));

            portField = new JIntegerField(parent.getPort(), 65536, true, true);
            portField.setToolTipText(I18n.getToolTip("HttpServerPort"));
            portField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                    parent.optionen.notifyChange();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    changedUpdate(e);
                }
            });

            defaultPort = new JCheckBox(I18n.get("DefaultPort"));
            defaultPort.setToolTipText(I18n.getToolTip("HttpServerDefaultPort"));
            defaultPort.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    portField.setEnabled(!defaultPort.isSelected());
                    if (defaultPort.isSelected()) {
                        portField.setInt(80);
                    }
                    parent.optionen.notifyChange();
                }
            });

            exportOnlyComplete = new JRadioButton(I18n.get("SendOnlyCompleteDisciplines"));
            exportOnlyComplete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    parent.optionen.notifyChange();
                }
            });
            exportFiltered = new JRadioButton(I18n.get("SendOnlyFilteredData"));
            exportFiltered.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    parent.optionen.notifyChange();
                }
            });
            exportEverything = new JRadioButton(I18n.get("SendEverything"));
            exportEverything.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    parent.optionen.notifyChange();
                }
            });

            exportGroup = new ButtonGroup();
            exportGroup.add(exportEverything);
            exportGroup.add(exportFiltered);
            exportGroup.add(exportOnlyComplete);

            if (parent.isRunning()) {
                setEnabled(false);
            }

            panel.add(new JLabel("Port"), CC.xy(2, 2));
            panel.add(portField, CC.xy(4, 2));
            panel.add(defaultPort, CC.xy(6, 2));
            panel.add(new JLabel(I18n.get("SendTitle")), CC.xyw(2, 4, 5));
            panel.add(exportEverything, CC.xyw(4, 6, 3));
            panel.add(exportOnlyComplete, CC.xyw(4, 8, 3));
            panel.add(exportFiltered, CC.xyw(4, 10, 3));

            cancel();
        }
        return new JPanel[] { panel };
    }
}