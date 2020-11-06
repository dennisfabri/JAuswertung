/*
 * Created on 19.10.2005
 */
package de.df.jauswertung.gui.plugins.zielrichterentscheid;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.LaufInfo;
import de.df.jauswertung.gui.util.SchwimmerUtils;

class ZielrichterentscheidListCellRenderer extends DefaultListCellRenderer {

    /**
     * 
     */
    private static final long serialVersionUID = -7196014169046780708L;

    public ZielrichterentscheidListCellRenderer() {
        // Nothing to do
    }

    @SuppressWarnings("unchecked")
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if ((value == null) || (!(value instanceof Zielrichterentscheid))) {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }

        Zielrichterentscheid<ASchwimmer> ze = (Zielrichterentscheid<ASchwimmer>) value;
        ASchwimmer s = ze.getSchwimmer().getFirst();
        LaufInfo li = SchwimmerUtils.getLaufInfo(s.getWettkampf(), s, ze.getDisziplin());
        return super.getListCellRendererComponent(list,
                I18n.get("ZielrichterentscheidListLabel", li.getLauf(), s.getAK().getDisziplin(ze.getDisziplin(), s.isMaennlich()), s.getName()), index,
                isSelected, cellHasFocus);
    }
}