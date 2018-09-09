package com.googlecode.lanterna.game;

import java.awt.Image;
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
import javax.imageio.ImageIO;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.graphics.TextImage;

public enum TextImageIO {

    ;

    private static final String GLYPHS = "glyphs.txt";
    private static final String FOREGROUND = "foreground.png";
    private static final String BACKGROUND = "background.png";

    public static TextImage read(final File textImageFile) throws IOException {
        if (textImageFile.isDirectory()) {
            // read from directory
            final BufferedInputStream glyphsStream = new BufferedInputStream(
                            new FileInputStream(new File(textImageFile, GLYPHS)));
            final List<String> glyphs = readGlyphs(glyphsStream);
            final BufferedImage foreground = readImage(new BufferedInputStream(
                            new FileInputStream(new File(textImageFile, FOREGROUND))));
            final BufferedImage background = readImage(new BufferedInputStream(
                            new FileInputStream(new File(textImageFile, BACKGROUND))));
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
                    final Image foreground, final Image background) {
        for (int row = 0; row < textImage.getSize().getRows(); row++) {
            final String line = glyphs.get(row);
            for (int column = 0; column < textImage.getSize().getColumns(); column++) {
                final char glyph;
                if (column > line.length()) {
                    glyph = ' ';
                } else {
                    glyph = line.charAt(column);
                }
            }
        }
    }

    private static TerminalSize getImageSize(final List<String> lines) {
        final int columns = lines.stream() //
                        .map(String::length) //
                        .max(Comparator.naturalOrder()).get();
        final int rows = lines.size();
        return new TerminalSize(columns, rows);
    }

    private static TextImage read(final List<String> glyphs, final Image foreground,
                    final Image background) throws IOException {
        final TerminalSize imageSize = getImageSize(glyphs);
        final TextImage textImage = new BasicTextImage(imageSize);
        fillImage(textImage, glyphs, foreground, background);
        return textImage;
    }

    private static List<String> readGlyphs(final InputStream glyphStream) throws IOException {
        try (final BufferedReader reader = new BufferedReader(
                        new InputStreamReader(glyphStream, StandardCharsets.UTF_8))) {
            return reader.lines() //
                            .collect(Collectors.toList());
        }
    }

    private static BufferedImage readImage(final InputStream imageStream) throws IOException {
        final BufferedImage image = ImageIO.read(imageStream);
        imageStream.close();
        return image;
    }

}
