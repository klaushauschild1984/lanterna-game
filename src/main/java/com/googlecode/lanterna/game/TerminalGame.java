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

package com.googlecode.lanterna.game;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.Timer;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.game.event.ActionBinding;
import com.googlecode.lanterna.game.event.Event;
import com.googlecode.lanterna.game.event.GameEvent;
import com.googlecode.lanterna.game.event.Handler;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFrame;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class TerminalGame {

    private final int columns;
    private final int rows;
    private final String title;

    private Terminal terminal;
    private Timer timer;

    private Update update;
    private Render render;
    private Handler handler;
    private ActionBinding actionBinding;
    private Font font;
    private int fontSize = 28;

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

    public TerminalGame handler(final Handler handler, final ActionBinding actionBinding) {
        this.handler = handler;
        this.actionBinding = actionBinding;
        return this;
    }

    public TerminalGame font(final Font font) {
        this.font = font;
        return this;
    }

    public TerminalGame fontSize(final int fontSize) {
        this.fontSize = fontSize;
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

    private void clearScreen() {
        try {
            terminal.clearScreen();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void flush() {
        try {
            terminal.flush();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private float frameTime(final long currentTime) {
        return (System.currentTimeMillis() - currentTime) / 1000f;
    }

    private void gameLoop() throws IOException {
        final Integer fpsLimit = 60;
        final long[] currentTime = {System.currentTimeMillis()};
        final TextGraphics textGraphics = terminal.newTextGraphics();
        timer = new Timer(-1, event -> {
            while (fpsLimit != null && fpsLimit > 0
                            && frameTime(currentTime[0]) < (1f / fpsLimit)) {
                sleep();
            }
            handleInput();
            final float elapsed = frameTime(currentTime[0]);
            update(elapsed);
            clearScreen();
            render(textGraphics);
            flush();
            currentTime[0] = System.currentTimeMillis();
        });
        timer.start();
    }

    private void handleInput() {
        if (handler == null || actionBinding == null) {
            return;
        }
        try {
            KeyStroke keyStroke;
            while ((keyStroke = terminal.pollInput()) != null) {
                actionBinding.resolve(keyStroke) //
                                .ifPresent(this::dispatch);
            }
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void update(final float elapsed) {
        if (update == null) {
            return;
        }
        update.update(elapsed);
    }

    private void render(final TextGraphics textGraphics) {
        if (render == null) {
            return;
        }
        render.render(textGraphics);
    }

    private void dispatch(final Event event) {
        if (handler == null) {
            return;
        }
        handler.handle(this, event);
    }

    private Terminal initializeTerminal() throws Exception {
        if (font == null) {
            font = new Font("DejaVu Sans Mono", Font.BOLD, fontSize);
        } else {
            font = font.deriveFont(Font.BOLD, fontSize);
        }
        if (AWTTerminalFontConfiguration.filterMonospaced(font).length != 1) {
            throw new IllegalArgumentException(String.format("Font %s is not mono-spaced.", font));
        }

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
