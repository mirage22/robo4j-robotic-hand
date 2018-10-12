/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.wengnermiro.robotic.hand;

import com.robo4j.RoboApplication;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.util.SystemUtil;

import java.io.InputStream;

/**
 * RoboticHandMain
 *
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboticHandMain {

    public static void main(String[] args) throws Exception {
        SimpleLoggingUtil.info(RoboticHandMain.class, "... Robotic Hand ...");

        InputStream systemIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystem.xml");
        InputStream contextIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4j.xml");
        RoboBuilder builder = new RoboBuilder(systemIS);
        builder.add(contextIS);

        RoboContext system = builder.build();
        system.start();
        SimpleLoggingUtil.info(RoboApplication.class, SystemUtil.printStateReport(system));
        System.out.println("Press key...");
        System.in.read();
        system.shutdown();
        System.out.println("Bye!");


    }
}
