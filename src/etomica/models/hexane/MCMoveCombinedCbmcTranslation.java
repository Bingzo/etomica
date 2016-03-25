/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.models.hexane;

import etomica.action.AtomActionTranslateBy;
import etomica.action.MoleculeChildAtomAction;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.api.IVectorMutable;
import etomica.atom.AtomPositionGeometricCenter;
import etomica.atom.MoleculeSourceRandomMolecule;
import etomica.atom.iterator.AtomIterator;
import etomica.atom.iterator.AtomIteratorArrayListSimple;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.integrator.mcmove.MCMoveBox;
import etomica.space.ISpace;


/**
 * MC move which performs a configuration bias monte carlo moved, then moves 
 * that atom to a new position so that its center of mass does not change.
 * 
 * @author cribbin
 *
 */
public class MCMoveCombinedCbmcTranslation extends MCMoveBox {

    protected MCMoveCBMC cbmcMove;
    protected double uNew, uOld;
    protected IMolecule molecule;
    protected MoleculeSourceRandomMolecule moleculeSource;
    private AtomIteratorArrayListSimple affectedAtomIterator;
    protected MoleculeChildAtomAction moveAction;
    protected IVectorMutable oldGeo, newGeo, temp, transVect;
    protected AtomPositionGeometricCenter centerer;
    protected MeterPotentialEnergy energyMeter;
    protected final IRandom random;
    private ISpace space;
    
    public MCMoveCombinedCbmcTranslation(IPotentialMaster pm, MCMoveCBMC mv,
            IRandom nRandom, ISpace _space){
        super(pm);
        this.cbmcMove = mv;
        this.random = nRandom;
        this.space = _space;
        
        moleculeSource = new MoleculeSourceRandomMolecule();
        moleculeSource.setRandomNumberGenerator(random);
        energyMeter = new MeterPotentialEnergy(pm);
        
        affectedAtomIterator = new AtomIteratorArrayListSimple();
        
        AtomActionTranslateBy translator = new AtomActionTranslateBy(space);
        transVect = translator.getTranslationVector();
        moveAction = new MoleculeChildAtomAction(translator);
        centerer = new AtomPositionGeometricCenter(space);
        
        oldGeo = space.makeVector();
        newGeo = space.makeVector();
        temp = space.makeVector();
    }
    
    public MCMoveCombinedCbmcTranslation(IPotentialMaster pm, MCMoveCBMC mv, 
            IRandom nRandom, IBox ph, ISpace _space){
        this(pm, mv, nRandom, _space);
        setBox(ph);
    }
    
    public void setBox(IBox newBox) {
        super.setBox(newBox);
        moleculeSource.setBox(newBox);
        energyMeter.setBox(newBox);
    }
    
    public AtomIterator affectedAtoms() { return affectedAtomIterator; }
    
    public double energyChange()  {
        return (uNew - uOld) + cbmcMove.energyChange();
    }

    public void acceptNotify() {
        // Nothing needs to be done!
//        System.out.println("MCMoveCombinedCbmcTranslation accepts a move");
    }

    public boolean doTrial() {
//        System.out.println("doTrial MCMoveCombinedCbmcTranslation called");
        
        molecule = moleculeSource.getMolecule();
        if(molecule == null) {return false;}
        affectedAtomIterator.setList(molecule.getChildList());
        
        //save the old position, and apply the cbmc move.
        oldGeo.E(centerer.position(molecule));
        transVect.E(oldGeo);
        if(!cbmcMove.doTrial(molecule)) {return false;}
//        accepted = cbmcMove.doTrial(molecule);

        //Find the new position, and apply the translation move.
        transVect.ME(centerer.position(molecule));
        uOld = energyMeter.getDataAsScalar();
        moveAction.actionPerformed(molecule);
        uNew = energyMeter.getDataAsScalar();

        return true;
    }

    public double getA() {
        return 1.0 * cbmcMove.getA();
    }

    public double getB() {
        return -(uNew - uOld) + cbmcMove.getB();
    }

    public void rejectNotify() {
        transVect.TE(-1.0);
        moveAction.actionPerformed(molecule);
        cbmcMove.rejectNotify();
//        System.out.println("MCMoveCombinedCbmcTranslation rejects a move");
    }

}
