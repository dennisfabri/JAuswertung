/*
 * Created on 06.06.2004
 */
package de.df.jauswertung.gui.akeditor;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;

/**
 * @author Dennis Fabri
 * @date 06.06.2004
 */
class AKsZusatzwertungPanel extends JPanel {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257288011191498036L;
    private JWarningTextField zusatz = null;
    private JWarningTextField zusatzKurz = null;
    private JIntegerField points = null;

    JAKsEditor options = null;

    public AKsZusatzwertungPanel(JAKsEditor jod) {
        initFormeln();
        initGUI();

        options = jod;
    }

    private void initFormeln() {
        zusatz = new JWarningTextField(true, true);
        zusatz.setAutoSelectAll(true);
        zusatz.getDocument().addDocumentListener(new ChangeListener());

        zusatzKurz = new JWarningTextField(true, true);
        zusatzKurz.setAutoSelectAll(true);
        zusatzKurz.getDocument().addDocumentListener(new ChangeListener());

        points = new JIntegerField(true, true);
        points.setAutoSelectAll(true);
        points.getDocument().addDocumentListener(new ChangeListener());
    }

    private void initGUI() {
        SimpleFormBuilder sfm = new SimpleFormBuilder(this);
        sfm.setBorder(BorderUtils.createLabeledBorder(I18n.get("AdditionalPoints")));

        sfm.add(I18n.get("AdditionalPoints"), zusatz);
        sfm.add(I18n.get("AdditionalPointsShort.Text"), zusatzKurz);
        sfm.add(I18n.get("DefaultPoints"), points);

        sfm.getPanel();
    }

    public void setSettings(Regelwerk aks) {
        zusatz.setText(aks.getZusatzwertung());
        zusatzKurz.setText(aks.getZusatzwertungShort());
        points.setInt(aks.getZusatzwertungBasispunkte());
    }

    public void getSettings(Regelwerk aks) {
        aks.setZusatzwertung(zusatz.getText());
        aks.setZusatzwertungKurz(zusatzKurz.getText());
        aks.setZusatzwertungBasispunkte(points.getInt());
    }

    private final class ChangeListener implements DocumentListener {
        @Override
        public void changedUpdate(DocumentEvent e) {
            options.notifyChange();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            options.notifyChange();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            options.notifyChange();
        }
    }
}