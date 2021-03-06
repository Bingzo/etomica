/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.nbr;

import etomica.api.IAtomType;
import etomica.api.IBox;
import etomica.api.IPotentialAtomic;
import etomica.api.IPotentialMolecular;
import etomica.api.ISimulation;
import etomica.api.ISpecies;
import etomica.atom.IAtomPositionDefinition;
import etomica.atom.iterator.IteratorDirective;
import etomica.box.BoxAgentManager;
import etomica.box.BoxCellManager;
import etomica.nbr.cell.NeighborCellManager;
import etomica.nbr.cell.PotentialMasterCell;
import etomica.nbr.list.BoxAgentSourceCellManagerList;
import etomica.nbr.list.NeighborListManager;
import etomica.nbr.list.PotentialMasterList;
import etomica.potential.PotentialCalculation;
import etomica.potential.PotentialGroup;
import etomica.space.ISpace;

/**
 * PotentialMaster that uses both neighbor-cell iteration and cell-list 
 * iteration.  This is needed by simulations that employ both Monte Carlo
 * and molecular dynamics integration steps, alternately as the simulation
 * proceeds.  See DCVGCMD simulation module for an example.
 * <br>
 */
public class PotentialMasterHybrid extends PotentialMasterNbr {

    /**
	 * Invokes superclass constructor, specifying IteratorFactoryCell
     * for generating molecule iterators.  Sets default nCells of 10,
     * and position definition to null, causing cell assignment to be
     * based on atom type's position definition. 
	 */
	public PotentialMasterHybrid(ISimulation sim, double range, ISpace space) {
        this(sim, null, range, space);
    }
    
    /**
     * Constructs class using given position definition for all atom cell assignments.
     * @param positionDefinition if null, specifies use of atom type's position definition
     */
    public PotentialMasterHybrid(ISimulation sim, IAtomPositionDefinition positionDefinition, double range, ISpace space) {
        this(sim, range, new BoxAgentSourceCellManagerList(sim, positionDefinition, space), space);
    }
    
    private PotentialMasterHybrid(ISimulation sim, double range, BoxAgentSourceCellManagerList boxAgentSource, ISpace space) {
        this(sim, range, boxAgentSource, new BoxAgentManager<NeighborCellManager>(boxAgentSource, BoxCellManager.class), space);
    }
    
    private PotentialMasterHybrid(ISimulation sim, double range, BoxAgentSourceCellManagerList boxAgentSource,
            BoxAgentManager<NeighborCellManager> agentManager, ISpace _space) {
        super(sim, boxAgentSource, agentManager);
        potentialMasterList = new PotentialMasterList(sim, range, boxAgentSource, agentManager, _space);
        potentialMasterCell = new PotentialMasterCell(sim, range, boxAgentSource, agentManager, _space);
	}
    
    public PotentialGroup makePotentialGroup(int nBody) {
        return new PotentialGroupHybrid(nBody);
    }
    
    public PotentialMasterList getPotentialMasterList() {
        return potentialMasterList;
    }
    
    public PotentialMasterCell getPotentialMasterCell() {
        return potentialMasterCell;
    }

    /**
     * Overrides superclass method to enable direct neighbor-list iteration
     * instead of iteration via species/potential hierarchy. If no target atoms are
     * specified in directive, neighborlist iteration is begun with
     * speciesMaster of box, and repeated recursively down species hierarchy;
     * if one atom is specified, neighborlist iteration is performed on it and
     * down species hierarchy from it; if two or more atoms are specified,
     * superclass method is invoked.
     */
    public void calculate(IBox box, IteratorDirective id, PotentialCalculation pc) {
		if(!enabled) return;
        if (useNbrLists) potentialMasterList.calculate(box,id,pc);
        else potentialMasterCell.calculate(box,id,pc);
    }
    
    public double getCellRange() {
        return potentialMasterCell.getRange();
    }
    
    public void setCellRange(int newRange) {
        potentialMasterList.setCellRange(newRange);
        potentialMasterCell.setCellRange(newRange);
    }
    
    public double getRange() {
        return potentialMasterCell.getRange();
    }

    public void setRange(double newRange) {
        potentialMasterList.setRange(newRange);
        potentialMasterCell.setRange(newRange);
    }

    public NeighborCellManager getNbrCellManager(IBox box) {
        return potentialMasterList.getNbrCellManager(box);
    }
    
    public void setUseNbrLists(boolean flag) {
        useNbrLists = flag;
    }
    
    public void addPotential(IPotentialMolecular potential, ISpecies[] species) {
        potentialMasterList.addPotential(potential, species);
        potentialMasterCell.addPotential(potential, species);
        if (potential instanceof PotentialGroup) {
            // potential masters will attempt to set themselves as the group's 
            // PotentialMaster, but it will resist because it only has eyes for 
            // us.
            ((PotentialGroup)potential).setPotentialMaster(this);
        }
    }
    
    protected void addRangedPotentialForTypes(IPotentialAtomic potential, IAtomType[] atomTypes) {
    }
    
    public void potentialAddedNotify(IPotentialAtomic subPotential, PotentialGroup pGroup) {
        potentialMasterList.potentialAddedNotify(subPotential, pGroup);
        potentialMasterCell.potentialAddedNotify(subPotential, pGroup);
    }
    
    public NeighborListManager getNeighborManager(IBox box) {
        return potentialMasterList.getNeighborManager(box);
    }

    public void removePotential(IPotentialAtomic potential) {
        potentialMasterList.removePotential(potential);
        potentialMasterCell.removePotential(potential);
    }

    private boolean useNbrLists;
    private final PotentialMasterList potentialMasterList;
    private final PotentialMasterCell potentialMasterCell;
}
