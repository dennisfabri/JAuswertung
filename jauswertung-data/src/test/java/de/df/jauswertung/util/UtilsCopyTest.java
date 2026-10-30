package de.df.jauswertung.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class UtilsCopyTest {

    record Values(String name, List<int[]> values) {
    }

    private record Hidden(int number) {
    }

    record Node(List<Object> neighbours) {
    }

    @Test
    void copiesRecordWithDeepCopiesOfItsComponents() {
        Values original = new Values("A", new ArrayList<>(List.of(new int[] { 1, 2 })));

        Values copy = Utils.copy(original);

        assertNotSame(original, copy);
        assertEquals("A", copy.name());
        assertNotSame(original.values(), copy.values());
        assertNotSame(original.values().getFirst(), copy.values().getFirst());
        assertArrayEquals(new int[] { 1, 2 }, copy.values().getFirst());
    }

    @Test
    void copiesSharedRecordOnce() {
        Values shared = new Values("A", new ArrayList<>());
        List<Values> original = new ArrayList<>(List.of(shared, shared));

        List<Values> copy = Utils.copy(original);

        assertNotSame(shared, copy.get(0));
        assertSame(copy.get(0), copy.get(1));
    }

    @Test
    void keepsObjectSharedBetweenRecordAndOutsideShared() {
        int[] shared = { 1 };
        List<Object> original = new ArrayList<>(List.of(shared, new Values("A", new ArrayList<>(List.of(shared)))));

        List<Object> copy = Utils.copy(original);

        assertNotSame(shared, copy.get(0));
        assertSame(copy.get(0), ((Values) copy.get(1)).values().getFirst());
    }

    @Test
    void copiesPrivateNestedRecord() {
        Hidden copy = Utils.copy(new Hidden(7));

        assertEquals(7, copy.number());
    }

    @Test
    void reportsCycleThroughRecord() {
        Node node = new Node(new ArrayList<>());
        node.neighbours().add(node);

        RecordCycleException e = assertThrows(RecordCycleException.class, () -> Utils.copy(node));

        assertTrue(e.getMessage().contains(Node.class.getName()), e.getMessage());
    }
}
