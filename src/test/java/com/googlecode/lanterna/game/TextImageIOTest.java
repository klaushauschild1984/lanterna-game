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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.googlecode.lanterna.game.image.TextImageIO;
import com.googlecode.lanterna.graphics.TextImage;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class TextImageIOTest {

    @Test
    public void readTest() throws IOException {
        final TextImage textImage = TextImageIO.read(
                        new File("src/test/resources/com/googlecode/lanterna/game/image/image"));
        assertThat(textImage.getSize().getColumns(), is(2));
        assertThat(textImage.getSize().getRows(), is(4));
    }

}
