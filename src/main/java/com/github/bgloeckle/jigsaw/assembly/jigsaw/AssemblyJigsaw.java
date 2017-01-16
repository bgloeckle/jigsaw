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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.assembly.Assembly;
import com.github.bgloeckle.jigsaw.assembly.Tile;
import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.util.EdgeDirection;
import com.github.bgloeckle.jigsaw.util.Pair;

/**
 * Finds the most-likely-well {@link Assembly}s of an input edge {@link Image} with given horizontal and vertical cuts.
 * 
 * <p>
 * This internally works by first inspecting the cuts, building {@link TileInfo}s and then forwarding that information
 * to a {@link JigsawSolverStrategy}.
 *
 * @author Bastian Gloeckle
 */
public class AssemblyJigsaw {
    private static final Logger logger = LoggerFactory.getLogger(AssemblyJigsaw.class);

    private static final double TILE_BORDER_MATCH_COUNT_DIFF_PERCENT = .2;

    private static final JigsawSolverStrategy SOLVER_STRATEGY = new GreedyJigsawSolverStrategy();

    private Image origImg;
    private int cutEveryX;
    private int cutEveryY;
    private List<Tile> tiles;

    private int tileCountWidth;
    private int tileCountHeight;

    public AssemblyJigsaw(Image origImg, int cutEveryX, int cutEveryY) {
        this.origImg = origImg;
        this.cutEveryX = cutEveryX;
        this.cutEveryY = cutEveryY;

        tileCountWidth = 0;
        tileCountHeight = 0;
        tiles = new ArrayList<>();
        for (int x = 0; x < origImg.getWidth(); x += cutEveryX) {
            for (int y = 0; y < origImg.getHeight(); y += cutEveryY) {
                Tile t = new Tile(origImg, x, y, Math.min(x + cutEveryX, origImg.getWidth()) - x,
                                Math.min(y + cutEveryY, origImg.getHeight()) - y);
                tiles.add(t);
                tileCountHeight++;
            }
            tileCountWidth++;
        }
        tileCountHeight /= tileCountWidth;
    }

    public Set<Assembly> findBestAssemblies(double bestStitchPercent) {
        Map<Integer, List<TileInfo>> topBorderEdgeCountTiles = new HashMap<>();
        Map<Integer, List<TileInfo>> leftBorderEdgeCountTiles = new HashMap<>();
        Map<Integer, List<TileInfo>> rightBorderEdgeCountTiles = new HashMap<>();
        Map<Integer, List<TileInfo>> bottomBorderEdgeCountTiles = new HashMap<>();

        logger.debug("Identifying the edges on the borders of tiles cut every ({}/{} => tileWidth={}, tileHeight={})",
                        cutEveryX, cutEveryY, tileCountWidth, tileCountHeight);
        List<TileInfo> tileInfos = tiles.stream().map(t -> new TileInfo(t)).collect(Collectors.toList());
        for (TileInfo t : tileInfos) {
            // inspect top border
            fillTileBorderBitSet(t, it(t.getTile(), p -> new Pair<>(p.getLeft() + 1, p.getRight()), new Pair<>(0, 0)),
                            EdgeDirection.EAST_WEST, tile -> tile.getTopBorder(), p -> p.getLeft());
            // inspect bottom border
            fillTileBorderBitSet(t,
                            it(t.getTile(), p -> new Pair<>(p.getLeft() + 1, p.getRight()),
                                            new Pair<>(0, t.getTile().getHeight() - 1)),
                            EdgeDirection.EAST_WEST, tile -> tile.getBottomBorder(), p -> p.getLeft());
            // inspect left border
            fillTileBorderBitSet(t, it(t.getTile(), p -> new Pair<>(p.getLeft(), p.getRight() + 1), new Pair<>(0, 0)),
                            EdgeDirection.NORTH_SOUTH, tile -> tile.getLeftBorder(), p -> p.getRight());
            // inspect right border
            fillTileBorderBitSet(t,
                            it(t.getTile(), p -> new Pair<>(p.getLeft(), p.getRight() + 1),
                                            new Pair<>(0, t.getTile().getWidth() - 1)),
                            EdgeDirection.NORTH_SOUTH, tile -> tile.getRightBorder(), p -> p.getRight());

            // for quick access, add tile to the maps
            topBorderEdgeCountTiles.computeIfAbsent(t.getTopBorder().cardinality(), k -> new ArrayList<>()).add(t);
            leftBorderEdgeCountTiles.computeIfAbsent(t.getLeftBorder().cardinality(), k -> new ArrayList<>()).add(t);
            rightBorderEdgeCountTiles.computeIfAbsent(t.getRightBorder().cardinality(), k -> new ArrayList<>()).add(t);
            bottomBorderEdgeCountTiles.computeIfAbsent(t.getBottomBorder().cardinality(), k -> new ArrayList<>())
                            .add(t);
        }

        int verticalCutsMaxDiff = (int) Math.round(cutEveryY * TILE_BORDER_MATCH_COUNT_DIFF_PERCENT);
        int horizontalCutsMaxDiff = (int) Math.round(cutEveryX * TILE_BORDER_MATCH_COUNT_DIFF_PERCENT);
        logger.debug("Building a graph by accepting neighbours with a max edge count diff of {}/{}",
                        verticalCutsMaxDiff, horizontalCutsMaxDiff);
        for (TileInfo t : tileInfos) {
            populateNextSet(t, t.getTopBorder(), bottomBorderEdgeCountTiles, ti -> ti.getBottomBorder(),
                            p -> {
                                t.getNextTop().add(p);
                                p.getLeft().getNextBottom().add(new Pair<>(t, p.getRight()));
                            }, horizontalCutsMaxDiff);
            populateNextSet(t, t.getLeftBorder(), rightBorderEdgeCountTiles, ti -> ti.getRightBorder(),
                            p -> {
                                t.getNextLeft().add(p);
                                p.getLeft().getNextRight().add(new Pair<>(t, p.getRight()));
                            }, verticalCutsMaxDiff);
            // populateNextSet(t, t.getBottomBorder(), topBorderEdgeCountTiles, ti -> ti.getTopBorder(),
            // p -> t.getNextBottom().add(p), horizontalCutsMaxDiff);
            // populateNextSet(t, t.getRightBorder(), leftBorderEdgeCountTiles, ti -> ti.getLeftBorder(),
            // p -> t.getNextRight().add(p), verticalCutsMaxDiff);
        }

        if (logger.isDebugEnabled()) {
            long edgeCount = 0;
            for (TileInfo t : tileInfos) {
                edgeCount += t.getNextRight().size();
                edgeCount += t.getNextBottom().size();
            }
            logger.debug("Found a graph with {} vertices and {} edges", tileInfos.size(), edgeCount);
        }

        return SOLVER_STRATEGY.solve(origImg, tileInfos, tileCountWidth, tileCountHeight);
    }

    private void fillTileBorderBitSet(TileInfo t, Iterator<Pair<Integer, Integer>> posIt, EdgeDirection ignoreDirection,
                    Function<TileInfo, BitSet> bitSetSelector, Function<Pair<Integer, Integer>, Integer> bitSelector) {
        while (posIt.hasNext()) {
            Pair<Integer, Integer> curPos = posIt.next();

            if (t.getTile().getColor(curPos.getLeft(), curPos.getRight()) != 0 && !ignoreDirection.equals(EdgeDirection
                            .fromGradientRadian(t.getTile().getDirection(curPos.getLeft(), curPos.getRight())))) {
                int bit = bitSelector.apply(curPos);
                bitSetSelector.apply(t).set(bit);
            }
        }
    }

    private void populateNextSet(TileInfo t, BitSet tBitSet, Map<Integer, List<TileInfo>> borderCountMap,
                    Function<TileInfo, BitSet> otherBitSetProvider, Consumer<Pair<TileInfo, Double>> resConsumer,
                    int maxEdgeCountDiff) {
        int cardinality = tBitSet.cardinality();
        // if (cardinality == 0) {
        // return;
        // }
        Set<TileInfo> visited = new HashSet<>(Arrays.asList(t));

        for (int absDelta = 0; absDelta <= maxEdgeCountDiff; absDelta++) {
            Set<Integer> deltasWorkedOn = new HashSet<>();
            for (int delta = absDelta; !deltasWorkedOn.contains(delta); delta = delta * -1) {
                try {
                    int idx = cardinality + delta;
                    if (!borderCountMap.containsKey(idx)) {
                        continue;
                    }

                    for (TileInfo interestingTile : borderCountMap.get(idx)) {
                        if (visited.contains(interestingTile)) {
                            continue;
                        }
                        visited.add(interestingTile);
                        BitSet workBitSet = BitSet.valueOf(otherBitSetProvider.apply(interestingTile).toLongArray());
                        workBitSet.xor(tBitSet);

                        int c = workBitSet.cardinality();
                        if (c <= maxEdgeCountDiff) {
                            Pair<TileInfo, Double> r = new Pair<>(interestingTile,
                                            (double) (maxEdgeCountDiff - Math.abs(cardinality - delta)));
                            // logger.trace("Found neighbour of {}: {}", t, r);
                            resConsumer.accept(r);
                        } else {
                            // System.out.println();
                        }
                    }
                } finally {
                    deltasWorkedOn.add(delta);
                }
            }
        }
    }



    /**
     * @return An {@link Iterator} that iterates over valid x/y values in the given tile, computing a new pair of values
     *         with the given function and starting by the given start location.
     */
    private Iterator<Pair<Integer, Integer>> it(Tile tile,
                    Function<Pair<Integer, Integer>, Pair<Integer, Integer>> providerFn, Pair<Integer, Integer> start) {
        return new Iterator<Pair<Integer, Integer>>() {
            private Pair<Integer, Integer> next = start;

            @Override
            public boolean hasNext() {
                return next.getLeft() >= 0 && next.getLeft() < tile.getWidth() && next.getRight() >= 0
                                && next.getRight() < tile.getHeight();
            }

            @Override
            public Pair<Integer, Integer> next() {
                Pair<Integer, Integer> cur = next;
                next = providerFn.apply(cur);
                return cur;
            }
        };
    }

}
