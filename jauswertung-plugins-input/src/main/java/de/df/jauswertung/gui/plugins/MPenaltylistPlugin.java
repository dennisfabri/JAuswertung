/*
 * Created on 02.05.2005
 */
package de.df.jauswertung.gui.plugins;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_AKS_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_GLIEDERUNG_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_LOAD_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_NEW_WK;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PENALTY;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_CHANGED;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_SWIMMER_DELETED;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.penalties.PenaltyUIUtils;
import de.df.jauswertung.gui.penalties.PenaltyUtils;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.gui.util.PenaltyPluginUtils;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.layout.SimpleListBuilder;
import de.df.jutils.gui.util.ModalFrameUtil;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

public class MPenaltylistPlugin extends ANullPlugin {

    public static final long BITMASK = REASON_NEW_WK | REASON_AKS_CHANGED | REASON_LOAD_WK | REASON_GLIEDERUNG_CHANGED
            | REASON_SWIMMER_CHANGED | REASON_PENALTY
            | REASON_SWIMMER_DELETED;

    JPenaltyList penalties;
    CorePlugin core;
    FEditorPlugin edit;
    private JMenuItem item;

    public MPenaltylistPlugin() {
        item = new JMenuItem(I18n.get("Penaltylist"), IconManager.getSmallIcon("penalty"));
        item.setToolTipText(I18n.getToolTip("Penaltylist"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPenalties();
            }
        });
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        if (core == null) {
            throw new NullPointerException();
        }
        edit = (FEditorPlugin) plugincontroller.getFeature("de.df.jauswertung.editor", pluginuid);
        if (edit == null) {
            throw new NullPointerException();
        }
    }

    @Override
    public MenuInfo[] getMenues() {
        return new MenuInfo[] { new MenuInfo(I18n.get("Information"), 550, item, 1010) };
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        if ((due.getChangeReason() & BITMASK) > 0) {
            if ((penalties != null) && (penalties.isVisible())) {
                penalties.update();
            }
            item.setEnabled(PenaltyUtils.hasPenalties(core.getFilteredWettkampf()));
        }
    }

    void showPenalties() {
        if (penalties == null) {
            penalties = new JPenaltyList(getController().getWindow());
        }
        penalties.update();
        if (penalties.getPreferredSize().getWidth() > penalties.getWidth()) {
            penalties.setSize((int) penalties.getPreferredSize().getWidth() + 20, penalties.getHeight());
        }
        ModalFrameUtil.showAsModal(penalties, getController().getWindow());
    }

    private class JPenaltyList extends JFrame {

        private static final long serialVersionUID = 8515041458848007238L;

        private final JPanel panel = new JPanel();

        private Window parent;

        private JScrollPane scroller;

        public JPenaltyList(JFrame owner) {
            super(I18n.get("Penaltylist"));
            setIconImages(IconManager.getTitleImages());
            scroller = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroller.getVerticalScrollBar().setUnitIncrement(10);
            scroller.getHorizontalScrollBar().setUnitIncrement(10);
            scroller.setBorder(new ShadowBorder());

            WindowUtils.addEscapeAction(this);

            add(scroller);
            parent = owner;
            pack();
            setSize(Math.max(getWidth(), 400), Math.max(getHeight(), 300));
            UIStateUtils.uistatemanage(parent, this, "JPenaltyList");
        }

        @Override
        public void setVisible(boolean visible) {
            if (parent != null) {
                parent.setEnabled(!visible);
            }
            super.setVisible(visible);
        }

        @SuppressWarnings("rawtypes")
        public void update() {
            panel.removeAll();
            SimpleListBuilder sfm = new SimpleListBuilder(panel, new FormLayout("4dlu,fill:0px:grow,4dlu", "4dlu"));
            JPanel[] panels = PenaltyPluginUtils.getPenalties(core, new PenaltyUIUtils.PenaltyListener() {
                @SuppressWarnings("unchecked")
                @Override
                public void edit(AWettkampf wk, ASchwimmer s, int disziplin) {
                    edit.runPenaltyEditor(JPenaltyList.this, wk, s, disziplin);
                }
            }, false, false);
            for (JPanel panel1 : panels) {
                sfm.add(panel1);
            }

            scroller.setViewportView(sfm.getPanel(false));
        }
    }
}