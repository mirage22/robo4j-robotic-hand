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
import com.robo4j.net.LookupService;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.util.SystemUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * RoboticArmMain
 *
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboticArmMain {

    public static void main(String[] args) throws Exception {
        SimpleLoggingUtil.info(RoboticArmMain.class, "... Robotic Arm ...");

        final InputStream systemIS;
        final InputStream contextIS;

        switch (args.length) {
            case 0:
                systemIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystem.xml");
                contextIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4j.xml");
                System.out.println("Default configuration used");
                break;
            case 1:
                systemIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystem.xml");
                Path contextPath = Paths.get(args[0]);
                contextIS = Files.newInputStream(contextPath);
                System.out.println("Robo4j config file has been used: " + args[0]);
                break;
            case 2:
                Path systemPath2 = Paths.get(args[0]);
                Path contextPath2 = Paths.get(args[1]);
                systemIS = Files.newInputStream(systemPath2);
                contextIS = Files.newInputStream(contextPath2);
                System.out.println(String.format("Custom configuration used system: %s, context: %s", args[0], args[1]));
                break;
            default:
                System.out.println("Could not find the *.xml settings for the CameraClient!");
                System.out.println("java -jar camera.jar system.xml context.xml");
                System.exit(2);
                throw new IllegalStateException("see configuration");
        }

        RoboBuilder builder = new RoboBuilder(systemIS);
        builder.add(contextIS);

        RoboContext system = builder.build();
        system.start();

        LookupService service = LookupServiceProvider.getDefaultLookupService();
        try {
            service.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleLoggingUtil.info(RoboApplication.class, SystemUtil.printStateReport(system));
        System.out.println("Press key...");
        System.in.read();
        service.stop();
        system.shutdown();
        System.out.println("Bye!");


    }
}
