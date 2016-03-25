/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.units;
import java.io.ObjectStreamException;

import etomica.util.Constants;

/**
 * The Joule unit of energy, equal to 1 N-m or 1 kg-m^2/s^2.
 */
public final class Joule extends SimpleUnit {

  /**
   * Singleton instance of this unit.
   */
    public static final Joule UNIT = new Joule();
    
    private Joule() {
        super(Energy.DIMENSION,
        	Constants.AVOGADRO*1000.*1e20*1e-24, //6.022e22; conversion from kg-m^2/s^2 to Dalton-A^2/ps^2
        	"joules", "J", Prefix.ALLOWED
        	);   
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