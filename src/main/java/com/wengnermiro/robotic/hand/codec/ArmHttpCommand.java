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

/**
 * @author Miroslav Wengner (@miragemiko)
 */
public class ArmHttpCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private String target;
    private Short amount;

    public ArmHttpCommand() {
    }

    public ArmHttpCommand(String target, Short amount) {
        this.target = target;
        this.amount = amount;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Short getAmount() {
        return amount;
    }

    public void setAmount(Short amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "ArmHttpCommand{" +
                "target='" + target + '\'' +
                ", amount=" + amount +
                '}';
    }
}
