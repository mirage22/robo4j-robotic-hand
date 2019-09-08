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
import com.robo4j.hw.rpi.pad.LF710Button;
import com.robo4j.hw.rpi.pad.LF710JoystickButton;
import com.robo4j.hw.rpi.pad.LF710Message;
import com.robo4j.hw.rpi.pad.LF710Part;
import com.robo4j.hw.rpi.pad.LF710State;
import com.robo4j.logging.SimpleLoggingUtil;
import com.wengnermiro.robotic.hand.codec.ArmHttpCommand;
import com.wengnermiro.robotic.hand.codec.ArmHttpMessage;

import java.util.concurrent.TimeUnit;


/**
 * @author Miroslav Wengner (@miragemiko)
 */
public class ArmHttpController extends RoboUnit<ArmHttpMessage> {
    private static class CommandPair {
        private LF710Message start;
        private LF710Message stop;

        public CommandPair(ArmHttpCommand command) {
            createStartCommand(command);
        }

        private void createStartCommand(ArmHttpCommand command) {
            switch (command.getTarget()) {
                case "platformX":
                    start = new LF710Message(System.currentTimeMillis(), command.getAmount(),
                            LF710Part.JOYSTICK, LF710JoystickButton.RIGHT_X, LF710State.PRESSED);
                    stop = new LF710Message(System.currentTimeMillis(), (short) 0,
                            LF710Part.JOYSTICK, LF710JoystickButton.RIGHT_X, LF710State.RELEASED);
                    break;
                case "armYX":
                    start = new LF710Message(System.currentTimeMillis(), command.getAmount(),
                            LF710Part.JOYSTICK, LF710JoystickButton.LEFT_X, LF710State.PRESSED);
                    stop = new LF710Message(System.currentTimeMillis(), (short) 0,
                            LF710Part.JOYSTICK, LF710JoystickButton.LEFT_X, LF710State.RELEASED);
                    break;
                case "armY":
                    start = new LF710Message(System.currentTimeMillis(), command.getAmount(),
                            LF710Part.JOYSTICK, LF710JoystickButton.LEFT_Y, LF710State.PRESSED);
                    stop = new LF710Message(System.currentTimeMillis(), (short) 0,
                            LF710Part.JOYSTICK, LF710JoystickButton.LEFT_Y, LF710State.RELEASED);
                    break;
                case "headX":
                    start = new LF710Message(System.currentTimeMillis(), command.getAmount(),
                            LF710Part.JOYSTICK, LF710JoystickButton.PAD_X, LF710State.PRESSED);
                    stop = new LF710Message(System.currentTimeMillis(), (short) 0,
                            LF710Part.JOYSTICK, LF710JoystickButton.PAD_X, LF710State.RELEASED);
                    break;
                case "headY":
                    start = new LF710Message(System.currentTimeMillis(), command.getAmount(),
                            LF710Part.JOYSTICK, LF710JoystickButton.PAD_Y, LF710State.PRESSED);
                    stop = new LF710Message(System.currentTimeMillis(), (short) 0,
                            LF710Part.JOYSTICK, LF710JoystickButton.PAD_Y, LF710State.RELEASED);
                    break;

                case "headRotation":
                    LF710Button br = command.getAmount() > 0 ? LF710Button.FRONT_UP_RIGHT : LF710Button.FRONT_DOWN_RIGHT;
                    start = new LF710Message(System.currentTimeMillis(), command.getAmount(),
                            LF710Part.BUTTON, br, LF710State.PRESSED);
                    stop = new LF710Message(System.currentTimeMillis(), command.getAmount(),
                            LF710Part.BUTTON, br, LF710State.RELEASED);
                    break;


            }
        }

    }

    public static final String NAME = "armHttpController";
    public static final String ATTR_TARGET = "target";
    

    private String target;

    public ArmHttpController(RoboContext context, String id) {
        super(ArmHttpMessage.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(ATTR_TARGET, null);
        if (target == null) {
            throw new ConfigurationException(ATTR_TARGET);
        }
    }

    @Override
    public void onMessage(ArmHttpMessage message) {
        System.out.println("RECEIVED MESSAGE:" + message + "target: " + target);

        getContext().getScheduler().execute(() -> {
            for (ArmHttpCommand command : message.getCommands()) {
                CommandPair pair = new CommandPair(command);
                try {
                    getContext().getReference(target).sendMessage(pair.start);
                    commandExecution(command.getAmount());
                } finally {
                    getContext().getReference(target).sendMessage(pair.stop);
                }
                commandExecution(command.getAmount());
            }
        });
    }

    private void commandExecution(short amount) {
        try {
            TimeUnit.MILLISECONDS.sleep(Math.abs(amount));
        } catch (InterruptedException e) {
            SimpleLoggingUtil.error(getClass(), e.getMessage());
        }
    }

}
