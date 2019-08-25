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
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.adafruitoled.BiColor;
import com.robo4j.hw.rpi.i2c.adafruitoled.PackElement;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.units.rpi.led.LEDBackpackMessage;
import com.robo4j.units.rpi.led.LEDBackpackMessageType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RemoteBargraphController send the proper information to the
 * {@link com.robo4j.units.rpi.led.Adafruit24BargraphUnit} by message {@link com.robo4j.units.rpi.led.LEDBackpackMessage}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RemoteBargraphController extends RoboUnit<Float> {

    public static final String NAME = "remoteBargraphController";

    public static final String ATTR_TARGET_SYSTEM = "targetSystem";
    public static final String ATTR_TARGET = "target";
    private static final LEDBackpackMessage clearMessage = new LEDBackpackMessage();
    private String targetSystem;
    private String target;

    private int bargraphCounter = 0;
    private boolean isIncrement = true;

    public RemoteBargraphController(RoboContext context, String id) {
        super(Float.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        targetSystem = configuration.getString(ATTR_TARGET_SYSTEM, null);
        target = configuration.getString(ATTR_TARGET, null);
    }

    @Override
    public void onMessage(Float message) {
        SimpleLoggingUtil.info(getClass(), "FLOAT MESSAGE:" + message + ", targetSystem:" + targetSystem + ", target:" + target);
        RoboContext remoteContext = LookupServiceProvider.getDefaultLookupService().getContext(targetSystem);
        if(remoteContext != null){
            RoboReference<LEDBackpackMessage> remoteUnit = remoteContext.getReference(target);
            if(remoteUnit != null){

                if(isIncrement) {
                    bargraphCounter++;
                }else {
                    bargraphCounter--;
                }

                remoteUnit.sendMessage(clearMessage);
                LEDBackpackMessage addMessage = new LEDBackpackMessage(LEDBackpackMessageType.DISPLAY);
                for(int i=0; i < bargraphCounter; i++){
                    PackElement element = new PackElement(i, BiColor.GREEN);
                    addMessage.addElement(element);
                }
                remoteUnit.sendMessage(addMessage);

                if(isIncrement && bargraphCounter >= 23){
                    bargraphCounter = 23;
                    isIncrement = false;
                }
                if(!isIncrement && bargraphCounter <= 0){
                    bargraphCounter=0;
                    isIncrement = true;
                }
            } else {
                SimpleLoggingUtil.error(getClass(), "remoteUnit: NULL, target:" + target);
            }
        } else {
            SimpleLoggingUtil.error(getClass(), "remoteContext: NULL, targetSystem:" + targetSystem);
        }
    }
}
