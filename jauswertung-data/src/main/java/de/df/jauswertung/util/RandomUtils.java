package de.df.jauswertung.util;

import java.util.Random;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;

public class RandomUtils {
    public static Random getRandomNumberGenerator() {
        return new RandomAdaptor(new MersenneTwister());
    }

}
