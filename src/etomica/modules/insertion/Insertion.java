/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.modules.insertion;
import etomica.action.BoxImposePbc;
import etomica.action.activity.ActivityIntegrate;
import etomica.api.IAtomType;
import etomica.api.IBox;
import etomica.api.IVectorMutable;
import etomica.box.Box;
import etomica.chem.elements.ElementSimple;
import etomica.config.ConfigurationLattice;
import etomica.integrator.IntegratorHard;
import etomica.integrator.IntegratorMD.ThermostatType;
import etomica.lattice.LatticeCubicFcc;
import etomica.lattice.LatticeOrthorhombicHexagonal;
import etomica.listener.IntegratorListenerAction;
import etomica.potential.P1HardPeriodic;
import etomica.potential.P2DoubleWell;
import etomica.potential.P2HardWrapper;
import etomica.potential.P2SquareWell;
import etomica.potential.PotentialMasterMonatomic;
import etomica.simulation.Simulation;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.species.SpeciesSpheresMono;
import etomica.util.RandomMersenneTwister;

public class Insertion extends Simulation {
    
    public SpeciesSpheresMono species, speciesGhost;
    public IBox box;
    public IntegratorHard integrator;
    public P2HardWrapper potentialWrapper;
    public P2DoubleWell potentialGhost;
    public ActivityIntegrate activityIntegrate;
    
    public Insertion(Space _space) {
        super(_space);
        setRandom(new RandomMersenneTwister(2));
        PotentialMasterMonatomic potentialMaster = new PotentialMasterMonatomic(this); //List(this, 2.0);
        
        int N = space.D() == 3 ? 256 : 100;  //number of atoms
        
        double sigma = 1.0;
        double lambda = 1.5;
        
        //controller and integrator
	    integrator = new IntegratorHard(this, potentialMaster, space);
	    integrator.setTimeStep(1.0);
	    integrator.setTemperature(1.0);
	    integrator.setIsothermal(false);
        integrator.setThermostat(ThermostatType.ANDERSEN_SCALING);
        integrator.setThermostatNoDrift(true);
        integrator.setThermostatInterval(1);
        P1HardPeriodic nullPotential = new P1HardPeriodic(space, sigma*lambda);
        activityIntegrate = new ActivityIntegrate(integrator);
        getController().addAction(activityIntegrate);

	    //species and potentials
	    species = new SpeciesSpheresMono(this, space);//index 1
	    species.setIsDynamic(true);
        addSpecies(species);
        integrator.setNullPotential(nullPotential, species.getLeafType());
        speciesGhost = new SpeciesSpheresMono(this, space);
        speciesGhost.setIsDynamic(true);
        ((ElementSimple)speciesGhost.getLeafType().getElement()).setMass(Double.POSITIVE_INFINITY);
        addSpecies(speciesGhost);
        
        //instantiate several potentials for selection in combo-box
	    P2SquareWell potentialSW = new P2SquareWell(space, sigma, lambda, 1.0, true);
        potentialWrapper = new P2HardWrapper(space,potentialSW);
        potentialMaster.addPotential(potentialWrapper,new IAtomType[]{species.getLeafType(), species.getLeafType()});

        potentialGhost = new P2DoubleWell(space, 1.0, lambda, 0.0, 0.0);
        potentialMaster.addPotential(potentialGhost,new IAtomType[]{species.getLeafType(), speciesGhost.getLeafType()});

        //construct box
	    box = new Box(space);
        addBox(box);
        IVectorMutable dim = space.makeVector();
        dim.E(space.D() == 3 ? 8 : 13.5);
        box.getBoundary().setBoxSize(dim);
        box.setNMolecules(species, N);
        new ConfigurationLattice(space.D() == 3 ? new LatticeCubicFcc(space) : new LatticeOrthorhombicHexagonal(space), space).initializeCoordinates(box);
        box.setNMolecules(speciesGhost, 1);
        integrator.setBox(box);

        integrator.getEventManager().addListener(new IntegratorListenerAction(new BoxImposePbc(box, space)));
    }
    
    public static void main(String[] args) {
        Space space = Space3D.getInstance();
        if(args.length != 0) {
            try {
                int D = Integer.parseInt(args[0]);
                if (D == 3) {
                    space = Space3D.getInstance();
                }
            } catch(NumberFormatException e) {}
        }
            
        Insertion sim = new Insertion(space);
        sim.getController().actionPerformed();
    }
}
