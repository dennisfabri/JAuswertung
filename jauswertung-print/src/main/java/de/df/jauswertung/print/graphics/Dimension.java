package de.df.jauswertung.print.graphics;

import java.io.Serializable;

public class Dimension implements Serializable {

    private static final long serialVersionUID = 8771743770783648534L;

    public int width;
    public int height;

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}