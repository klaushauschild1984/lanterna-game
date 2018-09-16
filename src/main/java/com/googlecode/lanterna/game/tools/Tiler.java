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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class Tiler {

    public static void main(final String[] args) {
        final String imageFileName = args[0];
        final Dimension tileSize = readTileSize(args[1], args[2]);
        final String processedImageFileName = getProcessedImageFileName(imageFileName, tileSize);
        final BufferedImage image = readImage(imageFileName);
        final BufferedImage processedImage = processImage(image, tileSize);
        writeImage(imageFileName, processedImageFileName, processedImage);
    }

    private static void writeImage(final String imageFileName, final String processedImageFileName,
                    final BufferedImage processedImage) {
        try {
            ImageIO.write(processedImage, getExtension(new File(imageFileName)),
                            new File(processedImageFileName));
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static BufferedImage processImage(final BufferedImage image, final Dimension tileSize) {
        final BufferedImage processedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                        BufferedImage.TYPE_INT_RGB);
        final Graphics graphics = processedImage.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.setColor(Color.WHITE);
        for (int x = 1; x < image.getWidth(); x++) {
            for (int y = 1; y < image.getHeight(); y++) {
                if (x % tileSize.getWidth() == 0 //
                                || y % tileSize.getHeight() == 0) {
                    graphics.drawLine(x, y, x, y);
                }
            }
        }
        return processedImage;
    }

    private static BufferedImage readImage(final String imageFileName) {
        try {
            return ImageIO.read(new File(imageFileName));
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static Dimension readTileSize(final String width, final String height) {
        return new Dimension(Integer.valueOf(width), Integer.valueOf(height));
    }

    private static String getProcessedImageFileName(final String imageFileName,
                    final Dimension tileSize) {
        final String extension = getExtension(new File(imageFileName));
        return String.format("%s_tiled%dx%d.%s", imageFileName.replace("." + extension, ""),
                        ((int) tileSize.getWidth()), ((int) tileSize.getHeight()), extension);
    }


    private static String getExtension(final File file) {
        final String fileName = file.getName();
        final int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(dotIndex + 1);
    }

}
