/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.data.meter;
import etomica.api.IAtom;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IPotentialMaster;
import etomica.atom.iterator.IteratorDirective;
import etomica.data.DataSourceScalar;
import etomica.potential.PotentialCalculationEnergySum;
import etomica.units.Energy;

/**
 * Meter for evaluation of the potential energy in a box.
 * Includes several related methods for computing the potential energy of a single
 * atom or molecule with all neighboring atoms
 *
 * @author David Kofke
 */
 
public class MeterPotentialEnergy extends DataSourceScalar {
    
    public MeterPotentialEnergy(IPotentialMaster potentialMaster) {
        super("Potential Energy",Energy.DIMENSION);
        iteratorDirective.includeLrc = true;
        potential = potentialMaster;
        iteratorDirective.setDirection(IteratorDirective.Direction.UP);
    }

    /**
     * Sets flag indicating whether calculated energy should include
     * long-range correction for potential truncation (true) or not (false).
     */
    public void setIncludeLrc(boolean b) {
    	iteratorDirective.includeLrc = b;
    }
    /**
     * Indicates whether calculated energy should include
     * long-range correction for potential truncation (true) or not (false).
     */
    public boolean isIncludeLrc() {
    	return iteratorDirective.includeLrc;
    }

    public void setTarget(IAtom atom) {
    	iteratorDirective.setTargetAtom(atom);
        iteratorDirective.setDirection(atom == null ? IteratorDirective.Direction.UP : null);
    }

    public void setTarget(IMolecule mole) {
        iteratorDirective.setTargetMolecule(mole);
        iteratorDirective.setDirection(mole == null ? IteratorDirective.Direction.UP : null);
    }
    
   /**
    * Computes total potential energy for box.
    * Currently, does not include long-range correction to truncation of energy
    */
    public double getDataAsScalar() {
        if (box == null) throw new IllegalStateException("must call setBox before using meter");
    	energy.zeroSum();
        potential.calculate(box, iteratorDirective, energy);
        return energy.getSum();
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
    
    public void setPotentialCalculation(PotentialCalculationEnergySum newEnergySummer) {
        energy = newEnergySummer;
    }

    protected IBox box;
    protected final IteratorDirective iteratorDirective = new IteratorDirective();
    protected PotentialCalculationEnergySum energy = new PotentialCalculationEnergySum();
    protected final IPotentialMaster potential;
}
