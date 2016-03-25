/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.normalmode;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBoundary;
import etomica.api.IBox;
import etomica.api.IPotentialAtomic;
import etomica.api.IVectorMutable;
import etomica.atom.AtomArrayList;
import etomica.space.ISpace;

public class P1ConstraintNbrHcp implements IPotentialAtomic{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // this could take a NeighborListManager to try to speed up finding neighbors
    public P1ConstraintNbrHcp(ISpace space, double neighborDistance, IBox box) {
        boundary = box.getBoundary();

        neighborRadiusSq = neighborDistance*neighborDistance;

        IAtomList list = box.getLeafList();

        //Check for neighboring sites
        drj = space.makeVector();
        drk = space.makeVector();
        neighborAtoms = new int[list.getAtomCount()][12];
        AtomArrayList tmpList = new AtomArrayList(12);

        for (int i=0; i<list.getAtomCount(); i++) {
            IAtom atomi = list.getAtom(i);
            tmpList.clear();
            for (int j=0; j<list.getAtomCount(); j++) {
                if (i==j) continue;
                IAtom atomj = list.getAtom(j);
                drj.Ev1Mv2(atomi.getPosition(), atomj.getPosition());
                boundary.nearestImage(drj);
                if (drj.squared() < neighborRadiusSq*1.01) {
                    tmpList.add(atomj);
                }
            }
            for (int j=0; j<12; j++) {
                neighborAtoms[i][j] = tmpList.getAtom(j).getLeafIndex();
            }
        }
    }

    public int nBody() {
        return 1;
    }

    public double getRange() {
        return Double.POSITIVE_INFINITY;
    }

    public void setBox(IBox box) {
        leafList = box.getLeafList();
    }


    /**
     * Returns sum of energy for all triplets containing the given atom
     */
	public double energy(IAtomList atoms) {
	    IAtom atom = atoms.getAtom(0);
	    double u = energyi(atom);
	    if (u == Double.POSITIVE_INFINITY) {
	        return u;
	    }

	    return u;
	}

	/**
	 * Returns the energy for atom i due to requirements for i and its
	 * neighbors (but not for i as a neighbor)
	 */
	public double energyi(IAtom atom) {

	    IVectorMutable posAtom = atom.getPosition();

	    int atomIndex = atom.getLeafIndex();
	    int[] list = neighborAtoms[atomIndex];
	    for (int i=0; i<12; i++) {
	        IAtom atomj = leafList.getAtom(list[i]);
	        drj.Ev1Mv2(posAtom, atomj.getPosition());
	        boundary.nearestImage(drj);
	        if (drj.squared() > neighborRadiusSq*3.0) {
	        	p1Counter++;
	            return Double.POSITIVE_INFINITY;
	        }
	   
	    }
	    return 0;
	}
	
	public int getp1Counter(){
		return p1Counter;
	}
	

	protected final int[][] neighborAtoms;
	protected final IVectorMutable drj, drk;
	protected double neighborRadiusSq;
	protected final IBoundary boundary;
	protected IAtomList leafList;
	protected int p1Counter=0;
}
