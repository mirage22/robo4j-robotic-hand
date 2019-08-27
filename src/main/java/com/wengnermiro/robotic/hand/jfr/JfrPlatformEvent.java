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

package com.wengnermiro.robotic.hand.jfr;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;

/**
 * JfrPlatformEvent jfr platform event
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@Category("RoboticArm-Demo")
@Label("JfrPlatformEvent")
@Description("Robotic Arm Platform movement event")
public class JfrPlatformEvent extends Event {

    @Label("source")
    private final String source;

    @Label("value")
    private final float value;

    @Label("Thread Name")
    private final String threadName;

    public JfrPlatformEvent(String source, float value, String threadName) {
        this.source = source;
        this.value = value;
        this.threadName = threadName;
    }
}
