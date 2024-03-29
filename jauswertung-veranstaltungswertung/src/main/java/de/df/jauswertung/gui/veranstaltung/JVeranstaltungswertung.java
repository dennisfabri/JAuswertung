package de.df.jauswertung.gui.veranstaltung;

import java.awt.CardLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.lisasp.swing.filechooser.FileChooserUtils;
import org.lisasp.swing.filechooser.filefilter.SimpleFileFilter;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.buttonbar.JButtonBar;

import de.df.jauswertung.daten.veranstaltung.Veranstaltung;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.InputManager;
import de.df.jauswertung.io.OutputManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.JGlassFrame;
import de.df.jutils.gui.border.ShadowBorder;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;

class JVeranstaltungswertung extends JGlassFrame {

    private String filename = null;
    boolean modified = false;

    private SimpleFileFilter ff = new SimpleFileFilter(I18n.get("Veranstaltung"), "vs");

    CardLayout cards = new CardLayout();
    JPanel panels = new JPanel();

    private JMenuBar menu;
    private JMenu file;
    private JMenuItem neu;
    private JMenuItem open;
    private JMenuItem save;
    private JMenuItem saveas;
    private JMenuItem quit;

    private Veranstaltung vs;

    private JCompetitionCollector competitions = new JCompetitionCollector(this);
    private JVeranstaltungseinstellungen properties = new JVeranstaltungseinstellungen(this);
    private JVeranstaltungsausgabe output = new JVeranstaltungsausgabe(this);
    private JVeranstaltungsschriftart font = new JVeranstaltungsschriftart(this);

    public JVeranstaltungswertung() {
        super(I18n.get("Veranstaltungswertung"));
        setIconImages(IconManager.getTitleImages());

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        panels.setLayout(cards);
        panels.add(this.competitions, "1");
        panels.add(this.properties, "2");
        panels.add(this.font, "3");
        panels.add(this.output, "4");
        cards.show(panels, "1");

        JButton competitionsbutton = new JButton(I18n.get("Competitions"), IconManager.getBigIcon("competition"));
        competitionsbutton.addActionListener(new ButtonsListener("1"));
        JButton propertiesbutton = new JButton(I18n.get("Properties"), IconManager.getBigIcon("eigenschaften"));
        propertiesbutton.addActionListener(new ButtonsListener("2"));
        JButton fontbutton = new JButton(I18n.get("Font"), IconManager.getBigIcon("font"));
        fontbutton.addActionListener(new ButtonsListener("3"));
        JButton outputbutton = new JButton(I18n.get("Output"), IconManager.getBigIcon("resulttable"));
        outputbutton.addActionListener(new ButtonsListener("4"));

        JButtonBar buttons = new JButtonBar(SwingConstants.VERTICAL);
        buttons.setBorder(new ShadowBorder());
        buttons.add(competitionsbutton);
        buttons.add(propertiesbutton);
        buttons.add(fontbutton);
        buttons.add(outputbutton);

        setLayout(new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu", "4dlu,fill:default:grow,4dlu"));

        add(buttons, CC.xy(2, 2));
        add(panels, CC.xy(4, 2));

        createMenues();

        pack();
        UIStateUtils.uistatemanage(this, "Veranstaltungswertung");

        vs = new Veranstaltung();
        updateUI();
    }

    private void createMenues() {
        neu = new JMenuItem(I18n.get("New"), IconManager.getSmallIcon("newfile"));
        neu.addActionListener(e -> {
            neu();
        });
        open = new JMenuItem(I18n.get("Open"), IconManager.getSmallIcon("openfile"));
        open.addActionListener(e -> {
            open();
        });
        save = new JMenuItem(I18n.get("Save"), IconManager.getSmallIcon("savefile"));
        save.addActionListener(e -> {
            save();
        });
        saveas = new JMenuItem(I18n.get("SaveAs"), IconManager.getSmallIcon("saveasfile"));
        saveas.addActionListener(e -> {
            saveas();
        });
        quit = new JMenuItem(I18n.get("Close"), IconManager.getSmallIcon("close"));
        quit.addActionListener(e -> {
            close();
        });

        file = new JMenu(I18n.get("File"));
        file.add(neu);
        file.add(open);
        file.add(save);
        file.add(saveas);
        file.add(new JSeparator());
        file.add(quit);

        menu = new JMenuBar();
        menu.add(file);

        setJMenuBar(menu);

    }

    public JVeranstaltungswertung(String filename) {
        this();
        if (filename != null) {
            ladeVeranstaltung(filename);
        }
    }

    public Veranstaltung getVeranstaltung() {
        updateVeranstaltung();
        return Utils.copy(vs);
    }

    private void updateVeranstaltung() {
        competitions.getProperties(vs);
        properties.getProperties(vs);
    }

    private void updateUI() {
        competitions.setProperties(vs);
        properties.setProperties(vs);
    }

    void open() {
        ladeVeranstaltung();
    }

    private void ladeVeranstaltung() {
        String filename = FileChooserUtils.openFile(this, ff);
        if (filename == null) {
            return;
        }
        ladeVeranstaltung(filename);
    }

    private void ladeVeranstaltung(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("name must not be null.");
        }
        Veranstaltung v = (Veranstaltung) InputManager.ladeObject(filename);
        if (v != null) {
            this.filename = filename;
            vs = v;
            updateUI();
            modified = false;
        } else {
            DialogUtils.error(this, I18n.get("OpenFailed"), I18n.get("OpenFailedText", this.filename),
                    I18n.get("OpenFailedText.Note", this.filename));
        }
    }

    void saveas() {
        String name = FileChooserUtils.saveFile(this, ff);
        if (name == null) {
            return;
        }
        speichereVeranstaltung(name);
    }

    void save() {
        speichereVeranstaltung();
    }

    private void speichereVeranstaltung() {
        if (filename != null) {
            speichereVeranstaltung(filename);
        } else {
            saveas();
        }
    }

    private void speichereVeranstaltung(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        updateVeranstaltung();
        boolean result = OutputManager.speichereObject(name, vs);
        if (!result) {
            DialogUtils.error(this, I18n.get("SaveFailed"), I18n.get("SaveFailedText", filename),
                    I18n.get("SaveFailed.Note", filename));
        } else {
            filename = name;
            modified = false;
        }
    }

    private final class ButtonsListener implements ActionListener {

        private final String id;

        public ButtonsListener(String key) {
            id = key;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            cards.show(panels, id);
        }
    }

    void close() {
        if (modified) {
            boolean yes = DialogUtils.ask(this, I18n.get("QuitEditorText", "die Veranstaltungswertung"), null);
            if (!yes) {
                return;
            }
        }
        EDTUtils.niceExit();

    }

    void setModified() {
        modified = true;
    }

    public Font getSelectedFont() {
        return font.getSelectedFont();
    }

    void neu() {
        filename = null;
        vs = new Veranstaltung();
        updateUI();
        modified = false;
    }
}