/*
 * Created on 02.05.2005
 */
package de.df.jauswertung.gui.plugins.properties;

import java.awt.AWTKeyStroke;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
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
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.PropertyConstants;
import de.df.jauswertung.daten.Wettkampfart;
import de.df.jauswertung.daten.laufliste.HeatsNumberingScheme;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jauswertung.web.iscupload.ISCUploadCredentialRepository;
import de.df.jauswertung.web.iscupload.ISCUploadCredentials;
import de.df.jutils.gui.JImagePanel;
import de.df.jutils.gui.JIntSpinner;
import de.df.jutils.gui.JWarningTextField;
import de.df.jutils.gui.layout.SimpleFormBuilder;
import de.df.jutils.gui.util.DialogUtils;
import de.df.jutils.gui.window.JOptionsDialog;

import static de.df.jauswertung.daten.PropertyConstants.*;

public final class JPropertiesTabbedPane extends JTabbedPane {

    private final ISCUploadCredentialRepository authKeys = new ISCUploadCredentialRepository();

    private JComboBox<Wettkampfart> art;
    private JWarningTextField name;
    private JWarningTextField shortname;
    private JTextPane location;
    private JTextPane organizer;
    private JTextPane ausrichter;
    private JWarningTextField date;
    private JWarningTextField begin;
    private JWarningTextField end;
    private JTextPane competitionOther;
    private JComboBox<String> nameRegistration;
    private JComboBox<String> printNamesInResults;
    private JComboBox<String> snFormat;
    private JComboBox<HeatsNumberingDisplay> heatNumberFormat;
    private JComboBox<String> printReferees;

    private String[] snFormats;

    private JComboBox<Integer> year;

    private JTextPane nameOfPool;
    private JTextPane depthOfPool;
    private JWarningTextField temperatureOfPool;
    private JWarningTextField poolLength;
    private JIntSpinner numberOfLanes;
    private JCheckBox elektronischeZeitnahme;
    private JTextPane manikin;
    private JTextPane locationOther;

    JTextPane infopage;

    JImagePanel image;

    JOptionsDialog dialog;

    private JWarningTextField edvNumber;
    private JWarningTextField competitionId;
    private JWarningTextField authKey;

    @SuppressWarnings("rawtypes")
    private AWettkampf wk;

    public JPropertiesTabbedPane(JOptionsDialog parent) {
        this.dialog = parent;
    }

    private void addChangeListeners() {
        ItemListener item = e -> dialog.setChanged(true);

        DocumentListener doc = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                dialog.setChanged(true);
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

        ChangeListener change = e -> dialog.setChanged(true);

        ActionListener action = e -> dialog.setChanged(true);

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

        nameRegistration.addItemListener(item);
        printNamesInResults.addActionListener(action);
        snFormat.addActionListener(action);
        heatNumberFormat.addItemListener(item);
        year.addItemListener(item);
        printReferees.addActionListener(action);

        nameOfPool.getDocument().addDocumentListener(doc);
        depthOfPool.getDocument().addDocumentListener(doc);
        poolLength.getDocument().addDocumentListener(doc);
        temperatureOfPool.getDocument().addDocumentListener(doc);
        numberOfLanes.addChangeListener(change);
        elektronischeZeitnahme.addActionListener(action);
        manikin.getDocument().addDocumentListener(doc);

        infopage.getDocument().addDocumentListener(doc);

        image.addChangeListener(change);

        edvNumber.getDocument().addDocumentListener(doc);
        competitionId.getDocument().addDocumentListener(doc);
        authKey.getDocument().addDocumentListener(doc);
    }

    private void setTraversalKeys(JTextPane textArea) {
        Set<AWTKeyStroke> set = new HashSet<>(
                textArea.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        set.add(KeyStroke.getKeyStroke("TAB"));
        textArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);

        set = new HashSet<>(textArea.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        set.add(KeyStroke.getKeyStroke("shift TAB"));
        textArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, set);
    }

    private JTextPane createTextPane() {
        JTextPane tp = new JTextPane();
        setTraversalKeys(tp);
        return tp;
    }

    private void init() {
        art = new JComboBox<>(Wettkampfart.values());
        name = new JWarningTextField();
        shortname = new JWarningTextField();
        location = createTextPane();
        organizer = createTextPane();
        ausrichter = createTextPane();
        date = new JWarningTextField();
        year = new JComboBox<>(createYears());
        begin = new JWarningTextField();
        end = new JWarningTextField();
        competitionOther = createTextPane();
        nameRegistration = new JComboBox<>(
                new String[] { I18n.get("TeammembersNamesOnly"), I18n.get("TeammembersStrict") });
        printNamesInResults = new JComboBox<>(
                new String[] { I18n.get("TeamnameOnly"), I18n.get("TeamnameAndMembers") });

        snFormats = StartnumberFormatManager.getFormats();
        String[] formats = new String[snFormats.length];
        for (int x = 0; x < formats.length; x++) {
            String key = "Startnumberformat." + snFormats[x];
            formats[x] = I18n.get(key);
            if (formats[x].equals(key)) {
                formats[x] = snFormats[x];
            }
        }
        snFormat = new JComboBox<>(formats);
        heatNumberFormat = new JComboBox<>(HeatsNumberingDisplay.values());

        printReferees = new JComboBox<>(
                new String[] { I18n.get("Standard"), I18n.get("Compact"), I18n.get("VeryCompact") });

        nameOfPool = createTextPane();
        depthOfPool = createTextPane();
        poolLength = new JWarningTextField();
        temperatureOfPool = new JWarningTextField();
        numberOfLanes = new JIntSpinner(6, 1, 99, 1);
        elektronischeZeitnahme = new JCheckBox();
        manikin = createTextPane();
        manikin.addMouseListener(new ManikinMouseListener());
        locationOther = new JTextPane();

        infopage = new JTextPane();

        image = new JImagePanel();

        edvNumber = new JWarningTextField();
        competitionId = new JWarningTextField();
        authKey = new JWarningTextField();

        name.setAutoSelectAll(true);
        shortname.setAutoSelectAll(true);
        date.setAutoSelectAll(true);
        begin.setAutoSelectAll(true);
        end.setAutoSelectAll(true);
        poolLength.setAutoSelectAll(true);
        temperatureOfPool.setAutoSelectAll(true);
        edvNumber.setAutoSelectAll(true);
        competitionId.setAutoSelectAll(true);
        authKey.setAutoSelectAll(true);
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
        sfm.add(I18n.get("Startnumberformat"), snFormat);
        sfm.add(I18n.get("HeatsNumberingScheme"), heatNumberFormat);
        if (wk instanceof MannschaftWettkampf) {
            sfm.add(I18n.get("TeamnameModeLabel"), nameRegistration);
            sfm.add(I18n.get("PrintResults"), printNamesInResults);
        }
        sfm.add(I18n.get("PrintReferees"), printReferees);

        return sfm.getPanel();
    }

    private JPanel createOrtPanel() {
        SimpleFormBuilder sfm = new SimpleFormBuilder();

        sfm.add(I18n.get("NameOfPool"), new JScrollPane(nameOfPool), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("DepthOfPool"), new JScrollPane(depthOfPool), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("Poollength"), poolLength);
        sfm.add(I18n.get("NumberOfLanes"), numberOfLanes);
        sfm.add(I18n.get("ElektronischeZeitnahme"), elektronischeZeitnahme);
        sfm.add(I18n.get("Watertemperature"), temperatureOfPool);
        sfm.add(I18n.get("Puppenaufnahme"), new JScrollPane(manikin), SimpleFormBuilder.GrowModel.Resize);
        sfm.add(I18n.get("Other"), new JScrollPane(locationOther), SimpleFormBuilder.GrowModel.Resize);

        return sfm.getPanel();
    }

    private JPanel createInformationPanel() {
        FormLayout layout = new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default:grow,4dlu");
        JPanel p = new JPanel(layout);
        p.add(new JScrollPane(infopage), CC.xy(2, 2));
        return p;
    }

    private JPanel createISCPanel() {
        SimpleFormBuilder sfm = new SimpleFormBuilder(true, false);

        sfm.add(I18n.get("EDVNumber"), edvNumber);
        sfm.add(I18n.get("CompetitionId"), competitionId);
        sfm.add(I18n.get("ISCUploadAuthKey"), authKey);
        sfm.addText(I18n.get("ISCUploadAuthKey.Info"));

        return sfm.getPanel();
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
            wk.setProperty(NAMENTLICHE_MELDUNG_STRIKT, nameRegistration.getSelectedIndex() == 1);
            wk.setProperty(RESULT_MULTILINE, printNamesInResults.getSelectedIndex() == 1);
            wk.setProperty(STARTNUMBERFORMAT, snFormats[snFormat.getSelectedIndex()]);
            wk.setProperty(HEATS_NUMBERING_SCHEME, getHeatsNumberingScheme().getValue());
            wk.setProperty(PRINT_REFEREES_COMPACT, printReferees.getSelectedIndex());

            wk.setProperty(NAME_OF_POOL, nameOfPool.getText());
            wk.setProperty(DEPTH_OF_POOL, depthOfPool.getText());
            wk.setProperty(LENGTH_OF_POOL, poolLength.getText());
            wk.setProperty(HEATS_LANES, numberOfLanes.getInt());
            wk.setProperty(ELEKTRONISCHE_ZEITNAHME, elektronischeZeitnahme.isSelected());
            wk.setProperty(POSITION_OF_MANAKIN, manikin.getText());
            wk.setProperty(WATERTEMPERATURE, temperatureOfPool.getText());
            wk.setProperty(OTHER_LOCATION_INFO, locationOther.getText());

            wk.setProperty(INFOPAGE, infopage.getText());

            wk.setLogo(image.getImageData());

            wk.setProperty(ISC_RESULT_UPLOAD_EDVNUMBER, edvNumber.getText());
            wk.setProperty(ISC_RESULT_UPLOAD_COMPETITION_ID, competitionId.getText());

            authKeys.putCredentials(new ISCUploadCredentials(wk.getStringProperty(ISC_RESULT_UPLOAD_EDVNUMBER),
                                                             wk.getStringProperty(ISC_RESULT_UPLOAD_COMPETITION_ID), authKey.getText()));
        }
    }

    private HeatsNumberingScheme getHeatsNumberingScheme() {
        HeatsNumberingDisplay scheme = (HeatsNumberingDisplay) heatNumberFormat.getSelectedItem();
        if (scheme == null) {
            return HeatsNumberingScheme.Standard;
        }
        return scheme.getScheme();
    }

    public void update(@SuppressWarnings("rawtypes") AWettkampf wettkampf) {
        if (wettkampf == null) {
            return;
        }
        // Make sure this happens only once!
        boolean change = false;
        synchronized (this) {
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
        addTab(I18n.get("ISCUpload"), createISCPanel());

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
            nameRegistration.setSelectedIndex(wk.getBooleanProperty(NAMENTLICHE_MELDUNG_STRIKT) ? 1 : 0);
            printNamesInResults.setSelectedIndex(wk.getBooleanProperty(RESULT_MULTILINE) ? 1 : 0);
            snFormat.setSelectedIndex(
                    StartnumberFormatManager.GetIndex(wk.getStringProperty(STARTNUMBERFORMAT, "Default")));
            heatNumberFormat.setSelectedIndex(getHeatsNumberingSchemeIndex(wk.getStringProperty(HEATS_NUMBERING_SCHEME, HeatsNumberingScheme.Standard.getValue())));
            printReferees.setSelectedIndex(wk.getIntegerProperty(PRINT_REFEREES_COMPACT, 1));

            nameOfPool.setText(wk.getStringProperty(NAME_OF_POOL));
            depthOfPool.setText(wk.getStringProperty(DEPTH_OF_POOL));
            poolLength.setText(wk.getStringProperty(LENGTH_OF_POOL));
            numberOfLanes.setInt(wk.getIntegerProperty(HEATS_LANES));
            elektronischeZeitnahme.setSelected(wk.getBooleanProperty(ELEKTRONISCHE_ZEITNAHME));
            temperatureOfPool.setText(wk.getStringProperty(WATERTEMPERATURE));
            manikin.setText(wk.getStringProperty(POSITION_OF_MANAKIN));
            locationOther.setText(wk.getStringProperty(OTHER_LOCATION_INFO));

            infopage.setText(wk.getStringProperty(INFOPAGE));

            edvNumber.setText(wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_EDVNUMBER));
            competitionId.setText(wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_COMPETITION_ID));
            authKey.setText(authKeys.getCredentials(wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_EDVNUMBER),
                                                    wk.getStringProperty(PropertyConstants.ISC_RESULT_UPLOAD_COMPETITION_ID)));

            try {
                image.setImageData(wk.getLogo());
            } catch (Exception ex) {
                image.setImage(null);
                DialogUtils.warn(dialog, I18n.get("ProblemWithImage.Information"), I18n.get("ProblemWithImage"));
            }

            dialog.setChanged(false);
        }
    }

    private int getHeatsNumberingSchemeIndex(String scheme) {
        HeatsNumberingScheme[] values = HeatsNumberingScheme.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].getValue().equalsIgnoreCase(scheme)) {
                return i;
            }
        }
        return 0;
    }

    class ManikinMouseListener extends MouseAdapter {

        private final ManikinIndividualPopup individualPopup = new ManikinIndividualPopup();
        private final ManikinTeamPopup teamPopup = new ManikinTeamPopup();

        @Override
        public void mousePressed(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                if (wk instanceof EinzelWettkampf) {
                    individualPopup.show(manikin, evt.getX(), evt.getY());
                } else {
                    teamPopup.show(manikin, evt.getX(), evt.getY());
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

    class ManikinIndividualPopup extends JPopupMenu {

        public ManikinIndividualPopup() {
            JMenuItem toptop = new JMenuItem(I18n.get("ManakinTopTop"));
            JMenuItem manikinTopBottom = new JMenuItem(I18n.get("ManakinTopBottom"));
            JMenuItem manikinBottomTop = new JMenuItem(I18n.get("ManakinBottomTop"));
            JMenuItem manikinBottomBottom = new JMenuItem(I18n.get("ManakinBottomBottom"));
            JMenuItem rescue = new JMenuItem(I18n.get("ManakinCombinedRescue"));
            toptop.addActionListener(e -> append(manikin, I18n.get("ManakinTopTopText")));
            manikinTopBottom.addActionListener(e -> append(manikin, I18n.get("ManakinTopBottomText")));
            manikinBottomTop.addActionListener(e -> append(manikin, I18n.get("ManakinBottomTopText")));
            manikinBottomBottom.addActionListener(e -> append(manikin, I18n.get("ManakinBottomBottomText")));
            rescue.addActionListener(e -> append(manikin, I18n.get("ManakinCombinedRescueText")));

            add(toptop);
            add(manikinTopBottom);
            add(manikinBottomTop);
            add(manikinBottomBottom);
            add(rescue);
        }
    }

    class ManikinTeamPopup extends JPopupMenu {

        public ManikinTeamPopup() {
            JMenuItem top = new JMenuItem(I18n.get("ManakinTeamTop"));
            top.addActionListener(e -> append(manikin, I18n.get("ManakinTeamTopText")));
            JMenuItem bottom = new JMenuItem(I18n.get("ManakinTeamBottom"));
            bottom.addActionListener(e -> append(manikin, I18n.get("ManakinTeamBottomText")));

            add(top);
            add(bottom);
        }
    }
}