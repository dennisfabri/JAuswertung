/*
 * Created on 19.02.2006
 */
package de.df.jauswertung.gui.plugins.check;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.HLWStates;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.editor.FEditorPlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.io.util.ZWUtils;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.JDoubleField;
import de.df.jutils.gui.JGlassPanel;
import de.df.jutils.gui.JTransparentButton;
import de.df.jutils.gui.border.BorderUtils;
import de.df.jutils.gui.layout.CenterLayout;
import de.df.jutils.gui.layout.SimpleTableBuilder;
import de.df.jutils.util.StringTools;

public class JZWInputPanel extends JGlassPanel<JPanel> {

    private static final long  serialVersionUID = 8023494074221318513L;

    private CorePlugin         core;
    private FEditorPlugin      editor;
    private JMissingInputFrame parent;

    private ASchwimmer[]       swimmers;
    private int[]              indizes;
    JDoubleField[]             points;
    private JButton[]          edit;

    private JPanel             panel;

    private boolean            changed          = false;

    public JZWInputPanel(JMissingInputFrame parent, CorePlugin core, FEditorPlugin editor) {
        super(new JPanel());
        panel = getComponent();
        this.core = core;
        this.parent = parent;
        this.editor = editor;
        init();
    }

    public boolean hasChanged() {
        return changed;
    }

    public void unsetChanged() {
        changed = false;
    }

    private void init() {
        JLabel info = new JLabel(I18n.get("NoMissingZWInfo"));
        info.setBorder(BorderUtils.createLabeledBorder(I18n.get("Information"), true));
        getGlassPanel().setLayout(new CenterLayout());
        getGlassPanel().add(info);
        swimmers = new ASchwimmer[0];
        indizes = new int[0];
        points = new JDoubleField[0];
        edit = new JButton[0];

        setEnabled(false);
    }

    void updateData() {
        AWettkampf<ASchwimmer> wk = core.getWettkampf();
        if (!wk.hasSchwimmer()) {
            swimmers = new ASchwimmer[0];
            return;
        }
        LinkedList<SwimmerIndex> nohlw = new LinkedList<>();

        LinkedList<ASchwimmer> swimmerlist = wk.getSchwimmer();
        ListIterator<ASchwimmer> li = swimmerlist.listIterator();
        while (li.hasNext()) {
            ASchwimmer s = li.next();
            if (s.getAK().hasHLW()) {
                for (int x = 0; x < s.getMaximaleHLW(); x++) {
                    if (!s.hasHLWSet(x)) {
                        nohlw.addLast(new SwimmerIndex(s, x));
                    } else {
                        long punkte = Math.round(s.getHLWPunkte(x) * 100);
                        if (punkte % 20000 != 0) {
                            nohlw.addLast(new SwimmerIndex(s, x));
                        }
                    }
                }
            }
        }

        updateSwimmers(nohlw);
    }

    private void updateSwimmers(LinkedList<SwimmerIndex> nohlw) {
        sortSwimmers(nohlw);

        swimmers = new ASchwimmer[nohlw.size()];
        indizes = new int[nohlw.size()];
        int x = 0;
        for (SwimmerIndex t : nohlw) {
            swimmers[x] = t.getSwimmer();
            indizes[x] = t.getIndex();
            x++;
        }
    }

    private static void sortSwimmers(LinkedList<SwimmerIndex> swimmers) {
        Collections.sort(swimmers, new Comparator<SwimmerIndex>() {
            @Override
            public int compare(SwimmerIndex t1, SwimmerIndex t2) {
                ASchwimmer o1 = t1.getSwimmer();
                ASchwimmer o2 = t2.getSwimmer();
                if (o1.getAKNummer() != o2.getAKNummer()) {
                    return o1.getAKNummer() - o2.getAKNummer();
                }
                if (o1.isMaennlich() != o2.isMaennlich()) {
                    return (o1.isMaennlich() ? 1 : -1);
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    void updateGUI() {
        setEnabled(swimmers.length > 0);

        panel.removeAll();
        SimpleTableBuilder dfb = new SimpleTableBuilder(panel, new boolean[] { false, true, true, true, true, false }, false);
        dfb.add(new JLabel(I18n.get("StartnumberShort")), "center,center");
        dfb.add(new JLabel(I18n.get("Name")), "center,center");
        dfb.add(new JLabel(I18n.get("Organisation")), "center,center");
        dfb.add(new JLabel(I18n.get("AgeGroup")), "center,center");
        dfb.add(new JLabel(I18n.get("Points")), "center,center");
        dfb.add(new JLabel());

        points = new JDoubleField[swimmers.length];
        edit = new JButton[swimmers.length];
        for (int x = 0; x < swimmers.length; x++) {
            ASchwimmer s = swimmers[x];

            points[x] = new JDoubleField();
            points[x].setAutoSelectAll(true);
            points[x].setSpecialStrings("n", I18n.get("DidNotStartShort"));
            points[x].setEnabled(s.getAK().hasHLW());
            if (s.getAK().hasHLW()) {
                switch (s.getHLWState(indizes[x])) {
                case NICHT_ANGETRETEN:
                    points[x].setText(I18n.get("DidNotStartShort"));
                    break;
                case DISQALIFIKATION:
                    points[x].setText(I18n.get("DisqualificationShort"));
                    break;
                case ENTERED:
                    points[x].setDouble(s.getHLWPunkte(indizes[x]));
                    break;
                case NOT_ENTERED:
                    // Leave field empty
                    break;
                }
                points[x].getDocument().addDocumentListener(new InputListener(x));
                points[x].addKeyListener(new CursorListener(x));
            }
            edit[x] = new JTransparentButton(IconManager.getSmallIcon("edit"));
            edit[x].addActionListener(new Editor(x));

            dfb.add(new JLabel(StartnumberFormatManager.format(s)));
            StringBuilder sb = new StringBuilder();
            sb.append(s.getName());
            if (s.getMaximaleHLW() > 1) {
                sb.append(" (");
                sb.append(StringTools.ABC[indizes[x]]);
                sb.append(")");
            }
            dfb.add(new JLabel(sb.toString()));
            dfb.add(new JLabel(s.getGliederung()));
            dfb.add(new JLabel(I18n.getAgeGroupAsString(s)));
            dfb.add(points[x]);
            dfb.add(edit[x]);
        }

        dfb.getPanel();
    }

    void changeHLW(int index) {
        changed = true;
        if (points[index].getText().length() == 0) {
            swimmers[index].setHLWState(indizes[index], HLWStates.NOT_ENTERED);
        } else {
            if (points[index].isSpecialString()) {
                swimmers[index].setHLWState(indizes[index], ZWUtils.getHLWState(swimmers[index].getWettkampf(), points[index].getText()));
            } else {
                swimmers[index].setHLWPunkte(indizes[index], points[index].getDouble());
            }
        }
    }

    void check(ASchwimmer s) {
        for (int x = 0; x < s.getMaximaleHLW(); x++) {
            check(s, x);
        }
    }
    
    private static class SwimmerIndex {
        
        private final ASchwimmer swimmer;
        private final int index;

        public SwimmerIndex(ASchwimmer swimmer, int index) {
            super();
            this.swimmer = swimmer;
            this.index = index;
        }

        public ASchwimmer getSwimmer() {
            return swimmer;
        }
        public int getIndex() {
            return index;
        }
    }

    private void check(ASchwimmer s, int pos) {
        boolean remove = false;
        boolean add = false;
        if (s.getAK().hasHLW()) {
            if (!s.hasHLWSet()) {
                add = true;
            }
        } else {
            remove = true;
        }

        if (add) {
            LinkedList<SwimmerIndex> temp = new LinkedList<>();
            for (int x = 0; x < swimmers.length; x++) {
                if (swimmers[x].equals(s) && (pos == indizes[x])) {
                    return;
                }
                temp.addLast(new SwimmerIndex(swimmers[x], indizes[x]));
            }
            temp.addLast(new SwimmerIndex(s, pos));
            updateSwimmers(temp);
        } else if (remove) {
            boolean found = false;
            LinkedList<SwimmerIndex> temp = new LinkedList<SwimmerIndex>();
            for (int x = 0; x < swimmers.length; x++) {
                if (swimmers[x].equals(s) && (indizes[x] == pos)) {
                    found = true;
                } else {
                    temp.addLast(new SwimmerIndex(swimmers[x], indizes[x]));
                }
            }
            if (found) {
                updateSwimmers(temp);
            }
        }
    }

    void edit(int index) {
        editor.editSchwimmer(parent, swimmers[index], false);
        parent.check(swimmers[index]);
        parent.updateGUI();
    }

    private class InputListener implements DocumentListener {

        private final int index;

        public InputListener(int x) {
            index = x;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            changeHLW(index);
        }
    }

    private class Editor implements ActionListener {

        private final int index;

        public Editor(int x) {
            index = x;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            edit(index);
        }
    }

    private class CursorListener implements KeyListener {

        private final int index;

        public CursorListener(int x) {
            index = x;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if ((index > 0) && (e.getKeyCode() == KeyEvent.VK_UP)) {
                points[index - 1].requestFocus();
            }
            if ((index + 1 < points.length) && ((e.getKeyCode() == KeyEvent.VK_DOWN) || (e.getKeyCode() == KeyEvent.VK_ENTER))) {
                points[index + 1].requestFocus();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // keyPressed(e);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            keyPressed(e);
        }
    }
}