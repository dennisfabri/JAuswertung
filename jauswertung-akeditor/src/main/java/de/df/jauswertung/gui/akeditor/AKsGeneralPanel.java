/*
 * Created on 06.06.2004
 */
package de.df.jauswertung.gui.akeditor;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.SimpleFormBuilder;

/**
 * @author Dennis Fabri
 * @date 06.06.2004
 */
class AKsGeneralPanel extends JPanel {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257288011191498036L;
    private JWarningTextField description = null;

    JAKsEditor options = null;

    public AKsGeneralPanel(JAKsEditor jod) {
        initFormeln();
        initGUI();

        options = jod;
    }

    private void initFormeln() {
        description = new JWarningTextField(true, true);
        description.setAutoSelectAll(true);
        description.getDocument().addDocumentListener(new DocumentListener() {

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
        });
    }

    private void initGUI() {
        SimpleFormBuilder sfm = new SimpleFormBuilder(this);
        sfm.setBorder(BorderUtils.createLabeledBorder(I18n.get("General")));

        sfm.add(I18n.get("Description"), description);

        sfm.getPanel();
    }

    public void setSettings(Regelwerk aks) {
        description.setText(aks.getBeschreibung());
    }

    public void getSettings(Regelwerk aks) {
        aks.setBeschreibung(description.getText());
    }
}