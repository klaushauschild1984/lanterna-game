package com.googlecode.lanterna.game;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.Font;
import java.io.IOException;

public class TerminalGame {

    private final int columns;
    private final int rows;
    private final String title;

    private Terminal terminal;
    private Timer timer;

    private Update update;
    private Render render;

    public TerminalGame(final String title, final int columns, final int rows) {
        this.title = title;
        this.columns = columns;
        this.rows = rows;
    }

    public TerminalGame update(final Update update) {
        this.update = update;
        return this;
    }

    public TerminalGame render(final Render render) {
        this.render = render;
        return this;
    }

    public void launch() {
        try {
            Thread.currentThread().setName(title);
            terminal = initializeTerminal();
            gameLoop();
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void finish() {
        try {
            timer.stop();
            terminal.close();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
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

    private void gameLoop()
            throws IOException {
        final Integer fpsLimit = 60;
        final long[] currentTime = {System.currentTimeMillis()};
        final TextGraphics textGraphics = terminal.newTextGraphics();
        timer = new Timer(-1, event -> {
            while (fpsLimit != null && fpsLimit > 0
                    && frameTime(currentTime[0]) < (1f / fpsLimit)) {
                sleep();
            }
            final float elapsed = frameTime(currentTime[0]);
            if (update != null) {
                update.update(elapsed);
            }
            clearScreen(terminal);
            if (render != null) {
                render.render(textGraphics);
            }
            flush(terminal);
            currentTime[0] = System.currentTimeMillis();
        });
        timer.start();
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
            swingTerminal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            swingTerminal.setResizable(false);
            swingTerminal.setLocationRelativeTo(null);
            swingTerminal.setAlwaysOnTop(true);
        }
        return terminal;
    }

    private void sleep() {
        try {
            Thread.sleep(0);
        } catch (final InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

}
