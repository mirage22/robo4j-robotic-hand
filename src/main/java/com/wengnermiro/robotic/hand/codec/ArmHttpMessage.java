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

package com.wengnermiro.robotic.hand.codec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Miroslav Wengner (@miragemiko)
 */
public class ArmHttpMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ArmHttpCommand> commands = new ArrayList<>();

    public ArmHttpMessage() {
    }

    public ArmHttpMessage(List<ArmHttpCommand> commands) {
        this.commands = commands;
    }

    public List<ArmHttpCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<ArmHttpCommand> commands) {
        this.commands = commands;
    }

    @Override
    public String toString() {
        return "ArmHttpMessage{" +
                "commands=" + commands +
                '}';
    }
}
