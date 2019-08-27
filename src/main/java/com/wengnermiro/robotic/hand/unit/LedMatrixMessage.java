/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.wengnermiro.robotic.hand.unit;

import com.robo4j.hw.rpi.i2c.adafruitbackpack.LEDBackpackUtils;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.PackElement;

/**
 * LedMatrixMessage provides message that can be displayed on
 * Led Matrix
 *
 * @author Miroslav Wengner (@miragemiko)
 */
public enum LedMatrixMessage {

    //@formatter:off
    FACE_SMILE      ("00333300,03000030,30300303,30000003,30300303,30033003,03000030,00333300".toCharArray()),
    FACE_NEUTRAL    ("00222200,02000020,20200202,20000002,20222202,20000002,02000020,00222200".toCharArray() ),
    FACE_SAD        ("00111100,01000010,10100101,10000001,10011001,10100101,01000010,00111100".toCharArray())
    ;
    //@formatter:on

    private static final int MATRIX_SIZE = 8;
    private final PackElement[] elements;

    LedMatrixMessage(char[] elements) {
        this.elements = createArray(elements);
    }

    public PackElement[] getElements() {
        return elements;
    }

    private PackElement[] createArray(char[] array) {
        final byte[] byteArray = LEDBackpackUtils.createMatrixBiColorArrayByCharSequence(MATRIX_SIZE, ',',
                array);
        return LEDBackpackUtils.createMatrixByBiColorByteArray(MATRIX_SIZE, byteArray);
    }
}
