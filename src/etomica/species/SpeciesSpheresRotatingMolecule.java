/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.species;

import etomica.api.IAtom;
import etomica.api.IAtomType;
import etomica.api.IMolecule;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.atom.Atom;
import etomica.atom.AtomTypeLeaf;
import etomica.atom.MoleculeOriented;
import etomica.atom.MoleculeOrientedDynamic;
import etomica.chem.elements.ElementSimple;
import etomica.simulation.Simulation;
import etomica.space.ISpace;

/**
 * Species in which molecules are made of a single atom.  The molecule itself
 * holds the orientation.
 *
 * @author Andrew Schultz
 */
public class SpeciesSpheresRotatingMolecule extends SpeciesSpheresMono implements ISpeciesOriented {
    
    public SpeciesSpheresRotatingMolecule(Simulation sim, ISpace _space) {
        this(sim, _space, makeNominalMoment(_space));
    }

    protected static final IVectorMutable makeNominalMoment(ISpace space) {
        IVectorMutable m = space.makeVector();
        m.E(1);
        return m;
    }

    public SpeciesSpheresRotatingMolecule(Simulation sim, ISpace _space, IVector moment) {
        this(_space, new AtomTypeLeaf(new ElementSimple(sim)), moment);
    }
    
    public SpeciesSpheresRotatingMolecule(ISpace _space, IAtomType atomType, IVector moment) {
        super(_space, atomType);
        this.moment = _space.makeVector();
        this.moment.E(moment);
    }

    /**
     * Constructs a new group.
     */
     public IMolecule makeMolecule() {
         MoleculeOriented group = isDynamic ? new MoleculeOrientedDynamic(space, this, 1)
                                            : new MoleculeOriented(space, this, 1);
         group.addChildAtom(makeLeafAtom());
         return group;
     }

    protected IAtom makeLeafAtom() {
        return new Atom(space, leafAtomType);
    }

    public double getMass() {
        return leafAtomType.getMass();
    }

    public IVector getMomentOfInertia() {
        return moment;
    }
    
    /**
     * Sets the species' moment of inertia to the given moment.
     */
    public void setMomentOfInertia(IVector newMoment) {
        moment.E(newMoment);
    }

    protected IVectorMutable moment;
    private static final long serialVersionUID = 1L;
}
