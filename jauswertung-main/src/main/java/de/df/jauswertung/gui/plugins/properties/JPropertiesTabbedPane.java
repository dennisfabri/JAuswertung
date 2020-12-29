/*
 * Created on 02.05.2005
 */
package de.df.jauswertung.gui.plugins.properties;

import static de.df.jauswertung.daten.PropertyConstants.ART_DES_WETTKAMPFS;
import static de.df.jauswertung.daten.PropertyConstants.AUSRICHTER;
import static de.df.jauswertung.daten.PropertyConstants.BEGIN;
import static de.df.jauswertung.daten.PropertyConstants.DATE;
import static de.df.jauswertung.daten.PropertyConstants.DEPTH_OF_POOL;
import static de.df.jauswertung.daten.PropertyConstants.ELEKTRONISCHE_ZEITNAHME;
import static de.df.jauswertung.daten.PropertyConstants.END;
import static de.df.jauswertung.daten.PropertyConstants.HEATS_LANES;
import static de.df.jauswertung.daten.PropertyConstants.INFOPAGE;
import static de.df.jauswertung.daten.PropertyConstants.LENGTH_OF_POOL;
import static de.df.jauswertung.daten.PropertyConstants.LOCATION;
import static de.df.jauswertung.daten.PropertyConstants.NAME;
import static de.df.jauswertung.daten.PropertyConstants.NAMENTLICHE_MELDUNG_STRIKT;
import static de.df.jauswertung.daten.PropertyConstants.NAME_OF_POOL;
import static de.df.jauswertung.daten.PropertyConstants.ORGANIZER;
import static de.df.jauswertung.daten.PropertyConstants.OTHER_COMPETITION_INFO;
import static de.df.jauswertung.daten.PropertyConstants.OTHER_LOCATION_INFO;
import static de.df.jauswertung.daten.PropertyConstants.POSITION_OF_MANAKIN;
import static de.df.jauswertung.daten.PropertyConstants.PRINT_REFEREES_COMPACT;
import static de.df.jauswertung.daten.PropertyConstants.RESULT_MULTILINE;
import static de.df.jauswertung.daten.PropertyConstants.SHORTNAME;
import static de.df.jauswertung.daten.PropertyConstants.STARTNUMBERFORMAT;
import static de.df.jauswertung.daten.PropertyConstants.WATERTEMPERATURE;
import static de.df.jauswertung.daten.PropertyConstants.YEAR_OF_COMPETITION;

import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Wettkampfart;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JImagePanel;
import de.df.jutils.gui.JIntSpinner;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.window.JOptionsDialog;

public final class JPropertiesTabbedPane extends JTabbedPane {

    private static final long       serialVersionUID = 3256442495306316086L;

    private JComboBox<Wettkampfart> art;
    private JWarningTextField       name;
    private JWarningTextField       shortname;
    private JTextPane               location;
    private JTextPane               organizer;
    private JTextPane               ausrichter;
    private JWarningTextField       date;
    private JWarningTextField       begin;
    private JWarningTextField       end;
    private JTextPane               competitionOther;
    private JComboBox<String>       nameregistration;
    private JComboBox<String>       printNamesInResults;
    private JComboBox<String>       snformat;
    private JComboBox<String>       printReferees;

    private String[]                snformats;

    private JComboBox<Integer>      year;

    private JTextPane               nameOfPool;
    private JTextPane               depthOfPool;
    private JWarningTextField       temperatureOfPool;
    private JWarningTextField       poollength;
    private JIntSpinner             numberoflanes;
    private JCheckBox               elektronischeZeitnahme;
    JTextPane                       manakin;
    private JTextPane               locationOther;

    JTextPane                       infopage;

    JImagePanel                     image;

    JOptionsDialog                  parent;

    @SuppressWarnings("rawtypes")
    AWettkampf                      wk;

    public JPropertiesTabbedPane(JOptionsDialog parent) {
        this.parent = parent;
    }

    private void addChangeListeners() {
        ItemListener item = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                parent.setChanged(true);
            }
        };

        DocumentListener doc = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                parent.setChanged(true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        };

        ChangeListener change = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                parent.setChanged(true);
            }
        };

        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                parent.setChanged(true);
            }
        };

        art.addItemListener(item);
        name.getDocument().addDocumentListener(doc);
        shortname.getDocument().addDocumentListener(doc);
        location.getDocument().addDocumentListener(doc);
        organizer.getDocument().addDocumentListener(doc);
        ausrichter.getDocument().addDocumentListener(doc);
        date.getDocument().addDocumentListener(doc);
        begin.getDocument().addDocumentListener(doc);
        end.getDocument().addDocumentListener(doc);
        competitionOther.getDocument().addDocumentListener(doc);

        nameregistration.addItemListener(item);
        printNamesInResults.addActionListener(action);
        snformat.addActionListener(action);
        year.addItemListener(item);
        printReferees.addActionListener(action);

        nameOfPool.getDocument().addDocumentListener(doc);
        depthOfPool.getDocument().addDocumentListener(doc);
        poollength.getDocument().addDocumentListener(doc);
        temperatureOfPool.getDocument().addDocumentListener(doc);
        numberoflanes.addChangeListener(change);
        elektronischeZeitnahme.addActionListener(action);
        manakin.getDocument().addDocumentListener(doc);

        infopage.getDocument().addDocumentListener(doc);

        image.addChangeListener(change);
    }

    private void setTraversalKeys(JTextPane textArea) {
        Set<AWTKeyStroke> set = new HashSet<AWTKeyStroke>(
                textArea.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        set.add(KeyStroke.getKeyStroke("TAB"));
        textArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);

        set = new HashSet<AWTKeyStroke>(textArea.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        set.add(KeyStroke.getKeyStroke("shift TAB"));
        textArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, set);
    }

    private JTextPane createTextPane() {
        JTextPane tp = new JTextPane();
        setTraversalKeys(tp);
        return tp;
    }

    private void init() {
        art = new JComboBox<Wettkampfart>(Wettkampfart.values());
        name = new JWarningTextField();
        shortname = new JWarningTextField();
        location = createTextPane();
        organizer = createTextPane();
        ausrichter = createTextPane();
        date = new JWarningTextField();
        year = new JComboBox<Integer>(createYears());
        begin = new JWarningTextField();
        end = new JWarningTextField();
        competitionOther = createTextPane();
        nameregistration = new JComboBox<String>(
                new String[] { I18n.get("TeammembersNamesOnly"), I18n.get("TeammembersStrict") });
        printNamesInResults = new JComboBox<String>(
                new String[] { I18n.get("TeamnameOnly"), I18n.get("TeamnameAndMembers") });

        snformats = StartnumberFormatManager.getFormats();
        String[] formats = new String[snformats.length];
        for (int x = 0; x < formats.length; x++) {
            String key = "Startnumberformat." + snformats[x];
            formats[x] = I18n.get(key);
            if (formats[x].equals(key)) {
                formats[x] = snformats[x];
            }
        }
        snformat = new JComboBox<>(formats);

        printReferees = new JComboBox<>(new String[] { I18n.get("Standard"), I18n.get("Compact"), I18n.get("VeryCompact") });

        nameOfPool = createTextPane();
        depthOfPool = createTextPane();
        poollength = new JWarningTextField();
        temperatureOfPool = new JWarningTextField();
        numberoflanes = new JIntSpinner(6, 1, 99, 1);
        elektronischeZeitnahme = new JCheckBox();
        manakin = createTextPane();
        manakin.addMouseListener(new ManakinMouseListener());
        // phantoms = new JIntSpinner(6, 1, 20);
        locationOther = new JTextPane();

        infopage = new JTextPane();

        image = new JImagePanel();

        name.setAutoSelectAll(true);
        shortname.setAutoSelectAll(true);
        date.setAutoSelectAll(true);
        begin.setAutoSelectAll(true);
        end.setAutoSelectAll(true);
        poollength.setAutoSelectAll(true);
        temperatureOfPool.setAutoSelectAll(true);
    }

    private Integer[] createYears() {
        int min = 1999;
        int max = Calendar.getInstance().get(Calendar.YEAR) + 1;
        Integer[] data = new Integer[max - min + 1];
        for (int x = 0; x < max - min + 1; x++) {
            data[x] = min + x;
        }
        return data;
    }

    private JPanel createWettkampfPanel() {
        SimpleFormBuilder sfm = new SimpleFormBuilder();

        sfm.add(I18n.get("TypeOfCompetition"), art);
        sfm.add(I18n.get("Name"), name);
        sfm.add(I18n.get("Shortname"), shortname);
        sfm.add(I18n.get("Location"), new JScrollPane(location), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("Organizer"), new JScrollPane(organizer), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("Ausrichter"), new JScrollPane(ausrichter), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("Date"), date);
        sfm.add(I18n.get("Begin"), begin);
        sfm.add(I18n.get("End"), end);
        sfm.add(I18n.get("Other"), new JScrollPane(competitionOther), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("Year"), year);
        sfm.add(I18n.get("Startnumberformat"), snformat);
        if (wk instanceof MannschaftWettkampf) {
            sfm.add(I18n.get("TeamnameModeLabel"), nameregistration);
            sfm.add(I18n.get("PrintResults"), printNamesInResults);
        }
        sfm.add(I18n.get("PrintReferees"), printReferees);

        return sfm.getPanel();
    }

    private JPanel createOrtPanel() {
        SimpleFormBuilder sfm = new SimpleFormBuilder();

        sfm.add(I18n.get("NameOfPool"), new JScrollPane(nameOfPool), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("DepthOfPool"), new JScrollPane(depthOfPool), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("Poollength"), poollength);
        sfm.add(I18n.get("NumberOfLanes"), numberoflanes);
        sfm.add(I18n.get("ElektronischeZeitnahme"), elektronischeZeitnahme);
        sfm.add(I18n.get("Watertemperature"), temperatureOfPool);
        sfm.add(I18n.get("Puppenaufnahme"), new JScrollPane(manakin), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("Other"), new JScrollPane(locationOther), SimpleFormBuilder.GrowModel.Resize);

        return sfm.getPanel();
    }

    private JPanel createInformationPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default:grow,4dlu");
        JPanel p = new JPanel(layout);
        p.add(new JScrollPane(infopage), CC.xy(2, 2));
        return p;
    }

    private JImagePanel createImagePanel() {
        return image;
    }

    public void apply() {
        synchronized (wk) {
            wk.setProperty(ART_DES_WETTKAMPFS, art.getSelectedItem());
            wk.setProperty(NAME, name.getText());
            wk.setProperty(SHORTNAME, shortname.getText());
            wk.setProperty(LOCATION, location.getText());
            wk.setProperty(ORGANIZER, organizer.getText());
            wk.setProperty(AUSRICHTER, ausrichter.getText());
            wk.setProperty(DATE, date.getText());
            wk.setProperty(YEAR_OF_COMPETITION, year.getSelectedItem());
            wk.setProperty(BEGIN, begin.getText());
            wk.setProperty(END, end.getText());
            wk.setProperty(OTHER_COMPETITION_INFO, competitionOther.getText());
            wk.setProperty(NAMENTLICHE_MELDUNG_STRIKT, nameregistration.getSelectedIndex() == 1);
            wk.setProperty(RESULT_MULTILINE, printNamesInResults.getSelectedIndex() == 1);
            wk.setProperty(STARTNUMBERFORMAT, snformats[snformat.getSelectedIndex()]);
            wk.setProperty(PRINT_REFEREES_COMPACT, printReferees.getSelectedIndex());

            wk.setProperty(NAME_OF_POOL, nameOfPool.getText());
            wk.setProperty(DEPTH_OF_POOL, depthOfPool.getText());
            wk.setProperty(LENGTH_OF_POOL, poollength.getText());
            wk.setProperty(HEATS_LANES, numberoflanes.getInt());
            wk.setProperty(ELEKTRONISCHE_ZEITNAHME, elektronischeZeitnahme.isSelected());
            wk.setProperty(POSITION_OF_MANAKIN, manakin.getText());
            wk.setProperty(WATERTEMPERATURE, temperatureOfPool.getText());
            wk.setProperty(OTHER_LOCATION_INFO, locationOther.getText());

            wk.setProperty(INFOPAGE, infopage.getText());

            wk.setLogo(image.getImageData());
        }
    }

    public void update(@SuppressWarnings("rawtypes") AWettkampf wettkampf) {
        if (wettkampf == null) {
            return;
        }
        // Make sure this happens only once!
        boolean change = false;
        synchronized (wettkampf) {
            if (wk != wettkampf) {
                change = true;
                wk = wettkampf;
            }
        }

        init();
        addChangeListeners();

        addTab(I18n.get("Competition"), createWettkampfPanel());
        addTab(I18n.get("LocationOfCompetition"), createOrtPanel());
        addTab(I18n.get("Infopage"), createInformationPanel());
        addTab(I18n.get("Logo"), createImagePanel());

        if (change) {
            art.setSelectedItem(wk.getProperty(ART_DES_WETTKAMPFS));
            name.setText(wk.getStringProperty(NAME));
            shortname.setText(wk.getStringProperty(SHORTNAME));
            location.setText(wk.getStringProperty(LOCATION));
            organizer.setText(wk.getStringProperty(ORGANIZER));
            ausrichter.setText(wk.getStringProperty(AUSRICHTER));
            date.setText(wk.getStringProperty(DATE));
            year.setSelectedItem(wk.getIntegerProperty(YEAR_OF_COMPETITION, Calendar.getInstance().get(Calendar.YEAR)));
            begin.setText(wk.getStringProperty(BEGIN));
            end.setText(wk.getStringProperty(END));
            competitionOther.setText(wk.getStringProperty(OTHER_COMPETITION_INFO));
            nameregistration.setSelectedIndex(wk.getBooleanProperty(NAMENTLICHE_MELDUNG_STRIKT) ? 1 : 0);
            printNamesInResults.setSelectedIndex(wk.getBooleanProperty(RESULT_MULTILINE) ? 1 : 0);
            snformat.setSelectedIndex(
                    StartnumberFormatManager.GetIndex(wk.getStringProperty(STARTNUMBERFORMAT, "Default")));
            printReferees.setSelectedIndex(wk.getIntegerProperty(PRINT_REFEREES_COMPACT, 1));

            nameOfPool.setText(wk.getStringProperty(NAME_OF_POOL));
            depthOfPool.setText(wk.getStringProperty(DEPTH_OF_POOL));
            poollength.setText(wk.getStringProperty(LENGTH_OF_POOL));
            numberoflanes.setInt(wk.getIntegerProperty(HEATS_LANES));
            elektronischeZeitnahme.setSelected(wk.getBooleanProperty(ELEKTRONISCHE_ZEITNAHME));
            temperatureOfPool.setText(wk.getStringProperty(WATERTEMPERATURE));
            manakin.setText(wk.getStringProperty(POSITION_OF_MANAKIN));
            locationOther.setText(wk.getStringProperty(OTHER_LOCATION_INFO));

            infopage.setText(wk.getStringProperty(INFOPAGE));

            try {
                image.setImageData(wk.getLogo());
            } catch (Exception ex) {
                image.setImage(null);
                DialogUtils.warn(parent, I18n.get("ProblemWithImage.Information"), I18n.get("ProblemWithImage"));
            }

            parent.setChanged(false);
        }
    }

    class ManakinMouseListener extends MouseAdapter {

        private ManakinSinglePopup popup1 = new ManakinSinglePopup();
        private ManakinTeamPopup   popup2 = new ManakinTeamPopup();

        @Override
        public void mousePressed(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                if (wk instanceof EinzelWettkampf) {
                    popup1.show(manakin, evt.getX(), evt.getY());
                } else {
                    popup2.show(manakin, evt.getX(), evt.getY());
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
            mousePressed(evt);
        }
    }

    static void append(JTextPane pane, String id) {
        try {
            int pos = pane.getCaretPosition();
            if ((pos <= 0) || (pos > pane.getText().length())) {
                pos = pane.getText().length();
            }
            pane.getDocument().insertString(pos, id, null);
        } catch (BadLocationException ble) {
            throw new RuntimeException(ble);
        }
    }

    class ManakinSinglePopup extends JPopupMenu {

        private static final long serialVersionUID = -1770264789718344382L;

        public ManakinSinglePopup() {
            JMenuItem toptop = new JMenuItem(I18n.get("ManakinTopTop"));
            JMenuItem topbottom = new JMenuItem(I18n.get("ManakinTopBottom"));
            JMenuItem bottomtop = new JMenuItem(I18n.get("ManakinBottomTop"));
            JMenuItem bottombottom = new JMenuItem(I18n.get("ManakinBottomBottom"));
            JMenuItem rescue = new JMenuItem(I18n.get("ManakinCombinedRescue"));
            toptop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    append(manakin, I18n.get("ManakinTopTopText"));
                }
            });
            topbottom.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    append(manakin, I18n.get("ManakinTopBottomText"));
                }
            });
            bottomtop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    append(manakin, I18n.get("ManakinBottomTopText"));
                }
            });
            bottombottom.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    append(manakin, I18n.get("ManakinBottomBottomText"));
                }
            });
            rescue.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    append(manakin, I18n.get("ManakinCombinedRescueText"));
                }
            });

            add(toptop);
            add(topbottom);
            add(bottomtop);
            add(bottombottom);
            add(rescue);
        }
    }

    class ManakinTeamPopup extends JPopupMenu {

        private static final long serialVersionUID = -1770264789718344382L;

        public ManakinTeamPopup() {
            JMenuItem top = new JMenuItem(I18n.get("ManakinTeamTop"));
            top.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    append(manakin, I18n.get("ManakinTeamTopText"));
                }
            });
            JMenuItem bottom = new JMenuItem(I18n.get("ManakinTeamBottom"));
            bottom.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    append(manakin, I18n.get("ManakinTeamBottomText"));
                }
            });

            add(top);
            add(bottom);
        }
    }
}