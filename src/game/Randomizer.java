package game;

import java.util.Random;

public class Randomizer {

    private static Random r;

    public static void setRandom(long seed) {
        r = new Random(seed);
    }

    public static int next() {
        return r.nextInt();
    }

    public static int next(int bound) {
        return r.nextInt(bound);
    }

    public static Random getRandom() {
        return r;
    }
}