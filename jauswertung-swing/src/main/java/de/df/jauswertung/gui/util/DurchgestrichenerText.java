package de.df.jauswertung.gui.util;

import de.df.jutils.gui.renderer.TableRenderDataProvider;

public record DurchgestrichenerText(Object text) implements TableRenderDataProvider {

    public static Object wenn(boolean gestrichen, Object text) {
        return gestrichen ? new DurchgestrichenerText(text) : text;
    }

    @Override
    public Object getTableRenderData() {
        return text;
    }

    @Override
    public String toString() {
        return String.valueOf(text);
    }
}
