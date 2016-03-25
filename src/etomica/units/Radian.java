/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.units;

import java.io.ObjectStreamException;

/**
 * Simulation unit for the measure of an angle.
 */
public final class Radian extends SimpleUnit {

    /**
     * Singleton instance of this unit.
     */
    public static final Radian UNIT = new Radian();

    private Radian() {
        super(Angle.DIMENSION, 1.0, "radians", "rad", Prefix.ALLOWED);
    }
    
    /**
     * Required to guarantee singleton when deserializing.
     * 
     * @return the singleton UNIT
     */
    private Object readResolve() throws ObjectStreamException {
        return UNIT;
    }
    
    private static final long serialVersionUID = 1;

}