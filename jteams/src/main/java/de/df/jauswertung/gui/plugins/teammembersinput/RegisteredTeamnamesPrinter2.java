/*
 * Created on 17.10.2004
 */
package de.df.jauswertung.gui.plugins.teammembersinput;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.Printable;

import javax.swing.JButton;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.print.TeamnamesRegistrationPrintable;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;
import de.df.jutils.print.PrintExecutor;
import de.df.jutils.print.api.PrintableCreator;

/**
 * @author Dennis Fabri
 * @date 17.10.2004
 */
class RegisteredTeamnamesPrinter2 {

    private CorePlugin core;
    private IPluginManager controller;

    private JButton print;
    private JButton preview;

    private String gliederung = null;

    public RegisteredTeamnamesPrinter2(IPluginManager window, CorePlugin plugin) {
        core = plugin;
        controller = window;
        initGUI();
    }

    private void initGUI() {
        print = new JButton(I18n.get("Print"), IconManager.getSmallIcon("print"));
        print.addActionListener(new PrintActionListener());
        print.setEnabled(false);

        preview = new JButton(I18n.get("Preview"), IconManager.getSmallIcon("preview"));
        preview.addActionListener(new PreviewActionListener());
        preview.setEnabled(false);
    }

    @SuppressWarnings("rawtypes")
    void checkMeldungen(AWettkampf wk) {
        boolean b = wk instanceof MannschaftWettkampf && wk.hasSchwimmer();
        print.setEnabled(b);
        preview.setEnabled(b);
    }

    @SuppressWarnings("rawtypes")
    public void dataUpdated(UpdateEvent due, AWettkampf wk, AWettkampf filteredwk) {
        checkMeldungen(filteredwk);
    }

    Printable getPrintable() {
        MannschaftWettkampf wk = (MannschaftWettkampf) (AWettkampf<?>) core.getFilteredWettkampf();
        return new TeamnamesRegistrationPrintable(wk, gliederung);
    }

    void print() {
        PrintExecutor.print(getPrintable(), I18n.get("RegisteredTeamnames"), true, controller.getWindow());
    }

    void preview() {
        PrintableCreator pc = new MeldelistenPC();
        PrintExecutor.preview(controller.getWindow(), pc, I18n.get("RegisteredTeamnames"), IconManager.getIconBundle(),
                IconManager.getTitleImages());
    }

    private final class MeldelistenPC implements PrintableCreator {

        public MeldelistenPC() {
        }

        @Override
        public Printable create() {
            return getPrintable();
        }
    }

    final class PrintActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            print();
        }
    }

    final class PreviewActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {
            preview();
        }
    }

    public void setOrganisation(String selected) {
        gliederung = selected;
    }

    public ButtonInfo[] getQuickButtons() {
        return new ButtonInfo[] { new ButtonInfo(preview, 1000), new ButtonInfo(print, 1001) };
    }
}