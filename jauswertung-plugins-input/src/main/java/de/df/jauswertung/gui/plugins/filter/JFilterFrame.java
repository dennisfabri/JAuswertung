/*
 * Created on 15.03.2006
 */
package de.df.jauswertung.gui.plugins.filter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Filter;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JGlassFrame;
import de.df.jutils.gui.JInvisibleSplitPane;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.jlist.ModifiableListModel;
import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

class JFilterFrame<T extends ASchwimmer> extends JGlassFrame {

    private static final long serialVersionUID = -3625688252323330067L;

    private AWettkampf<T> wk = null;

    private JPanel main;
    private JList list;
    private ModifiableListModel<String> model = new ModifiableListModel<String>();
    private CardLayout cards;
    private LinkedList<JFilterPanel<T>> panels = new LinkedList<JFilterPanel<T>>();
    private Filter[] filters = null;
    private String[] gliederungen = null;

    private JButton ok = null;
    private JButton apply = null;
    private JButton close = null;

    private JPopupMenu popup = null;
    private JMenuItem add = null;
    private JMenuItem remove = null;

    private JButton addFilter = null;
    private JButton removeFilter = null;

    private ISimpleCallback<JFilterFrame<T>> callback = null;

    private JFrame parent;

    public JFilterFrame(JFrame parent, AWettkampf<T> w, ISimpleCallback<JFilterFrame<T>> sc) {
        super(I18n.get("EditFilters"));
        if (parent == null) {
            throw new NullPointerException();
        }
        if (w == null) {
            throw new NullPointerException();
        }
        setIconImages(IconManager.getTitleImages());
        wk = w;
        this.parent = parent;
        callback = sc;

        WindowUtils.addEscapeAction(this, new Runnable() {
            @Override
            public void run() {
                doClose();
            }
        });

        gliederungen = wk.getGliederungenMitQGliederung().toArray(new String[wk.getGliederungen().size()]);

        initGUI();
        initPopup();

        updateRemoveButtons();
        list.setSelectedIndex(0);
    }

    @Override
    public void setVisible(boolean arg0) {
        updateButtons();
        parent.setEnabled(!arg0);
        super.setVisible(arg0);
    }

    private void initGUI() {
        JInvisibleSplitPane jisp = new JInvisibleSplitPane();
        jisp.setBorder(new EmptyBorder(5, 5, 0, 5));

        cards = new CardLayout();
        list = new JList(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        main = new JPanel(cards);

        filters = wk.getFilter();
        String[] t = new String[filters.length];
        for (int x = 0; x < filters.length; x++) {
            panels.addLast(new JFilterPanel<T>(this, filters[x], gliederungen, x));
            main.add(panels.getLast(), "" + x);
            t[x] = "";
        }
        model.addAll(t);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                updateSelection();
            }
        });

        JScrollPane scroller = new JScrollPane(list);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        addFilter = new JTransparentButton(IconManager.getSmallIcon("new"));
        removeFilter = new JTransparentButton(IconManager.getSmallIcon("remove"));
        addFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                addFilter();
            }
        });
        removeFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                removeFilter();
            }
        });

        JPanel p = new JPanel(
                new FormLayout("4dlu,fill:default:grow,4dlu", "4dlu,fill:default:grow,4dlu,fill:default,4dlu"));

        p.setBorder(BorderUtils.createLabeledBorder(I18n.get("Filter")));

        JPanel p1 = new JPanel(new FormLayout("0dlu:grow,fill:default,4dlu,fill:default", "fill:default"));
        p1.add(addFilter, CC.xy(2, 1));
        p1.add(removeFilter, CC.xy(4, 1));

        p.add(scroller, CC.xy(2, 2));
        p.add(p1, CC.xy(2, 4));

        jisp.setLeftComponent(p);
        jisp.setRightComponent(main);

        add(jisp);

        JPanel buttons = new JPanel(new GridLayout(1, 3, 5, 5));
        ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        apply = new JButton(I18n.get("Apply"), IconManager.getSmallIcon("apply"));
        close = new JButton(I18n.get("Close"), IconManager.getSmallIcon("close"));
        buttons.add(ok);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                doOk();
            }
        });
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                doClose();
            }
        });
        apply.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                doApply();
            }
        });
        buttons.add(apply);
        buttons.add(close);

        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        b.add(buttons);
        add(b, BorderLayout.SOUTH);

        updateList();
        updateButtons();

        pack();
        UIStateUtils.uistatemanage(parent, this, "EditFilters");
    }

    private void initPopup() {
        remove = new JMenuItem(I18n.get("RemoveFilter"));
        add = new JMenuItem(I18n.get("AddFilter"));
        popup = new JPopupMenu();
        popup.add(add);
        popup.add(remove);
        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent me) {
                showPopup(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                showPopup(me);
            }
        });
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                addFilter();
            }
        });
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                removeFilter();
            }
        });
    }

    private void updateButtons() {
        boolean id = inputDone();
        boolean iv = inputValid();
        apply.setEnabled(id && iv);
        ok.setEnabled(id && iv);
        if (id) {
            close.setText(I18n.get("Cancel"));
            close.setIcon(IconManager.getSmallIcon("cancel"));
        } else {
            close.setText(I18n.get("Close"));
            close.setIcon(IconManager.getSmallIcon("close"));
        }
    }

    private boolean changed = false;
    private boolean input = false;

    public boolean hasChanged() {
        return changed;
    }

    public void notifyInput() {
        updateList();
        updateButtons();
    }

    @SuppressWarnings("unchecked")
    private void updateList() {
        JFilterPanel<T>[] p = panels.toArray(new JFilterPanel[panels.size()]);
        for (int x = 0; x < p.length; x++) {
            String name = p[x].getFiltername();
            if (name.length() == 0) {
                name = I18n.get("FilterNr", x);
            }
            model.setValueAt(x, name);
        }
    }

    public Filter[] getFilter() {
        return filters;
    }

    @SuppressWarnings("unchecked")
    private boolean inputDone() {
        JFilterPanel<T>[] p = panels.toArray(new JFilterPanel[panels.size()]);
        for (JFilterPanel<T> aP : p) {
            if (aP.hasChanged()) {
                return true;
            }
        }
        return input;
    }

    @SuppressWarnings("unchecked")
    private boolean inputValid() {
        JFilterPanel<T>[] p = panels.toArray(new JFilterPanel[panels.size()]);
        for (JFilterPanel<T> aP : p) {
            if (!aP.isValidFilter()) {
                return false;
            }
        }
        return true;
    }

    void doOk() {
        doApply();
        doClose();
    }

    @SuppressWarnings("unchecked")
    void doApply() {
        JFilterPanel<T>[] p = panels.toArray(new JFilterPanel[panels.size()]);
        Filter[] f = new Filter[p.length];
        boolean c = input;
        for (int x = 0; x < p.length; x++) {
            f[x] = p[x].getFilter();
            c = c | p[x].hasChanged();
            p[x].unsetChanged();
        }
        if (c) {
            changed = true;
            input = false;
            filters = f;
            callback.callback(this);
        }
    }

    void doClose() {
        setVisible(false);
    }

    void updateSelection() {
        cards.show(main, "" + list.getSelectedIndex());
        updateRemoveButtons();
    }

    void showPopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            list.setSelectedIndex(list.locationToIndex(evt.getPoint()));
            popup.show(list, evt.getX(), evt.getY());
        }
    }

    void updateRemoveButtons() {
        boolean b = list.getSelectedIndex() > 0;
        remove.setEnabled(b);
        removeFilter.setEnabled(b);
    }

    void removeFilter() {
        int index = list.getSelectedIndex();
        if (index <= 0) {
            return;
        }
        int size = model.getSize();
        model.remove(index);
        main.remove(panels.get(index));
        panels.remove(index);

        assert panels.size() == size - 1;
        assert model.getSize() == size - 1;

        input = true;
        notifyInput();
        cards.show(main, "" + (index - 1));
        list.setSelectedIndex(index - 1);
    }

    void addFilter() {
        int index = list.getSelectedIndex();
        if (index < 0) {
            return;
        }
        index++;
        JFilterPanel<T> p = new JFilterPanel<T>(this, new Filter(), gliederungen, index);
        model.add("", index);
        panels.add(index, p);
        main.removeAll();
        for (int x = 0; x < panels.size(); x++) {
            p = panels.get(x);
            p.updateIndex(x);
            main.add(p, "" + x);
        }
        input = true;
        notifyInput();
        cards.show(main, "" + index);
    }

    public Filter getSelectedFilter() {
        String name = (String) list.getSelectedValue();
        for (Filter f : filters) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }
}