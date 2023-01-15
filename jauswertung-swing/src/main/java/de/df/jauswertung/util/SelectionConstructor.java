package de.df.jauswertung.util;

import java.util.LinkedList;

public class SelectionConstructor {

    private LinkedList<String> text = new LinkedList<>();
    private LinkedList<Boolean> values = new LinkedList<>();

    public SelectionConstructor() {
        // Nothing to do
    }

    public void add(String t, boolean v) {
        text.addLast(t);
        values.addLast(v);
    }

    public String[] getTexts() {
        return text.toArray(new String[text.size()]);
    }

    public boolean[] getValues() {
        Boolean[] temp = values.toArray(new Boolean[values.size()]);
        boolean[] result = new boolean[temp.length];
        for (int x = 0; x < temp.length; x++) {
            result[x] = temp[x];
        }
        return result;
    }

    public void reset() {
        text.clear();
        values.clear();
    }
}