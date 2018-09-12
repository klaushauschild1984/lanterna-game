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

package com.googlecode.lanterna.game.image;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.graphics.TextGraphics;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class TransparentTextImage extends BasicTextImage {

    public TransparentTextImage(final TerminalSize size) {
        super(size);
    }

    public void drawImageWithTransparency(final TextGraphics textGraphics, final TerminalPosition topLeft) {
        drawImageWithTransparency(textGraphics, topLeft, TerminalPosition.TOP_LEFT_CORNER, getSize());
    }

    public void drawImageWithTransparency(final TextGraphics textGraphics,
                                          TerminalPosition topLeft,
                                          TerminalPosition sourceImageTopLeft,
                                          TerminalSize sourceImageSize) {

        // If the source image position is negative, offset the whole image
        if (sourceImageTopLeft.getColumn() < 0) {
            topLeft = topLeft.withRelativeColumn(-sourceImageTopLeft.getColumn());
            sourceImageSize = sourceImageSize.withRelativeColumns(sourceImageTopLeft.getColumn());
            sourceImageTopLeft = sourceImageTopLeft.withColumn(0);
        }
        if (sourceImageTopLeft.getRow() < 0) {
            topLeft = topLeft.withRelativeRow(-sourceImageTopLeft.getRow());
            sourceImageSize = sourceImageSize.withRelativeRows(sourceImageTopLeft.getRow());
            sourceImageTopLeft = sourceImageTopLeft.withRow(0);
        }

        // cropping specified image-subrectangle to the image itself:
        int fromRow = Math.max(sourceImageTopLeft.getRow(), 0);
        int untilRow = Math.min(sourceImageTopLeft.getRow() + sourceImageSize.getRows(), getSize().getRows());
        int fromColumn = Math.max(sourceImageTopLeft.getColumn(), 0);
        int untilColumn = Math.min(sourceImageTopLeft.getColumn() + sourceImageSize.getColumns(), getSize().getColumns());

        // difference between position in image and position on target:
        int diffRow = topLeft.getRow() - sourceImageTopLeft.getRow();
        int diffColumn = topLeft.getColumn() - sourceImageTopLeft.getColumn();

        // top/left-crop at target(TextGraphics) rectangle: (only matters, if topLeft has a negative coordinate)
        fromRow = Math.max(fromRow, -diffRow);
        fromColumn = Math.max(fromColumn, -diffColumn);

        // bot/right-crop at target(TextGraphics) rectangle: (only matters, if topLeft has a negative coordinate)
        untilRow = Math.min(untilRow, textGraphics.getSize().getRows() - diffRow);
        untilColumn = Math.min(untilColumn, textGraphics.getSize().getColumns() - diffColumn);

        if (fromRow >= untilRow || fromColumn >= untilColumn) {
            return;
        }
        for (int row = fromRow; row < untilRow; row++) {
            for (int column = fromColumn; column < untilColumn; column++) {
                TextCharacter character = getCharacterAt(column, row);
                if (character instanceof TransparentTextCharacter) {
                    final TextColor backgroundColor = textGraphics.getCharacter(column + diffColumn, row + diffRow).getBackgroundColor();
                    character = new TextCharacter(character.getCharacter(), character.getForegroundColor(), backgroundColor);
                }
                textGraphics.setCharacter(column + diffColumn, row + diffRow, character);
            }
        }
    }

}
