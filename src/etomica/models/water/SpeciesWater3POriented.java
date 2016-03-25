/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.models.water;

import etomica.api.IAtomType;
import etomica.api.IMolecule;
import etomica.atom.Atom;
import etomica.atom.AtomTypeLeaf;
import etomica.atom.MoleculeOriented;
import etomica.atom.MoleculeOrientedDynamic;
import etomica.chem.elements.Hydrogen;
import etomica.chem.elements.Oxygen;
import etomica.space.ISpace;
import etomica.species.SpeciesOriented;

public class SpeciesWater3POriented extends SpeciesOriented {

    public SpeciesWater3POriented(ISpace space, boolean isDynamic) {
        super(space);
        this.isDynamic = isDynamic;
        this.space = space;
        hType = new AtomTypeLeaf(Hydrogen.INSTANCE);
        oType = new AtomTypeLeaf(Oxygen.INSTANCE);
        addChildType(hType);
        addChildType(oType);

        setConformation(new ConformationWater3P(space));
        init();
    }
    
    public IMolecule makeMolecule() {
        MoleculeOriented water = isDynamic ? new MoleculeOrientedDynamic(space, this, 3) :
                                             new MoleculeOriented(space, this, 3);
        water.addChildAtom(new Atom(space, hType));
        water.addChildAtom(new Atom(space, hType));
        water.addChildAtom(new Atom(space, oType));
        conformation.initializePositions(water.getChildList());
        return water;
    }

    public IAtomType getHydrogenType() {
        return hType;
    }
    
    public IAtomType getOxygenType() {
        return oType;
    }

    public int getNumLeafAtoms() {
        return 3;
    }
    
    public final static int indexH1 = 0;
    public final static int indexH2 = 1;
    public final static int indexO  = 2;

    private static final long serialVersionUID = 1L;
    protected final ISpace space;
    protected final AtomTypeLeaf oType, hType;
    protected final boolean isDynamic;
}
