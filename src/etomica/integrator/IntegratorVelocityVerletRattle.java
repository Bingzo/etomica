/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.integrator;

import etomica.api.IAtom;
import etomica.api.IAtomKinetic;
import etomica.api.IAtomList;
import etomica.api.IBoundary;
import etomica.api.IMolecule;
import etomica.api.IMoleculeList;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.api.ISimulation;
import etomica.api.IVectorMutable;
import etomica.atom.AtomSetSinglet;
import etomica.integrator.IntegratorVelocityVerlet.MyAgent;
import etomica.space.ISpace;
import etomica.units.Joule;
import etomica.units.Kelvin;
import etomica.util.Constants;
import etomica.util.Debug;

/**
 * Integrator implementing RATTLE algorithm.
 *
 * @author Andrew Schultz
 */
public class IntegratorVelocityVerletRattle extends IntegratorVelocityVerletShake {

    private static final long serialVersionUID = 1L;
    protected final IVectorMutable dv;

    public IntegratorVelocityVerletRattle(ISimulation sim, IPotentialMaster potentialMaster, ISpace _space) {
        this(sim, potentialMaster, sim.getRandom(), 0.05, 1.0, _space);
    }
    
    public IntegratorVelocityVerletRattle(ISimulation sim, IPotentialMaster potentialMaster, IRandom random,
            double timeStep, double temperature, ISpace _space) {
        super(sim, potentialMaster,random,timeStep,temperature, _space);
        dv = space.makeVector();
    }
    
    public void doStepInternal() {
        currentTime += timeStep;

        IMoleculeList molecules = box.getMoleculeList();

        // RATTLE
        int numBondedMolecules = 0;
        int numIterations = 0;
        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            IMolecule molecule = molecules.getMolecule(i);
            BondConstraints bondConstraints = (BondConstraints)shakeAgentManager.getAgent(molecule.getType());
            if (bondConstraints != null) {
                numBondedMolecules++;
                IAtomList childList = molecule.getChildList();
                IBoundary boundary = box.getBoundary();

                if (drOld.length < bondConstraints.bondedAtoms.length) {
                    IVectorMutable[] newDrOld = new IVectorMutable[bondConstraints.bondedAtoms.length];
                    System.arraycopy(drOld, 0, newDrOld, 0, drOld.length);
                    for (int j=drOld.length; j<newDrOld.length; j++) {
                        newDrOld[j] = space.makeVector();
                    }
                    drOld = newDrOld;
                }

                for (int j=0; j<bondConstraints.bondedAtoms.length; j++) {
                    IAtom atom0 = childList.getAtom(bondConstraints.bondedAtoms[j][0]);
                    IAtom atom1 = childList.getAtom(bondConstraints.bondedAtoms[j][1]);
                    drOld[j].Ev1Mv2(atom1.getPosition(), atom0.getPosition());
                    boundary.nearestImage(drOld[j]);
                }
            }
            
            IAtomList leafList = molecule.getChildList();
            int nLeaf = leafList.getAtomCount();
            for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
                IAtomKinetic a = (IAtomKinetic)leafList.getAtom(iLeaf);
                MyAgent agent = agentManager.getAgent(a);
                IVectorMutable r = a.getPosition();
                IVectorMutable v = a.getVelocity();
                if (Debug.ON && Debug.DEBUG_NOW && Debug.anyAtom(new AtomSetSinglet((IAtom)a))) {
                    System.out.println("first "+a+" r="+r+", v="+v+", f="+agent.force);
                }
                if  (a.getType().getMass() != 0) {
                    v.PEa1Tv1(0.5*timeStep*a.getType().rm(),agent.force);  // p += f(old)*dt/2
                }
                r.PEa1Tv1(timeStep,v);         // r += p*dt/m
            }

            IAtomList childList = molecule.getChildList();
            int[][] bondedAtoms = bondConstraints.bondedAtoms;
            IBoundary boundary = box.getBoundary();
            double[] bondLengths = bondConstraints.bondLengths;

            if (childList.getAtomCount() > moved.length) {
                moved = new boolean[2][childList.getAtomCount()];
            }
            for (int j=0; j<childList.getAtomCount(); j++) {
                moved[1][j] = true;
            }
            
            for (int iter = 0; iter<maxIterations; iter++) {
                numIterations++;
                boolean success = true;
                for (int j=0; j<childList.getAtomCount(); j++) {
                    moved[0][j] = moved[1][j];
                    moved[1][j] = false;
                }
                for (int j=0; j<bondConstraints.bondedAtoms.length; j++) {
                    int iAtom1 = bondedAtoms[j][0];
                    int iAtom2 = bondedAtoms[j][1];
                    if (!moved[0][iAtom1] && !moved[0][iAtom2]) {
                        continue;
                    }
                    IAtomKinetic atom1 = (IAtomKinetic)childList.getAtom(iAtom1);
                    IAtomKinetic atom2 = (IAtomKinetic)childList.getAtom(iAtom2);
                    dr.Ev1Mv2(atom2.getPosition(), atom1.getPosition());
                    boundary.nearestImage(dr);
//                    System.out.println(iter+" "+j+" "+atom1.getVelocity());
//                    System.out.println(iter+" "+j+" "+atom2.getVelocity());
                    double dr2 = dr.squared();
                    double bl2 = bondLengths[j]*bondLengths[j];
//                    System.out.println(Math.sqrt(dr2)+" "+Math.sqrt(bl2));
                    double diffSq = bl2 - dr2;
                    if (Math.abs(diffSq/bl2) > shakeTol) {
                        double mass1 = ((IAtom)atom1).getType().getMass();
                        double mass2 = ((IAtom)atom2).getType().getMass();
                        double rMass = 1.0/mass1 + 1.0/mass2;
                        double drDotDrOld = dr.dot(drOld[j]);
                        if  (drDotDrOld / bl2 < 0.1) {
                            System.out.println("molecule "+i);
                            System.out.println("dr "+dr);
                            System.out.println("drOld "+drOld[j]);
                            System.out.println("drDotDrOld "+drDotDrOld);
                            throw new RuntimeException("oops");
                        }
                        double gab = diffSq / (2.0 * rMass * drDotDrOld);
                        atom2.getPosition().PEa1Tv1( gab/mass2, drOld[j]);
                        atom1.getPosition().PEa1Tv1(-gab/mass1, drOld[j]);

                        gab /= timeStep;
                        
                        atom2.getVelocity().PEa1Tv1( gab/mass2, drOld[j]);
                        atom1.getVelocity().PEa1Tv1(-gab/mass1, drOld[j]);
                        
//                        System.out.println("new "+atom1.getVelocity());
//                        System.out.println("new "+atom2.getVelocity());
                        
                        moved[1][iAtom1] = true;
                        moved[1][iAtom2] = true;
                        success = false;
                    }
                }
                if (success) {
                    break;
                }
                if (iter == maxIterations-1) {
                    System.err.println("failed to converge in shake for molecule "+i);
                }
            }
        }

        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            IMolecule molecule = molecules.getMolecule(i);
            BondConstraints bondConstraints = (BondConstraints)shakeAgentManager.getAgent(molecule.getType());
            if (bondConstraints == null) {
                continue;
            }
            bondConstraints.relaxMolecule(molecule);
        }

        forceSum.reset();
        //Compute forces on each atom
        potentialMaster.calculate(box, allAtoms, forceSum);

        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            IMolecule molecule = molecules.getMolecule(i);
            BondConstraints bondConstraints = (BondConstraints)shakeAgentManager.getAgent(molecule.getType());
            if (bondConstraints == null) {
                continue;
            }
            bondConstraints.redistributeForces(molecule, agentManager);
        }

        //Finish integration step
        IAtomList leafList = box.getLeafList();
        int nLeaf = leafList.getAtomCount();
        for (int iLeaf=0; iLeaf<nLeaf; iLeaf++) {
            IAtomKinetic a = (IAtomKinetic)leafList.getAtom(iLeaf);
//            System.out.println("force: "+((MyAgent)a.ia).force.toString());
            IVectorMutable velocity = a.getVelocity();
            if (Debug.ON && Debug.DEBUG_NOW && Debug.anyAtom(new AtomSetSinglet(a))) {
                System.out.println("second "+a+" v="+velocity+", f="+agentManager.getAgent(a).force);
            }
            if (a.getType().getMass() != 0) {
                velocity.PEa1Tv1(0.5*timeStep*a.getType().rm(),agentManager.getAgent(a).force);  //p += f(new)*dt/2
            }
        }

        /*
         * Rattle Part II
         */
        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            IMolecule molecule = molecules.getMolecule(i);
            BondConstraints bondConstraints = (BondConstraints)shakeAgentManager.getAgent(molecule.getType());
            if (bondConstraints == null) {
                continue;
            }
            
            IAtomList childList = molecule.getChildList();
            int[][] bondedAtoms = bondConstraints.bondedAtoms;
            IBoundary boundary = box.getBoundary();
            double[] bondLengths = bondConstraints.bondLengths;

            for (int j=0; j<childList.getAtomCount(); j++) {
                moved[1][j] = true;
            }
            
            for (int iter = 0; iter<maxIterations; iter++) {
                numIterations++;
                boolean success = true;
                for (int j=0; j<childList.getAtomCount(); j++) {
                    moved[0][j] = moved[1][j];
                    moved[1][j] = false;
                }
                for (int j=0; j<bondConstraints.bondedAtoms.length; j++) {
                    int iAtom1 = bondedAtoms[j][0];
                    int iAtom2 = bondedAtoms[j][1];
                    if (!moved[0][iAtom1] && !moved[0][iAtom2]) {
                        continue;
                    }
                    IAtomKinetic atom1 = (IAtomKinetic)childList.getAtom(iAtom1);
                    IAtomKinetic atom2 = (IAtomKinetic)childList.getAtom(iAtom2);
                    dr.Ev1Mv2(atom2.getPosition(), atom1.getPosition());
                    boundary.nearestImage(dr);
                    dv.Ev1Mv2(atom2.getVelocity(), atom1.getVelocity());
                    double drdotdv = dr.dot(dv);
                    double mass1 = ((IAtom)atom1).getType().getMass();
                    double mass2 = ((IAtom)atom2).getType().getMass();
                    double bl2 = bondLengths[j]*bondLengths[j];
                    double g = -drdotdv / ((1.0/mass1+1.0/mass2) * bl2);
                    if (Math.abs(g) > shakeTol) {
                        dr.TE(g);
                        
                        atom2.getVelocity().PEa1Tv1( 1.0/(mass2), dr);
                        atom1.getVelocity().PEa1Tv1(-1.0/(mass1), dr);
                        
                        moved[1][iAtom1] = true;
                        moved[1][iAtom2] = true;
                        success = false;
                    }
                }
                if (success) {
                    break;
                }
                if (iter == maxIterations-1) {
                    System.err.println("failed to converge in rattle for molecule "+i);
                }
            }
        }

        if(isothermal) {
            doThermostatInternal();
        }

        if (printInterval > 0 && stepCount%printInterval == 0) {
            double PE = meterPE.getDataAsScalar();
            double KE = meterKE.getDataAsScalar();
            int moleculeCount = box.getMoleculeList().getMoleculeCount();
            double fac = Joule.UNIT.fromSim(1.0/moleculeCount)*Constants.AVOGADRO;
            System.out.println(currentTime+" "+((double)numIterations)/numBondedMolecules+" "+Kelvin.UNIT.fromSim(2*KE/moleculeCount/6)+" "
                              +fac*KE+" "+fac*PE+" "+fac*(PE+KE));
        }
    }

    public void reset() {
        super.reset();
        /*
         * Rattle Part I
         */
        IMoleculeList molecules = box.getMoleculeList();
        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            IMolecule molecule = molecules.getMolecule(i);
            BondConstraints bondConstraints = (BondConstraints)shakeAgentManager.getAgent(molecule.getType());
            if (bondConstraints == null) {
                continue;
            }
            bondConstraints.redistributeForces(molecule, agentManager);
            
            IAtomList childList = molecule.getChildList();
            int[][] bondedAtoms = bondConstraints.bondedAtoms;
            IBoundary boundary = box.getBoundary();
            double[] bondLengths = bondConstraints.bondLengths;

            if (childList.getAtomCount() > moved[0].length) {
                moved = new boolean[2][childList.getAtomCount()];
            }
            for (int j=0; j<childList.getAtomCount(); j++) {
                moved[1][j] = true;
            }
            
            for (int iter = 0; iter<maxIterations; iter++) {
                boolean success = true;
                for (int j=0; j<childList.getAtomCount(); j++) {
                    moved[0][j] = moved[1][j];
                    moved[1][j] = false;
                }
                for (int j=0; j<bondConstraints.bondedAtoms.length; j++) {
                    int iAtom1 = bondedAtoms[j][0];
                    int iAtom2 = bondedAtoms[j][1];
                    if (!moved[0][iAtom1] && !moved[0][iAtom2]) {
                        continue;
                    }
                    IAtomKinetic atom1 = (IAtomKinetic)childList.getAtom(iAtom1);
                    IAtomKinetic atom2 = (IAtomKinetic)childList.getAtom(iAtom2);
                    dr.Ev1Mv2(atom2.getPosition(), atom1.getPosition());
                    boundary.nearestImage(dr);
                    dv.Ev1Mv2(atom2.getVelocity(), atom1.getVelocity());
                    double drdotdv = dr.dot(dv);
//                    System.out.println(j+" "+drdotdv);
                    double mass1 = ((IAtom)atom1).getType().getMass();
                    double mass2 = ((IAtom)atom2).getType().getMass();
                    double bl2 = bondLengths[j]*bondLengths[j];
                    double g = -drdotdv / ((1.0/mass1+1.0/mass2) * bl2);
                    if (Math.abs(g) > shakeTol) {
                        dr.TE(g);
                        
                        atom2.getVelocity().PEa1Tv1( 1.0/(mass2), dr);
                        atom1.getVelocity().PEa1Tv1(-1.0/(mass1), dr);
                        
                        moved[1][iAtom1] = true;
                        moved[1][iAtom2] = true;
                        success = false;
                    }
                }
                if (success) {
                    break;
                }
                if (iter == maxIterations-1) {
//                    System.err.println("failed to converge in shake for molecule "+i);
                }
            }
        }
    }
}
