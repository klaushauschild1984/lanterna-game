package com.googlecode.lanterna.game;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.game.event.Event;
import com.googlecode.lanterna.game.event.GameEvent;
import com.googlecode.lanterna.game.event.Handler;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

import javax.swing.Timer;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class TerminalGame {

    private final int columns;
    private final int rows;
    private final String title;

    private Terminal terminal;
    private Timer timer;

    private Update update;
    private Render render;
    private Handler handler;

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

    public TerminalGame handler(final Handler handler) {
        this.handler = handler;
        return this;
    }

    public void launch() {
        try {
            Thread.currentThread().setName(title);
            terminal = initializeTerminal();
            dispatch(GameEvent.INITIALIZE);
            gameLoop();
        } catch (final Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void finish(final boolean exitVm) {
        try {
            timer.stop();
            dispatch(GameEvent.FINALIZE);
            terminal.close();
            if (exitVm) {
                System.exit(0);
            }
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

    private void dispatch(final Event event) {
        if (handler == null) {
            return;
        }
        handler.handle(event);
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
            swingTerminal.setResizable(false);
            swingTerminal.setLocationRelativeTo(null);
            swingTerminal.setAlwaysOnTop(true);
            swingTerminal.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(final WindowEvent event) {
                    finish(true);
                }

            });
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
