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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class ActionBinding {

    private final Map<KeyStroke, Action> actions = new HashMap<>();

    public static ActionBinding from(final InputStream stream) {
        try {
            final Properties bindings = new Properties();
            bindings.load(stream);

            final ActionBinding actionBinding = new ActionBinding();
            bindings.forEach((key, value) -> {
                final KeyStroke keyStroke = getKeyStroke(key.toString());
                final Action action = getAction(value.toString());
                actionBinding.bind(keyStroke, action);
            });
            return actionBinding;
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static KeyStroke getKeyStroke(final String key) {
        return KeyStroke.fromString(key);
    }

    private static Action getAction(final String value) {
        try {
            final Class<?> actionClass = Class.forName(value);
            return (Action) actionClass.newInstance();
        } catch (final Exception exception) {
            final int lastIndexOfDot = value.lastIndexOf(".");
            final String className = value.substring(0, lastIndexOfDot);
            final String fieldName = value.substring(lastIndexOfDot + 1);
            try {
                final Class<?> actionContainerClass = Class.forName(className);
                final Field field = actionContainerClass.getField(fieldName);
                return (Action) field.get(null);
            } catch (final Exception exception2) {
                throw new RuntimeException(exception2);
            }
        }
    }

    public ActionBinding bind(final KeyStroke keyStroke, final Action action) {
        actions.put(keyStroke, action);
        return this;
    }

    public Optional<Action> resolve(final KeyStroke keyStroke) {
        return Optional.ofNullable(actions.get(keyStroke));
    }

}
