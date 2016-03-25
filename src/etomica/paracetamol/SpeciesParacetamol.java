/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.paracetamol;

import etomica.api.IAtom;
import etomica.api.IAtomType;
import etomica.api.IAtomType;
import etomica.api.IMolecule;
import etomica.atom.Atom;
import etomica.atom.AtomLeafDynamic;
import etomica.atom.AtomTypeLeaf;
import etomica.atom.Molecule;
import etomica.chem.elements.Carbon;
import etomica.chem.elements.Hydrogen;
import etomica.chem.elements.Nitrogen;
import etomica.chem.elements.Oxygen;
import etomica.space.ISpace;
import etomica.species.Species;
import etomica.units.ElectronVolt;

public class SpeciesParacetamol extends Species {

	public SpeciesParacetamol(ISpace _space, boolean isDynamic) {
		super();
		space = _space;
		this.isDynamic = isDynamic;
        
        //atomic Instance Class, atomic diameter
        //L. Pauling, The Nature of the Chemical Bond, Cornell University Press, USA, 1945.
        //1.7, 1.55, 1.52, 1.2
        oType = new AtomTypeLeaf(Oxygen.INSTANCE);
        addChildType(oType);
        cType = new AtomTypeLeaf(Carbon.INSTANCE);
        addChildType(cType);
        nType = new AtomTypeLeaf(Nitrogen.INSTANCE);
        addChildType(nType);
        hpType = new AtomTypeLeaf(HydrogenP.INSTANCE);
        addChildType(hpType);
        hyType = new AtomTypeLeaf(Hydrogen.INSTANCE);
        addChildType(hyType);

        //CoordinateFactory leafCoordFactory = new CoordinateFactorySphere(sim);
        setConformation(new ConformationParacetamolOrthorhombic(space));
    }
    
    
    public IMolecule makeMolecule() {

        Molecule moleculeParacetamol = new Molecule(this, 20);

        int countC = 0;
        int countH = 0;
        int countHp = 0;
        int countO = 0;
        int countN = 0;
        for (int i=0; i<20; i++) {
            if (indexC.length < countC && indexC[countC] == i) {
                moleculeParacetamol.addChildAtom(makeLeafAtom(cType));
                countC++;
            }
            else if (indexH.length < countH && indexH[countH] == i) {
                moleculeParacetamol.addChildAtom(makeLeafAtom(hyType));
                countH++;
            }
            else if (indexHp.length < countHp && indexHp[countHp] == i) {
                moleculeParacetamol.addChildAtom(makeLeafAtom(hpType));
                countHp++;
            }
            else if (indexO.length < countO && indexO[countO] == i) {
                moleculeParacetamol.addChildAtom(makeLeafAtom(oType));
                countO++;
            }
            else if (indexN.length < countN && indexN[countN] == i) {
                moleculeParacetamol.addChildAtom(makeLeafAtom(nType));
                countN++;
            }
            else {
                throw new RuntimeException("oops, couldn't figure out atom "+i);
            }
        }            
        
        conformation.initializePositions(moleculeParacetamol.getChildList());
        return moleculeParacetamol; 
    }
    
    protected IAtom makeLeafAtom(IAtomType leafType) {
        return isDynamic ? new AtomLeafDynamic(space, leafType)
                         : new Atom(space, leafType);
    }

    
    public int getNumLeafAtoms() {
        return 20;
    }
    
	public IAtomType getCType() {
        return cType;
    }


    public IAtomType getOType() {
        return oType;
    }


    public IAtomType getNType() {
        return nType;
    }


    public IAtomType getHpType() {
        return hpType;
    }


    public IAtomType getHyType() {
        return hyType;
    }


    public final static int[] indexC = new int[]{0,1,3,5,7,9,14,16};
    public final static int[] indexH = new int[]{2,4,8,10,17,18,19};
    public final static int[] indexHp = new int[]{11,13};
    public final static int[] indexO = new int[]{6,15};
    public final static int[] indexN = new int[]{12};
    
    public final static double [] Echarge = new double [20];
    static {
        Echarge[ 0] = ElectronVolt.UNIT.toSim( 0.382743);
        Echarge[ 1] = ElectronVolt.UNIT.toSim(-0.227865);
        Echarge[ 2] = ElectronVolt.UNIT.toSim( 0.168364);
        Echarge[ 3] = ElectronVolt.UNIT.toSim(-0.173831);
        Echarge[ 4] = ElectronVolt.UNIT.toSim( 0.140597);
        Echarge[ 5] = ElectronVolt.UNIT.toSim( 0.343891);
        Echarge[ 6] = ElectronVolt.UNIT.toSim(-0.595151);
        Echarge[ 7] = ElectronVolt.UNIT.toSim(-0.241393);
        Echarge[ 8] = ElectronVolt.UNIT.toSim( 0.121492);
        Echarge[ 9] = ElectronVolt.UNIT.toSim(-0.227674);
        Echarge[10] = ElectronVolt.UNIT.toSim( 0.130830);
        Echarge[11] = ElectronVolt.UNIT.toSim( 0.422442);
        Echarge[12] = ElectronVolt.UNIT.toSim(-0.748988);
        Echarge[13] = ElectronVolt.UNIT.toSim( 0.352011);
        Echarge[14] = ElectronVolt.UNIT.toSim( 0.808224);
        Echarge[15] = ElectronVolt.UNIT.toSim(-0.554225);
        Echarge[16] = ElectronVolt.UNIT.toSim(-0.417884);
        Echarge[17] = ElectronVolt.UNIT.toSim( 0.093811);
        Echarge[18] = ElectronVolt.UNIT.toSim( 0.110836);
        Echarge[19] = ElectronVolt.UNIT.toSim( 0.111772);
    }
    
	private static final long serialVersionUID = 1L;
	protected final ISpace space;
	protected final boolean isDynamic;
    protected final AtomTypeLeaf cType, oType, nType, hpType, hyType;
}