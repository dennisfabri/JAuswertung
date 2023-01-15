package de.df.jauswertung.gui.util;

import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JPanel;

import de.df.jauswertung.gui.penalties.PenaltyUIUtils;
import de.df.jauswertung.gui.penalties.PenaltyUIUtils.PenaltyListener;
import de.df.jauswertung.gui.plugins.CorePlugin;

public class PenaltyPluginUtils {
    public static JPanel[] getPenalties(CorePlugin core, int index, boolean[][] selected, boolean ignoreNA,
            boolean kurz) {
        return getPenalties(core, index, selected, null, ignoreNA, kurz);
    }

    public static JPanel[] getPenalties(CorePlugin core, int index, boolean[][] selected, PenaltyListener pl,
            boolean ignoreNA, boolean kurz) {
        return getPenalties(core, index, selected, pl != null, pl, ignoreNA, kurz);
    }

    public static JPanel[] getPenalties(CorePlugin core, PenaltyListener pl, boolean ignoreNA, boolean kurz) {
        return getPenalties(core, -1, null, pl != null, pl, ignoreNA, kurz);
    }

    private static JPanel[] getPenalties(CorePlugin core, int index, boolean[][] selected, boolean border,
            PenaltyListener pl, boolean ignoreNA, boolean kurz) {
        Hashtable<String, JPanel> panels = new Hashtable<>();
        PenaltyUIUtils.add(panels, core.getWettkampf(), index, selected, border, pl, ignoreNA, kurz);

        LinkedList<String> indizes = new LinkedList<>();
        for (String key : panels.keySet()) {
            indizes.add(key);
        }
        Collections.sort(indizes);

        LinkedList<JPanel> result = new LinkedList<>();
        ListIterator<String> li = indizes.listIterator();
        while (li.hasNext()) {
            result.addLast(panels.get(li.next()));
        }

        return result.toArray(new JPanel[result.size()]);
    }
}
