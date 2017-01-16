/**
 * jigsaw: Solve image jigsaws.
 *
 * Copyright (C) 2016, 2017 Bastian Gloeckle
 *
 * This file is part of jigsaw.
 *
 * diqube is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    public Assembly(Image origImage, NavigableMap<Integer, NavigableMap<Integer, Tile>> tiles) {
        this.origImg = origImage;
        this.tiles = tiles;
    }

    public Assembly(Image origImage, Assembly other) {
        this.origImg = origImage;
        this.tiles = new TreeMap<>();
        for (Entry<Integer, NavigableMap<Integer, Tile>> e : other.tiles.entrySet()) {
            tiles.put(e.getKey(), new TreeMap<>());
            for (Entry<Integer, Tile> e2 : e.getValue().entrySet()) {
                tiles.get(e.getKey()).put(e2.getKey(), new Tile(origImage, e2.getValue()));
            }
        }
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
