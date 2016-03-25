/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.normalmode;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBoundary;
import etomica.api.IBox;
import etomica.api.IVectorMutable;
import etomica.data.DataSourceScalar;
import etomica.nbr.list.NeighborListManager;
import etomica.space.ISpace;
import etomica.units.Length;

/**
 * measures the maximum amount by which by which the HS diameter could increase
 * without causing overlap
 * 
 * @author Andrew Schultz
 */
public class MeterMaxExpansion extends DataSourceScalar {

    protected final IVectorMutable dr;
    protected final NeighborListManager neighborManager;
    protected final IBox box;
    
    public MeterMaxExpansion(ISpace space, IBox box, NeighborListManager neighborManager) {
        super("displacement", Length.DIMENSION);
        this.neighborManager = neighborManager;
        this.box = box;
        dr = space.makeVector();
    }
    
    public double getDataAsScalar() {
        IBoundary boundary = box.getBoundary();
        IAtomList leafList = box.getLeafList();
        double min = 1e10;
        for (int i=0; i<leafList.getAtomCount(); i++) {
            IAtom atomi = leafList.getAtom(i);
            IAtomList nbrs = neighborManager.getUpList(atomi)[0];
            for (int j=0; j<nbrs.getAtomCount(); j++) {
                IAtom atomj = nbrs.getAtom(j);
                dr.Ev1Mv2(atomi.getPosition(), atomj.getPosition());
                boundary.nearestImage(dr);
                double r2 = dr.squared();
                if (r2 < min) min = r2;
            }
        }
        return Math.sqrt(min)-1;
    }
}
