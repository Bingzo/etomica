/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.atom;

import java.io.Serializable;

import etomica.api.IAtomList;
import etomica.api.IBoundary;
import etomica.api.IMolecule;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.space.ISpace;

/**
 * Calculates the geometric center over a set of atoms. The position of the
 * atom or child atoms are accumulated and used to compute their
 * center (unweighted by mass). Calculated center is obtained via the getPosition
 * method.
 * 
 * This implementation handles molecules that span the periodic boundaries.
 * The returned position will always be within the boundary.
 * 
 * @author David Kofke, Andrew Schultz
 */
public class AtomPositionGeometricCenterPBC implements IAtomPositionDefinition, Serializable {

    public AtomPositionGeometricCenterPBC(ISpace space, IBoundary boundary) {
        center = space.makeVector();
        dr = space.makeVector();
        this.boundary = boundary;
    }

    public IVector position(IMolecule atom) {
        center.E(0.0);
        IAtomList children = atom.getChildList();
        int nAtoms = children.getAtomCount();
        IVector pos0 = children.getAtom(0).getPosition();
        for (int i=0; i<nAtoms; i++) {
            dr.Ev1Mv2(children.getAtom(i).getPosition(), pos0);
            boundary.nearestImage(dr);
            center.PE(dr);
        }
        center.TE(1.0 / nAtoms);
        center.PE(pos0);
        center.ME(boundary.centralImage(center));
        return center;
    }

    private static final long serialVersionUID = 1L;
    protected final IVectorMutable center, dr;
    protected final IBoundary boundary;
}
 