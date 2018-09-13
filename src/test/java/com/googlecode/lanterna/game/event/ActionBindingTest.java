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

package com.googlecode.lanterna.game.event;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

/**
 * @author Klaus Hauschild
 * @since 3.0.1
 */
public class ActionBindingTest {

    @Test
    public void fromTest() {
        final ActionBinding actionBinding =
                        ActionBinding.from(getClass().getResourceAsStream("binding.properties"));

        assertThat(actionBinding.resolve(new KeyStroke(KeyType.F5)).get().getClass().getName(),
                        is(ActionClass.class.getName()));
        assertThat(actionBinding.resolve(new KeyStroke(KeyType.Escape)).get(),
                        is(ActionEnum.EVENT));
    }

}
