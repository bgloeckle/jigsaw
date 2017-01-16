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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.assembly.Assembly;
import com.github.bgloeckle.jigsaw.colorcoding.ColorCoding;
import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.util.Pair;
import com.google.common.collect.Sets;

/**
 * A {@link JigsawSolverStrategy} which uses {@link ColorCoding} to select the left-sided tiles, startuing from those it
 * will try to fill whole {@link Assembly}s.
 * 
 * <p>
 * TODO this is unfortunately way too slow.
 *
 * @author Bastian Gloeckle
 */
public class ColorCodingJigsawSolverStrategy implements JigsawSolverStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ColorCodingJigsawSolverStrategy.class);

    @Override
    public Set<Assembly> solve(Image origImage, Collection<TileInfo> graph, int tileCountWidth, int tileCountHeight) {
        ColorCoding<TileInfo> colorCoding = new ColorCoding<>(graph);
        Set<TileInfo> potentialLeftTiles = colorCoding.findVerticesWithLengthGreater(tileCountWidth);

        if (potentialLeftTiles.size() < tileCountHeight)

        {
            logger.warn("Did not find enough tiles that could potentially be placed on the left border. Found {} but need {}.",
                            potentialLeftTiles.size(), tileCountHeight);
            return new HashSet<>();
        }

        logger.debug("Placing left tiles in all combinations and trying to find solutions");
        ThreadLocal<TileInfo[][]> boards = ThreadLocal.withInitial(() -> new TileInfo[tileCountWidth][tileCountHeight]);
        potentialLeftTiles.stream().parallel().forEach(startTile -> {
            TileInfo[][] board = boards.get();
            Set<TileInfo> visited = new HashSet<>();
            for (int y = 0; y < board[0].length; y++) {
                // try to place this tile at all x=0 locations.
                visited.clear();
                board[0][y] = startTile;
                fillLeftSide(board, y, visited, () -> {
                    logger.debug("Inspecting possibilities with left side: {}", IntStream.range(0, board[0].length)
                                    .mapToObj(ty -> board[0][ty]).collect(Collectors.toList()));
                    // for (Pair<TileInfo, Double> p : board[0][0].getNextRight()) {
                    // if (!visited.contains(p.getLeft())) {
                    // board[1][0] = p.getLeft();
                    // findAllPossibleSolutions(board, 1, 0, visited, 0., solutionPair -> {
                    // System.out.println("Found new solution: " + solutionPair);
                    // });
                    // board[1][0] = null;
                    // }
                    // }
                });
                board[0][y] = null;
            }
        });
        return null;
    }

    private void fillLeftSide(TileInfo[][] board, int curY, Set<TileInfo> visited, Runnable innerExecution) {
        TileInfo curTile = board[0][curY];
        visited.add(curTile);
        try {
            if (curY > 0 && board[0][curY - 1] == null) {
                // go up
                for (Pair<TileInfo, Double> p : curTile.getNextTop()) {
                    if (!visited.contains(p.getLeft())) {
                        board[0][curY - 1] = p.getLeft();
                        fillLeftSide(board, curY - 1, visited, innerExecution);
                        board[0][curY - 1] = null;
                    }
                }
                return;
            }
            if (curY == 0) {
                while (curY < board[0].length && board[0][curY] != null) {
                    curY++;
                }
                if (curY < board[0].length && board[0][curY] == null) {
                    curY--;
                }
            }
            if (curY < board[0].length - 1) {
                curTile = board[0][curY];

                // go down
                for (Pair<TileInfo, Double> p : curTile.getNextBottom()) {
                    if (!visited.contains(p.getLeft())) {
                        board[0][curY + 1] = p.getLeft();
                        fillLeftSide(board, curY + 1, visited, innerExecution);
                        board[0][curY + 1] = null;
                    }
                }
                return;
            }

            // we're fully down, execute inner stuff
            innerExecution.run();
        } finally {
            visited.remove(curTile);
        }
    }

    private void findAllPossibleSolutions(TileInfo[][] board, int curX, int curY, Set<TileInfo> visited,
                    double curJudgement, int tileCountWidth, int tileCountHeight,
                    Consumer<Pair<TileInfo[][], Double>> resConsumer) {
        if (visited.size() == tileCountHeight * tileCountWidth) {
            resConsumer.accept(new Pair<>(board, curJudgement));
            return;
        }

        TileInfo curTile = board[curX][curY];

        if (visited.contains(curTile)) {
            return;
        }

        // logger.trace("Inspecting with tile at pos {}: {}", new Pair<>(curX, curY), curTile);
        visited.add(curTile);

        if (curY == board[0].length - 1) {
            // next column to the right
            for (Pair<TileInfo, Double> p : board[curX][0].getNextRight()) {
                if (!visited.contains(p.getLeft())) {
                    board[curX + 1][0] = p.getLeft();
                    findAllPossibleSolutions(board, curX + 1, 0, visited, curJudgement, tileCountWidth, tileCountHeight,
                                    resConsumer);
                    board[curX + 1][0] = null;
                }
            }
        } else {
            // next down in same column
            Set<TileInfo> bottomSet = curTile.getNextBottom().stream().map(p -> p.getLeft())
                            .collect(Collectors.toSet());
            Set<TileInfo> rightSet = board[curX - 1][curY + 1].getNextRight().stream().map(p -> p.getLeft())
                            .collect(Collectors.toSet());
            Set<TileInfo> possibleTiles = Sets.difference(Sets.intersection(bottomSet, rightSet), visited);
            // if (!possibleTiles.isEmpty()) {
            // logger.debug("Will try {} variants advancing down", possibleTiles.size());
            // }
            for (TileInfo t : possibleTiles) {
                board[curX][curY + 1] = t;
                findAllPossibleSolutions(board, curX, curY + 1, visited, curJudgement, tileCountWidth, tileCountHeight,
                                resConsumer);
                board[curX][curY + 1] = null;
            }
        }

        visited.remove(curTile);
    }

    @Override
    public String toString() {
        return "ColorCodingJigsawSolverStrategy []";
    }

}
