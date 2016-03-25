/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial;

import etomica.api.IAtomType;
import etomica.api.IMolecule;
import etomica.atom.Atom;
import etomica.atom.AtomLeafDynamic;
import etomica.atom.AtomTypeLeaf;
import etomica.atom.Molecule;
import etomica.chem.elements.Carbon;
import etomica.chem.elements.ElementSimple;
import etomica.space.ISpace;
import etomica.species.Species;

/**
 *  
 * Species Anthracene molecule
 * this is for TraPPE, the Anthracene is rigid , LJ potential, 10 interaction site
 * reference: TraPPE 4, UA description of linear and branched alkanes and alkylbenzenes, Siepmann
 * modified from  Species Anthracene molecule class
 * @author shu
 * March.19.2011
 *
 */
public class SpeciesTraPPEAnthracene extends Species {

    public SpeciesTraPPEAnthracene(ISpace space) {
        this(space, false);
    }
    
    public SpeciesTraPPEAnthracene(ISpace space, boolean isDynamic) {
        super();
        this.space = space;
        this.isDynamic = isDynamic;
        // no change for chType and cType, adopted from naphthalene totally
        chType = new AtomTypeLeaf(new ElementSimple("CH", 13.0107));
        cType = new AtomTypeLeaf(Carbon.INSTANCE);
        ////should change because it is not united atom!!!
        addChildType(chType);
        addChildType(cType);

        setConformation(new ConformationAnthraceneTraPPE(space)); 
     }

     public IMolecule makeMolecule() {
         Molecule Anthracene = new Molecule(this, 14);
         // 4 Carbon without H, 10 Carbon with H
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, cType) : new Atom(space, cType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, cType) : new Atom(space, cType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, cType) : new Atom(space, cType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, cType) : new Atom(space, cType));
         
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         Anthracene.addChildAtom(isDynamic ? new AtomLeafDynamic(space, chType) : new Atom(space, chType));
         
         conformation.initializePositions(Anthracene.getChildList());
         return Anthracene;
     }

     public IAtomType getCType() {
         return cType;
     }

     public IAtomType getCHType() {
         return chType;
     }


     public int getNumLeafAtoms() {
         return 14;
     }
    
    public final static int indexC1 = 0;
    public final static int indexC2 = 1;
    public final static int indexC3 = 2;
    public final static int indexC4 = 3;
    
    public final static int indexCH1 = 4;
    public final static int indexCH2 = 5;
    public final static int indexCH3 = 6;
    public final static int indexCH4 = 7;
    public final static int indexCH5 = 8;
    public final static int indexCH6 = 9;
    public final static int indexCH7 = 10;
    public final static int indexCH8 = 11;
    public final static int indexCH9 = 12;
    public final static int indexCH10 = 13;
    
    private static final long serialVersionUID = 1L;
    protected final ISpace space;
    protected final boolean isDynamic;
    protected final AtomTypeLeaf cType, chType;
}