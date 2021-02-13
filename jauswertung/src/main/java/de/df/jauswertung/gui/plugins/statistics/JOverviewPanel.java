/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.statistics;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.StatisticsUtils;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.layout.SimpleListBuilder;
import de.df.jutils.gui.util.UIUtils;

public class JOverviewPanel extends JGlassPanel<JPanel> {

    private static final long serialVersionUID = 8023494074221318513L;

    private CorePlugin        core;

    public JOverviewPanel(CorePlugin core) {
        super(new JPanel());
        this.core = core;
        init();
    }

    private void init() {
        setName(I18n.get("Overview"));

        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();

        SimpleListBuilder slb = new SimpleListBuilder(0);
        {
            @SuppressWarnings("unchecked")
            JComponent p = StatisticsUtils.createStatistics(wk.getSchwimmer(), wk.getRegelwerk(), I18n.get("Overview"), false);
            slb.add(p);
        }

        // Add a little space
        slb.add(new JLabel(" "));

        {
            @SuppressWarnings("unchecked")
            JComponent p = StatisticsUtils.getStarts(wk.getSchwimmer(), wk.getRegelwerk(), false);
            slb.add(p);
        }

        setLayout(new BorderLayout());
        add(UIUtils.surroundWithScroller(slb.getPanel(false)), BorderLayout.CENTER);
    }
}