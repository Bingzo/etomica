/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.modules.sam;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IAtomType;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IMoleculeList;
import etomica.api.ISimulation;
import etomica.api.ISpecies;
import etomica.atom.MoleculeAgentManager;
import etomica.atom.MoleculeAgentManager.MoleculeAgentSource;
import etomica.nbr.NeighborCriterion;

/**
 * Returns first leaf atom of each polymer molecule and the atom its bonded to.
 */
public class CriterionTether3 implements NeighborCriterion, MoleculeAgentSource {

    public CriterionTether3(ISimulation sim, ISpecies polymerSpecies, IAtomType surfaceType) {
        this.sim = sim;
        this.polymerSpecies = polymerSpecies;
        this.surfaceType = surfaceType;
    }
    
    public void setBox(IBox newBox) {
        if (box != newBox) {
            if (box != null) {
                bondManager.dispose();
            }
            bondManager = new MoleculeAgentManager(sim, newBox, this);
        }
        box = newBox;
        polymerList = box.getMoleculeList(polymerSpecies);
    }

    public void setBondedSurfaceAtoms(IMolecule polymerMolecule, IAtomList surfaceAtoms) {
        bondManager.setAgent(polymerMolecule, surfaceAtoms);
    }

    public boolean accept(IAtomList pair) {
        IAtom atom1 = pair.getAtom(0);
        IAtom atom2 = pair.getAtom(1);
        if (atom1.getIndex() != 0 || atom1.getParentGroup().getType() != polymerSpecies) {
            IAtom foo = atom2;
            atom2 = atom1;
            atom1 = foo;
            if (atom1.getIndex() != 0 || atom1.getParentGroup().getType() != polymerSpecies) {
                return false;
            }
        }
        if (atom2.getType() != surfaceType) {
            return false;
        }
        IAtomList bondedSurfaceAtoms = ((IAtomList)bondManager.getAgent(atom1.getParentGroup()));
        if (bondedSurfaceAtoms == null) {
            return false;
        }
        for (int i=0; i<bondedSurfaceAtoms.getAtomCount(); i++) {
            if (bondedSurfaceAtoms.getAtom(i) == atom2) {
                return true;
            }
        }
        return false;
    }

    public boolean needUpdate(IAtom atom) {
        return false;
    }

    public void reset(IAtom atom) {
    }

    public boolean unsafe() {
        return false;
    }

    public Class getMoleculeAgentClass() {
        return IAtomList.class;
    }

    public Object makeAgent(IMolecule a) {
        return null;
    }

    public void releaseAgent(Object agent, IMolecule atom) {
    }

    protected final ISimulation sim;
    protected IBox box;
    protected final ISpecies polymerSpecies;
    protected IMoleculeList polymerList;
    protected MoleculeAgentManager bondManager;
    protected int cursor;
    protected int surfaceCursor;
    protected IMolecule targetMolecule;
    protected final IAtomType surfaceType;
}
