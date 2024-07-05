package de.df.jauswertung.util;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.util.Feedback;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class Progress {

    private final Feedback fb;
    private final int length;
    private final int stepSize;

    private int counter = 0;
    private int previousPercent = 0;

    public Progress(Feedback fb, double length, int stepSize) {
        this(fb, (int) length, stepSize);
    }

    public void increase() {
        int percent = (counter * 100) / length;
        if (percent >= previousPercent + stepSize) {
            previousPercent = percent;
            double y = ((double) percent) / 100;
            fb.showFeedback(I18n.get("Percent", y));
        }

        counter++;
    }
}
