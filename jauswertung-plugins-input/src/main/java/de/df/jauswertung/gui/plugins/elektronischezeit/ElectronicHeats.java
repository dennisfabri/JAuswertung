package de.df.jauswertung.gui.plugins.elektronischezeit;

import java.util.Map;

import org.lisasp.alphatiming.legacy.api.Heat;
import org.lisasp.alphatiming.legacy.api.Lane;
import org.lisasp.alphatiming.legacy.api.LaneStatus;

final class ElectronicHeats {

    private ElectronicHeats() {
    }

    static Heat inHundredths(Heat heat) {
        Heat converted = new Heat(heat.getId(), heat.getEvent(), heat.getHeat());
        for (Map.Entry<Integer, Lane> entry : heat.getLanes().entrySet()) {
            Lane lane = new Lane(entry.getKey());
            long[] times = entry.getValue().getTimes();
            LaneStatus[] stati = entry.getValue().getStati();
            for (int x = 0; x < times.length; x++) {
                lane.store(times[x] / 10, stati[x]);
            }
            converted.getLanes().put(entry.getKey(), lane);
        }
        return converted;
    }

    static int maxLane(Heat heat) {
        return heat.getLanes().keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
    }

    static long[][] times(Heat heat) {
        long[][] result = new long[maxLane(heat) + 1][0];
        heat.getLanes().forEach((index, lane) -> result[index] = lane.getTimes().clone());
        return result;
    }

    static boolean hasMissingTime(Heat heat, int lanecount) {
        for (int x = 0; x < lanecount; x++) {
            Lane lane = heat.getLanes().get(x);
            if (lane == null || lane.getTimes().length == 0) {
                return true;
            }
        }
        return false;
    }
}
