/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.units;


/**
 * The Borh Radius unit of length, corresponding to the size of a Hydrogen
 * atom's electron cloud.
 */
public final class BohrRadius extends SimpleUnit {

    /**
     * Singleton instance of this unit.
     */
    public static final BohrRadius UNIT = new BohrRadius();
    
    private BohrRadius() {
    	    super(Length.DIMENSION,
    	            5.2917720859E-1, //conversion from Bohr Radii to Angstroms
    	            "Bohr Radii", "a0", Prefix.ALLOWED
    	    );
    }
    
    /**
     * Required to guarantee singleton when deserializing.
     * 
     * @return the singleton UNIT
     */
    private Object readResolve() {
        return UNIT;
    }
    
    private static final long serialVersionUID = 1;

}