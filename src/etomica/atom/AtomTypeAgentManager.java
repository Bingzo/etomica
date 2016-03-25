/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.atom;

import java.io.Serializable;
import java.lang.reflect.Array;

import etomica.api.IAtomType;
import etomica.api.ISimulation;
import etomica.api.ISimulationAtomTypeIndexEvent;
import etomica.api.ISimulationBoxEvent;
import etomica.api.ISimulationIndexEvent;
import etomica.api.ISimulationListener;
import etomica.api.ISimulationSpeciesEvent;
import etomica.api.ISimulationSpeciesIndexEvent;
import etomica.api.ISpecies;
import etomica.util.Arrays;

/**
 * AtomTypeAgentManager acts on behalf of client classes (an AgentSource) to 
 * manage agents in every AtomType in a box.  When species are added or 
 * removed from the simulation, the agents array (indexed by the AtomType's 
 * global index) is updated.  The client should call getAgents() at any point 
 * where an atom might have have been added to the system because the old array
 * would be stale at that point.
 * @author andrew
 */
public class AtomTypeAgentManager implements ISimulationListener, Serializable {

    public AtomTypeAgentManager(AgentSource source) {
        agentSource = source;
    }
    
    public AtomTypeAgentManager(AgentSource source, ISimulation sim) {
        agentSource = source;
        init(sim);
    }
    
    /**
     * Returns an iterator that returns each non-null agent
     */
    public AgentIterator makeIterator() {
        return new AgentIterator(this);
    }
    
    /**
     * Sets the agent associated with the given atom type to be the given
     * agent.  The AtomType must be from the ISimulation.  The AtomType's old
     * agent is not "released".  This should be done manually if needed.
     */
    public void setAgent(IAtomType atomType, Object newAgent) {
        if (agents == null) {
            agents = new Object[atomType.getIndex()+1];
        }
        else if (agents.length <= atomType.getIndex()) {
            agents = Arrays.resizeArray(agents, atomType.getIndex()+1);
        }
        agents[atomType.getIndex()] = newAgent;
    }
    
    /**
     * Convenience method to return the agent the given AtomType.  For repeated
     * access to the agents from multiple AtomTypes, it might be faster to use 
     * the above getAgents method.
     */
    public Object getAgent(IAtomType type) {
        return agents[type.getIndex()];
    }
    
    /**
     * Releases the agents associated with the given AtomType and its children.
     */
    private void releaseAgents(ISpecies parentType) {
        for (int i=0; i<parentType.getAtomTypeCount(); i++) {
            IAtomType leafType = parentType.getAtomType(i);
            Object agent = agents[leafType.getIndex()];
            if (agent != null) {
                if (agentSource != null) {
                    agentSource.releaseAgent(agent, leafType);
                }
                agents[leafType.getIndex()] = null;
            }
        }
    }
    
    private void makeAllAgents() {
        for (int i=0; i<sim.getSpeciesCount(); i++) {
            ISpecies parentType = sim.getSpecies(i);
            for (int j=0; j<parentType.getAtomTypeCount(); j++) {
                addAgent(parentType.getAtomType(j));
            }
        }
    }
    
    /**
     * Returns the max index of all the children of the given AtomType
     */
    private int getGlobalMaxIndex() {
        int max = 0;
        for (int i=0; i<sim.getSpeciesCount(); i++) {
            if (sim.getSpecies(i).getIndex() > max) {
                max = sim.getSpecies(i).getIndex();
            }
            int childMax = getMaxIndexOfChildren(sim.getSpecies(i));
            if (childMax > max) {
                max = childMax;
            }
        }
        return max;
    }
    
    /**
     * Returns the max index of all the children of the given AtomType
     */
    private static int getMaxIndexOfChildren(ISpecies parentType) {
        int max = 0;
        for (int i=0; i<parentType.getAtomTypeCount(); i++) {
            if (parentType.getAtomType(i).getIndex() > max) {
                max = parentType.getAtomType(i).getIndex();
            }
        }
        return max;
    }
    
    /**
     * Unregisters this class as a listener for AtomType-related events and 
     * releases its agents.
     */
    public void dispose() {
        // remove ourselves as a listener to the old box
        sim.getEventManager().removeListener(this);
        for (int i=0; i<sim.getSpeciesCount(); i++) {
            releaseAgents(sim.getSpecies(i));
        }
        agents = null;
    }
    
    /**
     * Sets the SpeciesRoot for which this AtomAgentManager will manage 
     * AtomType agents.
     */
    public void init(ISimulation newSim) {
        sim = newSim;
        sim.getEventManager().addListener(this);

        int numTypes = getGlobalMaxIndex()+1;
        
        agents = (Object[])Array.newInstance(agentSource != null ? agentSource.getSpeciesAgentClass() : Object.class, numTypes);
        // fill in the array with agents from all the atoms
        makeAllAgents();
    }
    
    public void simulationSpeciesAdded(ISimulationSpeciesEvent e) {
        ISpecies species = e.getSpecies();
        for(int i = 0; i < species.getAtomTypeCount(); i++) {
            IAtomType newType = species.getAtomType(i);
            int indexMax = newType.getIndex();
            agents = Arrays.resizeArray(agents, indexMax+1);
            addAgent(newType);
        }
    }
    
    public void simulationSpeciesRemoved(ISimulationSpeciesEvent e) {
        releaseAgents(e.getSpecies());
    }
    
    public void simulationAtomTypeIndexChanged(ISimulationAtomTypeIndexEvent e) {
        IAtomType atomType = e.getAtomType();
        int oldIndex = e.getIndex();
        int newIndex = atomType.getIndex();
        if (newIndex >= agents.length) {
            agents = Arrays.resizeArray(agents, newIndex+1);
        }
        agents[newIndex] = agents[oldIndex];
        agents[oldIndex] = null;
    }
    
    
    public void simulationAtomTypeMaxIndexChanged(ISimulationIndexEvent e) {
        int maxIndex = e.getIndex();
        agents = Arrays.resizeArray(agents, maxIndex+1);
    }
    
    public void simulationSpeciesIndexChanged(ISimulationSpeciesIndexEvent e) {}
    public void simulationSpeciesMaxIndexChanged(ISimulationIndexEvent e) {}
    public void simulationBoxAdded(ISimulationBoxEvent e) {}
    public void simulationBoxRemoved(ISimulationBoxEvent e) {}

    
    protected void addAgent(IAtomType type) {
        if (agentSource != null) {
            agents[type.getIndex()] = agentSource.makeAgent(type);
        }
    }
    
    /**
     * Interface for an object that wants an agent associated with each 
     * AtomType in a Simulation.
     */
    public interface AgentSource {
        /**
         * Returns the Class of the agent.  This is used to create an array of 
         * the appropriate Class.
         */
        public Class getSpeciesAgentClass();

        /**
         * Returns an agent for the given AtomType.
         */
        public Object makeAgent(IAtomType type);
        
        /**
         * This informs the agent source that the agent is going away and that 
         * the agent source should disconnect the agent from other elements.
         */
        public void releaseAgent(Object agent, IAtomType type);
    }

    private static final long serialVersionUID = 1L;
    private final AgentSource agentSource;
    protected Object[] agents;
    protected ISimulation sim;

    /**
     * Iterator that loops over the agents, skipping null elements
     */
    public static class AgentIterator implements Serializable {
        protected AgentIterator(AtomTypeAgentManager agentManager) {
            this.agentManager = agentManager;
        }
        
        public void reset() {
            cursor = 0;
            agents = agentManager.agents;
        }
        
        public boolean hasNext() {
            while (cursor < agents.length) {
                if (agents[cursor] != null) {
                    return true;
                }
                cursor++;
            }
            return false;
        }
        
        public Object next() {
            cursor++;
            while (cursor-1 < agents.length) {
                if (agents[cursor-1] != null) {
                    return agents[cursor-1];
                }
                cursor++;
            }
            return null;
        }
        
        private static final long serialVersionUID = 1L;
        private final AtomTypeAgentManager agentManager;
        private int cursor;
        private Object[] agents;
    }
}
