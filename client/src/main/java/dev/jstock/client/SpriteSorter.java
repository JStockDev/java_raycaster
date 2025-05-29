package dev.jstock.client;

import java.util.*;


// Sorter class to sort out sprites based on their distance from the players camera
public class SpriteSorter {

    static class SpritePair implements Comparable<SpritePair> {
        double dist;
        int order;

        SpritePair(double dist, int order) {
            this.dist = dist;
            this.order = order;
        }

        @Override
        public int compareTo(SpritePair other) {
            return Double.compare(other.dist, this.dist);
        }
    }

    public static void sortSprites(int[] order, double[] dist) {
        int amount = order.length;
        List<SpritePair> sprites = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            sprites.add(new SpritePair(dist[i], order[i]));
        }

        Collections.sort(sprites); 

        for (int i = 0; i < amount; i++) {
            dist[i] = sprites.get(i).dist;
            order[i] = sprites.get(i).order;
        }
    }
}
