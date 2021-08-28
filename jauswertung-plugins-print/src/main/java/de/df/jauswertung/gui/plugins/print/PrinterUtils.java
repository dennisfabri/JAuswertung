/*
 * Created on 28.04.2005
 */
package de.df.jauswertung.gui.plugins.print;

import java.util.LinkedList;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.WarningPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.plugin.IPluginManager;

public final class PrinterUtils {

    private PrinterUtils() {
        super();
    }

    static Printer[] createPrinters(IPluginManager controller, CorePlugin plugin, WarningPlugin warn) {

        PrinterCollection competition = new PrinterCollection(I18n.get("Competition"));
        competition.add(new RegistrationPrinter(controller, plugin));
        competition.add(new RegisteredTimesPrinter(controller, plugin));
        competition.add(new RegisteredTeamnamesPrinter(controller, plugin));
        competition.add(new StartpassPrinter(controller, plugin));
        competition.add(new DopinglistenPrinter(controller, plugin));
        competition.add(new RulebookPrinter(controller, plugin));
        competition.add(new DisciplinesPrinter(controller, plugin));
        competition.add(new StatisticsPrinter(controller, plugin));
        competition.add(new PenaltykatalogPrinter(controller, plugin));
        competition.doLayout();

        PrinterCollection protocol = new PrinterCollection(I18n.get("Protocol"));
        protocol.add(new ProtocolPrinter(controller, plugin));
        protocol.add(new PropertiesPrinter(controller, plugin));
        protocol.add(new RefereePrinter(controller, plugin));
        protocol.add(new InfopagePrinter(controller, plugin));
        protocol.add(new ZielrichterentscheidPrinter(controller, plugin));
        protocol.doLayout();

        PrinterCollection ergebnisse = new PrinterCollection(I18n.get("Results"));
        ergebnisse.add(new ResultsPrinter(controller, plugin, warn));
        ergebnisse.add(new ResultsWithDetailedFilterPrinter(controller, plugin, warn));
        ergebnisse.add(new ResultsPerDisciplinePrinter(controller, plugin));
        // ergebnisse.add(new DisciplineresultsOverAllPrinter(controller,
        // plugin));
        ergebnisse.add(new GesamtwertungPrinter(controller, plugin));
        ergebnisse.add(new ListOfMedalsPrinter(controller, plugin));
        ergebnisse.add(new ListOfMedalsPerDisciplinePrinter(controller, plugin));
        ergebnisse.add(new MedaillenspiegelPrinter(controller, plugin));
        ergebnisse.add(new LaufergebnissePrinter(controller, plugin));
        ergebnisse.add(new PenaltylistePrinter(controller, plugin));
        ergebnisse.add(new DocumentsPrinter(controller, plugin));
        ergebnisse.add(new DocumentsEinzelwertungPrinter(controller, plugin));
        ergebnisse.add(new WeitermeldungPrinter(controller, plugin));
        ergebnisse.add(new ZWResultsPrinter(controller, plugin));
        ergebnisse.add(new BestzeitenPrinter(controller, plugin));
        ergebnisse.add(new BrokenRecordsPrinter(controller, plugin));
        ergebnisse.add(new BrokenLimitsPrinter(controller, plugin));
        ergebnisse.doLayout();

        PrinterCollection laufliste = new PrinterCollection(I18n.get("Laufliste"));
        laufliste.add(new LauflistenPrinter(controller, plugin));
        laufliste.add(new LauflistenMeldeergebnisPrinter(controller, plugin));
        laufliste.add(new SprecherlistePrinter(controller, plugin));
        laufliste.add(new LauflistenKampfrichterPrinter(controller, plugin));
        laufliste.add(new LauflistenRecorderPrinter(controller, plugin));
        laufliste.add(new LaufuebersichtPrinter(controller, plugin));
        laufliste.add(new LaufeinteilungPrinter(controller, plugin));
        laufliste.add(new LaufeinteilungKompaktPrinter(controller, plugin));
        laufliste.add(new BahnenlistenPrinter(controller, plugin));
        laufliste.add(new FilledStartkartenPrinter(controller, plugin));
        laufliste.add(new FilledZieleinlaufkartenPrinter(controller, plugin));
        laufliste.doLayout();

        PrinterCollection hlwliste = new PrinterCollection(I18n.get("ZWList"));
        hlwliste.add(new ZWListenPrinter(controller, plugin));
        hlwliste.add(new PuppenlistenPrinter(controller, plugin));
        hlwliste.add(new FilledZWStartkartenPrinter(controller, plugin));
        // hlwliste.add(new FilledHLWChecklistsPrinter(controller, plugin));
        hlwliste.add(new FilledSimpleZWStartkartenPrinter(controller, plugin));
        // hlwliste.add(new FilledSimpleHLWChecklistsPrinter(controller, plugin));
        hlwliste.doLayout();

        PrinterCollection misc = createEmptyFormPrinters(controller);

        LinkedList<Printer> printers = new LinkedList<Printer>();
        printers.addLast(competition);
        printers.addLast(protocol);
        printers.addLast(ergebnisse);
        printers.addLast(laufliste);
        printers.addLast(hlwliste);
        printers.addLast(misc);

        return printers.toArray(new Printer[printers.size()]);
    }

    public static PrinterCollection createEmptyFormPrinters(IPluginManager controller) {
        PrinterCollection misc = new PrinterCollection(I18n.get("PrintForms"));
        misc.add(new StartkartenPrinter(controller));
        misc.add(new ZWStartkartenPrinter(controller));
        // misc.add(new HLWChecklistPrinter(controller));
        misc.add(new ZieleinlaufkartenPrinter(controller));
        misc.add(new FehlermeldekartenPrinter(controller));
        misc.doLayout();
        return misc;
    }
}
