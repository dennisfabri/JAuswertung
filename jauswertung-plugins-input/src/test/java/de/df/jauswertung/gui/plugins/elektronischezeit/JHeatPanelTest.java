package de.df.jauswertung.gui.plugins.elektronischezeit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.gui.plugins.elektronischezeit.layer.HeatInfo;
import de.df.jauswertung.gui.plugins.elektronischezeit.layer.LaneInfo;
import de.df.jutils.plugin.IPluginManager;

class JHeatPanelTest {

    private static LaneInfo swimmer(String name, int time) {
        LaneInfo lane = mock(LaneInfo.class);
        when(lane.getName()).thenReturn(name);
        when(lane.getGliederung()).thenReturn("DLRG " + name);
        when(lane.getAgegroup()).thenReturn("AK 12");
        when(lane.getPenalty()).thenReturn("");
        when(lane.getTime()).thenReturn(time);
        return lane;
    }

    @SuppressWarnings("unchecked")
    private static JHeatPanel<ASchwimmer> panel() {
        HeatInfo full = new HeatInfo("AK 12", "100m", 1, 1, 0, new LaneInfo[] { swimmer("A", 6123), swimmer("B", 6234), swimmer("C", 6345) });
        HeatInfo smaller = new HeatInfo("AK 12", "100m", 1, 2, 0, new LaneInfo[] { swimmer("D", 7123), null });
        IETStrategy strategy = mock(IETStrategy.class);
        when(strategy.getLanecount()).thenReturn(3);
        when(strategy.getHeatnames()).thenReturn(new String[] { "1", "2" });
        when(strategy.getHeat(0)).thenReturn(full);
        when(strategy.getHeat(1)).thenReturn(smaller);
        JElektronischeZeit<ASchwimmer> parent = mock(JElektronischeZeit.class);
        when(parent.getStrategy()).thenReturn(strategy);
        when(parent.getController()).thenReturn(mock(IPluginManager.class));
        return new JHeatPanel<>(parent);
    }

    @Test
    void lanesWithoutSwimmerInTheNextHeatShowNothingOfThePreviousHeat() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JHeatPanel<ASchwimmer> panel = panel();
            assertFalse(panel.inputs[1].getText().isEmpty(), "time of the first heat");

            panel.heat.setSelectedIndex(1);

            assertAll(() -> assertEquals("D", panel.heatinfo[1][0].getText()),
                      () -> assertEquals("", panel.inputs[1].getText(), "empty lane"),
                      () -> assertFalse(panel.inputs[1].isEnabled(), "empty lane"),
                      () -> assertEquals("", panel.inputs[2].getText(), "lane beyond the heat"),
                      () -> assertFalse(panel.inputs[2].isEnabled(), "lane beyond the heat"),
                      () -> assertEquals("", panel.heatinfo[1][2].getText(), "lane beyond the heat"),
                      () -> assertEquals("", panel.heatinfo[2][2].getText(), "lane beyond the heat"));
        });
    }
}
