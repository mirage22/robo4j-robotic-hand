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

package com.wengnermiro.robotic.hand.unit;

import com.robo4j.ConfigurationException;
import com.robo4j.CriticalSectionTrait;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.pad.LF710Button;
import com.robo4j.hw.rpi.pad.LF710JoystickButton;
import com.robo4j.hw.rpi.pad.LF710Message;
import com.robo4j.logging.SimpleLoggingUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ArmController reacts on event produced by {@link com.robo4j.units.rpi.pad.LF710PadUnit}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@CriticalSectionTrait
public class ArmController extends RoboUnit<LF710Message> {

    private static final int DEFAULT_JOYSTICK_POS = 32767;
    private static final long DEFAULT_DELAY_MILLS = 200;
    public static final String PROP_ABS_RIGHT_JOYSTICK_POS = "absRightJoystickPos";
    public static final String PROP_ABS_LEFT_JOYSTICK_POS = "absLeftJoystickPos";
    public static final String PROP_ABS_PAD_JOYSTICK_POS = "absPadJoystickPos";
    public static final String PROP_TARGET_PLATFORM_X = "targetPlatformX";
    public static final String PROP_SERVO_PLATFORM_X_STEP = "servoPlatformXStep";
    public static final String PROP_TARGET_PLATFORM_Y = "targetPlatformY";
    public static final String PROP_TARGET_PLATFORM_YX = "targetPlatformYX";
    public static final String PROP_SERVO_PLATFORM_YX_STEP = "servoPlatformYXStep";
    public static final String PROP_TARGET_HEAD_X = "targetHeadX";
    public static final String PROP_TARGET_HEAD_Y = "targetHeadY";
    public static final String PROP_TARGET_HEAD_ROTATION = "targetHeadRotation";
    public static final String PROP_TARGET_GRIPPER = "targetGripper";
    public static final String PROP_SERVO_ROTATION_STEP = "servoRotationStep";
    public static final String PROP_DELAY = "delay";

    private Short absRightJoystickPos;
    private Short absLeftJoystickPos;
    private Short absPadJoystickPos;

    private String targetPlatformX;
    private String targetPlatformY;
    private String targetPlatformYX;
    private String targetHeadX;
    private String targetHeadY;
    private String targetHeadRotation;
    private String targetGripper;
    private Float servoRotationStep;
    private Float servoPlatformXStep;
    private Float servoPlatformYXStep;
    private float currentHeadRotation = 0;
    private float currentGripperRotation = 0;
    private float currentPlatformX = 0;
    private float currentPlatformYX = 0;
    private float currentPlatformY = 0;
    private float currentHeadX = 0;
    private float currentHeadY = 0;
    private long delay;

    private volatile AtomicBoolean notActive = new AtomicBoolean(true);

    public ArmController(RoboContext context, String id) {
        super(LF710Message.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        absRightJoystickPos = configuration.getInteger(PROP_ABS_RIGHT_JOYSTICK_POS, DEFAULT_JOYSTICK_POS).shortValue();
        absLeftJoystickPos = configuration.getInteger(PROP_ABS_LEFT_JOYSTICK_POS, DEFAULT_JOYSTICK_POS).shortValue();
        absPadJoystickPos = configuration.getInteger(PROP_ABS_PAD_JOYSTICK_POS, DEFAULT_JOYSTICK_POS).shortValue();

        targetPlatformX = configuration.getString(PROP_TARGET_PLATFORM_X, null);
        validateProperty(targetPlatformX, PROP_TARGET_PLATFORM_X);

        servoPlatformXStep = configuration.getFloat(PROP_SERVO_PLATFORM_X_STEP, null);
        validateProperty(servoPlatformXStep, PROP_SERVO_PLATFORM_X_STEP);


        targetPlatformY = configuration.getString(PROP_TARGET_PLATFORM_Y, null);
        validateProperty(targetPlatformY, PROP_TARGET_PLATFORM_Y);

        targetPlatformYX = configuration.getString(PROP_TARGET_PLATFORM_YX, null);
        validateProperty(targetPlatformYX, PROP_TARGET_PLATFORM_YX);

        servoPlatformYXStep = configuration.getFloat(PROP_SERVO_PLATFORM_YX_STEP, null);
        validateProperty(servoPlatformYXStep, PROP_SERVO_PLATFORM_YX_STEP);

        targetHeadX = configuration.getString(PROP_TARGET_HEAD_X, null);
        validateProperty(targetHeadX, PROP_TARGET_HEAD_X);

        targetHeadY = configuration.getString(PROP_TARGET_HEAD_Y, null);
        validateProperty(targetHeadY, PROP_TARGET_HEAD_Y);

        targetHeadRotation = configuration.getString(PROP_TARGET_HEAD_ROTATION, null);
        validateProperty(targetHeadRotation, PROP_TARGET_HEAD_ROTATION);


        servoRotationStep = configuration.getFloat(PROP_SERVO_ROTATION_STEP, null);
        validateProperty(servoRotationStep, PROP_SERVO_ROTATION_STEP);

        targetGripper = configuration.getString(PROP_TARGET_GRIPPER, null);
        validateProperty(targetGripper, PROP_TARGET_GRIPPER);

        delay = configuration.getLong(PROP_DELAY, DEFAULT_DELAY_MILLS);
    }

    @Override
    public void onMessage(LF710Message message) {
        if (notActive.get()) {
            getContext().getScheduler().schedule(() -> processPadMessage(message), delay, TimeUnit.MILLISECONDS);
        }

    }

    private void processPadMessage(LF710Message message) {
        switch (message.getPart()) {
            case BUTTON:
                processButton(message);
                break;
            case JOYSTICK:
                processJoystick(message);
                break;
            default:
                SimpleLoggingUtil.error(getClass(), String.format("part is not available: %s", message));
        }
    }

    private void processButton(LF710Message message) {
        LF710Button button = (LF710Button) message.getInput();
        switch (button) {
            case FRONT_UP_RIGHT:
                currentHeadRotation = headValue(true, currentHeadRotation, servoRotationStep);
                getContext().getReference(targetHeadRotation).sendMessage(currentHeadRotation);
                break;
            case FRONT_DOWN_RIGHT:
                currentHeadRotation = headValue(false, currentHeadRotation, servoRotationStep);
                getContext().getReference(targetHeadRotation).sendMessage(currentHeadRotation);
                break;
            case FRONT_UP_LEFT:
                currentGripperRotation = headValue(true, currentGripperRotation, servoRotationStep);
                getContext().getReference(targetGripper).sendMessage(currentGripperRotation);
                break;
            case FRONT_DOWN_LEFT:
                currentGripperRotation = headValue(false, currentGripperRotation, servoRotationStep);
                getContext().getReference(targetGripper).sendMessage(currentGripperRotation);
                break;
            default:
                SimpleLoggingUtil.info(getClass(), String.format("Button not implemented: %s", message));
        }
    }

    private void processJoystick(LF710Message message) {
        LF710JoystickButton joystick = (LF710JoystickButton) message.getInput();
        switch (joystick) {
            case RIGHT_X:
                currentPlatformX = normValue(currentPlatformX, message.getAmount(), absRightJoystickPos, servoPlatformXStep);
                getContext().getReference(targetPlatformX).sendMessage(currentPlatformX);
                break;
            case RIGHT_Y:
                // not necessary
                break;
            case LEFT_X:
                currentPlatformYX = normValue(currentPlatformYX, message.getAmount(), absLeftJoystickPos, servoPlatformYXStep);
                getContext().getReference(targetPlatformYX).sendMessage(currentPlatformYX);
                break;
            case LEFT_Y:
                currentPlatformY = normValue(currentPlatformY, message.getAmount(), absLeftJoystickPos, servoPlatformYXStep);
                getContext().getReference(targetPlatformY).sendMessage(currentPlatformY);
                break;
            case PAD_X:
                currentHeadX = normValue(currentHeadX, message.getAmount(), absPadJoystickPos, servoRotationStep);
                getContext().getReference(targetHeadX).sendMessage(currentHeadX);
                break;
            case PAD_Y:
                currentHeadY = normValue(currentHeadY, message.getAmount(), absPadJoystickPos, servoRotationStep);
                getContext().getReference(targetHeadY).sendMessage(currentHeadY);
                break;
            default:
                SimpleLoggingUtil.error(getClass(), String.format("joystick not available: %s", message));
        }
    }

    private float normValue(float current, Short value, int absValue, float step) {

        float nexValue = current + (Float.valueOf(value) / absValue) * step;
        if (Math.abs(nexValue) > 1) {
            return Math.signum(nexValue);
        }

        return nexValue;
    }

    private float headValue(boolean positive, float currentValue, float step) {
        if (Math.abs(currentValue) > 1) {
            return Math.signum(currentValue);
        }
        return positive ? currentValue + step : currentValue - step;
    }

    private void validateProperty(Object property, String name) throws ConfigurationException {
        if (property == null) {
            throw ConfigurationException.createMissingConfigNameException(name);
        }
    }
}
