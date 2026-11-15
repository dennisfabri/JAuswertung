package de.df.jauswertung.gui.plugins.print;

import java.awt.Component;
import java.awt.print.Printable;
import java.time.LocalDateTime;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.print.Aushangzeiten;
import de.df.jauswertung.print.UnterschriftPrintable;
import de.df.jauswertung.util.Utils;

class JUnterschriftAuswahl extends JPanel {

    private static final int MAXIMALER_VERSATZ_IN_MINUTEN = 24 * 60;

    private final JComboBox<Unterschriftsart> art = new JComboBox<>(Unterschriftsart.values());
    private final JLabel aushangIn = new JLabel(I18n.get("AushangIn"));
    private final JSpinner versatz;
    private final JLabel minuten = new JLabel(I18n.get("Unterschrift.Minuten"));

    /**
     * @param altePraeferenz boolean preference of the former signature checkbox; read only while the new preferences
     *                       are not set yet.
     */
    JUnterschriftAuswahl(String praeferenz, String altePraeferenz) {
        super(new FormLayout("fill:default,4dlu,fill:default,1dlu,fill:default,1dlu,fill:default", "fill:default"));
        setOpaque(false);

        Preferences prefs = Utils.getPreferences();
        String artKey = praeferenz + "Art";
        String versatzKey = praeferenz + "AushangInMinuten";

        art.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                String text = value instanceof Unterschriftsart u ? I18n.get(u.getI18nKey()) : "";
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });
        art.setSelectedItem(gespeicherteArt(prefs, artKey, altePraeferenz));
        art.addItemListener(e -> {
            prefs.put(artKey, getArt().name());
            aktualisiereVersatz();
        });

        int gespeicherterVersatz = Math.clamp(prefs.getInt(versatzKey, 0), 0, MAXIMALER_VERSATZ_IN_MINUTEN);
        versatz = new JSpinner(new SpinnerNumberModel(gespeicherterVersatz, 0, MAXIMALER_VERSATZ_IN_MINUTEN, 5));
        ((JSpinner.DefaultEditor) versatz.getEditor()).getTextField().setColumns(4);
        versatz.setToolTipText(I18n.getToolTip("AushangIn"));
        aushangIn.setToolTipText(I18n.getToolTip("AushangIn"));
        versatz.addChangeListener(e -> prefs.putInt(versatzKey, getVersatz()));

        add(art, CC.xy(1, 1));
        add(aushangIn, CC.xy(3, 1));
        add(versatz, CC.xy(5, 1));
        add(minuten, CC.xy(7, 1));

        setEnabled(false);
    }

    private static Unterschriftsart gespeicherteArt(Preferences prefs, String artKey, String altePraeferenz) {
        String name = prefs.get(artKey, null);
        if (name != null) {
            for (Unterschriftsart u : Unterschriftsart.values()) {
                if (u.name().equals(name)) {
                    return u;
                }
            }
        }
        return prefs.getBoolean(altePraeferenz, false) ? Unterschriftsart.LEER : Unterschriftsart.KEINE;
    }

    private Unterschriftsart getArt() {
        return (Unterschriftsart) art.getSelectedItem();
    }

    private int getVersatz() {
        return ((Number) versatz.getValue()).intValue();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        art.setEnabled(enabled);
        aktualisiereVersatz();
    }

    private void aktualisiereVersatz() {
        boolean mitZeiten = isEnabled() && getArt() == Unterschriftsart.MIT_AUSHANGZEITEN;
        aushangIn.setEnabled(mitZeiten);
        versatz.setEnabled(mitZeiten);
        minuten.setEnabled(mitZeiten);
    }

    /**
     * Must be called on the EDT when printing starts: the posting time is fixed at this moment.
     */
    UnaryOperator<Printable> getUnterschrift() {
        return switch (getArt()) {
        case KEINE -> p -> p;
        case LEER -> UnterschriftPrintable::new;
        case MIT_AUSHANGZEITEN -> {
            Aushangzeiten zeiten = Aushangzeiten.ab(LocalDateTime.now(), getVersatz());
            yield p -> new UnterschriftPrintable(p, zeiten);
        }
        };
    }
}
