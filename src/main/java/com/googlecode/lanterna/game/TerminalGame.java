package com.googlecode.lanterna.game;

import java.awt.Font;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.Timer;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

public class TerminalGame {

    private final int columns;
    private final int rows;
    private final String title;

    public TerminalGame(final String title, final int columns, final int rows) {
        this.title = title;
        this.columns = columns;
        this.rows = rows;
    }

    private void clearScreen(final Terminal terminal) {
        try {
            terminal.clearScreen();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void flush(final Terminal terminal) {
        try {
            terminal.flush();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private float frameTime(final long currentTime) {
        return (System.currentTimeMillis() - currentTime) / 1000f;
    }

    private void gameLoop(final Terminal terminal, final Update update, final Render render)
                    throws IOException {
        final Integer fpsLimit = 60;
        final long[] currentTime = {System.currentTimeMillis()};
        final TextGraphics textGraphics = terminal.newTextGraphics();
        new Timer(-1, event -> {
            while (fpsLimit != null && fpsLimit > 0
                            && frameTime(currentTime[0]) < (1f / fpsLimit)) {
                sleep();
            }
            final float elapsed = frameTime(currentTime[0]);
            update.update(elapsed);
            clearScreen(terminal);
            render.render(textGraphics);
            flush(terminal);
            currentTime[0] = System.currentTimeMillis();
        }).start();
    }

    private Terminal initializeTerminal() throws Exception {
        final Font font = new Font("DejaVu Sans Mono", Font.BOLD, 28);

        final Terminal terminal = new DefaultTerminalFactory() //
                        .setInitialTerminalSize(new TerminalSize(columns, rows)) //
                        .setTerminalEmulatorTitle(title) //
                        .setTerminalEmulatorFontConfiguration(
                                        SwingTerminalFontConfiguration.newInstance(font)) //
                        .createTerminal();
        terminal.setCursorVisible(false);
        if (terminal instanceof SwingTerminalFrame) {
            final SwingTerminalFrame swingTerminal = (SwingTerminalFrame) terminal;
            swingTerminal.setLocationRelativeTo(null);
            swingTerminal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }
        return terminal;
    }

    public void launch(final Update update, final Render render) {
        try {
            Thread.currentThread().setName(title);
            final Terminal terminal = initializeTerminal();
            gameLoop(terminal, update, render);
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(0);
        } catch (final InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

}
