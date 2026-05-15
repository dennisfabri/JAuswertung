package de.df.jauswertung.gui.plugins.properties;

import javax.swing.JFrame;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.window.JOptionsDialog;

/**
 * Created on 02.05.2005
 */
public final class JPropertiesDialog extends JOptionsDialog {

    final JPropertiesTabbedPane properties;
    private final JFrame parent;

    public JPropertiesDialog(JFrame parent) {
        super(parent, I18n.get("Properties"), true, IconManager.getIconBundle());
        this.parent = parent;
        properties = new JPropertiesTabbedPane(this);
        setContent(properties);
        pack();
        setSize(Math.max(getWidth(), 650), Math.max(getHeight(), 800));
        UIStateUtils.uistatemanage(parent, this, "JPropertiesDialog");
    }

    @Override
    public void setVisible(boolean visible) {
        if (parent != null) {
            parent.setEnabled(!visible);
        }
        super.setVisible(visible);
    }

    @SuppressWarnings("rawtypes")
    public void start(AWettkampf wk) {
        update(wk);
        setVisible(true);
    }

    public void apply() {
        properties.apply();
    }

    @SuppressWarnings("rawtypes")
    private void update(AWettkampf wk) {
        properties.update(wk);
    }
}
