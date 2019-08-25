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
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.pad.LF710Input;
import com.wengnermiro.robotic.hand.unit.RemoteBargraphController;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Miroslav Wengner (@miragemiko)
 */
public class ArmPlatformServoListenerImpl implements ArmListener {

    public static final short MAX_AMOUNT = 32767;

    private final String name;
    private final RoboContext context;
    private final AtomicBoolean active = new AtomicBoolean();
    private final LF710Input input;
    private float amount;
    private float value;
    private short absPos;
    private float servoStep;

    public ArmPlatformServoListenerImpl(String name, RoboContext context, LF710Input input,
                                        short absPos, float servoStep) {
        this.name = name;
        this.context = context;
        this.input = input;
        this.absPos = absPos;
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
            sendMessageToBarGraph(amount);
            short step = amount > 0 ? MAX_AMOUNT : -MAX_AMOUNT;
            value = normValue(value, step, absPos, servoStep);
            context.getReference(name).sendMessage(value);
        }
        return value;
    }

    private float normValue(float current, Short value, int absValue, float step) {
        float nexValue = current + (Float.valueOf(value) / absValue) * step;
        if (Math.abs(nexValue) > 1) {
            return Math.signum(nexValue);
        }
        return nexValue;
    }

    private void sendMessageToBarGraph(float  amount){
        RoboReference<Float> remoteBargraphController = context.getReference(RemoteBargraphController.NAME);
        if(remoteBargraphController != null){
            remoteBargraphController.sendMessage(amount);
        }
    }
}
