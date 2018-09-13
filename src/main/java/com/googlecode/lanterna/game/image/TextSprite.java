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

package com.googlecode.lanterna.game.image;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import com.google.gson.Gson;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.game.Render;
import com.googlecode.lanterna.game.Update;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class TextSprite implements Update, Render {

    private static final Gson GSON = new Gson();

    private final List<State> states;
    private State currentState;

    public TextSprite(final List<State> states) {
        this.states = states;
        currentState = this.states.get(0);
    }

    public static TextSprite read(final Resource spriteResource) {
        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> definition = GSON.fromJson(
                            new InputStreamReader(spriteResource.getInputStream()), Map.class);
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> stateDefinitions =
                            (List<Map<String, Object>>) definition.get("states");
            final List<State> states = stateDefinitions.stream() //
                            .map(stateDefinition -> {
                                try {
                                    final String name = stateDefinition.get("name").toString();
                                    final Object imageOrAnimation;
                                    final Object imageDefinition = stateDefinition.get("image");
                                    final Object animationDefinition =
                                                    stateDefinition.get("animation");
                                    if (imageDefinition != null) {
                                        imageOrAnimation = TextImageIO.read(spriteResource
                                                        .createRelative(imageDefinition
                                                                        .toString()));
                                    } else if (animationDefinition != null) {
                                        imageOrAnimation = TextAnimation.read(spriteResource
                                                        .createRelative(animationDefinition
                                                                        .toString()));
                                    } else {
                                        throw new IllegalArgumentException();
                                    }
                                    return new State(name, imageOrAnimation);
                                } catch (final IOException exception) {
                                    throw new RuntimeException(exception);
                                }
                            }) //
                            .collect(Collectors.toList());
            return new TextSprite(states);
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void render(final TextGraphics textGraphics) {
        // TODO respect postion

        currentImage() //
                        .ifPresent(image -> ((TransparentTextImage) image)
                                        .drawImageWithTransparency(textGraphics,
                                                        TerminalPosition.TOP_LEFT_CORNER));
        currentAnimation() //
                        .ifPresent(animation -> animation.render(textGraphics));
    }

    @Override
    public void update(final float elapsed) {
        currentAnimation() //
                        .ifPresent(animation -> animation.update(elapsed));
    }

    public void updateState(final String name) {
        currentState = states.stream() //
                        .filter(state -> Objects.equals(state.getKey(), name)) //
                        .findFirst() //
                        .orElse(currentState);
    }

    private Optional<TextImage> currentImage() {
        final Object imageOrAnimation = currentState.getValue();
        if (imageOrAnimation instanceof TextImage) {
            return Optional.of((TextImage) imageOrAnimation);
        }
        return Optional.empty();
    }

    private Optional<TextAnimation> currentAnimation() {
        final Object imageOrAnimation = currentState.getValue();
        if (imageOrAnimation instanceof TextAnimation) {
            return Optional.of((TextAnimation) imageOrAnimation);
        }
        return Optional.empty();
    }

    private static class State extends SimpleEntry<String, Object> {

        private State(final String name, final Object imageOrAnimation) {
            super(name, imageOrAnimation);
        }
    }

}
