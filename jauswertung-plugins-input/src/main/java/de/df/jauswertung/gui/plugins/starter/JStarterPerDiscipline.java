package de.df.jauswertung.gui.plugins.starter;

import java.awt.BorderLayout;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.df.jauswertung.daten.Eingabe;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.layout.SimpleFormBuilder;

public class JStarterPerDiscipline extends JPanel {

    private final Mannschaft schwimmer;
    private final OWDisziplin<Mannschaft> disziplin;
    private final Eingabe eingabe;
    private final int disz;

    private String[] mannschaftsmitglieder;

    @SuppressWarnings("unchecked")
    private final JComboBox<String>[] starter = new JComboBox[4];
    @SuppressWarnings("unchecked")
    private DefaultComboBoxModel<String>[] model = new DefaultComboBoxModel[4];
    private JLabel title = new JLabel();

    public JStarterPerDiscipline(Mannschaft s, int disz) {
        schwimmer = s;
        disziplin = null;
        eingabe = null;
        this.disz = disz;

        title.setText(s.getAK().getDisziplin(disz, true).getName());

        initStarter();
        initUI();
    }

    public JStarterPerDiscipline(Mannschaft s, OWDisziplin<Mannschaft> o) {
        schwimmer = s;
        disziplin = o;
        eingabe = s.getEingabe(o.Id);
        this.disz = o.disziplin;

        boolean isFinal = s.getWettkampf().isFinal(o);

        StringBuilder sb = new StringBuilder();
        sb.append(s.getAK().getDisziplin(disz, true).getName());
        if (o.round > 0) {
            sb.append(" - (");
            sb.append(I18n.getRound(o.round, isFinal));
            sb.append(")");
        }

        title.setText(sb.toString());

        initStarter();
        initUI();
    }

    private void initStarter() {
        String[] mm = schwimmer.getMitgliedernamen();
        LinkedList<String> mmx = new LinkedList<String>();
        mmx.add("");
        for (String m : mm) {
            mmx.add(m);
        }
        mannschaftsmitglieder = mmx.toArray(new String[mmx.size()]);
    }

    private void initUI() {
        SimpleFormBuilder sfb = new SimpleFormBuilder();

        int[] sx = null;
        if (eingabe == null || disziplin.round == 0) {
            sx = schwimmer.getStarter(disz);
        } else {
            sx = eingabe.getStarter();
        }
        if (sx == null) {
            sx = new int[starter.length];
        }

        for (int x = 1; x <= starter.length; x++) {
            starter[x - 1] = new JComboBox<>();
            model[x - 1] = new DefaultComboBoxModel<>(mannschaftsmitglieder);
            starter[x - 1].setModel(model[x - 1]);
            try {
                starter[x - 1].setSelectedIndex(sx[x - 1]);
            } catch (Exception ex) {
                // Nothing to do
            }
            sfb.add(I18n.get("StarterNr", x), starter[x - 1]);
        }
        JPanel p = sfb.getPanel();

        this.setLayout(new BorderLayout());
        add(p, BorderLayout.CENTER);
    }

    public String getTitle() {
        return title.getText();
    }

    public boolean doApply() {
        int[] sx = new int[starter.length];
        for (int x = 0; x < starter.length; x++) {
            sx[x] = starter[x].getSelectedIndex();
        }
        if (eingabe == null || disziplin.round == 0) {
            schwimmer.setStarter(disz, sx);
        } else {
            eingabe.setStarter(sx);
        }
        return true;
    }
}
