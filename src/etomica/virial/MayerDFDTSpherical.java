/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial;

import etomica.api.IBox;
import etomica.api.IMoleculeList;
import etomica.api.IPotential;
import etomica.potential.Potential2Spherical;
/**
 * Required for computing temperature derivatives of virial coefficients
 * @author kate
 */
public class MayerDFDTSpherical implements MayerFunction {

	/**
	 * Constructor for MayerESpherical.
	 */
	public MayerDFDTSpherical(Potential2Spherical potential) {
		this.potential = potential;
	}

	/**
	 * @see etomica.virial.MayerFunctionSpherical#f(etomica.AtomPair, double, double)
	 */
	public double f(IMoleculeList pair, double r2, double beta) {
		double u = potential.u(r2);
		if (Double.isInfinite(u)) {
			return 0;
		}
		double dfdkT = Math.exp(-beta*u)*u*beta*beta;
		
		if (Double.isNaN(dfdkT)) {
			throw new RuntimeException ("dfdT is NaN");
		}
		return dfdkT;
	}
	
	public void setBox(IBox newBox) {
	    potential.setBox(newBox);
	}

	private final Potential2Spherical potential;

	public IPotential getPotential() {
		return potential;
	}

}
