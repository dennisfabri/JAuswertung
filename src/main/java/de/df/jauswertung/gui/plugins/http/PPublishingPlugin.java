package de.df.jauswertung.gui.plugins.http;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.regelwerk.*;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.*;
import de.df.jauswertung.util.SearchUtils;
import de.df.jutils.gui.JLabelSeparator;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.PanelInfo;
import de.df.jutils.plugin.UpdateEvent;

public class PPublishingPlugin extends ANullPlugin {

    private CorePlugin       core        = null;
    private HttpServerPlugin http        = null;

    private PanelInfo[]      panels      = null;

    private JPanel           panel       = null;

    private JComboBox[][]    disciplines = null;
    private JButton[][]      publish     = null;
    private JButton[][]      revoke      = null;
    private JLabel[][]       status      = null;

    public PPublishingPlugin() {
        panels = new PanelInfo[1];
        panels[0] = new PanelInfo(I18n.get("Webpublishing"), IconManager.getBigIcon("webpublishing"), false, true, 3000) {
            @Override
            public JPanel getPanelI() {
                return PPublishingPlugin.this.getPanel();
            }
        };
    }

    @Override
    public void setController(IPluginManager plugincontroller, String pluginuid) {
        super.setController(plugincontroller, pluginuid);
        core = (CorePlugin) plugincontroller.getFeature("de.df.jauswertung.core", pluginuid);
        http = (HttpServerPlugin) plugincontroller.getFeature("de.df.jauswertung.http", pluginuid);

        // TODO: Remove
        http.getPort();
    }

    @Override
    public PanelInfo[] getPanelInfos() {
        return panels;
    }

    private JComboBox createComboBox(Altersklasse ak, boolean maennlich, boolean empty) {
        LinkedList<String> result = new LinkedList<String>();
        for (int x = 0; x <= ak.getDiszAnzahl(); x++) {
            result.addLast(I18n.get("Discipline1toN", x));
        }
        result.addLast(I18n.get("FinalResult"));
        JComboBox jcb = new JComboBox(result.toArray());
        if (empty) {
            jcb.setEnabled(false);
        }
        return jcb;
    }

    private JButton createButton(boolean ok, int x, int y, boolean empty) {
        String name = I18n.get("Publish");
        String icon = "ok";
        if (!ok) {
            name = I18n.get("Revoke");
            icon = "cancel";
        }
        JButton b = new JButton(name, IconManager.getSmallIcon(icon));
        if (!ok) {
            b.setEnabled(false);
            b.addActionListener(new RevokeActionListener(x, y));
        } else {
            b.addActionListener(new PublishActionListener(x, y));
        }
        if (empty) {
            b.setEnabled(false);
        }
        return b;
    }

    @SuppressWarnings({})
    @Override
    public void dataUpdated(UpdateEvent due) {
        if (panel == null) {
            return;
        }
        if (due.isReason(UpdateEventConstants.REASON_AKS_CHANGED)) {
            panel.removeAll();

            AWettkampf<?> wk = core.getWettkampf();
            Regelwerk aks = wk.getRegelwerk();

            FormLayout layout = new FormLayout(
                    "4dlu,fill:default,4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,4dlu,fill:default," + "4dlu,fill:default,0px:grow,4dlu",
                    FormLayoutUtils.createLayoutString(aks.size() * 3));

            layout.setColumnGroups(new int[][] { { 6, 8 } });

            panel.setLayout(layout);

            disciplines = new JComboBox[aks.size()][2];
            publish = new JButton[aks.size()][2];
            revoke = new JButton[aks.size()][2];
            status = new JLabel[aks.size()][2];

            int cols = 12;
            for (int x = 0; x < aks.size(); x++) {
                int row = x * 6;
                Altersklasse ak = aks.getAk(x);
                panel.add(new JLabelSeparator(ak.getName()), CC.xyw(2, row + 2, cols));

                panel.add(new JLabel(I18n.geschlechtToString(aks, false)), CC.xy(2, row + 4));
                panel.add(new JLabel(I18n.geschlechtToString(aks, true)), CC.xy(2, row + 6));

                int offset = 0;
                for (int y = 0; y < 2; y++) {
                    boolean empty = !SearchUtils.hasSchwimmer(wk, ak, y == 1);

                    disciplines[x][y] = createComboBox(ak, y == 1, empty);
                    publish[x][y] = createButton(true, x, y, empty);
                    revoke[x][y] = createButton(false, x, y, empty);
                    status[x][y] = new JLabel(I18n.get("NotPublished"));

                    panel.add(disciplines[x][y], CC.xy(4, row + 4 + offset));
                    panel.add(publish[x][y], CC.xy(6, row + 4 + offset));
                    panel.add(revoke[x][y], CC.xy(8, row + 4 + offset));

                    panel.add(new JLabel(I18n.get("Status") + ":"), CC.xy(10, row + 4 + offset));
                    panel.add(status[x][y], CC.xy(12, row + 4 + offset));
                    offset += 2;
                }
            }
        }
    }

    JPanel getPanel() {
        if (panel == null) {
            panel = new JPanel();
            dataUpdated(UpdateEventConstants.EVERYTHING_CHANGED);
        }
        return panel;
    }

    private static final class RevokeActionListener implements ActionListener {

        private int x;
        private int y;

        public RevokeActionListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO: Implement
            y = x;
            x = y;
        }
    }

    private static final class PublishActionListener implements ActionListener {

        private int x;
        private int y;

        public PublishActionListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO: Implement
            y = x;
            x = y;
        }
    }

}