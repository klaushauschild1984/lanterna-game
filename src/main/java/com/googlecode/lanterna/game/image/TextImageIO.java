/*
 * This file is part of Lanterna Game.
 *
 * Lanterna Game is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lanterna Game is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lanterna Game.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This file is part of Lanterna Game.
 *
 * Lanterna Game is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lanterna Game is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lanterna Game.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.lanterna.game.image;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor.RGB;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public enum TextImageIO {

    ;

    public static final String GLYPHS = "glyphs.txt";
    public static final String FOREGROUND = "foreground.png";
    public static final String BACKGROUND = "background.png";

    public static TextImage read(final File textImageFile) throws IOException {
        if (textImageFile.isDirectory()) {
            // read from directory
            final BufferedInputStream glyphsStream = new BufferedInputStream(
                    new FileInputStream(new File(textImageFile, GLYPHS)));
            final List<String> glyphs = readGlyphs(glyphsStream);
            glyphsStream.close();
            final BufferedInputStream foregroundStream = new BufferedInputStream(
                    new FileInputStream(new File(textImageFile, FOREGROUND)));
            final BufferedImage foreground = readImage(foregroundStream);
            foregroundStream.close();
            final BufferedInputStream backgroundStream = new BufferedInputStream(
                    new FileInputStream(new File(textImageFile, BACKGROUND)));
            final BufferedImage background = readImage(backgroundStream);
            backgroundStream.close();
            return read(glyphs, foreground, background);
        }
        // read from zip archive
        return read(new BufferedInputStream(new FileInputStream(textImageFile)));
    }

    public static TextImage read(final InputStream textImageArchiveStream) throws IOException {
        List<String> glyphs = null;
        BufferedImage foreground = null;
        BufferedImage background = null;

        try (final ZipInputStream zipStream = new ZipInputStream(textImageArchiveStream)) {
            int loaded = 0;
            while (loaded != 3) {
                final ZipEntry nextEntry = zipStream.getNextEntry();
                switch (nextEntry.getName()) {
                    case GLYPHS:
                        glyphs = readGlyphs(zipStream);
                        loaded++;
                        break;
                    case FOREGROUND:
                        foreground = readImage(zipStream);
                        loaded++;
                        break;
                    case BACKGROUND:
                        background = readImage(zipStream);
                        loaded++;
                        break;
                }
            }
        }

        return read(glyphs, foreground, background);
    }

    private static void fillImage(final TextImage textImage, final List<String> glyphs,
                                  final BufferedImage foregroundImage, final BufferedImage backgroundImage) {
        final TextGraphics textGraphics = textImage.newTextGraphics();
        for (int row = 0; row < textImage.getSize().getRows(); row++) {
            final String line = glyphs.get(row);
            for (int column = 0; column < textImage.getSize().getColumns(); column++) {
                final char glyph;
                if (column >= line.length()) {
                    glyph = ' ';
                } else {
                    glyph = line.charAt(column);
                }
                final RGB foreground = toRGB(foregroundImage, column, row);
                final RGB background = toRGB(backgroundImage, column, row);
                final TextCharacter character;
                if (background != null) {
                    character = new TextCharacter(glyph, foreground, background);
                } else {
                    character = new TransparentTextCharacter(glyph, foreground);
                }
                textGraphics.setCharacter(column, row, character);
            }
        }
    }

    private static RGB toRGB(final BufferedImage image, final int column, final int row) {
        final Color color = new Color(image.getRGB(column, row), true);
        if (color.getAlpha() == 0) {
            return null;
        }
        return new RGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    private static TerminalSize getImageSize(final List<String> lines) {
        final int columns = lines.stream() //
                .map(String::length) //
                .max(Comparator.naturalOrder()).get();
        final int rows = lines.size();
        return new TerminalSize(columns, rows);
    }

    private static TextImage read(final List<String> glyphs, final BufferedImage foreground,
                                  final BufferedImage background) {
        final TerminalSize imageSize = getImageSize(glyphs);
        final TextImage textImage = new TransparentTextImage(imageSize);
        fillImage(textImage, glyphs, foreground, background);
        return textImage;
    }

    private static List<String> readGlyphs(final InputStream glyphStream) {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(glyphStream, StandardCharsets.UTF_8));
        return reader.lines() //
                .collect(Collectors.toList());
    }

    private static BufferedImage readImage(final InputStream imageStream) throws IOException {
        return ImageIO.read(imageStream);
    }

}
