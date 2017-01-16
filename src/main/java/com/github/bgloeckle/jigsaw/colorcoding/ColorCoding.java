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
package com.github.bgloeckle.jigsaw.colorcoding;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.util.Pair;

/**
 * Color-coding graphs in order to find simple paths inside it that have a specific minimum length.
 * 
 * <p>
 * This class is based on the algorithm proposed by Noga Alon, Raphy Yuster and Uri Zwick in "Color-coding: a new method
 * for finding simple paths, cycles and other small subgraphs within large graphs" (1994).
 * 
 * See also https://en.wikipedia.org/wiki/Color-coding.
 * 
 * @author Bastian Gloeckle
 */
public class ColorCoding<V extends Vertex> {
    private static final Logger logger = LoggerFactory.getLogger(ColorCoding.class);
    private static final int NO_OP_K_GREATER_THAN = 10;

    private Collection<V> inGraph;

    public ColorCoding(Collection<V> inGraph) {
        this.inGraph = inGraph;
    }

    public Set<V> findVerticesWithLengthGreater(int k) {
        if (k > NO_OP_K_GREATER_THAN) {
            return null;
        }

        Supplier<Integer> nextIdSupplier = new Supplier<Integer>() {
            int nextId = 0;

            @Override
            public Integer get() {
                return nextId++;
            }
        };
        Map<Vertex, VertexInfo> toInfoMap = new IdentityHashMap<>();
        List<VertexInfo> graph = inGraph.stream().map(v -> {
            VertexInfo res = new VertexInfo(nextIdSupplier.get(), v, k, vertex -> toInfoMap.get(vertex));
            toInfoMap.put(v, res);
            return res;
        }).collect(Collectors.toList());

        Set<VertexInfo> res = new HashSet<>();

        long numberOfTimesToExecute = (long) Math.ceil(Math.exp(k));
        logger.debug("Color-Coding will need {} iterations", numberOfTimesToExecute);
        for (long l = 0; l < numberOfTimesToExecute; l++) {
            if (l % (numberOfTimesToExecute / 10) == 0) {
                logger.debug("Executed {} iterations", l);
            }
            int color[] = new int[graph.size()];
            for (int i = 0; i < color.length; i++) {
                color[i] = ThreadLocalRandom.current().nextInt(k);
            }

            graph.stream().parallel().forEach(v -> v.removeVisitedColors(k));

            // len = 1, initialize
            for (VertexInfo v : graph) {
                for (VertexInfo u : v.getNext()) {
                    BitSet s = new BitSet(k);
                    s.set(color[v.getId()]);
                    u.addVisitedColorsWithStartVertex(1, new Pair<>(s, v));
                }
            }

            // len > 1, reduce all the color-sets
            for (int curLen = 2; curLen <= k; curLen++) {
                // walk along all edges and the color sets of the originating vertex
                int len = curLen;
                // release some memory
                graph.stream().parallel().forEach(v -> {
                    for (Pair<BitSet, VertexInfo> visitedColorsPair : v.getVisitedColorsWithStartVertex(len - 1)) {
                        BitSet visitedColors = visitedColorsPair.getLeft();
                        VertexInfo startVertex = visitedColorsPair.getRight();

                        for (VertexInfo u : v.getNext()) {
                            if (!visitedColors.get(color[u.getId()])) {
                                if (len == k) {
                                    res.add(startVertex);
                                    // logger.debug("Found valid start at {}", startVertex.getVertex());
                                } else {
                                    BitSet newVisitedColors = new BitSet(k);
                                    newVisitedColors.or(visitedColors);
                                    newVisitedColors.set(color[u.getId()]);
                                    u.addVisitedColorsWithStartVertex(len,
                                                    new Pair<>(newVisitedColors, startVertex));
                                }
                            }
                        }
                    }
                });
            }
        }

        return res.stream().map(vertexInfo -> vertexInfo.getVertex()).collect(Collectors.toSet());
    }

    private class VertexInfo {
        private V vertex;
        private Set<Pair<BitSet, VertexInfo>>[] visitedColorsWithStartVertex;
        private int id;
        private Object nextSync = new Object();
        private volatile List<VertexInfo> next;
        private Function<Vertex, VertexInfo> toInfoFn;

        @SuppressWarnings("unchecked")
        public VertexInfo(int id, V vertex, int k, Function<Vertex, VertexInfo> toInfoFn) {
            this.id = id;
            this.vertex = vertex;
            this.toInfoFn = toInfoFn;
            visitedColorsWithStartVertex = new Set[k];
            for (int i = 0; i < k; i++) {
                visitedColorsWithStartVertex[i] = new HashSet<>();
            }

            next = null;
        }

        public V getVertex() {
            return vertex;
        }

        public Set<Pair<BitSet, VertexInfo>> getVisitedColorsWithStartVertex(int len) {
            return visitedColorsWithStartVertex[len - 1];
        }

        public synchronized void addVisitedColorsWithStartVertex(int len,
                        Pair<BitSet, VertexInfo> visitedColorsWithStartVertex) {
            this.visitedColorsWithStartVertex[len - 1].add(visitedColorsWithStartVertex);
        }

        public void removeVisitedColors(int upToLen) {
            int upToIdx = upToLen - 1;
            while (upToIdx-- > 0) {
                if (!this.visitedColorsWithStartVertex[upToIdx].isEmpty()) {
                    this.visitedColorsWithStartVertex[upToIdx] = new HashSet<>();
                }
            }
        }

        public Collection<VertexInfo> getNext() {
            if (next == null) {
                synchronized (nextSync) {
                    if (next == null) {
                        next = vertex.getNext().stream().map(v -> toInfoFn.apply(v)).collect(Collectors.toList());
                    }
                }
            }
            return next;
        }

        public int getId() {
            return id;
        }
    }
}
