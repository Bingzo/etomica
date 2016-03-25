/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.normalmode;

import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IMoleculeList;
import etomica.api.ISpecies;
import etomica.api.IVectorMutable;
import etomica.data.DataSourceScalar;
import etomica.space.ISpace;
import etomica.units.Null;

/**
 * Meter that measures the average tilt angle (not the angle of average tilt!)
 *
 * @author Andrew Schultz
 */
public class MeterThetaDeviation extends DataSourceScalar {

    public MeterThetaDeviation(ISpace space) {
        super("theta deviation", Null.DIMENSION);
        dr = space.makeVector();
    }
    
    public void setBox(IBox newBox) {
        box = newBox;
    }

    public double getDataAsScalar() {
        IMoleculeList molecules = box.getMoleculeList();
        int nMolecules = molecules.getMoleculeCount();
        double sum = 0;
        double cosNominalTheta = Math.cos(nominalTheta);
        for (int i=0; i<nMolecules; i++) {
            IMolecule molecule = molecules.getMolecule(i);
            IAtomList atomList = molecule.getChildList();
            int leafCount = atomList.getAtomCount();
            dr.E(atomList.getAtom(leafCount-1).getPosition());
            dr.ME(atomList.getAtom(0).getPosition());
            dr.normalize();
            double dcostheta = (dr.getX(2) - cosNominalTheta);
            sum += dcostheta*dcostheta;
        }
        return Math.sqrt(sum/nMolecules);
    }

    public void setNominalTheta(double newTheta) {
      nominalTheta = newTheta;
    }

    private static final long serialVersionUID = 1L;
    protected IBox box;
    protected final IVectorMutable dr;
    protected double nominalTheta;
}
