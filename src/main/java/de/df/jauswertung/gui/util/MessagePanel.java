/*
 * Created on 16.04.2006
 */
package de.df.jauswertung.gui.util;

import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import de.df.jutils.gui.layout.FormLayoutUtils;

public class MessagePanel extends JPanel {

    private static final long serialVersionUID = -5958437895949458261L;

    private JLabel            title;
    private JLabel[]          labels;
    private int[]             indizes;
    private boolean[]         shown;
    private String[]          messages;
    LinkedList<String>        text;

    private String            empty;
    private String            normal;

    public MessagePanel(String normalTitle, String emptyTitle, String[] messages) {
        this(normalTitle, emptyTitle, messages, messages.length);
    }

    public MessagePanel(String normalTitle, String emptyTitle, String[] messages, int length) {
        normal = normalTitle;
        empty = emptyTitle;

        title = new JLabel(empty);

        this.messages = messages;
        labels = new JLabel[length];
        shown = new boolean[length];
        indizes = new int[length];
        for (int x = 0; x < length; x++) {
            labels[x] = new JLabel();
            shown[x] = false;
        }
        text = new LinkedList<String>();

        FormLayout layout = new FormLayout("4dlu,10dlu,fill:default:grow,4dlu", FormLayoutUtils.createLayoutString(length + 1));
        FormLayoutUtils.setRowGroups(layout, length + 1);
        setLayout(layout);

        add(title, CC.xyw(2, 2, 2));
        for (int x = 0; x < labels.length; x++) {
            add(labels[x], CC.xy(3, 2 * x + 4));
        }
    }

    public void showMessage(int index) {
        if (!shown[index]) {
            indizes[index] = text.size();
            shown[index] = true;
            labels[indizes[index]].setText(messages[index]);
            text.addLast(messages[index]);
            title.setText(normal);
        }
    }

    public void hideMessage(int index) {
        if (shown[index]) {
            shown[index] = false;
            text.remove(messages[index]);
            if (text.isEmpty()) {
                title.setText(empty);
            }
            labels[text.size()].setText("");

            int cindex = indizes[index];
            for (int x = 0; x < indizes.length; x++) {
                if (indizes[x] > cindex) {
                    indizes[x]--;
                }
            }
            for (int x = 0; x < Math.min(text.size(), labels.length); x++) {
                labels[x].setText(text.get(x));
            }
        }
    }
}