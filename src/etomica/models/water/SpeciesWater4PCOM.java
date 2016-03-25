/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.models.water;

import etomica.api.IAtomType;
import etomica.api.IMolecule;
import etomica.atom.Atom;
import etomica.atom.AtomLeafDynamic;
import etomica.atom.AtomTypeLeaf;
import etomica.atom.Molecule;
import etomica.chem.elements.ElementSimple;
import etomica.chem.elements.Hydrogen;
import etomica.chem.elements.Oxygen;
import etomica.space.ISpace;
import etomica.species.Species;

/**
 * Species for 4-point water molecule.  This species has an explicit site for
 * the center of mass.
 */
public class SpeciesWater4PCOM extends Species {

    public SpeciesWater4PCOM(ISpace space) {
        this(space, false);
    }
    
    public SpeciesWater4PCOM(ISpace space, boolean isDynamic) {
        super();
        this.space = space;
        this.isDynamic = isDynamic;
        hType = new AtomTypeLeaf(Hydrogen.INSTANCE);
        oType = new AtomTypeLeaf(Oxygen.INSTANCE);
        mType = new AtomTypeLeaf(new ElementSimple("M", 0.0));
        cType = new AtomTypeLeaf(new ElementSimple("COM", 0.0));
        addChildType(hType);
        addChildType(oType);
        addChildType(mType);
        addChildType(cType);

        setConformation(new ConformationWaterGCPMCOM(space)); 
     }

     public IMolecule makeMolecule() {
         Molecule water = new Molecule(this, 4);
         water.addChildAtom(isDynamic ? new AtomLeafDynamic(space, hType) : new Atom(space, hType));
         water.addChildAtom(isDynamic ? new AtomLeafDynamic(space, hType) : new Atom(space, hType));
         water.addChildAtom(isDynamic ? new AtomLeafDynamic(space, oType) : new Atom(space, oType));
         water.addChildAtom(isDynamic ? new AtomLeafDynamic(space, mType) : new Atom(space, mType));
         water.addChildAtom(isDynamic ? new AtomLeafDynamic(space, cType) : new Atom(space, cType));
         conformation.initializePositions(water.getChildList());
         return water;
     }

     public IAtomType getHydrogenType() {
         return hType;
     }

     public IAtomType getOxygenType() {
         return oType;
     }

     public IAtomType getMType() {
         return mType;
     }

     public IAtomType getCOMType() {
         return cType;
     }

     public int getNumLeafAtoms() {
         return 4;
     }
    
    public final static int indexH1 = 0;
    public final static int indexH2 = 1;
    public final static int indexO  = 2;
    public final static int indexM  = 3;
    public final static int indexC  = 4;

    protected final ISpace space;
    protected final boolean isDynamic;
    protected final AtomTypeLeaf oType, hType, mType, cType;
}