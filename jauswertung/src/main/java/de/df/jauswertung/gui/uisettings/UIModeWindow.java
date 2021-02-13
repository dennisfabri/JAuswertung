package de.df.jauswertung.gui.uisettings;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.Utils;
import de.df.jutils.gui.layout.FormLayoutUtils;
import de.df.jutils.gui.util.UIPerformanceMode;
import de.df.jutils.gui.util.WindowUtils;

public class UIModeWindow extends JFrame {

    private JRadioButton uiDefault  = new JRadioButton("Standard");
    private JRadioButton uiOpenGL   = new JRadioButton("OpenGL");
    private JRadioButton uiSoftware = new JRadioButton("Software");

    private JButton      ok         = new JButton("Ok");
    private JButton      cancel     = new JButton("Abbrechen");

    private ButtonGroup  group      = new ButtonGroup();

    public UIModeWindow() {
        prepareUI();
        addListeners();
        initWindow();
        initValues();
    }

    private void initValues() {
        UIPerformanceMode uipm = Utils.getUIPerformanceMode();
        switch (uipm) {
        default:
            uiDefault.setSelected(true);
            break;
        case OpenGL:
            uiOpenGL.setSelected(true);
            break;
        case Software:
            uiSoftware.setSelected(true);
            break;
        }
    }

    private void initWindow() {
        setTitle("Grafikbeschleunigung konfigurieren");
        setIconImages(IconManager.getTitleImages());

        pack();
        setMinimumSize(this.getSize());
        WindowUtils.checkMinimumSize(this);
        WindowUtils.center(this);
    }

    private void prepareUI() {
        group.add(uiDefault);
        group.add(uiOpenGL);
        group.add(uiSoftware);

        FormLayout layout = new FormLayout("4dlu,10dlu,4dlu,fill:default:grow," + FormLayoutUtils.createLayoutString(2),
                "0dlu,fill:default," + FormLayoutUtils.createLayoutString(6) + ",fill:default:grow,4dlu");
        setLayout(layout);
        layout.setColumnGroups(new int[][] { { 6, 8 } });

        JComponent header = createText(
                "Java kann verschiedene Schnittstellen zur Anzeige der Benutzeroberfläche nutzen. Auf einzelnen Systemen kann die Standardeinstellung zu Darstellugsfehlern führen.");
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        header.setOpaque(true);
        header.setBackground(Color.WHITE);
        add(header, CC.xyw(1, 2, 9));
        add(createText("Java nutzt die favorisierte Schnittstelle des Betriebsystems.", true), CC.xyw(2, 6, 7));
        add(createText("Java nutzt die Hardwarebeschleunigung einer Grafikkarte über OpenGL", true), CC.xyw(2, 10, 7));
        add(createText("Java nutzt die CPU zur Berechnung der Darstellung (sicherste Methode). Diese Option sollte bei Darstellungsfehlern gewählt werden.",
                true), CC.xyw(2, 14, 7));

        add(uiDefault, CC.xyw(2, 4, 7));
        add(uiOpenGL, CC.xyw(2, 8, 7));
        add(uiSoftware, CC.xyw(2, 12, 7));

        add(ok, CC.xy(6, 16, "fill,bottom"));
        add(cancel, CC.xy(8, 16, "fill,bottom"));
    }

    private JComponent createText(String text) {
        return createText(text, false);
    }

    private JComponent createText(String text, boolean indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><p width=\"300\">");
        sb.append(text);
        sb.append("</p></body></html>");

        JLabel lbl = new JLabel(sb.toString());
        if (indent) {
            lbl.setBorder(new EmptyBorder(0, 10, 0, 0));
        }
        return lbl;
    }

    private void addListeners() {
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                doExit();
            }
        });

        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doSave();
            }
        });

        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doExit();
            }
        });
    }

    private void doSave() {
        UIPerformanceMode mode = UIPerformanceMode.Default;
        if (uiOpenGL.isSelected()) {
            mode = UIPerformanceMode.OpenGL;
        } else if (uiSoftware.isSelected()) {
            mode = UIPerformanceMode.Software;
        }
        Utils.setUIPerformanceMode(mode);
        doExit();
    }

    private void doExit() {
        System.exit(0);
    }
}
