package com.googlecode.lanterna.game.tools;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.game.TerminalGame;
import com.googlecode.lanterna.game.TextImageIO;
import com.googlecode.lanterna.graphics.TextImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class TextImageViewer {

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("no file");
        }
        final File file = new File(args[0]);
        if (file.exists()) {
            loadImage(file);
        } else {
            createImage(file, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
    }

    private static void loadImage(final File file) throws Exception {
        TerminalGame terminalGame = launchViewer(file);

        final WatchService watcher = FileSystems.getDefault().newWatchService();
        file.toPath().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
        while (true) {
            final WatchKey watchKey = watcher.take();
            if (!watchKey.pollEvents().isEmpty()) {
                terminalGame.finish(false);
                terminalGame = launchViewer(file);
            }
            watchKey.reset();
        }
    }

    private static TerminalGame launchViewer(final File file) throws IOException {
        final TextImage textImage = TextImageIO.read(file);
        final TerminalGame terminalGame = new TerminalGame(file.getName(), textImage.getSize().getColumns(), textImage.getSize().getRows()) //
                .render(textGraphics -> {
                    textGraphics.drawImage(new TerminalPosition(0, 0), textImage);
                });
        terminalGame.launch();
        return terminalGame;
    }

    private static void createImage(final File file, final int columns, final int rows) {

    }

}
