/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.potential;

import etomica.api.IAtomList;
import etomica.api.IPotentialAtomic;
import etomica.api.IVector;
import etomica.integrator.IntegratorBox;
import etomica.space.ISpace;
import etomica.space.Tensor;

/**
 * Sums the force on each iterated atom and adds it to the integrator agent
 * associated with the atom.  Additionally, this class has the potential
 * calculate the pressureTensor (which can be done efficiently during the
 * gradient calculation).
 */
public class PotentialCalculationForcePressureSum extends PotentialCalculationForceSum {
        
    private static final long serialVersionUID = 1L;
    protected final Tensor pressureTensor;
    
    public PotentialCalculationForcePressureSum(ISpace space) {
        pressureTensor = space.makeTensor();
    }
    
    /**
     * Zeros out the pressureTensor.  This method should be called before
     * invoking potentialMaster.calculate so that the pressureTensor is
     * correct at the end of the calculation.
     */
    public void reset() {
        super.reset();
        pressureTensor.E(0);
    }
    
    /**
	 * Adds forces due to given potential acting on the atoms produced by the iterator.
	 * Implemented for only 1- and 2-body potentials.
	 */
	public void doCalculation(IAtomList atoms, IPotentialAtomic potential) {
		PotentialSoft potentialSoft = (PotentialSoft)potential;
		int nBody = potential.nBody();
		IVector[] f = potentialSoft.gradient(atoms, pressureTensor);
		switch(nBody) {
			case 1:
				((IntegratorBox.Forcible)integratorAgentManager.getAgent(atoms.getAtom(0))).force().ME(f[0]);
				break;
			case 2:
                ((IntegratorBox.Forcible)integratorAgentManager.getAgent(atoms.getAtom(0))).force().ME(f[0]);
                ((IntegratorBox.Forcible)integratorAgentManager.getAgent(atoms.getAtom(1))).force().ME(f[1]);
		 		break;
            default:
                //XXX atoms.count might not equal f.length.  The potential might size its 
                //array of vectors to be large enough for one AtomSet and then not resize it
                //back down for another AtomSet with fewer atoms.
                for (int i=0; i<atoms.getAtomCount(); i++) {
                    ((IntegratorBox.Forcible)integratorAgentManager.getAgent(atoms.getAtom(i))).force().ME(f[i]);
                }
		}
	}

    /**
     * Returns the virial portion of pressure tensor calculated during the last
     * potential calculation.  In order to be valid, reset() must be called
     * before invoking potentialMaster.calculate.  The given tensor has not
     * been normalized by the system volume.
     */
    public Tensor getPressureTensor() {
        return pressureTensor;
    }
}
