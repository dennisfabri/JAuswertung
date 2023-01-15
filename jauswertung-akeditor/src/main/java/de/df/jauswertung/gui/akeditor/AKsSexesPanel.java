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
class AKsSexesPanel extends JPanel {

    private JWarningTextField sex1NameSubject = null;
    private JWarningTextField sex1Name = null;
    private JWarningTextField sex1Short = null;

    private JWarningTextField sex2NameSubject = null;
    private JWarningTextField sex2Name = null;
    private JWarningTextField sex2Short = null;

    JAKsEditor options = null;

    public AKsSexesPanel(JAKsEditor jod) {
        initFormeln();
        initGUI();

        options = jod;
    }

    private void initFormeln() {
        sex1NameSubject = new JWarningTextField(true, true);
        sex1NameSubject.setAutoSelectAll(true);
        sex1NameSubject.getDocument().addDocumentListener(new ChangeListener());

        sex1Name = new JWarningTextField(true, true);
        sex1Name.setAutoSelectAll(true);
        sex1Name.getDocument().addDocumentListener(new ChangeListener());

        sex1Short = new JWarningTextField(true, true);
        sex1Short.setValidator(value -> value != null && value.length() <= 3);
        sex1Short.setAutoSelectAll(true);
        sex1Short.getDocument().addDocumentListener(new ChangeListener());

        sex2NameSubject = new JWarningTextField(true, true);
        sex2NameSubject.setAutoSelectAll(true);
        sex2NameSubject.getDocument().addDocumentListener(new ChangeListener());

        sex2Name = new JWarningTextField(true, true);
        sex2Name.setAutoSelectAll(true);
        sex2Name.getDocument().addDocumentListener(new ChangeListener());

        sex2Short = new JWarningTextField(true, true);
        sex2Short.setValidator(value -> value != null && value.length() <= 3);
        sex2Short.setAutoSelectAll(true);
        sex2Short.getDocument().addDocumentListener(new ChangeListener());
    }

    private void initGUI() {
        SimpleFormBuilder sfm = new SimpleFormBuilder(this);
        sfm.setBorder(BorderUtils.createLabeledBorder(I18n.get("Sexes")));

        sfm.add(I18n.get("SexNumber", 1));
        sfm.add(I18n.get("LeadingUpper"), sex2NameSubject);
        sfm.add(I18n.get("LeadingLower"), sex2Name);
        sfm.add(I18n.get("Short"), sex2Short);

        sfm.add(I18n.get("SexNumber", 2));
        sfm.add(I18n.get("LeadingUpper"), sex1NameSubject);
        sfm.add(I18n.get("LeadingLower"), sex1Name);
        sfm.add(I18n.get("Short"), sex1Short);

        sfm.getPanel();
    }

    public void setSettings(Regelwerk aks) {
        sex1NameSubject.setText(aks.getTranslation("Male", I18n.get("Male")));
        sex1Name.setText(aks.getTranslation("male", I18n.get("male")));
        sex1Short.setText(aks.getTranslation("maleShort", I18n.get("maleShort")));

        sex2NameSubject.setText(aks.getTranslation("Female", I18n.get("Female")));
        sex2Name.setText(aks.getTranslation("female", I18n.get("female")));
        sex2Short.setText(aks.getTranslation("femaleShort", I18n.get("femaleShort")));
    }

    public void getSettings(Regelwerk aks) {
        aks.setTranslation("Male", sex1NameSubject.getText());
        aks.setTranslation("male", sex1Name.getText());
        aks.setTranslation("maleShort", sex1Short.getText());

        aks.setTranslation("Female", sex2NameSubject.getText());
        aks.setTranslation("female", sex2Name.getText());
        aks.setTranslation("femaleShort", sex2Short.getText());
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