package com.googlecode.lanterna.game.event;

import com.googlecode.lanterna.input.KeyStroke;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ActionBinding {

    private final Map<KeyStroke, Action> actions = new HashMap<>();

    public ActionBinding bind(final KeyStroke keyStroke, final Action action) {
        actions.put(keyStroke, action);
        return this;
    }

    public Optional<Action> resolve(final KeyStroke keyStroke) {
        return Optional.ofNullable(actions.get(keyStroke));
    }

}
