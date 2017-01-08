package com.github.bgloeckle.jigsaw.assembly;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.util.Pair;

/**
 * An {@link Image} that is based on a specific positioning of various {@link Tile}s which in turn base on a different
 * {@link Image}.
 *
 * @author Bastian Gloeckle
 */
public class Assembly implements Image {
    private static final long serialVersionUID = 1L;

    private Image origImg;

    private NavigableMap<Integer, NavigableMap<Integer, Tile>> tiles = new TreeMap<>();

    /* package */ Assembly(Image origImage, NavigableMap<Integer, NavigableMap<Integer, Tile>> tiles) {
        this.origImg = origImage;
        this.tiles = tiles;
    }

    private Pair<Tile, Pair<Integer, Integer>> findTile(int x, int y) {
        Entry<Integer, NavigableMap<Integer, Tile>> m = tiles.floorEntry(x);
        if (m == null) {
            return null;
        }
        int destX = m.getKey();
        Entry<Integer, Tile> e = m.getValue().floorEntry(y);
        if (e == null) {
            return null;
        }
        int destY = e.getKey();
        return new Pair<>(e.getValue(), new Pair<>(destX, destY));
    }

    @Override
    public int getColor(int x, int y) {
        Pair<Tile, Pair<Integer, Integer>> tileDestPair = findTile(x, y);

        return tileDestPair.getLeft().getColor(x - tileDestPair.getRight().getLeft(),
                        y - tileDestPair.getRight().getRight());
    }

    @Override
    public void setColor(int x, int y, int newColor) {
        Pair<Tile, Pair<Integer, Integer>> tileDestPair = findTile(x, y);

        tileDestPair.getLeft().setColor(x - tileDestPair.getRight().getLeft(), y - tileDestPair.getRight().getRight(),
                        newColor);
    }

    @Override
    public double getDirection(int x, int y) {
        Pair<Tile, Pair<Integer, Integer>> tileDestPair = findTile(x, y);

        return tileDestPair.getLeft().getDirection(x - tileDestPair.getRight().getLeft(),
                        y - tileDestPair.getRight().getRight());
    }

    @Override
    public void setDirection(int x, int y, double direction) {
        Pair<Tile, Pair<Integer, Integer>> tileDestPair = findTile(x, y);

        tileDestPair.getLeft().setDirection(x - tileDestPair.getRight().getLeft(),
                        y - tileDestPair.getRight().getRight(), direction);

    }

    @Override
    public int getWidth() {
        return origImg.getWidth();
    }

    @Override
    public int getHeight() {
        return origImg.getHeight();
    }

    @Override
    public Image copy() {
        Image newOrigImage = origImg.copy();
        NavigableMap<Integer, NavigableMap<Integer, Tile>> newTiles = new TreeMap<>();
        for (Entry<Integer, NavigableMap<Integer, Tile>> e1 : tiles.entrySet()) {
            NavigableMap<Integer, Tile> t = new TreeMap<>();

            newTiles.put(e1.getKey(), t);
            for (Entry<Integer, Tile> e2 : e1.getValue().entrySet()) {
                t.put(e2.getKey(), new Tile(newOrigImage, e2.getValue()));
            }
        }

        return new Assembly(newOrigImage, newTiles);
    }

}
