package de.df.jauswertung.gui.util;

import de.df.jutils.gui.util.ISimpleCallback;
import de.df.jutils.plugin.JPFrame;

public class PrintStatusCallBack implements ISimpleCallback<Integer> {

    private final JPFrame jpframe;

    public PrintStatusCallBack(JPFrame frame) {
        jpframe = frame;
    }

    @Override
    public void callback(Integer t) {
        if (t == null) {
            jpframe.setStatusBarText(null);
            return;
        }
        int page = t + 1;
        if (page <= 0) {
            jpframe.setStatusBarText(null);
        } else {
            jpframe.setStatusBarText(I18n.get("PrintingPageNr", page));
        }
    }
}