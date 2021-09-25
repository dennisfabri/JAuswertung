/*
 * Created on 18.03.2007
 */
package de.df.jauswertung.gui.plugins.urkunden;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.fontchooser.JFontChooser;

import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jutils.gui.util.UIStateUtils;
import de.df.jutils.gui.util.WindowUtils;

class JFontDialog extends JDialog {

    private static final long serialVersionUID = -9025893350153624458L;

    private JFontChooser fonts = new JFontChooser();
    private boolean accepted = false;

    public JFontDialog(JFrame parent) {
        super(parent, I18n.get("Font"), true);

        JButton ok = new JButton(I18n.get("Ok"), IconManager.getSmallIcon("ok"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });
        JButton cancel = new JButton(I18n.get("Cancel"), IconManager.getSmallIcon("cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        FormLayout layout = new FormLayout("4dlu,0px:grow,fill:default,4dlu,fill:default,4dlu",
                "4dlu,fill:default:grow,4dlu,fill:default,4dlu");
        setLayout(layout);

        add(fonts, CC.xyw(2, 2, 4, "fill,fill"));
        add(ok, CC.xy(3, 4));
        add(cancel, CC.xy(5, 4));

        pack();
        UIStateUtils.uistatemanage(parent, this, "FontDialog");
    }

    @Override
    public void setVisible(boolean v) {
        if (v) {
            accepted = false;
        }
        super.setVisible(v);
    }

    public void setSelectedFont(Font f) {
        if (f == null) {
            return;
        }
        fonts.setSelectedFont(f);
    }

    public Font getSelectedFont() {
        if (!accepted) {
            return null;
        }
        return fonts.getSelectedFont();
    }

    /**
     * 
     */
    void doOk() {
        accepted = true;
        setVisible(false);
    }
}
