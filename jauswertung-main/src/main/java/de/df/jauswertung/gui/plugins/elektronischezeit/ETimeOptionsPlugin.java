/**
 * 
 */
package de.df.jauswertung.gui.plugins.elektronischezeit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.plugins.MOptionenPlugin;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.Sources;
import de.df.jauswertung.gui.plugins.elektronischezeit.sources.SourcesConfig;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.layout.FormLayoutUtils;

class ETimeOptionsPlugin implements MOptionenPlugin.OptionsPlugin {

    private final class ChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateTextFields();
            parent.optionen.notifyChange();
        }
    }

    private final class DocumentChangeListener implements DocumentListener {
        @Override
        public void changedUpdate(DocumentEvent e) {
            parent.optionen.notifyChange();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            parent.optionen.notifyChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            parent.optionen.notifyChange();
        }
    }

    void updateTextFields() {
        address.setEnabled(as.isSelected());
        aresfile.setEnabled(af.isSelected());
    }

    final MElektronischeZeitPlugin parent;

    /**
     * @param httpServerPlugin
     */
    ETimeOptionsPlugin(MElektronischeZeitPlugin ServerPlugin) {
        parent = ServerPlugin;
    }

    private JPanel    panel;

    JWarningTextField address;
    JWarningTextField aresfile;

    JRadioButton      as;
    JRadioButton      af;
    ButtonGroup       bg;

    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public void apply() {
        SourcesConfig.setSource(as.isSelected() ? Sources.http : Sources.aresfile);
        SourcesConfig.setAddress(address.getText());
        SourcesConfig.setAresfile(aresfile.getText());
    }

    @Override
    public void cancel() {
        address.setText(SourcesConfig.getAddress());
        aresfile.setText(SourcesConfig.getAresfile());
        updateButtons();
        updateTextFields();
    }

    private void updateButtons() {
        switch (SourcesConfig.getSource()) {
        case aresfile:
            af.setSelected(true);
            break;
        case http:
            as.setSelected(true);
            break;
        default:
            as.setSelected(true);
            break;
        }
    }

    @Override
    public JPanel[] getPanels() {
        if (panel == null) {
            address = new JWarningTextField();
            address.setText(SourcesConfig.getAddress());
            address.getDocument().addDocumentListener(new DocumentChangeListener());

            aresfile = new JWarningTextField();
            aresfile.setText(SourcesConfig.getAresfile());
            aresfile.getDocument().addDocumentListener(new DocumentChangeListener());

            as = new JRadioButton("AlphaServer");
            as.addActionListener(new ChangeListener());
            af = new JRadioButton("ARES21-Ergebnisdatei");
            af.addActionListener(new ChangeListener());

            bg = new ButtonGroup();
            bg.add(as);
            bg.add(af);

            updateButtons();
            updateTextFields();

            FormLayout layout = new FormLayout("4dlu,4dlu,4dlu,fill:default,4dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(4));
            panel = new JPanel(layout);
            panel.setName(I18n.get("ElektronischeZeitnahme"));
            panel.setToolTipText(I18n.getToolTip("ElektronischeZeitnahme"));

            panel.add(as, CC.xyw(2, 2, 5));
            panel.add(new JLabel(I18n.get("Server")), CC.xy(4, 4));
            panel.add(address, CC.xy(6, 4));
            panel.add(af, CC.xyw(2, 6, 5));
            panel.add(new JLabel(I18n.get("File")), CC.xy(4, 8));
            panel.add(aresfile, CC.xy(6, 8));
        }
        return new JPanel[] { panel };
    }
}