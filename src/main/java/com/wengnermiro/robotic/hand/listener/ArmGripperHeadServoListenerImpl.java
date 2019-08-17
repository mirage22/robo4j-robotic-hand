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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Miroslav Wengner (@miragemiko)
 */
public class ArmGripperHeadServoListenerImpl implements ArmListener {

    private final String name;
    private final RoboContext context;
    private final AtomicBoolean active = new AtomicBoolean();
    private final LF710Input input;
    private final boolean positive;
    private short amount;
    private float value;
    private float servoStep;

    public ArmGripperHeadServoListenerImpl(String name, RoboContext context, LF710Input input,
                                           boolean positive, float servoStep) {
        this.name = name;
        this.context = context;
        this.input = input;
        this.positive = positive;
        this.servoStep = servoStep;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LF710Input getInput() {
        return input;
    }

    @Override
    public boolean isActive() {
        return active.get();
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

    @Override
    public float process() {
        if (active.get()) {
            value = headValue(positive, value, servoStep);
            context.getReference(name).sendMessage(value);
            System.out.println("LISTENER ArmGripperServoListenerImpl value : " + value);
        }
        return value;
    }

    private float headValue(boolean positive, float currentValue, float step) {
        float result = positive ? currentValue + step : currentValue - step;
        if (Math.abs(result) > 1) {
            result = Math.signum(result);
        }
        return result;
    }
}
