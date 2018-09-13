/*
 * This file is part of Lanterna Game.
 *
 * Lanterna Game is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Lanterna Game is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Lanterna Game. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.googlecode.lanterna.game.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.googlecode.lanterna.game.image.TextImageIO;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class TextImagePacker {

    public static void main(final String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("no directory");
        }
        final File directory = new File(args[0]);
        traverse(directory);
    }

    public static File pack(final File textImageDirectory) {
        final File textImageFile = new File(textImageDirectory.getParent(),
                        textImageDirectory.getName() + ".zip");
        try (final ZipOutputStream zipOutputStream = new ZipOutputStream(
                        new BufferedOutputStream(new FileOutputStream(textImageFile)))) {
            copy(textImageDirectory, TextImageIO.GLYPHS, zipOutputStream);
            copy(textImageDirectory, TextImageIO.FOREGROUND, zipOutputStream);
            copy(textImageDirectory, TextImageIO.BACKGROUND, zipOutputStream);
            return textImageFile;
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static void traverse(final File directory) {
        if (!directory.isDirectory()) {
            return;
        }
        if (isTextImageDirectory(directory)) {
            pack(directory);
            return;
        }
        for (File file : directory.listFiles()) {
            traverse(file);
        }
    }

    private static void copy(final File textImageDirectory, final String name,
                    final ZipOutputStream zipOutputStream) {
        try {
            final ZipEntry zipEntry = new ZipEntry(name);
            zipOutputStream.putNextEntry(zipEntry);
            Files.copy(new File(textImageDirectory, name).toPath(), zipOutputStream);
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static boolean isTextImageDirectory(final File textImageDirectory) {
        if (!textImageDirectory.isDirectory()) {
            return false;
        }
        final List<String> files = Arrays.asList(textImageDirectory.list());
        if (files.size() != 3) {
            return false;
        }
        if (!files.contains(TextImageIO.GLYPHS)) {
            return false;
        }
        if (!files.contains(TextImageIO.FOREGROUND)) {
            return false;
        }
        if (!files.contains(TextImageIO.BACKGROUND)) {
            return false;
        }
        return true;
    }

}
