package de.df.jauswertung.gui.plugins;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.lisasp.swing.filechooser.FileChooserUtils;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.print.PrintManager;

public final class OAdvertisementOptionsPlugin extends ANullPlugin implements MOptionenPlugin.OptionsPlugin {

    MOptionenPlugin   optionen = null;

    private JPanel    panel    = null;

    JCheckBox         enabled;
    boolean           oldstate = true;

    JWarningTextField ptop;
    JWarningTextField pbottom;
    JWarningTextField ltop;
    JWarningTextField lbottom;

    JButton           ptopfile;
    JButton           pbottomfile;
    JButton           ltopfile;
    JButton           lbottomfile;

    public OAdvertisementOptionsPlugin() {
        // Nothing to do
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        optionen = (MOptionenPlugin) plugincontroller.getFeature("de.df.jauswertung.options", pluginuid);
        optionen.addOptionsPlugin(this);

        getPanels();
        apply();
    }

    @Override
    public void apply() {
        Utils.getPreferences().putBoolean("AdvertisementEnabled", enabled.isSelected());
        Utils.getPreferences().put("AdvertisementPortraitTop", ptop.getText());
        Utils.getPreferences().put("AdvertisementPortraitBottom", pbottom.getText());
        Utils.getPreferences().put("AdvertisementLandscapeTop", ltop.getText());
        Utils.getPreferences().put("AdvertisementLandscapeBottom", lbottom.getText());

        BufferedImage iptop = null;
        BufferedImage ipbottom = null;
        BufferedImage iltop = null;
        BufferedImage ilbottom = null;
        if (enabled.isSelected()) {
            try {
                iptop = ImageIO.read(new File(ptop.getText()));
            } catch (IOException io) {
                // Nothing to do
            }
            try {
                ipbottom = ImageIO.read(new File(pbottom.getText()));
            } catch (IOException io) {
                // Nothing to do
            }
            try {
                iltop = ImageIO.read(new File(ltop.getText()));
            } catch (IOException io) {
                // Nothing to do
            }
            try {
                ilbottom = ImageIO.read(new File(lbottom.getText()));
            } catch (IOException io) {
                // Nothing to do
            }

            PrintManager.registerAds(iptop, ipbottom, iltop, ilbottom, I18n.get("Results"), I18n.get("DisciplineOverAllAgeGroups"), I18n.get("Einzelwertung"),
                    I18n.get("GroupEvaluation"), I18n.get("ZWList"), I18n.get("ZWResults"), I18n.get("Heatarrangement"), I18n.get("Laufliste"),
                    I18n.get("Heatoverview"), I18n.get("ListOfMedals"), I18n.get("Registrations"), I18n.get("Meldezeiten"), I18n.get("Penaltylist"),
                    I18n.get("Startunterlagenkontrolle"), I18n.get("Statistics"), I18n.get("TeamGroupEvaluation"), I18n.get("TeamResults"),
                    I18n.get("Registration"), I18n.get("Disciplines"));
        } else {
            PrintManager.registerAds(null, null, null, null);
        }
    }

    @Override
    public void cancel() {
        enabled.setSelected(Utils.getPreferences().getBoolean("AdvertisementEnabled", false));
        ptop.setText(Utils.getPreferences().get("AdvertisementPortraitTop", ""));
        pbottom.setText(Utils.getPreferences().get("AdvertisementPortraitBottom", ""));
        ltop.setText(Utils.getPreferences().get("AdvertisementLandscapeTop", ""));
        lbottom.setText(Utils.getPreferences().get("AdvertisementLandscapeBottom", ""));
    }

    @Override
    public JPanel[] getPanels() {
        if (panel == null) {
            enabled = new JCheckBox(I18n.get("AdvertisementEnabled"));
            enabled.setSelected(true);
            enabled.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent ce) {
                    boolean e = enabled.isSelected();
                    ptop.setEnabled(e);
                    pbottom.setEnabled(e);
                    ltop.setEnabled(e);
                    lbottom.setEnabled(e);
                    ptopfile.setEnabled(e);
                    pbottomfile.setEnabled(e);
                    ltopfile.setEnabled(e);
                    lbottomfile.setEnabled(e);

                    if (e != oldstate) {
                        optionen.notifyChange();
                        oldstate = e;
                    }
                }
            });

            ptop = getTextField();
            pbottom = getTextField();
            ltop = getTextField();
            lbottom = getTextField();

            ptopfile = new JButton(I18n.get("Dots"));
            pbottomfile = new JButton(I18n.get("Dots"));
            ltopfile = new JButton(I18n.get("Dots"));
            lbottomfile = new JButton(I18n.get("Dots"));

            cancel();

            SimpleFormBuilder sfb = new SimpleFormBuilder();
            sfb.add(enabled);
            sfb.addSeparator(I18n.get("Portrait"));
            sfb.add(I18n.get("Top"), getButtons(ptop, ptopfile));
            sfb.add(I18n.get("Bottom"), getButtons(pbottom, pbottomfile));
            sfb.addSeparator(I18n.get("Landscape"));
            sfb.add(I18n.get("Top"), getButtons(ltop, ltopfile));
            sfb.add(I18n.get("Bottom"), getButtons(lbottom, lbottomfile));

            panel = sfb.getPanel();

            JPanel p = new JPanel(new BorderLayout());
            p.add(panel);
            panel = p;

            panel.setName(I18n.get("Advertisement"));
        }
        return new JPanel[] { panel };
    }

    private JComponent getButtons(JTextField jtf, JButton file) {
        file.addActionListener(new FileActionListener(jtf));

        JPanel p = new JPanel(new FormLayout("0dlu,fill:default:grow,0dlu,fill:default,0dlu", "0dlu,fill:default,0dlu"));
        p.add(jtf, CC.xy(2, 2));
        p.add(file, CC.xy(4, 2));
        return p;
    }

    private final class FileActionListener implements ActionListener {

        private final JTextField jtf;

        public FileActionListener(JTextField tf) {
            jtf = tf;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            String d = FileChooserUtils.openFile(getController().getWindow());
            if (d != null) {
                jtf.setText(d);
            }
        }
    }

    private JWarningTextField getTextField() {
        JWarningTextField jtf = new JWarningTextField(false, false) {
            @Override
            public boolean isValidString() {
                if (!super.isValidString()) {
                    return false;
                }
                String name = getText();
                if (name.length() == 0) {
                    return true;
                }
                File f = new File(name);
                try {
                    return f.exists() && f.isFile() && (ImageIO.read(f) != null);
                } catch (IOException io) {
                    return false;
                }
            }
        };
        jtf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent arg0) {
                optionen.notifyChange();
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                changedUpdate(arg0);
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                changedUpdate(arg0);
            }
        });
        return jtf;
    }

    @Override
    public boolean isOk() {
        return ptop.isValidString() && pbottom.isValidString() && ltop.isValidString() && lbottom.isValidString();
    }
}
