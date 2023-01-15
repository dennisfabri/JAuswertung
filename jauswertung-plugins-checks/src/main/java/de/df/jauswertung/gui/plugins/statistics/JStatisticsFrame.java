/*
 * Created on 18.02.2006
 */
package de.df.jauswertung.gui.plugins.statistics;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JGlassFrame;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

class JStatisticsFrame extends JGlassFrame {

    private static final long serialVersionUID = -7597461480874952948L;

    private final JFrame parent;

    private JButton closebutton;

    public JStatisticsFrame(JFrame parent, CorePlugin core) {
        super(I18n.get("Statistics"));
        setIconImages(IconManager.getTitleImages());
        this.parent = parent;
        init(preparePanels(core));
        pack();
        setSize(Math.max(getWidth(), 800), Math.max(getHeight(), 600));
        UIStateUtils.uistatemanage(parent, this, "JStatisticsFrame");
        WindowUtils.addEscapeAction(this, () -> {
            if (isEnabled()) {
                setVisible(false);
            }
        });
    }

    private static Collection<JComponent> preparePanels(CorePlugin core) {
        LinkedList<JComponent> panels = new LinkedList<>();
        panels.add(new JOverviewPanel(core));
        panels.add(new JRegistrationOverviewPanel(core));
        panels.add(new JRegistrationLVOverviewPanel(core));
        panels.add(new JOrganizationDetailPanel(core));
        panels.add(new JQualiOrganizationDetailPanel(core));
        panels.add(new JPenaltyOverviewPanel(core));
        return panels;
    }

    @Override
    public void setVisible(boolean v) {
        parent.setEnabled(!v);
        super.setVisible(v);
    }

    private void init(Collection<JComponent> panels) {
        closebutton = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        closebutton.addActionListener(e -> {
            setVisible(false);
        });

        JTabbedPane tabs = new JTabbedPane();
        for (JComponent p : panels) {
            tabs.add(p.getName(), p);
        }

        FormLayout layout = new FormLayout("4dlu,fill:default:grow,0dlu,fill:default,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        add(tabs, CC.xyw(2, 2, 3, "fill,fill"));
        add(closebutton, CC.xy(4, 4));
    }
}