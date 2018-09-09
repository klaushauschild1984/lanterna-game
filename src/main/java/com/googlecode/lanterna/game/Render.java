package com.googlecode.lanterna.game;

import com.googlecode.lanterna.graphics.TextGraphics;

@FunctionalInterface
public interface Render {

    void render(TextGraphics textGraphics);

}
