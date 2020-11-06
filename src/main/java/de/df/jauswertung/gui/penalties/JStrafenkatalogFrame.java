package de.df.jauswertung.gui.penalties;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jauswertung.daten.regelwerk.Strafen;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

/**
 * @author Dennis Mueller
 * @since 7. August 2001, 22:01
 */
class JStrafenkatalogFrame extends JFrame {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long    serialVersionUID = 3257285846494558261L;

    private Window               parent           = null;
    private JStrafenkatalogPanel panel            = null;
    private boolean              cancelled        = false;

    public JStrafenkatalogFrame(JFrame p, Strafen s) {
        this(p, null, s);
    }

    public JStrafenkatalogFrame(JFrame p, Strafe str, Strafen s) {
        super(I18n.get("PenaltyCatalog"));
        init(p, str, s);
    }

    // ***********************************************************************

    private void init(JFrame p, Strafe str, Strafen s) {
        setIconImages(IconManager.getTitleImages());

        parent = p;

        panel = new JStrafenkatalogPanel(str, s);
        setContentPane(panel);

        initListeners();

        WindowUtils.setSize(this, 800, 600);
        WindowUtils.center(this, parent);
        UIStateUtils.uistatemanage(this, "JStrafenkatalogFrame");
    }

    @Override
    public void setVisible(boolean visible) {
        if (parent != null) {
            parent.setEnabled(!visible);
        } else {
            if (!visible) {
                EDTUtils.niceExit();
            }
        }
        super.setVisible(visible);
    }

    public Strafe getStrafe() {
        if (cancelled) {
            return null;
        }
        return panel.getStrafe();
    }

    private void addActions() {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = 3257572818995525944L;

            @Override
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    private void initListeners() {
        addActions();
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                doClose();
            }
        });
    }

    private void doClose() {
        cancelled = true;
        setVisible(false);
    }
}