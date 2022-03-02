package de.df.jauswertung.print.graphics;

import java.io.Serializable;

public class Point implements Serializable {
    
    private static final long serialVersionUID = 3380302078457582695L;
    
    public int x;
    public int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}