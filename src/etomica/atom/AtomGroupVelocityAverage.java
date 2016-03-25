/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.atom;

import java.io.Serializable;

import etomica.api.IAtom;
import etomica.api.IAtomKinetic;
import etomica.api.IAtomList;
import etomica.api.IMolecule;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.space.ISpace;

/**
 * Calculates the mass average velocity over a set of atoms. The velocity
 * and mass of the atom passed to getVelocityAverage all its child atoms 
 * are used to compute the mass-average velocity (total momentum divided by 
 * the total mass).
 * 
 * @author David Kofke
 */
public class AtomGroupVelocityAverage implements Serializable {

    public AtomGroupVelocityAverage(ISpace space) {
        vectorSum = space.makeVector();
    }
    
    /**
     * Returns the mass-average velocity of the given Atom and 
     * all its children.
     */
    public IVector getVelocityAverage(IMolecule molecule) {
        vectorSum.E(0.0);
        double massSum = 0;
        IAtomList children = molecule.getChildList();
        int nAtoms = children.getAtomCount();
        for (int i=0; i<nAtoms; i++) {
            IAtom a = children.getAtom(i);
            vectorSum.PE(((IAtomKinetic)a).getVelocity());
            massSum += a.getType().getMass();
        }
        vectorSum.TE(1.0 / massSum);
        return vectorSum;
    }

    private static final long serialVersionUID = 1L;
    private final IVectorMutable vectorSum;
}