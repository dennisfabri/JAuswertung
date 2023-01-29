/*
 * Created on 18.02.2006
 */
package de.df.jauswertung.gui.plugins.check;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.gui.UpdateEventConstants;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JInfiniteProgressFrame;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.InfiniteProgressUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.UIUtils;
import de.df.jutils.gui.util.WindowUtils;
import de.df.jutils.plugin.IFeature;
import de.df.jutils.plugin.UpdateEvent;

class JMissingInputFrame extends JInfiniteProgressFrame {

    private static final long serialVersionUID = -7597461480874952948L;

    private final CorePlugin core;
    private final FEditorPlugin editor;
    private final JFrame parent;

    private JInfoPanel general;
    private JZWInputPanel hlwpanel;
    private JTimesInputPanel timespanel;
    private JRegistrationTimesInputPanel registrationtimespanel;
    private JYearInputPanel yearpanel;
    private JMembersInputPanel memberspanel;
    private JRegistrationPanel registrationpanel;
    private JOrganizationSimilarityPanel similaritypanel;
    private JTeamnamesPanel teamnamespanel;
    private JNamesPanel namespanel;

    private JButton updatebutton;
    private JButton closebutton;

    private ISimpleCallback<JMissingInputFrame> callback;

    public JMissingInputFrame(JFrame parent, CorePlugin core, FEditorPlugin editor,
            ISimpleCallback<JMissingInputFrame> cb) {
        super(I18n.get("CheckInput"));
        setIconImages(IconManager.getTitleImages());
        this.core = core;
        this.editor = editor;
        this.parent = parent;
        callback = cb;
        init();
        pack();
        setSize(Math.max(getWidth(), 800), Math.max(getHeight(), 600));
        UIStateUtils.uistatemanage(parent, this, "JMissingInputFrame");
        WindowUtils.addEscapeAction(this, () -> {
            if (isEnabled()) {
                setVisible(false);
            }
        });
    }

    @Override
    public void setVisible(boolean v) {
        if (v) {
            if (isVisible()) {
                return;
            }
            registrationpanel.unsetChanged();
            registrationtimespanel.unsetChanged();
            hlwpanel.unsetChanged();
            timespanel.unsetChanged();
            similaritypanel.unsetChanged();
            if (yearpanel != null) {
                yearpanel.unsetChanged();
                namespanel.unsetChanged();
            } else {
                memberspanel.unsetChanged();
                teamnamespanel.unsetChanged();
            }
        }
        parent.setEnabled(!v);
        super.setVisible(v);
        if (v) {
            update();
        } else {
            if (callback != null) {
                callback.callback(this);
            }
        }
    }

    public void update() {
        // EDTUtils.setEnabled(this, false);
        startProgress();
        SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
            @Override
            protected Object doInBackground() throws Exception {
                updateData();
                return null;
            }

            @Override
            protected void done() {
                updateGUI();
                // setEnabled(true);
                stopProgress();
            }
        };
        sw.execute();
    }

    private void init() {
        registrationpanel = new JRegistrationPanel(this, core, editor);
        general = new JInfoPanel(this, core, editor);
        hlwpanel = new JZWInputPanel(this, core, editor);
        timespanel = new JTimesInputPanel(this, core, editor);
        registrationtimespanel = new JRegistrationTimesInputPanel(this, core, editor);
        similaritypanel = new JOrganizationSimilarityPanel(this, core, editor);

        if (((AWettkampf<?>) core.getWettkampf()) instanceof EinzelWettkampf) {
            yearpanel = new JYearInputPanel(this, core, editor);
            namespanel = new JNamesPanel(this, core, editor);
        } else {
            memberspanel = new JMembersInputPanel(this, core, editor);
            teamnamespanel = new JTeamnamesPanel(this, core, editor);
        }

        updatebutton = new JButton(I18n.get("Update"), IconManager.getSmallIcon("update"));
        updatebutton.addActionListener(e -> {
            update();
        });
        closebutton = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        closebutton.addActionListener(e -> {
            setVisible(false);
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(I18n.get("Overview"), UIUtils.surroundWithScroller(general));
        tabs.addTab(I18n.get("Registration"), UIUtils.surroundWithScroller(registrationpanel));
        tabs.addTab(I18n.get("Meldezeiten"), registrationtimespanel);
        tabs.addTab(I18n.get("Times"), timespanel);
        tabs.addTab(I18n.get("ZWPoints"), UIUtils.surroundWithScroller(hlwpanel));
        if (yearpanel != null) {
            tabs.addTab(I18n.get("YearOfBirth"), UIUtils.surroundWithScroller(yearpanel));
            tabs.addTab(I18n.get("Names"), UIUtils.surroundWithScroller(namespanel));
        } else {
            tabs.addTab(I18n.get("Teammembers"), UIUtils.surroundWithScroller(memberspanel));
            tabs.addTab(I18n.get("Teamnames"), UIUtils.surroundWithScroller(teamnamespanel));
        }
        tabs.addTab(I18n.get("Similarities"), UIUtils.surroundWithScroller(similaritypanel));

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,fill:default,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        layout.setColumnGroups(new int[][] { { 2, 5 } });
        setLayout(layout);

        add(tabs, CC.xyw(2, 2, 4, "fill,fill"));
        add(updatebutton, CC.xy(2, 4));
        add(closebutton, CC.xy(5, 4));
    }

    void updateData() {
        InfiniteProgressUtils.setTextAsync(this, I18n.get("Information.CheckingCompetition", I18n.get("Registration")));
        try {
            registrationpanel.updateData();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        InfiniteProgressUtils.setTextAsync(this, I18n.get("Information.CheckingCompetition", I18n.get("Meldezeiten")));
        try {
            registrationtimespanel.updateData();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        InfiniteProgressUtils.setTextAsync(this, I18n.get("Information.CheckingCompetition", I18n.get("General")));
        try {
            general.updateData();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        InfiniteProgressUtils.setTextAsync(this,
                I18n.get("Information.CheckingCompetition", I18n.get("AdditionalPoints")));
        try {
            hlwpanel.updateData();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        InfiniteProgressUtils.setTextAsync(this, I18n.get("Information.CheckingCompetition", I18n.get("Times")));
        try {
            timespanel.updateData();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        InfiniteProgressUtils.setTextAsync(this, I18n.get("Information.CheckingCompetition", I18n.get("Similarities")));
        try {
            similaritypanel.updateData();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
        if (yearpanel != null) {
            InfiniteProgressUtils.setTextAsync(this,
                    I18n.get("Information.CheckingCompetition", I18n.get("YearOfBirth")));
            try {
                yearpanel.updateData();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
            InfiniteProgressUtils.setTextAsync(this, I18n.get("Information.CheckingCompetition", I18n.get("Names")));
            try {
                namespanel.updateData();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        } else {
            InfiniteProgressUtils.setTextAsync(this,
                    I18n.get("Information.CheckingCompetition", I18n.get("Teammembers")));
            try {
                memberspanel.updateData();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
            InfiniteProgressUtils.setTextAsync(this,
                    I18n.get("Information.CheckingCompetition", I18n.get("Teamnames")));
            try {
                teamnamespanel.updateData();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        InfiniteProgressUtils.setTextAsync(this, "");
    }

    void check(ASchwimmer s) {
        registrationpanel.check(s);
        registrationtimespanel.check(s);
        general.check(s);
        hlwpanel.check(s);
        timespanel.check(s);
        similaritypanel.check(s);
        if (yearpanel != null) {
            yearpanel.check(s);
            namespanel.check(s);
        } else {
            memberspanel.check(s);
            teamnamespanel.check(s);
        }
    }

    void updateGUI() {
        setText(I18n.get("Information.UpdatingUI", I18n.get("Registration")));
        registrationpanel.updateGUI();
        setText(I18n.get("Information.UpdatingUI", I18n.get("Meldezeiten")));
        registrationtimespanel.updateGUI();
        setText(I18n.get("Information.UpdatingUI", I18n.get("General")));
        general.updateGUI();
        setText(I18n.get("Information.UpdatingUI", I18n.get("AdditionalPoints")));
        hlwpanel.updateGUI();
        setText(I18n.get("Information.UpdatingUI", I18n.get("Times")));
        timespanel.updateGUI();
        setText(I18n.get("Information.UpdatingUI", I18n.get("Similarities")));
        similaritypanel.updateGUI();
        if (yearpanel != null) {
            setText(I18n.get("Information.UpdatingUI", I18n.get("YearOfBirth")));
            yearpanel.updateGUI();
            setText(I18n.get("Information.UpdatingUI", I18n.get("Names")));
            namespanel.updateGUI();
        } else {
            setText(I18n.get("Information.UpdatingUI", I18n.get("Teammembers")));
            memberspanel.updateGUI();
            setText(I18n.get("Information.UpdatingUI", I18n.get("Teamnames")));
            teamnamespanel.updateGUI();
        }
        setText("");
    }

    void dataUpdate() {
        timespanel.updatePenalties();
    }

    public UpdateEvent getUpdateEvent(IFeature f) {
        boolean h = hlwpanel.hasChanged();
        boolean t = timespanel.hasChanged();
        boolean y = (yearpanel != null && yearpanel.hasChanged());
        boolean m = (memberspanel != null && memberspanel.hasChanged());
        boolean r = registrationpanel.hasChanged() || (namespanel != null && namespanel.hasChanged())
                || (teamnamespanel != null && teamnamespanel.hasChanged())
                || similaritypanel.hasChanged() || registrationtimespanel.hasChanged();
        boolean s = similaritypanel.hasChanged();
        boolean ymrs = y || m || r || s;
        if (h && t && ymrs) {
            return new UpdateEvent("ChangeTimeAndZWAndSwimmer",
                    UpdateEventConstants.REASON_PENALTY | UpdateEventConstants.REASON_POINTS_CHANGED
                            | UpdateEventConstants.REASON_SWIMMER_CHANGED,
                    f);
        }
        if (h && t) {
            return new UpdateEvent("ChangeTimeAndZW",
                    UpdateEventConstants.REASON_PENALTY | UpdateEventConstants.REASON_POINTS_CHANGED, f);
        }
        if (t && ymrs) {
            return new UpdateEvent("ChangeTimeAndSwimmer",
                    UpdateEventConstants.REASON_PENALTY | UpdateEventConstants.REASON_POINTS_CHANGED
                            | UpdateEventConstants.REASON_SWIMMER_CHANGED,
                    f);
        }
        if (h && ymrs) {
            return new UpdateEvent("ChangeZWAndSwimmer",
                    UpdateEventConstants.REASON_PENALTY | UpdateEventConstants.REASON_POINTS_CHANGED
                            | UpdateEventConstants.REASON_SWIMMER_CHANGED,
                    f);
        }
        if (ymrs) {
            return new UpdateEvent("ChangePerson", UpdateEventConstants.REASON_SWIMMER_CHANGED, f);
        }
        if (h) {
            return new UpdateEvent("ChangeZW", UpdateEventConstants.REASON_POINTS_CHANGED, f);
        }
        if (t) {
            return new UpdateEvent("ChangeTime",
                    UpdateEventConstants.REASON_PENALTY | UpdateEventConstants.REASON_POINTS_CHANGED, f);
        }
        return null;
    }
}