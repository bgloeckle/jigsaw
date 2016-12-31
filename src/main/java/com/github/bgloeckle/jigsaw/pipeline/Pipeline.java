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
package com.github.bgloeckle.jigsaw.pipeline;

import com.github.bgloeckle.jigsaw.image.Image;

/**
 * Pipeline of {@link Step}s which are executed consecutively on {@link #process(Image)}.
 *
 * @author Bastian Gloeckle
 */
public class Pipeline {
    private Step[] steps;

    public Pipeline(Step... steps) {
        this.steps = steps;
    }

    public Image process(Image input) {
        Image result = input.copy();

        for (Step s : steps) {
            s.accept(result);
        }

        return result;
    }
}
