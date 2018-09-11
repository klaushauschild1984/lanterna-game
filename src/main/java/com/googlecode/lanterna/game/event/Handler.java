package com.googlecode.lanterna.game.event;

import com.googlecode.lanterna.game.TerminalGame;

public interface Handler {

    void handle(TerminalGame terminalGame, Event event);

}
