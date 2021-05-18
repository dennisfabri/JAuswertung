/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.statistics;

import java.awt.BorderLayout;
import java.util.LinkedList;

import javax.swing.JPanel;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.util.StatisticsUtils;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.layout.SimpleListBuilder;
import de.df.jutils.gui.util.UIUtils;

public class JRegistrationLVOverviewPanel extends JGlassPanel<JPanel> {

    private static final long serialVersionUID = 8023494074221318513L;

    private CorePlugin        core;

    public JRegistrationLVOverviewPanel(CorePlugin core) {
        super(new JPanel());
        this.core = core;
        init();
    }

    private void init() {
        setName(I18n.get("RegistrationsPerQualiOrganisation"));

        @SuppressWarnings("rawtypes")
        AWettkampf wk = core.getWettkampf();

        SimpleListBuilder slb = new SimpleListBuilder(0);

        {
            @SuppressWarnings("unchecked")
            LinkedList<JPanel> panels = StatisticsUtils.createLVOverviewPage(wk, -1, false);
            for (JPanel px : panels) {
                slb.add(px);
            }
        }

        setLayout(new BorderLayout());
        add(UIUtils.surroundWithScroller(slb.getPanel(false)), BorderLayout.CENTER);
    }
}