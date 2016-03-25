/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial;

import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IRandom;
import etomica.integrator.mcmove.MCMoveAtom;
import etomica.space.ISpace;


/**
 *  Overrides MCMoveAtom to prevent index-0 molecule from being displaced
 */
public class MCMoveClusterAtom extends MCMoveAtom {

    public MCMoveClusterAtom(IRandom random, ISpace _space) {
        super(random, null, _space);
	}
	
    public void setBox(IBox p) {
        super.setBox(p);
    }
    
	public boolean doTrial() {
        IAtomList leafList = box.getLeafList();
		atom = leafList.getAtom(random.nextInt(1+leafList.getAtomCount()-1));
        uOld = ((BoxCluster)box).getSampleCluster().value((BoxCluster)box);
        translationVector.setRandomCube(random);
        translationVector.TE(stepSize);
        atom.getPosition().PE(translationVector);
		((BoxCluster)box).trialNotify();
        uNew = ((BoxCluster)box).getSampleCluster().value((BoxCluster)box);
		return true;
	}
	
    public double getB() {
    	return 0;
    }
    
    public double getA() {
        return (uOld==0.0) ? Double.POSITIVE_INFINITY : uNew/uOld;
    }

    public void rejectNotify() {
    	super.rejectNotify();
    	((BoxCluster)box).rejectNotify();
    }
    
    public void acceptNotify() {
    	super.acceptNotify();
    	((BoxCluster)box).acceptNotify();
    }

    private static final long serialVersionUID = 1L;
}
