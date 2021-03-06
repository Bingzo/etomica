/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.virial;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IVectorMutable;
import etomica.config.IConformation;
import etomica.space.ISpace;

 /**
  *  Conformation for Anthracene
  *  Reference paper: TraPPE: 4 UA description of linear and branched alkanes and alkylbenzenes, Siepmann et al
  *  modified from naphthalene conformation
 * @author Shu Yang
 *
 */
public class ConformationAnthraceneTraPPE implements IConformation, java.io.Serializable{
	
	public ConformationAnthraceneTraPPE(ISpace space){
		this.space = space;
		vector = space.makeVector();
	}

	public void initializePositions(IAtomList atomList) {
	    // the naphthalene is conformed as :put 2 C without H in y-axis , one is on the top, one is under the origin
		// this class is created based on naphthalene, so it does not seem so symmetric
		IAtom n1 = atomList.getAtom(SpeciesTraPPEAnthracene.indexC1);
		n1.getPosition().E(new double[] {0, a, 0});
		
		IAtom n2 = atomList.getAtom(SpeciesTraPPEAnthracene.indexC2);
		n2.getPosition().E(new double[] {0, -a, 0});
						
		IAtom n3 = atomList.getAtom(SpeciesTraPPEAnthracene.indexC3);
		n3.getPosition().E(new double[] {twosqrt3a, -a, 0});
				
		IAtom n4 = atomList.getAtom(SpeciesTraPPEAnthracene.indexC4);
		n4.getPosition().E(new double[] {twosqrt3a, a, 0});
		
		
		// put the other CH United atoms
		IAtom n5 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH1);
		n5.getPosition().E(new double[] {-sqrt3a, bond, 0});
		
		IAtom n6 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH2);
		n6.getPosition().E(new double[] {-twosqrt3a, a, 0});
		
		IAtom n7 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH3);
		n7.getPosition().E(new double[] {-twosqrt3a, -a, 0});
		
		IAtom n8 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH4);
		n8.getPosition().E(new double[] {-sqrt3a, -bond, 0});
		
		IAtom n9 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH5);
		n9.getPosition().E(new double[] {sqrt3a, -bond, 0});
		
		IAtom n10 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH6);
		n10.getPosition().E(new double[] {threesqrt3a, -bond, 0});
		
		IAtom n11 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH7);
		n11.getPosition().E(new double[] {foursqrt3a, -a, 0});
		
		IAtom n12 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH8);
		n12.getPosition().E(new double[] {foursqrt3a, a, 0});
		
		IAtom n13 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH9);
		n13.getPosition().E(new double[] {threesqrt3a, bond, 0});
		
		IAtom n14 = atomList.getAtom(SpeciesTraPPEAnthracene.indexCH10);
		n14.getPosition().E(new double[] {sqrt3a, bond, 0});
			
	}
	protected final ISpace space;
	protected static final double a = 0.7; // a is half of the bond length
	protected static final double bond = 1.4;

	protected static final double sqrt3a = a * Math.sqrt(3);
	protected static final double twosqrt3a =  sqrt3a* 2;
	protected static final double threesqrt3a = sqrt3a * 3 ;
	protected static final double foursqrt3a = sqrt3a * 4 ;

	protected IVectorMutable vector;
	
	private static final long serialVersionUID = 1L;
}
