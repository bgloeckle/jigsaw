package com.github.bgloeckle.jigsaw.assembly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.util.EdgeDirection;
import com.github.bgloeckle.jigsaw.util.Pair;
import com.google.common.collect.Sets;

/**
 * Finds the most-likely-well {@link Assembly}s of an input edge {@link Image} with given horizontal and vertical cuts.
 *
 * @author Bastian Gloeckle
 */
public class AssemblyJigsaw {
    private static final Logger logger = LoggerFactory.getLogger(AssemblyJigsaw.class);

    private static final Comparator<Pair<?, Double>> COMPARATOR_HIGHEST_FRONT = (l,
                    r) -> -l.getRight().compareTo(r.getRight());

    private static final int TILE_EDGE_MATCH_HALO = 1;

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
        Map<Integer, Set<Tile>> topTileEdges = new HashMap<>();
        Map<Integer, Set<Tile>> rightTileEdges = new HashMap<>();
        Map<Integer, Set<Tile>> bottomTileEdges = new HashMap<>();
        Map<Integer, Set<Tile>> leftTileEdges = new HashMap<>();

        logger.debug("Identifying the edges on the corners of tiles cut every ({}/{})", cutEveryX, cutEveryY);
        for (Tile t : tiles) {
            addMatching(topTileEdges, t, p -> p.getLeft(), EdgeDirection.EAST_WEST,
                            it(t, p -> new Pair<>(p.getLeft() + 1, p.getRight()), new Pair<>(0, 0)));
            addMatching(bottomTileEdges, t, p -> p.getLeft(), EdgeDirection.EAST_WEST,
                            it(t, p -> new Pair<>(p.getLeft() + 1, p.getRight()), new Pair<>(0, t.getHeight() - 1)));
            addMatching(leftTileEdges, t, p -> p.getRight(), EdgeDirection.NORTH_SOUTH,
                            it(t, p -> new Pair<>(p.getLeft(), p.getRight() + 1), new Pair<>(0, 0)));
            addMatching(rightTileEdges, t, p -> p.getRight(), EdgeDirection.NORTH_SOUTH,
                            it(t, p -> new Pair<>(p.getLeft(), p.getRight() + 1), new Pair<>(t.getWidth() - 1, 0)));
        }

        List<TileNode> nodes = tiles.stream().map(t -> new TileNode(t)).collect(Collectors.toList());
        Map<Tile, TileNode> tileToNode = nodes.stream().collect(Collectors.toMap(n -> n.getTile(), n -> n));

        fillNodesNeighbours(nodes, tileToNode, topTileEdges, bottomTileEdges, n -> n.getTop());
        fillNodesNeighbours(nodes, tileToNode, bottomTileEdges, topTileEdges, n -> n.getBottom());
        fillNodesNeighbours(nodes, tileToNode, leftTileEdges, rightTileEdges, n -> n.getLeft());
        fillNodesNeighbours(nodes, tileToNode, rightTileEdges, leftTileEdges, n -> n.getRight());

        // find all possible combinations
        logger.debug("Identifying all possible locations of all tiles and judging those variants");
        NavigableSet<Pair<TileNode[][], Double>> pureRes = new TreeSet<>(COMPARATOR_HIGHEST_FRONT);
        for (TileNode startNode : nodes) {
            TileNode[][] board = new TileNode[tileCountWidth][tileCountHeight];
            Pair<Integer, Integer> curPos = new Pair<>(0, 0);
            board[0][0] = startNode;
            findAllPossibleResults(nodes, new HashSet<>(), board, curPos, 0., pureRes);
        }

        if (pureRes.isEmpty()) {
            logger.info("Could not identify a valid organization of the tiles when cut every ({}/{})", cutEveryX,
                            cutEveryY);
            return new HashSet<>();
        }

        // normalize judgments
        double highestJudgement = pureRes.first().getRight();

        NavigableSet<Pair<TileNode[][], Double>> res = new TreeSet<>(COMPARATOR_HIGHEST_FRONT);
        for (Pair<TileNode[][], Double> pureP : pureRes) {
            res.add(new Pair<>(pureP.getLeft(), pureP.getRight() / highestJudgement));
        }

        // cut-off uninteresting results
        res = res.headSet(new Pair<TileNode[][], Double>(null, 1. * bestStitchPercent), true);

        logger.info("Found {} best results when cutting the original image every ({}/{})", res.size(), cutEveryX,
                        cutEveryY);

        return null;
    }

    private void findAllPossibleResults(List<TileNode> nodes, Set<TileNode> visited, TileNode[][] board,
                    Pair<Integer, Integer> curPos, double curJudgeSum, Set<Pair<TileNode[][], Double>> res) {
        if (visited.size() == nodes.size()) {
            // found a solution!
            res.add(new Pair<>(board, curJudgeSum));
            return;
        }

        TileNode curTile = board[curPos.getLeft()][curPos.getRight()];
        // go left
        if (curPos.getLeft() > 0 && board[curPos.getLeft() - 1][curPos.getRight()] == null) {
            for (Pair<TileNode, Double> p : curTile.getLeft()) {
                if (!visited.contains(p.getLeft())) {
                    Pair<Integer, Integer> newPos = new Pair<>(curPos.getLeft() - 1, curPos.getRight());
                    findAllPossibleResultsMove(nodes, visited, board, curJudgeSum, res, p, newPos);
                }
            }
        }
        // go right
        if (curPos.getLeft() < tileCountWidth - 1 && board[curPos.getLeft() + 1][curPos.getRight()] == null) {
            for (Pair<TileNode, Double> p : curTile.getRight()) {
                if (!visited.contains(p.getLeft())) {
                    Pair<Integer, Integer> newPos = new Pair<>(curPos.getLeft() + 1, curPos.getRight());
                    findAllPossibleResultsMove(nodes, visited, board, curJudgeSum, res, p, newPos);
                }
            }
        }
        // go up
        if (curPos.getRight() > 0 && board[curPos.getLeft()][curPos.getRight() - 1] == null) {
            for (Pair<TileNode, Double> p : curTile.getTop()) {
                if (!visited.contains(p.getLeft())) {
                    Pair<Integer, Integer> newPos = new Pair<>(curPos.getLeft(), curPos.getRight() - 1);
                    findAllPossibleResultsMove(nodes, visited, board, curJudgeSum, res, p, newPos);
                }
            }
        }
        // go down
        if (curPos.getRight() < tileCountHeight - 1 && board[curPos.getLeft()][curPos.getRight() + 1] == null) {
            for (Pair<TileNode, Double> p : curTile.getBottom()) {
                if (!visited.contains(p.getLeft())) {
                    Pair<Integer, Integer> newPos = new Pair<>(curPos.getLeft(), curPos.getRight() + 1);
                    findAllPossibleResultsMove(nodes, visited, board, curJudgeSum, res, p, newPos);
                }
            }
        }
    }

    private void findAllPossibleResultsMove(List<TileNode> nodes, Set<TileNode> visited, TileNode[][] board,
                    double curJudgeSum, Set<Pair<TileNode[][], Double>> res, Pair<TileNode, Double> tileChosen,
                    Pair<Integer, Integer> newPos) {
        logger.debug("Filled position at {}", newPos);
        double newJudgeSum = curJudgeSum + tileChosen.getRight();
        board[newPos.getLeft()][newPos.getRight()] = tileChosen.getLeft();
        visited.add(tileChosen.getLeft());
        findAllPossibleResults(nodes, visited, board, newPos, newJudgeSum, res);
        board[newPos.getLeft()][newPos.getRight()] = null;
        visited.remove(tileChosen.getLeft());
    }

    private void fillNodesNeighbours(List<TileNode> nodes, Map<Tile, TileNode> tileToNode,
                    Map<Integer, Set<Tile>> targetSideEdges, Map<Integer, Set<Tile>> otherSideEdges,
                    Function<TileNode, NavigableSet<Pair<TileNode, Double>>> targetSetProvider) {
        Map<TileNode, Map<TileNode, Integer>> nodeToNodeCount = new HashMap<>();
        for (int idx : targetSideEdges.keySet()) {
            if (!otherSideEdges.containsKey(idx)) {
                continue;
            }
            for (int delta = -TILE_EDGE_MATCH_HALO; delta <= TILE_EDGE_MATCH_HALO; delta++) {
                int otherIdx = idx + delta;
                if (!otherSideEdges.containsKey(otherIdx)) {
                    continue;
                }

                // add a favor of each matching edge
                Set<Tile> tilesWithMatchingEdges = Sets.intersection(targetSideEdges.get(idx),
                                otherSideEdges.get(otherIdx));
                countTileCombinations(tilesWithMatchingEdges, tileToNode, nodeToNodeCount, 1);

                // count non-matching edges in a negative way
                Set<Tile> tilesWithUnmatchingEdges = Sets.union(
                                Sets.difference(targetSideEdges.get(idx), otherSideEdges.get(otherIdx)),
                                Sets.difference(otherSideEdges.get(otherIdx), targetSideEdges.get(idx)));
                countTileCombinations(tilesWithUnmatchingEdges, tileToNode, nodeToNodeCount, -1);
            }
        }

        // fill the TileNodes
        for (TileNode sourceNode : nodes) {
            for (TileNode destNode : nodes) {
                if (sourceNode == destNode) {
                    continue;
                }

                if (nodeToNodeCount.containsKey(sourceNode) && nodeToNodeCount.get(sourceNode).containsKey(destNode)) {
                    double judgment = nodeToNodeCount.get(sourceNode).get(destNode);
                    if (judgment > 0.) {
                        targetSetProvider.apply(sourceNode)
                                        .add(new Pair<>(destNode, judgment / sourceNode.getTile().getWidth()));
                    }
                } else {
                    targetSetProvider.apply(sourceNode).add(new Pair<>(destNode, 0.));
                }
            }
        }
    }

    /**
     * Add counts to the destination map for all combinations of the input tiles.
     * 
     * @param tiles
     *            Each tile will be combined with all other tiles in this set and a count will be added to the
     *            destination.
     * @param tileToNode
     *            Map from a tile to its {@link TileNode}.
     * @param dest
     *            The destination map.
     * @param singleCountValue
     *            the value to add each time.
     */
    private void countTileCombinations(Set<Tile> tiles, Map<Tile, TileNode> tileToNode,
                    Map<TileNode, Map<TileNode, Integer>> dest, int singleCountValue) {
        @SuppressWarnings("unchecked")
        Set<List<Tile>> cartesianProduct = Sets.cartesianProduct(tiles, tiles);
        for (List<Tile> matchingTiles : cartesianProduct) {
            Tile t1 = matchingTiles.get(0);
            Tile t2 = matchingTiles.get(1);
            if (t1 == t2) {
                continue;
            }
            TileNode n1 = tileToNode.get(t1);
            TileNode n2 = tileToNode.get(t2);

            dest.putIfAbsent(n1, new HashMap<>());
            dest.get(n1).compute(n2, (k, v) -> (v == null) ? singleCountValue : v + singleCountValue);
        }
    }

    /**
     * Adds the given tile to those indices of the map at which the tile has an edge.
     * 
     * @param dest
     *            destination map.
     * @param tile
     *            The tile to work on
     * @param destIdxProvider
     *            provides the index to which to add the tile in the dest map based on a location where a pixel was
     *            found.
     * @param ignoreEdgeDirection
     *            {@link EdgeDirection} to ignore pixels of
     * @param posIt
     *            Iterator over the positions to check in the tile.
     */
    private void addMatching(Map<Integer, Set<Tile>> dest, Tile tile,
                    Function<Pair<Integer, Integer>, Integer> destIdxProvider, EdgeDirection ignoreEdgeDirection,
                    Iterator<Pair<Integer, Integer>> posIt) {
        while (posIt.hasNext()) {
            Pair<Integer, Integer> pos = posIt.next();
            if (tile.getColor(pos.getLeft(), pos.getRight()) != 0) {
                EdgeDirection dir = EdgeDirection.fromGradientRadian(tile.getDirection(pos.getLeft(), pos.getRight()));
                if (!dir.equals(ignoreEdgeDirection)) {
                    int destIdx = destIdxProvider.apply(pos);
                    if (!dest.containsKey(destIdx)) {
                        dest.put(destIdx, new HashSet<>());
                    }
                    dest.get(destIdx).add(tile);
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

    private static class TileNode {

        private Tile tile;
        private NavigableSet<Pair<TileNode, Double>> left = new TreeSet<>(COMPARATOR_HIGHEST_FRONT);
        private NavigableSet<Pair<TileNode, Double>> top = new TreeSet<>(COMPARATOR_HIGHEST_FRONT);
        private NavigableSet<Pair<TileNode, Double>> right = new TreeSet<>(COMPARATOR_HIGHEST_FRONT);
        private NavigableSet<Pair<TileNode, Double>> bottom = new TreeSet<>(COMPARATOR_HIGHEST_FRONT);

        private TileNode(Tile tile) {
            this.tile = tile;
        }

        public Tile getTile() {
            return tile;
        }

        public NavigableSet<Pair<TileNode, Double>> getLeft() {
            return left;
        }

        public NavigableSet<Pair<TileNode, Double>> getTop() {
            return top;
        }

        public NavigableSet<Pair<TileNode, Double>> getRight() {
            return right;
        }

        public NavigableSet<Pair<TileNode, Double>> getBottom() {
            return bottom;
        }

    }
}
