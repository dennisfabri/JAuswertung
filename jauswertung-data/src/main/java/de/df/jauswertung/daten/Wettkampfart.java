package de.df.jauswertung.daten;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public enum Wettkampfart {

    Bezirksmeisterschaften("Bezirksmeisterschaften"), Landesmeisterschaften("Landesmeisterschaften"),
    DeutscheMeisterschaften(
            "Deutsche Meisterschaften"),
    Freundschaftswettkampf("Freundschaftswettkampf"), SonstigerWettkampf("Sonstiger Wettkampf");

    @XStreamAsAttribute
    private final String id;

    private Wettkampfart(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}