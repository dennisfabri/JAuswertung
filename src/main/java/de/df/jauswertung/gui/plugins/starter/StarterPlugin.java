package de.df.jauswertung.gui.plugins.starter;

import javax.swing.JFrame;

import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jutils.plugin.AFeature;
import de.df.jutils.plugin.UpdateEvent;

public class StarterPlugin extends AFeature {

    public void editStarter(Mannschaft aSchwimmer) {
        JFrame parent = getController().getWindow();
        JStarterEditor.editStarter(parent, aSchwimmer, new Runnable() {

            @Override
            public void run() {
                getController().sendDataUpdateEvent("ChangeSwimmer", UpdateEventConstants.REASON_SWIMMER_CHANGED, StarterPlugin.this);
            }

        });
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // TODO Auto-generated method stub
    }
}
