/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.data.meter;

import etomica.api.IBox;
import etomica.api.ISpecies;
import etomica.data.DataSourceScalar;
import etomica.space.ISpace;
import etomica.units.DimensionRatio;
import etomica.units.Quantity;
import etomica.units.Volume;

/**
 * Meter for measurement of the total molecule number density in a box
 * Molecule number density is defined (number of molecules)/(volume of box)
 */
public class MeterDensity extends DataSourceScalar {
    
    public MeterDensity(ISpace space) {
        super("Number Density",new DimensionRatio(Quantity.DIMENSION, Volume.dimension(space.D())));
    }

    public void setSpecies(ISpecies s) {
        species = s;
    }
    public ISpecies getSpecies() {
    	return species;
    }

    public double getDataAsScalar() {
        if (box == null) throw new IllegalStateException("must call setBox before using meter");
        return (species == null ? 
        			box.getMoleculeList().getMoleculeCount() : 
        			box.getNMolecules(species))
				/box.getBoundary().volume();
    }
    
    /**
     * @return Returns the box.
     */
    public IBox getBox() {
        return box;
    }
    /**
     * @param box The box to set.
     */
    public void setBox(IBox box) {
        this.box = box;
    }

    private static final long serialVersionUID = 1L;
    private IBox box;
    private ISpecies species;
}
