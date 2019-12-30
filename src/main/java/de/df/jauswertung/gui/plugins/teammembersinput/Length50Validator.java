package de.df.jauswertung.gui.plugins.teammembersinput;

import de.df.jutils.gui.JWarningTextField;

public class Length50Validator implements JWarningTextField.Validator {

    @Override
    public boolean validate(String value) {
        try {
            if (value == null) {
                return true;
            }
            if (value.length() <= 50) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            // if error return true
            ex.printStackTrace();
            return true;
        }
    }
}