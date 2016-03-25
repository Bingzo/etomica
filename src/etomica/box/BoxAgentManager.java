/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.box;

import java.lang.reflect.Array;

import etomica.api.IBox;
import etomica.api.ISimulation;
import etomica.api.ISimulationAtomTypeIndexEvent;
import etomica.api.ISimulationBoxEvent;
import etomica.api.ISimulationEventManager;
import etomica.api.ISimulationIndexEvent;
import etomica.api.ISimulationListener;
import etomica.api.ISimulationSpeciesEvent;
import etomica.api.ISimulationSpeciesIndexEvent;
import etomica.simulation.SimulationBoxEvent;
import etomica.util.Arrays;

/**
 * BoxAgentManager acts on behalf of client classes (a BoxAgentSource) to manage 
 * agents for each Box in a simulation.  When Box instances are added or removed 
 * from the simulation, the agents array (indexed by the box's index) is updated.  
 * The client should call getAgents() at any point where a Box might have have been 
 * added to (or removed from) the system because the old array would be stale at that
 * point. 
 * @author andrew
 */
public class BoxAgentManager<E> implements ISimulationListener {

    public BoxAgentManager(BoxAgentSource<E> source, Class boxAgentClass) {
        agentSource = source;
        this.boxAgentClass = boxAgentClass;
        if (source == null) {
            agents = (E[])Array.newInstance(boxAgentClass, 0);
        }
    }

    public BoxAgentManager(BoxAgentSource<E> source, Class boxAgentClass, ISimulation sim) {
        agentSource = source;
        this.boxAgentClass = boxAgentClass;
        setSimulation(sim);
    }
    
    /**
     * Returns the agent associated with the given box
     */
    public E getAgent(IBox box) {
        return agents[box.getIndex()];
    }
    
    public void setAgent(IBox box, E agent) {
        int idx = box.getIndex();
        if (idx >= agents.length) {
            // no room in the array.  reallocate the array with an extra cushion.
            agents = (E[])Arrays.resizeArray(agents,idx+1);
        }
        agents[box.getIndex()] = agent;
    }

    /**
     * Returns an iterator that returns each non-null agent
     */
    public AgentIterator<E> makeIterator() {
        return new AgentIterator<E>(this);
    }
    
    /**
     * Sets the Simulation containing Boxs to be tracked.  This method should
     * not be called if setSimulationEventManager is called.
     */
    public void setSimulation(ISimulation sim) {
        simEventManager = sim.getEventManager();
        // this will crash if the given sim is in the middle of its constructor
        simEventManager.addListener(this);

        // hope the class returns an actual class with a null Atom and use it to construct
        // the array
        int boxCount = sim.getBoxCount();
        agents = (E[])Array.newInstance(boxAgentClass, boxCount);
        if (agentSource == null) {
            return;
        }
        for (int i=0; i<boxCount; i++) {
            addAgent(sim.getBox(i));
        }
    }
    
    /**
     * Notifies the BoxAgentManager that it should release all agents and 
     * stop listening for events from the simulation.
     */
    public void dispose() {
        // remove ourselves as a listener to the old box
        if (simEventManager != null) {
            simEventManager.removeListener(this);
        }
        if (agentSource != null) {
            for (int i=0; i<agents.length; i++) {
                if (agents[i] != null) {
                    agentSource.releaseAgent(agents[i]);
                }
            }
        }
        agents = null;
    }
    
    public void simulationBoxAdded(ISimulationBoxEvent e) {
        addAgent(((SimulationBoxEvent)e).getBox());
    }
    
    public void simulationBoxRemoved(ISimulationBoxEvent e) {
        IBox box = ((SimulationBoxEvent)e).getBox();
        // The given Box got removed.  The remaining boxes got shifted
        // down.
        int index = box.getIndex();
        if (agentSource != null) {
            agentSource.releaseAgent(agents[index]);
        }
        for (int i=index; i<agents.length-1; i++) {
            agents[i] = agents[i+1];
        }
        agents = (E[])Arrays.resizeArray(agents,agents.length-1);
    }
    
    public void simulationSpeciesAdded(ISimulationSpeciesEvent e) {}
    public void simulationSpeciesRemoved(ISimulationSpeciesEvent e) {}
    public void simulationSpeciesIndexChanged(ISimulationSpeciesIndexEvent e) {}
    public void simulationSpeciesMaxIndexChanged(ISimulationIndexEvent e) {}
    public void simulationAtomTypeIndexChanged(ISimulationAtomTypeIndexEvent e) {}
    public void simulationAtomTypeMaxIndexChanged(ISimulationIndexEvent e) {}
    
    protected void addAgent(IBox box) {
        agents = (E[])Arrays.resizeArray(agents,box.getIndex()+1);
        if (agentSource != null) {
            agents[box.getIndex()] = agentSource.makeAgent(box);
        }
    }
    
    /**
     * Interface for an object that makes an agent to be placed in each atom
     * upon construction.  AgentSource objects register with the AtomFactory
     * the produces the atom.
     */
    public interface BoxAgentSource<E> {
        public E makeAgent(IBox box);
        
        //allow any agent to be disconnected from other elements 
        public void releaseAgent(E agent); 
    }

    private static final long serialVersionUID = 1L;
    protected final BoxAgentSource<E> agentSource;
    protected final Class boxAgentClass;
    protected ISimulationEventManager simEventManager;
    protected E[] agents;
    
    /**
     * Iterator that loops over the agents, skipping null elements
     */
    public static class AgentIterator<E> {
        protected AgentIterator(BoxAgentManager<E> agentManager) {
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
        
        public E next() {
            cursor++;
            while (cursor-1 < agents.length) {
                if (agents[cursor-1] != null) {
                    return agents[cursor-1];
                }
                cursor++;
            }
            return null;
        }
        
        private final BoxAgentManager<E> agentManager;
        private int cursor;
        private E[] agents;
    }
}
