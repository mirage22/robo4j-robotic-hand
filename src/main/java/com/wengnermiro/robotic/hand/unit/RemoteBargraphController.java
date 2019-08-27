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
import com.robo4j.hw.rpi.i2c.adafruitbackpack.BiColor;
import com.robo4j.hw.rpi.i2c.adafruitbackpack.PackElement;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.units.rpi.led.LEDBackpackMessage;
import com.robo4j.units.rpi.led.LEDBackpackMessageType;
import com.wengnermiro.robotic.hand.jfr.JfrBargraphEvent;

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
    private RoboContext remoteContext;

    private AtomicInteger bargraphCounter = new AtomicInteger(0);
    private AtomicBoolean isIncrement = new AtomicBoolean(true);

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
        RoboContext remoteContext = LookupServiceProvider.getDefaultLookupService().getContext(targetSystem);
        if(remoteContext != null){
            RoboReference<LEDBackpackMessage> remoteUnit = remoteContext.getReference(target);
            if(remoteUnit != null){

                if(isIncrement.get()) {
                    bargraphCounter.incrementAndGet();
                }else {
                    bargraphCounter.decrementAndGet();
                }

                remoteUnit.sendMessage(clearMessage);
                LEDBackpackMessage addMessage = new LEDBackpackMessage(LEDBackpackMessageType.DISPLAY);
                for(int i=0; i < bargraphCounter.get(); i++){
                    PackElement element = new PackElement(i, BiColor.GREEN);
                    addMessage.addElement(element);
                }
                emitJfrEvent(message, bargraphCounter.get(), isIncrement.get());
                remoteUnit.sendMessage(addMessage);

                if(isIncrement.get() && bargraphCounter.get() >= 23){
                    bargraphCounter.set(23);
                    isIncrement.set(false);
                }
                if(!isIncrement.get() && bargraphCounter.get() <= 0){
                    bargraphCounter.set(0);
                    isIncrement.set(true);
                }
            }
        }
    }

    private void emitJfrEvent(float amount, int bargraphCounter, boolean inc){
        JfrBargraphEvent event = new JfrBargraphEvent(amount,bargraphCounter,  inc, Thread.currentThread().getName());
        event.commit();
    }
}
