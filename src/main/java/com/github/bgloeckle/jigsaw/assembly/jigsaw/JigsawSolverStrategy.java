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
import java.util.Set;

import com.github.bgloeckle.jigsaw.assembly.Assembly;
import com.github.bgloeckle.jigsaw.image.Image;

/**
 * Strategy to select those {@link TileInfo}s of a graph which should be placed at the very left side of the result
 * assembly.
 *
 * @author Bastian Gloeckle
 */
public interface JigsawSolverStrategy {
    /**
     * 
     * @param origImage
     *            the original image
     * @param graph
     *            All tiles
     * @param tileCountWidth
     *            Number of tiles to be placed horizontally
     * @param tileCountHeight
     *            Number of tiles to be placed vertically
     * @return <code>null</code> or a set of result {@link Assembly}s to place at the left border.
     */
    public Set<Assembly> solve(Image origImage, Collection<TileInfo> graph, int tileCountWidth, int tileCountHeight);
}
