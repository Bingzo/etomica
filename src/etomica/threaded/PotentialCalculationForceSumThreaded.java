/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.threaded;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IPotentialAtomic;
import etomica.api.IVectorMutable;
import etomica.atom.AtomLeafAgentManager;
import etomica.atom.AtomLeafAgentManager.AgentSource;
import etomica.integrator.IntegratorBox;
import etomica.integrator.IntegratorVelocityVerlet.MyAgent;
import etomica.potential.PotentialCalculation;
import etomica.potential.PotentialCalculationForceSum;
import etomica.space.ISpace;

public class PotentialCalculationForceSumThreaded extends PotentialCalculationForceSum implements IPotentialCalculationThreaded, AgentSource<MyAgent> {

	final protected PotentialCalculationForceSum[] pc;
	protected AtomLeafAgentManager[] atomAgentManager;
	private final ISpace space;
    
	public PotentialCalculationForceSumThreaded(PotentialCalculationForceSum[] pc, ISpace _space) {
		this.pc = pc;
		this.space = _space;
	}

    public void reset(){
        super.reset();
        for (int i=0; i<pc.length; i++){
            pc[i].reset();
        }
    }
    
	public void setAgentManager(AtomLeafAgentManager<? extends IntegratorBox.Forcible> agentManager) {
        super.setAgentManager(agentManager);
        atomAgentManager = new AtomLeafAgentManager[pc.length];
        
        for (int i=0; i<pc.length; i++){
            atomAgentManager[i] = new AtomLeafAgentManager<MyAgent>(this, agentManager.getBox(), MyAgent.class);
            pc[i].setAgentManager(atomAgentManager[i]);
            agentManager.getBox();
		}
		
	}
	
	public void doCalculation(IAtomList atoms, IPotentialAtomic potential) {
		throw new RuntimeException("This is not the correct 'doCalculation' to call.");
	}
	
	/* (non-Javadoc)
	 * @see etomica.threads.PotentialCalculationThreaded#getPotentialCalculations()
	 */
	public PotentialCalculation[] getPotentialCalculations(){
		return pc;
	}
	
	public void writeData(){
       
		IBox box = integratorAgentManager.getBox();
        IAtomList atomArrayList = box.getLeafList();
      
        for(int j=0; j<atomArrayList.getAtomCount(); j++){
            IVectorMutable force = integratorAgentManager.getAgent(atomArrayList.getAtom(j)).force();
      
            for(int i=0; i<pc.length; i++){
                force.PE(((IntegratorBox.Forcible)atomAgentManager[i].getAgent(atomArrayList.getAtom(j))).force());
               
                
            }
        }
            
	}
    
    public final MyAgent makeAgent(IAtom a) {
        return new MyAgent(space);
    }
    
    public void releaseAgent(MyAgent object, IAtom atom){
        
    }
    
}
