/*
 * Created on 15.04.2005
 */
package de.df.jauswertung.gui.util;

import javax.swing.Icon;

import de.df.jutils.i18n.UIElementsProvider;
import de.df.jutils.util.StringTools;

public final class WizardUIElementsProvider implements UIElementsProvider {

    private static WizardUIElementsProvider instance;

    private WizardUIElementsProvider() {
        // Hide constructor
    }

    public static WizardUIElementsProvider getInstance() {
        synchronized (WizardUIElementsProvider.class) {
            if (instance == null) {
                instance = new WizardUIElementsProvider();
            }
        }
        return instance;
    }

    @Override
    public String getString(String id) {
        return I18n.get(StringTools.capitalize(id.substring(id.lastIndexOf(".") + 1)));
    }

    @Override
    public Icon getIcon(String id) {
        return IconManager.getSmallIcon(id.substring(id.lastIndexOf(".") + 1));
    }

}
