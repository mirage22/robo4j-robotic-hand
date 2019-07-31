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
import com.robo4j.hw.rpi.pad.LF710Input;
import com.robo4j.hw.rpi.pad.LF710JoystickButton;
import com.robo4j.hw.rpi.pad.LF710Message;
import com.robo4j.logging.SimpleLoggingUtil;
import com.wengnermiro.robotic.hand.listener.ArmGripperHeadServoListenerImpl;
import com.wengnermiro.robotic.hand.listener.ArmServoListener;
import com.wengnermiro.robotic.hand.listener.ArmPlatformServoListenerImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.robo4j.hw.rpi.pad.LF710Button.FRONT_DOWN_LEFT;
import static com.robo4j.hw.rpi.pad.LF710Button.FRONT_DOWN_RIGHT;
import static com.robo4j.hw.rpi.pad.LF710Button.FRONT_UP_LEFT;
import static com.robo4j.hw.rpi.pad.LF710Button.FRONT_UP_RIGHT;
import static com.robo4j.hw.rpi.pad.LF710JoystickButton.LEFT_X;
import static com.robo4j.hw.rpi.pad.LF710JoystickButton.LEFT_Y;
import static com.robo4j.hw.rpi.pad.LF710JoystickButton.PAD_X;
import static com.robo4j.hw.rpi.pad.LF710JoystickButton.PAD_Y;
import static com.robo4j.hw.rpi.pad.LF710JoystickButton.RIGHT_X;
import static com.wengnermiro.robotic.hand.unit.UnitsUtil.validateProperty;

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
    public static final String PROP_SERVO_ROTATION_HEAD_LEFT_RIGHT_STEP = "servoRotationHeadRightLeftStep";
    public static final String PROP_DELAY = "delay";
    public static final String PROP_TARGET_DISPLAY = "targetDisplay";
    public static final String PAD_INPUT_PRESSED = "pressed";
    public static final String PAD_INPUT_RELEASED = "released";

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, (r) -> {
        Thread t = new Thread(r, "ArmController Internal Executor");
        t.setDaemon(true);
        return t;
    });

    private final Map<LF710Input, ArmServoListener> listeners = new ConcurrentHashMap<>();
    private final AtomicReference<LF710Input> activeKey = new AtomicReference<>();
    private final Map<String, Float> currentServoValues = new ConcurrentHashMap<>();

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
    private String targetDisplay;
    private Float servoRotationStep;
    private Float servoRotationHeadRightLeftStep;
    private Float servoPlatformXStep;
    private Float servoPlatformYXStep;
    private long delay;

    public ArmController(RoboContext context, String id) {
        super(LF710Message.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        absRightJoystickPos = configuration.getInteger(PROP_ABS_RIGHT_JOYSTICK_POS, DEFAULT_JOYSTICK_POS).shortValue();
        absLeftJoystickPos = configuration.getInteger(PROP_ABS_LEFT_JOYSTICK_POS, DEFAULT_JOYSTICK_POS).shortValue();
        absPadJoystickPos = configuration.getInteger(PROP_ABS_PAD_JOYSTICK_POS, DEFAULT_JOYSTICK_POS).shortValue();

        // targetPlatformX
        targetPlatformX = configuration.getString(PROP_TARGET_PLATFORM_X, null);
        validateProperty(targetPlatformX, PROP_TARGET_PLATFORM_X);
        currentServoValues.put(targetPlatformX, 0F);

        //targetPlatformY
        targetPlatformY = configuration.getString(PROP_TARGET_PLATFORM_Y, null);
        validateProperty(targetPlatformY, PROP_TARGET_PLATFORM_Y);
        currentServoValues.put(targetPlatformY, 0F);

        //targetPlatformYX
        targetPlatformYX = configuration.getString(PROP_TARGET_PLATFORM_YX, null);
        validateProperty(targetPlatformYX, PROP_TARGET_PLATFORM_YX);
        currentServoValues.put(targetPlatformYX, 0F);

        //targetHeadX
        targetHeadX = configuration.getString(PROP_TARGET_HEAD_X, null);
        validateProperty(targetHeadX, PROP_TARGET_HEAD_X);
        currentServoValues.put(targetHeadX, 0F);

        //targetHeadY
        targetHeadY = configuration.getString(PROP_TARGET_HEAD_Y, null);
        validateProperty(targetHeadY, PROP_TARGET_HEAD_Y);
        currentServoValues.put(targetHeadY, 0F);

        //targetHeadRotation
        targetHeadRotation = configuration.getString(PROP_TARGET_HEAD_ROTATION, null);
        validateProperty(targetHeadRotation, PROP_TARGET_HEAD_ROTATION);
        currentServoValues.put(targetHeadRotation, 0F);

        //targetGripper
        targetGripper = configuration.getString(PROP_TARGET_GRIPPER, null);
        validateProperty(targetGripper, PROP_TARGET_GRIPPER);
        currentServoValues.put(targetGripper, 0F);

        targetDisplay = configuration.getString(PROP_TARGET_DISPLAY, null);
        validateProperty(targetDisplay, PROP_TARGET_DISPLAY);


        servoPlatformXStep = configuration.getFloat(PROP_SERVO_PLATFORM_X_STEP, null);
        validateProperty(servoPlatformXStep, PROP_SERVO_PLATFORM_X_STEP);
        servoPlatformYXStep = configuration.getFloat(PROP_SERVO_PLATFORM_YX_STEP, null);
        validateProperty(servoPlatformYXStep, PROP_SERVO_PLATFORM_YX_STEP);
        servoRotationStep = configuration.getFloat(PROP_SERVO_ROTATION_STEP, null);
        validateProperty(servoRotationStep, PROP_SERVO_ROTATION_STEP);
        servoRotationHeadRightLeftStep = configuration.getFloat(PROP_SERVO_ROTATION_HEAD_LEFT_RIGHT_STEP, null);
        validateProperty(servoRotationHeadRightLeftStep, PROP_SERVO_ROTATION_HEAD_LEFT_RIGHT_STEP);

        delay = configuration.getLong(PROP_DELAY, DEFAULT_DELAY_MILLS);
    }

    @Override
    public void start() {
        super.start();

        final ArmServoListener listenerPlatformX = createJoystickPadServoListener(targetPlatformX,
                RIGHT_X, absRightJoystickPos, servoPlatformXStep);
        listeners.put(listenerPlatformX.getInput(), listenerPlatformX);

        final ArmServoListener listenerPlatformYX = createJoystickPadServoListener(targetPlatformYX,
                LEFT_X, absLeftJoystickPos, servoPlatformYXStep);
        listeners.put(listenerPlatformYX.getInput(), listenerPlatformYX);

        final ArmServoListener listenerPlatformY = createJoystickPadServoListener(targetPlatformY,
                LEFT_Y, absLeftJoystickPos, servoPlatformYXStep);
        listeners.put(listenerPlatformY.getInput(), listenerPlatformY);

        //targetHeadX
        final ArmServoListener listenerHeadX = createJoystickPadServoListener(targetHeadX,
                PAD_X, absPadJoystickPos, servoRotationStep);
        listeners.put(listenerHeadX.getInput(), listenerHeadX);

        //targetHeadY
        final ArmServoListener listenerHeadY = createJoystickPadServoListener(targetHeadY,
                PAD_Y, absPadJoystickPos, servoRotationStep);
        listeners.put(listenerHeadY.getInput(), listenerHeadY);

        //targetHeadRotation FRONT_UP_RIGHT, FRONT_DOWN_RIGHT
        final ArmServoListener listenerHeadRotationLeft = createButtonServoListener(targetHeadRotation,
                FRONT_UP_RIGHT, true, servoRotationHeadRightLeftStep);
        listeners.put(listenerHeadRotationLeft.getInput(), listenerHeadRotationLeft);

        //targetHeadRotation FRONT_DOWN_RIGHT
        final ArmServoListener listenerHeadRotationRight = createButtonServoListener(targetHeadRotation,
                FRONT_DOWN_RIGHT, false, servoRotationHeadRightLeftStep);
        listeners.put(listenerHeadRotationRight.getInput(), listenerHeadRotationRight);

        //targetGripper FRONT_UP_LEFT
        final ArmServoListener listenerHeadGripperRotationClose = createButtonServoListener(targetGripper,
                FRONT_UP_LEFT, true, servoRotationHeadRightLeftStep);
        listeners.put(listenerHeadGripperRotationClose.getInput(), listenerHeadGripperRotationClose);
        //targetGripper FRONT_DOWN_LEFT
        final ArmServoListener listenerHeadGripperRotationOpen = createButtonServoListener(targetGripper,
                FRONT_DOWN_LEFT, false, servoRotationHeadRightLeftStep);
        listeners.put(listenerHeadGripperRotationOpen.getInput(), listenerHeadGripperRotationOpen);
        System.out.println("listeners ADDED");

        executor.scheduleAtFixedRate(() -> {
            for (ArmServoListener l : listeners.values()) {
                if (l.isActive()) {
                    float value = l.process();
                    currentServoValues.replace(l.getName(), value);
                }
            }
        }, 0, delay, TimeUnit.MILLISECONDS);

    }

    private ArmServoListener createJoystickPadServoListener(String id, LF710Input input, short absPos, float servoStep){
        return new ArmPlatformServoListenerImpl(id, getContext(),  input, absPos, servoStep);
    }

    private ArmServoListener createButtonServoListener(String id, LF710Input input, boolean positive, float servoStep){
        return new ArmGripperHeadServoListenerImpl(id, getContext(), input, positive, servoStep);
    }

    @Override
    public void onMessage(LF710Message message) {
        processPadMessage(message);
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
        System.out.println("processButton:" + message);

        switch (button) {
            case FRONT_UP_RIGHT:
            case FRONT_DOWN_RIGHT:
            case FRONT_UP_LEFT:
            case FRONT_DOWN_LEFT:
                if(listeners.containsKey(button)){
                    if (activeKey.get() == null && message.getState().getName().equals(PAD_INPUT_PRESSED)) {
                        System.out.println("currentHeadRotation PRESSED : " + button);
                        ArmServoListener listener = listeners.get(button);
                        listener.setAmount(message.getAmount());
                        listener.setValue(currentServoValues.get(listener.getName()));
                        listener.setActive(true);
                        activeKey.set(listener.getInput());
                    } else if (activeKey.get() != null && activeKey.get().equals(button) && message.getState().getName().equals(PAD_INPUT_RELEASED)) {
                        System.out.println("currentHeadRotation RELEASED : " + button);
                        ArmServoListener listenerPlatformX = listeners.get(button);
                        listenerPlatformX.setActive(false);
                        activeKey.set(null);
                    }
                }
                break;
            case BLUE:
                getContext().getReference(targetDisplay).sendMessage(LedMatrixMessage.FACE_NEUTRAL);
                break;
            case GREEN:
                getContext().getReference(targetDisplay).sendMessage(LedMatrixMessage.FACE_SMILE);
                break;
            case YELLOW:
                getContext().getReference(targetDisplay).sendMessage(LedMatrixMessage.FACE_SAD);
                break;
            default:
                SimpleLoggingUtil.info(getClass(), String.format("Button not implemented: %s", message));
        }
    }

    private void processJoystick(LF710Message message) {
        LF710JoystickButton joystick = (LF710JoystickButton) message.getInput();
        if(listeners.containsKey(joystick)){
            if (activeKey.get() == null && message.getState().getName().equals(PAD_INPUT_PRESSED)) {
                ArmServoListener listener = listeners.get(joystick);
                listener.setAmount(message.getAmount());
                listener.setActive(true);
                activeKey.set(listener.getInput());
            } else if (activeKey.get() != null && activeKey.get().equals(joystick) && message.getState().getName().equals(PAD_INPUT_RELEASED)) {
                ArmServoListener listenerPlatformX = listeners.get(joystick);
                listenerPlatformX.setActive(false);
                activeKey.set(null);
            }
        }
    }

    private float headValue(boolean positive, float currentValue, float step) {
        if (Math.abs(currentValue) > 1) {
            return Math.signum(currentValue);
        }
        return positive ? currentValue + step : currentValue - step;
    }

}
