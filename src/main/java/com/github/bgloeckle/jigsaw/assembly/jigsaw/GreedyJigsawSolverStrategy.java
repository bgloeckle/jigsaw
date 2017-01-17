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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.assembly.Assembly;
import com.github.bgloeckle.jigsaw.assembly.Tile;
import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.util.Pair;
import com.google.common.collect.Sets;

/**
 * Simple {@link JigsawSolverStrategy} which simply takes the best fitting edges and uses them until the whole image is
 * stitched.
 *
 * @author Bastian Gloeckle
 */
public class GreedyJigsawSolverStrategy implements JigsawSolverStrategy {
    private static final Logger logger = LoggerFactory.getLogger(GreedyJigsawSolverStrategy.class);

    private enum Orientation {
        NORTH_SOUTH, EAST_WEST
    }

    private static final Comparator<Pair<Double, Pair<Pair<TileInfo, TileInfo>, Orientation>>> EDGE_COMPARATOR = new Comparator<Pair<Double, Pair<Pair<TileInfo, TileInfo>, Orientation>>>() {
        @Override
        public int compare(Pair<Double, Pair<Pair<TileInfo, TileInfo>, Orientation>> o1,
                        Pair<Double, Pair<Pair<TileInfo, TileInfo>, Orientation>> o2) {
            if (o1 == o2) {
                return 0;
            }

            int res = o1.getLeft().compareTo(o2.getLeft());
            if (res == 0) {
                NavigableSet<Integer> o1set = new TreeSet<>();
                o1set.add(System.identityHashCode(o1.getRight().getLeft().getLeft()));
                o1set.add(System.identityHashCode(o1.getRight().getLeft().getRight()));

                NavigableSet<Integer> o2set = new TreeSet<>();
                o2set.add(System.identityHashCode(o2.getRight().getLeft().getLeft()));
                o2set.add(System.identityHashCode(o2.getRight().getLeft().getRight()));

                if (o1set.first().equals(o2set.first()) && o1set.last().equals(o2set.last())) {
                    res = 0;
                } else if (o1set.first().equals(o2set.first())) {
                    res = o1set.last().compareTo(o2set.last());
                } else {
                    res = o1set.first().compareTo(o2set.first());
                }
            }
            return res;
        }
    };

    @Override
    public Set<Assembly> solve(Image origImage, Collection<TileInfo> graph, int tileCountWidth, int tileCountHeight) {
        // SortedSet<Pair<Double, Pair<Pair<TileInfo, TileInfo>, Orientation>>> edges = new TreeSet<>(EDGE_COMPARATOR);
        // for (TileInfo source : graph) {
        // source.getNextRight().stream().forEach(p -> edges.add(new Pair<>(p.getRight(),
        // new Pair<>(new Pair<>(source, p.getLeft()), Orientation.EAST_WEST))));
        // source.getNextBottom().stream().forEach(p -> edges.add(new Pair<>(p.getRight(),
        // new Pair<>(new Pair<>(source, p.getLeft()), Orientation.NORTH_SOUTH))));
        // }

        // for (TileInfo t : graph) {
        // retainFirstOnly(t.getNextBottom());
        // retainFirstOnly(t.getNextTop());
        // retainFirstOnly(t.getNextRight());
        // retainFirstOnly(t.getNextLeft());
        // }
        //
        for (TileInfo t : graph) {
            removeUnmatchedLink(t, i -> i.getNextBottom());
            removeUnmatchedLink(t, i -> i.getNextTop());
            removeUnmatchedLink(t, i -> i.getNextLeft());
            removeUnmatchedLink(t, i -> i.getNextRight());
        }

        Set<Assembly> res = new HashSet<>();

        Set<Pair<TileInfo, Pair<Integer, Integer>>> startTiles = new HashSet<>();

        // top left
        startTiles.addAll(graph.stream().filter(t -> t.getNextLeft().isEmpty() && t.getNextTop().isEmpty())
                        .map(t -> new Pair<>(t, new Pair<>(0, 0))).collect(Collectors.toSet()));
        // lower left
        startTiles.addAll(graph.stream().filter(t -> t.getNextLeft().isEmpty() && t.getNextBottom().isEmpty())
                        .map(t -> new Pair<>(t, new Pair<>(0, tileCountHeight - 1))).collect(Collectors.toSet()));
        // top right
        startTiles.addAll(graph.stream().filter(t -> t.getNextRight().isEmpty() && t.getNextTop().isEmpty())
                        .map(t -> new Pair<>(t, new Pair<>(tileCountWidth - 1, 0))).collect(Collectors.toSet()));
        // lower right
        startTiles.addAll(graph.stream().filter(t -> t.getNextRight().isEmpty() && t.getNextBottom().isEmpty())
                        .map(t -> new Pair<>(t, new Pair<>(tileCountWidth - 1, tileCountHeight - 1)))
                        .collect(Collectors.toSet()));

        for (Pair<TileInfo, Pair<Integer, Integer>> startTilePair : startTiles) {
            TileInfo[][] board = new TileInfo[tileCountWidth][tileCountHeight];

            TileInfo startTile = startTilePair.getLeft();
            Pair<Integer, Integer> startTilePos = startTilePair.getRight();
            board[startTilePos.getLeft()][startTilePos.getRight()] = startTile;
            Set<TileInfo> visited = new HashSet<>();

            fillAll(board, startTilePos.getLeft(), startTilePos.getRight(), visited, tileCountWidth, tileCountHeight);

            // fill up with tiles that did not have good edges
            if (visited.size() != graph.size()) {
                Set<TileInfo> remaining = Sets.difference(new HashSet<>(graph), visited);
                Iterator<TileInfo> it = remaining.iterator();

                logger.debug("Need to fill up {} unset fields on board when using the following tile on {}: {}",
                                remaining.size(), startTilePos, startTile);
                for (int x = 0; x < tileCountWidth; x++) {
                    for (int y = 0; y < tileCountHeight; y++) {
                        if (board[x][y] == null) {
                            board[x][y] = it.next();
                        }
                    }
                }
            }

            int tileHeight = board[0][0].getTile().getHeight();
            int tileWidth = board[0][0].getTile().getWidth();

            NavigableMap<Integer, NavigableMap<Integer, Tile>> tiles = new TreeMap<>();
            for (int x = 0; x < tileCountWidth; x++) {
                tiles.put(x * tileWidth, new TreeMap<>());
                for (int y = 0; y < tileCountHeight; y++) {
                    tiles.get(x * tileWidth).put(y * tileHeight, board[x][y].getTile());
                }
            }

            res.add(new Assembly(origImage, tiles));
        }
        return res;

    }

    private void fillAll(TileInfo[][] board, int curX, int curY, Set<TileInfo> visited, int tileCountWidth,
                    int tileCountHeight) {
        if (visited.size() == board.length * board[0].length || curX >= board.length || curY > board[0].length) {
            return;
        }

        TileInfo curTile = board[curX][curY];
        if (visited.contains(curTile)) {
            return;
        }
        visited.add(curTile);

        // fill right
        if (!curTile.getNextRight().isEmpty()) {
            Deque<TileInfo> rightQueue = new LinkedList<>();
            rightQueue.add(curTile.getNextRight().first().getLeft());
            int newX = curX + 1;
            while (!rightQueue.isEmpty() && newX < tileCountWidth) {
                board[newX][curY] = rightQueue.poll();
                if (visited.contains(board[newX][curY])) {
                    board[newX][curY] = null;
                    break;
                }
                visited.add(board[newX][curY]);
                if (!board[newX][curY].getNextRight().isEmpty()) {
                    rightQueue.add(board[newX][curY].getNextRight().first().getLeft());
                }
                newX++;
            }
        }

        // fill down
        if (!curTile.getNextBottom().isEmpty()) {
            Deque<TileInfo> bottomQueue = new LinkedList<>();
            bottomQueue.add(curTile.getNextBottom().first().getLeft());
            int newY = curY + 1;
            while (!bottomQueue.isEmpty() && newY < tileCountHeight) {
                board[curX][newY] = bottomQueue.poll();
                if (visited.contains(board[curX][newY])) {
                    board[curX][newY] = null;
                    break;
                }
                visited.add(board[curX][newY]);
                if (!board[curX][newY].getNextBottom().isEmpty()) {
                    bottomQueue.add(board[curX][newY].getNextBottom().first().getLeft());
                }
                newY++;
            }
        }

        if (curX < board.length - 1 && curY < board[0].length - 1) {
            Map<TileInfo, Double> leftTiles;
            if (board[curX][curY + 1] != null) {
                leftTiles = board[curX][curY + 1].getNextRight().stream()
                                .collect(Collectors.toMap(p -> p.getLeft(), p -> p.getRight()));
            } else {
                leftTiles = new HashMap<>();
            }

            Map<TileInfo, Double> topTiles;
            if (board[curX + 1][curY] != null) {
                topTiles = board[curX + 1][curY].getNextBottom().stream()
                                .collect(Collectors.toMap(p -> p.getLeft(), p -> p.getRight()));
            } else {
                topTiles = new HashMap<>();
            }

            NavigableMap<Double, TileInfo> maxJudgementMap = new TreeMap<>((d1, d2) -> -d1.compareTo(d2));
            for (TileInfo t : Sets.union(leftTiles.keySet(), topTiles.keySet())) {
                double judgement = 0.;
                if (leftTiles.containsKey(t)) {
                    judgement += leftTiles.get(t);
                }
                if (topTiles.containsKey(t)) {
                    judgement += topTiles.get(t);
                }
                maxJudgementMap.put(judgement, t);
            }

            if (!maxJudgementMap.isEmpty()) {
                TileInfo nextTile = maxJudgementMap.firstEntry().getValue();
                board[curX + 1][curY + 1] = nextTile;
                fillAll(board, curX + 1, curY + 1, visited, tileCountWidth, tileCountHeight);
            }
        }
    }

    private void retainFirstOnly(NavigableSet<?> s) {
        if (!s.isEmpty()) {
            s.retainAll(Arrays.asList(s.first()));
        }
    }

    private void removeUnmatchedLink(TileInfo t, Function<TileInfo, NavigableSet<Pair<TileInfo, Double>>> setFn) {
        if (!setFn.apply(t).isEmpty()) {
            TileInfo other = setFn.apply(t).first().getLeft();
            if (!setFn.apply(other).isEmpty()) {
                if (setFn.apply(other).first().getLeft() != t) {
                    setFn.apply(t).clear();
                }
            }
        }
    }

    private class DynamicAssemblyBuilder {
        private int tileCountWidth;
        private int tileCountHeight;
        private TileInfo[][] largeBoard;
        private Map<TileInfo, Pair<Integer, Integer>> pos = new IdentityHashMap<>();

        public DynamicAssemblyBuilder(int tileCountWidth, int tileCountHeight) {
            this.tileCountWidth = tileCountWidth;
            this.tileCountHeight = tileCountHeight;
            largeBoard = new TileInfo[2 * tileCountWidth + 1][2 * tileCountHeight + 1];
        }

        public void addRelation(TileInfo first, TileInfo second, Orientation orientation) {
            Pair<Integer, Integer> posFirst = pos.get(first);
            Pair<Integer, Integer> posSecond = pos.get(second);
            if (posFirst != null ^ posSecond != null) {
                // one tile positioned
                if (posFirst != null) {
                    Pair<Integer, Integer> posNew;
                    if (Orientation.EAST_WEST.equals(orientation)) {
                        posNew = new Pair<>(posFirst.getLeft() + 1, posFirst.getRight());
                    } else {
                        posNew = new Pair<>(posFirst.getLeft(), posFirst.getRight() + 1);
                    }
                    if (largeBoard[posNew.getLeft()][posNew.getRight()] != null) {
                        largeBoard[posNew.getLeft()][posNew.getRight()] = second;
                        pos.put(second, posNew);
                        // success!
                    } // else: board position taken already
                } else {
                    Pair<Integer, Integer> posNew;
                    if (Orientation.EAST_WEST.equals(orientation)) {
                        posNew = new Pair<>(posSecond.getLeft() - 1, posSecond.getRight());
                    } else {
                        posNew = new Pair<>(posSecond.getLeft(), posSecond.getRight() - 1);
                    }
                    if (largeBoard[posNew.getLeft()][posNew.getRight()] != null) {
                        largeBoard[posNew.getLeft()][posNew.getRight()] = first;
                        pos.put(first, posNew);
                        // success!
                    } // else: board position taken already
                }
            } else if (posFirst != null && posSecond != null) {
                // both tiles positioned already
                // noop.
            } else {
                // none of the tiles positioned.
            }
        }
    }
}
