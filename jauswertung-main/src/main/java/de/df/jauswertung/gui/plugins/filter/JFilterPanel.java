/*
 * Created on 15.03.2006
 */
package de.df.jauswertung.gui.plugins.filter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.Filter;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.jlist.SortableListModel;

class JFilterPanel<T extends ASchwimmer> extends JPanel {

    private static final long         serialVersionUID = 1149714415412966195L;

    private JTextField                name;

    private SortableListModel<String> model            = new SortableListModel<String>();
    private JList                     fi               = new JList(model);
    private LinkedList<String>        gl               = null;
    private JButton                   add              = new JTransparentButton(IconManager.getSmallIcon("new"));
    private JButton                   remove           = new JTransparentButton(IconManager.getSmallIcon("remove"));
    private JFilterFrame<T>           parent;

    public JFilterPanel(JFilterFrame<T> parent, Filter f, String[] n, int index) {
        if (n == null) {
            throw new NullPointerException();
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.parent = parent;
        name = new JTextField();

        String[] gliederungen = null;
        String[] filter = null;
        if (f != null) {
            name.setText(f.getName());
            filter = f.getGliederungen();
            if (filter == null) {
                filter = new String[0];
            }
        } else {
            filter = new String[0];
        }

        gliederungen = n;

        model.addAll(filter);
        gl = new LinkedList<String>();
        for (String aGliederungen : gliederungen) {
            boolean found = false;
            for (int y = 0; y < filter.length; y++) {
                if (aGliederungen.equals(filter[y])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                gl.addLast(aGliederungen);
            }
        }
        Collections.sort(gl);
        if (gl.size() == 0) {
            add.setEnabled(false);
        }
        updateIndex(index);

        FormLayout layout = new FormLayout("4dlu,fill:default,4dlu,fill:default:grow," + "4dlu,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default,4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        layout.setColumnGroups(new int[][] { { 6, 8 } });
        setLayout(layout);
        add(new JLabel(I18n.get("Name")), CC.xy(2, 2));
        add(name, CC.xyw(4, 2, 5));
        add(new JScrollPane(fi), CC.xyw(2, 4, 7));
        add(add, CC.xy(6, 6));
        add(remove, CC.xy(8, 6));

        remove.setEnabled(false);
        if (index == 0) {
            add.setEnabled(false);
            fi.setEnabled(false);
            name.setEnabled(false);
        }

        fi.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                selectionChanged();
            }
        });
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                doAdd();
            }
        });
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                doRemove();
            }
        });
        name.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                changedUpdate(arg0);
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                changedUpdate(arg0);
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                nameChange();
            }
        });
    }

    public void updateIndex(int index) {
        if (index == 0) {
            setBorder(BorderUtils.createLabeledBorder(I18n.get("PredefinedFilter")));
        } else {
            setBorder(BorderUtils.createLabeledBorder(I18n.get("FilterNr", index)));
        }
    }

    private boolean changed = false;

    public boolean hasChanged() {
        return changed;
    }

    public void unsetChanged() {
        changed = false;
    }

    public String getFiltername() {
        return name.getText();
    }

    public boolean isValidFilter() {
        return name.getText().length() > 0;
    }

    public Filter getFilter() {
        ListModel lm = fi.getModel();
        String[] f = new String[lm.getSize()];
        if (f.length == 0) {
            f = null;
        } else {
            for (int x = 0; x < f.length; x++) {
                f[x] = lm.getElementAt(x).toString();
            }
        }
        return new Filter(name.getText(), f);
    }

    void selectionChanged() {
        int l = fi.getSelectedIndices().length;
        remove.setEnabled(l > 0);
    }

    void doRemove() {
        changed = true;
        int[] indices = fi.getSelectedIndices();
        Arrays.sort(indices);
        for (int x = indices.length - 1; x >= 0; x--) {
            String n = model.getElementAt(indices[x]);
            model.remove(indices[x]);
            gl.add(n);
        }
        if (gl.size() > 0) {
            Collections.sort(gl);
            add.setEnabled(true);
        }
        parent.notifyInput();
    }

    void doAdd() {
        JFilterSelection jfs = new JFilterSelection(parent, gl);
        jfs.setVisible(true);
        String[] g = jfs.getSelection();
        if (g != null) {
            for (String aG : g) {
                gl.remove(aG);
            }
            if (gl.size() == 0) {
                add.setEnabled(false);
            }
            model.addAll(g);
            model.sort();
            changed = true;
            parent.notifyInput();
        }
    }

    void nameChange() {
        changed = true;
        parent.notifyInput();
    }
}