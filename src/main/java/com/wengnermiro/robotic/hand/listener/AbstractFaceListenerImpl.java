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

package com.wengnermiro.robotic.hand.listener;

import com.robo4j.RoboContext;
import com.robo4j.hw.rpi.pad.LF710Input;
import com.wengnermiro.robotic.hand.unit.LedMatrixMessage;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class AbstractFaceListenerImpl implements ArmListener {
    final String name;
    final RoboContext context;
    final AtomicBoolean active = new AtomicBoolean();
    final LF710Input input;
    short amount;
    float value;

    AbstractFaceListenerImpl(String name, RoboContext context, LF710Input input) {
        this.name = name;
        this.context = context;
        this.input = input;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActive() {
        return active.get();
    }

    @Override
    public LF710Input getInput() {
        return input;
    }

    @Override
    public void setActive(boolean state) {
        active.set(state);
    }

    @Override
    public void setAmount(short amount) {
        this.amount = amount;
    }

    @Override
    public void setValue(float value) {
        this.value = value;
    }

    public abstract float process();
}
