/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.units;

import java.io.ObjectStreamException;

/**
 * Decimal representation of something that represents the fractional 
 * amount of a whole (e.g., mole fraction) as a percentage value typically
 * between 0 and 100.
 */
public final class Percent extends SimpleUnit {

  /**
   * Singleton instance of this unit 
   */
	public static final Percent UNIT = new Percent();

	private Percent() {
       super(Fraction.DIMENSION,
        	0.01,
        	"Percent", "%", Prefix.NOT_ALLOWED
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
