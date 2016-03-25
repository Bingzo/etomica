/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.modules.rheology;

import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IMoleculeList;
import etomica.api.IVectorMutable;
import etomica.data.DataSourceScalar;
import etomica.space.ISpace;
import etomica.units.CompoundDimension;
import etomica.units.Dimension;
import etomica.units.Length;

/**
 * Meter that measures that end to end distance of the molecules
 *
 * @author Andrew Schultz
 */
public class MeterEndToEnd extends DataSourceScalar {

    public MeterEndToEnd(ISpace space) {
        super("end-to-end distance^2", new CompoundDimension(new Dimension[]{Length.DIMENSION}, new double[]{2}));
        dr = space.makeVector();
    }

    public void setBox(IBox newBox) {
        box = newBox;
    }
    
    public double getDataAsScalar() {
        IMoleculeList molecules = box.getMoleculeList();
        double ee_tot = 0;
        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            IAtomList atoms = molecules.getMolecule(i).getChildList();
            dr.E(atoms.getAtom(atoms.getAtomCount()-1).getPosition());
            dr.ME(atoms.getAtom(0).getPosition());
            box.getBoundary().nearestImage(dr);
            ee_tot += dr.squared();
        }
        return ee_tot/molecules.getMoleculeCount();
    }

    protected IBox box;
    protected final IVectorMutable dr;
}
