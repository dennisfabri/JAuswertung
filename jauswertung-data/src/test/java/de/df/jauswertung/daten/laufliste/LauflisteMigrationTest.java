package de.df.jauswertung.daten.laufliste;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

class LauflisteMigrationTest {

    @Test
    void migrator3RenamesEinteilungAndBlockEinteilung() throws Exception {
        Element laufliste = DocumentHelper.parseText("""
                <laufliste>
                  <verteilung2>
                    <LauflisteEinteilung><first>1</first><second>true</second><third>2</third></LauflisteEinteilung>
                  </verteilung2>
                  <blocks>
                    <LauflisteBlockEinteilung><first>1</first><second>false</second></LauflisteBlockEinteilung>
                  </blocks>
                </laufliste>
                """).getRootElement();

        Laufliste.migrator3(laufliste);

        assertEquals(List.of("startgroup", "male", "discipline"), names(laufliste.element("verteilung2")));
        assertEquals(List.of("startgroup", "male"), names(laufliste.element("blocks")));
    }

    @Test
    void migrator3AcceptsMissingLists() throws Exception {
        Element laufliste = DocumentHelper.parseText("<laufliste/>").getRootElement();

        Laufliste.migrator3(laufliste);
    }

    private static List<String> names(Element list) {
        return list.elements().getFirst().elements().stream().map(Element::getName).toList();
    }
}
