/*
 * This file is part of Lanterna Game.
 *
 * Lanterna Game is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lanterna Game is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lanterna Game.  If not, see <http://www.gnu.org/licenses/>.
 */

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
