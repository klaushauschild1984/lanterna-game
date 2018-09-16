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
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import javax.imageio.ImageIO;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.game.TerminalGame;
import com.googlecode.lanterna.game.event.ActionBinding;
import com.googlecode.lanterna.game.image.TextImageIO;
import com.googlecode.lanterna.graphics.TextImage;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class TextImageViewer {

    public static void main(final String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("no file");
        }
        final Resource resource = new FileSystemResource(args[0]);
        if (resource.exists()) {
            loadImage(resource);
        } else {
            int columns = -1;
            int rows = -1;
            if (args.length > 1) {
                columns = Integer.parseInt(args[1]);
                rows = columns;
            }
            if (args.length > 2) {
                rows = Integer.parseInt(args[2]);
            }
            if (columns == -1 || rows == -1) {
                throw new IllegalArgumentException("no columns, nor rows");
            }
            createImage(resource.getFile(), columns, rows);
        }
    }

    private static void loadImage(final Resource resource) {
        try {
            TerminalGame terminalGame = launchViewer(resource);

            final WatchService watcher = FileSystems.getDefault().newWatchService();
            resource.getFile().toPath().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                final WatchKey watchKey = watcher.take();
                if (!watchKey.pollEvents().isEmpty()) {
                    terminalGame.finish(false);
                    terminalGame = launchViewer(resource);
                }
                watchKey.reset();
            }
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private static TerminalGame launchViewer(final Resource resource) {
        final TextImage textImage = TextImageIO.read(resource);
        final TerminalGame terminalGame = new TerminalGame(resource.getFilename(),
                        textImage.getSize().getColumns(), textImage.getSize().getRows()) //
                                        .render(textGraphics -> {
                                            textGraphics.drawImage(new TerminalPosition(0, 0),
                                                            textImage);
                                        }) //
                                        .handler((game, event) -> {
                                            event.is(Action.QUIT, //
                                                            quit -> game.finish(true));
                                        }, new ActionBinding().bind(new KeyStroke(KeyType.Escape),
                                                        Action.QUIT));
        terminalGame.launch();
        return terminalGame;
    }

    private static void createImage(final File file, final int columns, final int rows) {
        try {
            file.mkdir();
            try (final BufferedWriter writer = new BufferedWriter(
                            new FileWriter(new File(file, TextImageIO.GLYPHS)))) {
                final StringBuilder lineBuilder = new StringBuilder();
                for (int column = 0; column < columns; column++) {
                    lineBuilder.append(" ");
                }
                for (int row = 0; row < rows; row++) {
                    if (row != 0) {
                        writer.newLine();
                    }
                    writer.write(lineBuilder.toString());
                }
            }
            final BufferedImage bufferedImage =
                            new BufferedImage(columns, rows, BufferedImage.TYPE_INT_RGB);
            ImageIO.write(bufferedImage, "png", new File(file, TextImageIO.BACKGROUND));

            final Graphics2D graphics = bufferedImage.createGraphics();
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.fillRect(0, 0, columns, rows);
            ImageIO.write(bufferedImage, "png", new File(file, TextImageIO.FOREGROUND));

            loadImage(new FileSystemResource(file));
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private enum Action implements com.googlecode.lanterna.game.event.Action {

        QUIT,

    }

}
