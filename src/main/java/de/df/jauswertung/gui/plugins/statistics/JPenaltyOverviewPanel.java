/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.statistics;

import java.awt.BorderLayout;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.StatisticsUtils;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.util.UIUtils;

public class JPenaltyOverviewPanel extends JGlassPanel<JPanel> {

    private static final long   serialVersionUID = 8023494074221318513L;

    private static final String HORIZONTAL       = "0px:grow,fill:default,0px:grow";
    private static final String VERTICAL         = "0px,fill:default,0px";

    private CorePlugin          core;

    public JPenaltyOverviewPanel(CorePlugin core) {
        super(new JPanel());
        this.core = core;
        init();
    }

    private void init() {
        setName(I18n.get("Penalties"));

        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();

        JTabbedPane tabs = new JTabbedPane();

        @SuppressWarnings("unchecked")
        LinkedList<JComponent> panels = StatisticsUtils.createPenaltiesStatistics(wk);
        for (JComponent c : panels) {
            JComponent p = new JPanel(new FormLayout(HORIZONTAL, VERTICAL));
            p.add(c, CC.xy(2, 2));
            tabs.add(c.getName(), UIUtils.surroundWithScroller(p));
        }

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }
}