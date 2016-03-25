/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.integrator;

import etomica.action.AtomAction;
import etomica.api.IAtomList;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.api.ISimulation;
import etomica.space.ISpace;

/**
 * Integrator that generates atom trajectories from an analytic formula.
 * Takes an IntegratorAnalytic.AtomAction instance and performs action on all atoms in each
 * time step; intention is for the action to set the position of the atom for the
 * current time (but it could do anything).
 *
 * @author David Kofke
 * @author Nancy Cribbin
 */
public class IntegratorAnalytic extends IntegratorMD {
    
    private static final long serialVersionUID = 1L;
    private AtomTimeAction action;
    
    public IntegratorAnalytic(ISimulation sim, IPotentialMaster potentialMaster, ISpace _space) {
        this(potentialMaster, sim.getRandom(), 0.05, _space);
    }
    
    public IntegratorAnalytic(IPotentialMaster potentialMaster, IRandom random,
                              double timeStep, ISpace _space) {
        super(potentialMaster,random,timeStep,0, _space);
    }
    
    public void doStepInternal() {
        super.doStepInternal();
        action.setTime(currentTime);
        IAtomList leafList = box.getLeafList();
        int nLeaf = leafList.getAtomCount();
        for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
            action.actionPerformed(leafList.getAtom(iLeaf));
        }
    }
    
    public void setAction(AtomTimeAction action) {this.action = action;}
    
    public AtomTimeAction getAction() {return action;}
    
    /**
     * Extends AtomAction class to add a method to set the time.
     */
    public static abstract class AtomTimeAction implements AtomAction {
        protected double time;
        public void setTime(double t) {time = t;}
    }
    
 }//end of IntegratorAnalytic
 
