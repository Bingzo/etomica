/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.data.meter;

import etomica.api.IBox;
import etomica.api.IMoleculeList;
import etomica.atom.AtomHydrogen;
import etomica.data.DataSourceScalar;
import etomica.units.Length;

public class MeterAvgBondLength extends DataSourceScalar {
    private IBox box;

    public MeterAvgBondLength() {
        super("BondLength",Length.DIMENSION);        
    }    
    public double getDataAsScalar() {
        IMoleculeList m = box.getMoleculeList();
        int mN = m.getMoleculeCount();
        double avgBondLength = 0.00;        
        for (int i=0; i<mN; i++) {
            int nAtoms = m.getMolecule(i).getChildList().getAtomCount();
            for (int j=0; j<nAtoms; j++) {
            	
                AtomHydrogen jAtom = (AtomHydrogen) m.getMolecule(i).getChildList().getAtom(j);
//                if (j==0) System.out.println("zero bond length = "+jAtom.getBondLength());
                avgBondLength += jAtom.getBondLength()/(mN*nAtoms);
            }
        }
//        System.out.println("avg = "+avgBondLength);
        return avgBondLength;
    }
    
    public IBox getBox() {
        return box;
    }
    
    public void setBox(IBox box) {
        this.box = box;
    }

}
