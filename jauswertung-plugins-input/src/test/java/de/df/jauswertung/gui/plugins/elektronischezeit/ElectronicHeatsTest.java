package de.df.jauswertung.gui.plugins.elektronischezeit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.lisasp.alphatiming.legacy.api.Heat;
import org.lisasp.alphatiming.legacy.api.Lane;
import org.lisasp.alphatiming.legacy.api.LaneStatus;

class ElectronicHeatsTest {

    @Test
    void convertsTheTimesToHundredths() {
        Heat heat = new Heat("4", 2, 3);
        heat.store(0, 33925, LaneStatus.RaceTimes);
        heat.store(0, 34000, LaneStatus.OfficialEnd);
        heat.store(2, 61239, LaneStatus.RaceTimes);

        Heat converted = ElectronicHeats.inHundredths(heat);

        Heat expected = new Heat("4", 2, 3);
        expected.store(0, 3392, LaneStatus.RaceTimes);
        expected.store(0, 3400, LaneStatus.OfficialEnd);
        expected.store(2, 6123, LaneStatus.RaceTimes);
        assertEquals(expected, converted);
        assertEquals(33925, heat.getLanes().get(0).getTimes()[0]);
    }

    @Test
    void keepsAnEmptyLastLaneWhenConverting() {
        Heat heat = new Heat("1", 1, 1);
        heat.getLanes().put(0, new Lane(0));

        assertEquals(heat, ElectronicHeats.inHundredths(heat));
    }

    @Test
    void returnsTheTimesOfEachLane() {
        Heat heat = new Heat("1", 1, 1);
        heat.store(0, 3392, LaneStatus.RaceTimes);
        heat.store(2, 3500, LaneStatus.RaceTimes);
        heat.store(2, 3512, LaneStatus.RaceTimes);

        assertEquals(2, ElectronicHeats.maxLane(heat));
        assertArrayEquals(new long[][] { { 3392 }, {}, { 3500, 3512 } }, ElectronicHeats.times(heat));
    }

    @Test
    void hasNoLanesWithoutTimes() {
        Heat heat = new Heat("1", 1, 1);

        assertEquals(-1, ElectronicHeats.maxLane(heat));
        assertEquals(0, ElectronicHeats.times(heat).length);
    }

    @Test
    void missesATimeWhenALaneHasNone() {
        Heat heat = new Heat("1", 1, 1);
        heat.store(0, 3392, LaneStatus.RaceTimes);
        heat.store(2, 3500, LaneStatus.RaceTimes);

        assertTrue(ElectronicHeats.hasMissingTime(heat, 3));
    }

    @Test
    void missesATimeWhenALaneIsMissing() {
        Heat heat = new Heat("1", 1, 1);
        heat.store(0, 3392, LaneStatus.RaceTimes);

        assertTrue(ElectronicHeats.hasMissingTime(heat, 2));
    }

    @Test
    void missesNoTimeWhenEveryLaneHasOne() {
        Heat heat = new Heat("1", 1, 1);
        heat.store(0, 3392, LaneStatus.RaceTimes);
        heat.store(1, 3500, LaneStatus.RaceTimes);

        assertFalse(ElectronicHeats.hasMissingTime(heat, 2));
    }
}
