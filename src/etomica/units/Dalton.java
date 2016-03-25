/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.units;

import java.io.ObjectStreamException;

public final class Dalton extends SimpleUnit {

    /**
     * Singleton instance of this unit.
     */
    public static final Dalton UNIT = new Dalton();

    private Dalton() {
        super(Mass.DIMENSION, 1.0, "daltons", "Da", Prefix.ALLOWED);
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