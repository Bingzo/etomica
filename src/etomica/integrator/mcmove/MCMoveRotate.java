/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.integrator.mcmove;

import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.atom.IAtomOriented;
import etomica.space.IOrientation;
import etomica.space.ISpace;
import etomica.space3d.Orientation3D;

/**
 * Performs a rotation of an atom (not a molecule) that has an orientation coordinate.
 */
public class MCMoveRotate extends MCMoveAtom {
    
    private static final long serialVersionUID = 2L;
    private final IOrientation oldOrientation;

    private transient IOrientation iOrientation;

    public MCMoveRotate(IPotentialMaster potentialMaster, IRandom random,
    		            ISpace _space) {
        super(potentialMaster, random, _space, Math.PI/2, Math.PI, false);
        //oldOrientation = _space.makeOrientation();
        oldOrientation = new Orientation3D(_space);
    }
    
    public boolean doTrial() {
        if(box.getMoleculeList().getMoleculeCount()==0) {return false;}
        atom = atomSource.getAtom();

        energyMeter.setTarget(atom);
        uOld = energyMeter.getDataAsScalar();
        iOrientation = ((IAtomOriented)atom).getOrientation(); 
        oldOrientation.E(iOrientation);  //save old orientation
        iOrientation.randomRotation(random, stepSize);
        uNew = energyMeter.getDataAsScalar();
        return true;
    }
    
    public void rejectNotify() {
        iOrientation.E(oldOrientation);
    }
}
