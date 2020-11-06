package de.df.jauswertung.gui.plugins.zielrichterentscheid;

import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.blogspot.rabbithole.JSmoothList;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Zielrichterentscheid;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.LaufInfo;
import de.df.jauswertung.gui.util.SchwimmerUtils;
import de.df.jutils.gui.JLabelSeparator;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.util.StringTools;

class JZielrichterentscheidPanel<T extends ASchwimmer> extends JPanel {

    private static final long                   serialVersionUID = 8251280743952459484L;

    private Zielrichterentscheid<T>             ze               = null;

    private JLabel                              zeit             = new JLabel();
    private JLabel                              ak               = new JLabel();
    private ModifiableListModel<Object[]>       model            = new ModifiableListModel<Object[]>();
    private JSmoothList<Object[]>               liste            = new JSmoothList<Object[]>(model);

    private final JZielrichterentscheidFrame<T> parent;

    public JZielrichterentscheidPanel(JZielrichterentscheidFrame<T> parent) {
        this.parent = parent;

        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        liste.setCellRenderer(new SchwimmerAndLaneListCellRenderer());

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow,4dlu",
                "4dlu,fill:default,4dlu,fill:default,8dlu,fill:default,4dlu,fill:default:grow,4dlu");
        setLayout(layout);

        add(new JLabel(I18n.get("AgeGroup") + ":"), CC.xy(2, 2));
        add(ak, CC.xy(4, 2));
        add(new JLabel(I18n.get("Time") + ":"), CC.xy(2, 4));
        add(zeit, CC.xy(4, 4));

        add(new JLabelSeparator(I18n.get("Order")), CC.xyw(2, 6, 3, "fill,fill"));

        JScrollPane scr = new JScrollPane(liste);
        scr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scr, CC.xyw(2, 8, 3, "fill,fill"));

        setEntscheid(null);
    }

    public void setEntscheid(Zielrichterentscheid<T> ze) {
        this.ze = null;

        if (ze == null) {
            setBorder(BorderUtils.createLabeledBorder(I18n.get("NoZielrichterentscheidSelected")));
            liste.setEnabled(false);
            liste.setListData(new Zielrichterentscheid[0]);
            zeit.setText("");
            ak.setText("");
            return;
        }

        liste.setEnabled(true);

        {
            T s = ze.getSchwimmer().getFirst();
            LaufInfo li = SchwimmerUtils.getLaufInfo(s.getWettkampf(), s, ze.getDisziplin());

            setBorder(BorderUtils.createLabeledBorder(I18n.get("HeatNr", li.getLauf(), 1) + ": " + s.getAK().getDisziplin(ze.getDisziplin(), s.isMaennlich())));
            ak.setText(I18n.getAgeGroupAsString(s));
        }

        LinkedList<Object[]> result = new LinkedList<Object[]>();
        for (ASchwimmer s : ze.getSchwimmer()) {
            Object[] o = new Object[2];
            o[0] = s;
            o[1] = SchwimmerUtils.getLaufInfo(s.getWettkampf(), s, ze.getDisziplin()).getBahn();

            result.addLast(o);
        }
        model = createModel(result);
        liste.setModel(model);
        if (model.getSize() > 0) {
            liste.setSelectedIndex(0);
        }

        zeit.setText(StringTools.zeitString(ze.getZeit()));

        this.ze = ze;
    }

    private ModifiableListModel<Object[]> createModel(LinkedList<Object[]> result) {
        ModifiableListModel<Object[]> m = new ModifiableListModel<Object[]>(result);
        m.addListDataListener(new ListDataListener() {
            @Override
            public void contentsChanged(ListDataEvent e) {
                updateZielrichterentscheid();
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                updateZielrichterentscheid();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                updateZielrichterentscheid();
            }
        });
        return m;
    }

    @SuppressWarnings("unchecked")
    void updateZielrichterentscheid() {
        if (ze != null) {
            LinkedList<T> result = new LinkedList<T>();
            for (Object[] o : model.getAllElements()) {
                result.addLast((T) o[0]);
            }

            ze.setSchwimmer(result);

            parent.setChanged(true);
        }
    }
}