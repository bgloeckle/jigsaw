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
package com.github.bgloeckle.jigsaw;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2 || args[0].equals("--help")) {
            displayHelp();
            return;
        }
        File inputFile = new File(args[0]);
        if (!inputFile.isFile() || !inputFile.exists()) {
            System.err.println("'" + args[0] + "' is no file or does not exist.");
            return;
        }

        File outputFile = new File(args[1]);

        new JigsawSolver(inputFile, outputFile).solve();
    }

    private static void displayHelp() {
        System.out.println("Solve picture jigsaws.");
        System.out.println("Parameters: [source image].png [destination image].png");
    }
}
