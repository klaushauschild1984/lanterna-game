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
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import com.google.gson.Gson;
import com.googlecode.lanterna.game.Update;
import com.googlecode.lanterna.graphics.TextImage;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class TextAnimation implements Update {

    private static final Gson GSON = new Gson();

    private final List<Frame> frames;
    private final Type type;

    private Frame currentFrame;
    private float animationTime = 0;
    private boolean running = false;

    private TextAnimation(final List<Frame> frames, final Type type) {
        this.frames = frames;
        this.type = type;
        stop();
    }

    public static TextAnimation read(final Resource animationResource) {
        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> definition = GSON.fromJson(
                            new InputStreamReader(animationResource.getInputStream()), Map.class);
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> frameDefinitions =
                            (List<Map<String, Object>>) definition.get("frames");
            @SuppressWarnings("unchecked")
            final List<Frame> frames = frameDefinitions.stream() //
                            .map(frameDefinition -> {
                                try {
                                    final Resource imageResource = animationResource.createRelative(
                                                    frameDefinition.get("image").toString());
                                    final TextImage image = TextImageIO.read(imageResource);
                                    final Float time = Float.parseFloat(
                                                    frameDefinition.get("time").toString());
                                    return new Frame(image, time);
                                } catch (IOException exception) {
                                    throw new RuntimeException(exception);
                                }
                            }) //
                            .collect(Collectors.toList());
            final Type type = Type.fromString(definition.get("type").toString());
            return new TextAnimation(frames, type);
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void start() {
        stop();
        resume();
    }

    public void pause() {
        running = false;
    }

    public void resume() {
        running = true;
    }

    public void stop() {
        currentFrame = frames.get(0);
        animationTime = 0;
        pause();
    }

    @Override
    public void update(final float elapsed) {
        animationTime += elapsed;
        final Optional<Frame> frameOptional = frames.stream() //
                        .filter(frame -> frame.getValue() > animationTime) //
                        .findFirst();
        if (frameOptional.isPresent()) {
            currentFrame = frameOptional.get();
        } else {
            switch (type) {
                case FORWARD:
                    pause();
                    break;
                case LOOP:
                    start();
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    public enum Type {

        FORWARD,

        LOOP,

        ;

        public static Type fromString(final String value) {
            try {
                return valueOf(value);
            } catch (final IllegalArgumentException exception) {
                return FORWARD;
            }
        }

    }

    private static class Frame extends SimpleEntry<TextImage, Float> {

        private Frame(final TextImage key, final Float value) {
            super(key, value);
        }

    }

}
