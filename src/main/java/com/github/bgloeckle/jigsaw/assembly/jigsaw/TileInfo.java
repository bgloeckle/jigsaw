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
package com.github.bgloeckle.jigsaw.assembly.jigsaw;

import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.github.bgloeckle.jigsaw.assembly.Tile;
import com.github.bgloeckle.jigsaw.colorcoding.Vertex;
import com.github.bgloeckle.jigsaw.util.Pair;

class TileInfo implements Vertex {
    private static final Comparator<Pair<?, Double>> COMPARATOR_LOWEST_FRONT = (l, r) -> l.getRight()
                    .compareTo(r.getRight());

    private Tile tile;

    private BitSet topBorder;
    private BitSet bottomBorder;
    private BitSet leftBorder;
    private BitSet rightBorder;

    private NavigableSet<Pair<TileInfo, Double>> nextTop = new TreeSet<>(COMPARATOR_LOWEST_FRONT);
    private NavigableSet<Pair<TileInfo, Double>> nextLeft = new TreeSet<>(COMPARATOR_LOWEST_FRONT);
    private NavigableSet<Pair<TileInfo, Double>> nextBottom = new TreeSet<>(COMPARATOR_LOWEST_FRONT);
    private NavigableSet<Pair<TileInfo, Double>> nextRight = new TreeSet<>(COMPARATOR_LOWEST_FRONT);

    // private int maxWidthRightStartingThisTile = -1;

    TileInfo(Tile tile) {
        this.tile = tile;
        topBorder = new BitSet(tile.getWidth());
        bottomBorder = new BitSet(tile.getWidth());
        leftBorder = new BitSet(tile.getHeight());
        rightBorder = new BitSet(tile.getHeight());
    }

    public Tile getTile() {
        return tile;
    }

    public BitSet getTopBorder() {
        return topBorder;
    }

    public BitSet getBottomBorder() {
        return bottomBorder;
    }

    public BitSet getLeftBorder() {
        return leftBorder;
    }

    public BitSet getRightBorder() {
        return rightBorder;
    }

    public NavigableSet<Pair<TileInfo, Double>> getNextLeft() {
        return nextLeft;
    }

    public NavigableSet<Pair<TileInfo, Double>> getNextBottom() {
        return nextBottom;
    }

    public NavigableSet<Pair<TileInfo, Double>> getNextRight() {
        return nextRight;
    }

    public NavigableSet<Pair<TileInfo, Double>> getNextTop() {
        return nextTop;
    }

    // private int calculateMaxWidth(TileInfo pos, Set<TileInfo> visited) {
    // visited.add(pos);
    //
    // int max = 0;
    // for (Pair<TileInfo, Double> p : pos.getNextRight()) {
    // if (visited.contains(p.getLeft())) {
    // continue;
    // }
    // int r = calculateMaxWidth(p.getLeft(), visited);
    // if (r > max) {
    // max = r;
    // }
    // }
    //
    // visited.remove(pos);
    //
    // return max + 1;
    // }
    //
    // public int calculateMaxWidth() {
    // if (maxWidthRightStartingThisTile != -1) {
    // return maxWidthRightStartingThisTile;
    // }
    //
    // maxWidthRightStartingThisTile = calculateMaxWidth(this, new HashSet<>());
    //
    // return maxWidthRightStartingThisTile;
    // }

    @Override
    public String toString() {
        return "TileInfo [tile=" + tile + "]";
    }

    @Override
    public Collection<Vertex> getNext() {
        return getNextRight().stream().map(p -> p.getLeft()).collect(Collectors.toList());
    }

}