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

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.units.rpi.led.LEDBackpackMessage;
import com.robo4j.units.rpi.led.LEDBackpackMessageType;

import java.util.Arrays;

import static com.wengnermiro.robotic.hand.unit.UnitsUtil.validateProperty;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LedMatrixController extends RoboUnit<LedMatrixMessage> {

    public static final String TARGET = "target";
    private final LEDBackpackMessage clearMessage = new LEDBackpackMessage();
    private String target;

    public LedMatrixController(RoboContext context, String id) {
        super(LedMatrixMessage.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(TARGET, null);
        validateProperty(target, TARGET);
    }

    @Override
    public void onMessage(LedMatrixMessage message) {
        getContext().getReference(target).sendMessage(clearMessage);
        LEDBackpackMessage addMessage = new LEDBackpackMessage(LEDBackpackMessageType.DISPLAY);
        addMessage.setElements(Arrays.asList(message.getElements()));
        getContext().getReference(target).sendMessage(addMessage);
    }
}
