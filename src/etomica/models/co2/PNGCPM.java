/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.models.co2;

import etomica.action.MoleculeActionTranslateTo;
import etomica.api.IAtomList;
import etomica.api.IAtomType;
import etomica.api.IBoundary;
import etomica.api.IBox;
import etomica.api.IElement;
import etomica.api.IMolecule;
import etomica.api.IMoleculeList;
import etomica.api.IPotentialMolecular;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.atom.AtomTypeAgentManager;
import etomica.atom.MoleculePair;
import etomica.chem.elements.Carbon;
import etomica.chem.elements.Oxygen;
import etomica.config.IConformation;
import etomica.models.water.PNWaterGCPM;
import etomica.models.water.SpeciesWater4P;
import etomica.models.water.SpeciesWater4PCOM;
import etomica.potential.PotentialMolecular;
import etomica.simulation.Simulation;
import etomica.space.ISpace;
import etomica.space3d.Space3D;
import etomica.species.SpeciesSpheresHetero;
import etomica.units.Electron;
import etomica.units.Kelvin;

/**
 * Generic GCPM potential class, capable of handling both CO2 and water.
 * The potential takes an AtomTypeAgentManager that is responsible for
 * returning parameters for each atom.  Cross-parameters are computing using
 * mixing rules published in
 * 
 * http://dx.doi.org/10.1063/1.3519022
 * 
 * @author Andrew and Dave
 */
public class PNGCPM extends PotentialMolecular {

    public PNGCPM(ISpace space, AtomTypeAgentManager typeManager, int nAtomTypes) {
        this(space, typeManager, nAtomTypes, Integer.MAX_VALUE);
    }
    
    public PNGCPM(ISpace space, AtomTypeAgentManager typeManager, int nAtomTypes, int nBody) {
	    super(nBody, space);
	    this.typeManager = typeManager;
	    this.nAtomTypes = nAtomTypes;
	    pairAgents = new GCPMAgent[nAtomTypes][nAtomTypes];
	    pair = new MoleculePair();
        coreFac = 0.57*0.57;

        oldMu = space.makeVector();
        shift = space.makeVector();

        rijVector = space.makeVector();

        work = space.makeVector();

        Eq = new IVectorMutable[0][0];
        Ep = new IVectorMutable[0][0];
        mu = new IVectorMutable[0][0];
        component = Component.FULL;
    }
    
    public void setComponent(Component comp) {
        component = comp;
    }

    protected boolean oops = false;
    public double energy(IMoleculeList atoms){
        double sum = 0;
        if (component != Component.INDUCTION) {
            for (int i=0; i<atoms.getMoleculeCount()-1; i++) {
                pair.atom0 = atoms.getMolecule(i);
                for (int j=i+1; j<atoms.getMoleculeCount(); j++) {
                    pair.atom1 = atoms.getMolecule(j);
                    sum += getNonPolarizationEnergy(pair);
                    if (Double.isInfinite(sum)) {
                        return sum;
                    }
                }
            }
        }

        if (component != Component.TWO_BODY) {
            sum += getPolarizationEnergy(atoms);
        }
        if (!oops && Double.isNaN(sum)) {
            oops = true;
            energy(atoms);
            throw new RuntimeException("oops NaN");
        }
        return sum;
    }
    
    public static boolean debugme = false;

    public GCPMAgent getPairAgent(IAtomType type1, IAtomType type2) {
        int idx1 = type1.getIndex();
        int idx2 = type2.getIndex();
        if (pairAgents[idx1][idx2] != null) return pairAgents[idx1][idx2];
        GCPMAgent agent1 = (GCPMAgent)typeManager.getAgent(type1);
        if (idx1==idx2) {
            pairAgents[idx1][idx2] = agent1;
            return agent1;
        }
        GCPMAgent agent2 = (GCPMAgent)typeManager.getAgent(type2);
        double sigma = 0.5*(agent1.sigma + agent2.sigma);
        double epsilon = 2*agent1.epsilon*agent2.epsilon;
        if (epsilon>0) epsilon /= (agent1.epsilon + agent2.epsilon);
        double gamma = 0.5*(agent1.gamma + agent2.gamma);
        double tau = Math.sqrt(0.5*(agent1.tau*agent1.tau + agent2.tau*agent2.tau));
        pairAgents[idx1][idx2] = new GCPMAgent(sigma, epsilon, tau, gamma, agent1.charge, agent2.charge, 0, 0, 0);
        pairAgents[idx2][idx1] = pairAgents[idx1][idx2];
        return pairAgents[idx1][idx2];
    }
    
    /**
     * This returns the pairwise-additive portion of the GCPM potential for a
     * pair of atoms (dispersion + fixed-charge electrostatics)
     */
    public double getNonPolarizationEnergy(IMoleculeList molecules) {
        IAtomList atoms1 = molecules.getMolecule(0).getChildList();
        IAtomList atoms2 = molecules.getMolecule(1).getChildList();

        IVectorMutable C1r = atoms1.getAtom(0).getPosition();
        IVectorMutable C2r = atoms2.getAtom(0).getPosition();
        
        work.Ev1Mv2(C1r, C2r);
        shift.Ea1Tv1(-1,work);
		boundary.nearestImage(work);
        shift.PE(work);
        final boolean zeroShift = shift.squared() < 0.1;
        
        double r2 = work.squared();
        
        double sum =0;
        if (zeroShift) {
            for (int i=0; i<atoms1.getAtomCount(); i++) {
                for (int j=0; j<atoms2.getAtomCount(); j++) {
                    GCPMAgent pairAgent = getPairAgent(atoms1.getAtom(i).getType(), atoms2.getAtom(j).getType());
                    double epsilon = pairAgent.epsilon;
                    double r = Double.NaN;
                    if (epsilon>0) {
                        double sigma = pairAgent.sigma;
                        double gamma = pairAgent.gamma;
                        r2 = atoms1.getAtom(i).getPosition().Mv1Squared(atoms2.getAtom(j).getPosition());
                        r = Math.sqrt(r2);
                        double rOverSigma = r/sigma;
                        double sigma2OverR2 = 1/(rOverSigma*rOverSigma);
                        if (1/sigma2OverR2 < coreFac) return Double.POSITIVE_INFINITY;
                        double sixOverGamma = 6/gamma;
                        sum += epsilon/(1 - sixOverGamma)*(sixOverGamma*Math.exp(gamma*(1 - rOverSigma)) - sigma2OverR2*sigma2OverR2*sigma2OverR2);
                    }

                    double charge2 = pairAgent.charge2;
                    if (charge2!=0) {
                        double tau = pairAgent.tau;
                        if (Double.isNaN(r)) {
                            r2 = atoms1.getAtom(i).getPosition().Mv1Squared(atoms2.getAtom(j).getPosition());
                            r = Math.sqrt(r2);
                        }
                        sum += charge2/r*(1-org.apache.commons.math3.special.Erf.erfc(Math.sqrt(r2)/(2*tau)));
                    }
                }
            }
        }
        else {
            for (int i=0; i<atoms1.getAtomCount(); i++) {
                for (int j=0; j<atoms2.getAtomCount(); j++) {
                    GCPMAgent pairAgent = getPairAgent(atoms1.getAtom(i).getType(), atoms2.getAtom(j).getType());
                    double epsilon = pairAgent.epsilon;
                    double r = Double.NaN;
                    if (epsilon>0) {

                        double sigma = pairAgent.sigma;
                        IVector r1 = atoms1.getAtom(i).getPosition();
                        shift.PE(r1);
                        r2 = atoms2.getAtom(j).getPosition().Mv1Squared(shift);
                        shift.ME(r1);
                        r = Math.sqrt(r2);
    
                        double gamma = pairAgent.gamma;
    
                        double rOverSigma = r/sigma;
                        double sigma2OverR2 = 1/(rOverSigma*rOverSigma);
                        if (1/sigma2OverR2 < coreFac) return Double.POSITIVE_INFINITY;
                        double sixOverGamma = 6/gamma;
                   
                        sum += epsilon/(1 - sixOverGamma)*(sixOverGamma*Math.exp(gamma*(1 - rOverSigma)) - sigma2OverR2*sigma2OverR2*sigma2OverR2);//exp-6 potential(Udisp)
                    }

                    double charge2 = pairAgent.charge2;
                    if (charge2!=0) {
                        double tau = pairAgent.tau;
                        if (Double.isNaN(r)) {
                            IVector r1 = atoms1.getAtom(i).getPosition();
                            shift.PE(r1);
                            r2 = atoms2.getAtom(j).getPosition().Mv1Squared(shift);
                            shift.ME(r1);
                            r = Math.sqrt(r2);
                        }
                        sum += charge2/r*(1-org.apache.commons.math3.special.Erf.erfc(Math.sqrt(r2)/(2*tau)));
                    }
                }
            }
        }

        return sum;
    }

    /**
     * This returns the polarizable portion of the GCPM potential for any
     * number of atoms.
     */
    public double getPolarizationEnergy(IMoleculeList molecules) {
        
        final int moleculeCount = molecules.getMoleculeCount();
        if (Eq.length < moleculeCount+1) {
            int oldSize = Eq.length;
            Eq = (IVectorMutable[][])etomica.util.Arrays.resizeArray(Eq, moleculeCount);
            Ep = (IVectorMutable[][])etomica.util.Arrays.resizeArray(Ep, moleculeCount);
            mu= (IVectorMutable[][])etomica.util.Arrays.resizeArray(mu, moleculeCount);
            for (int i=oldSize; i<moleculeCount; i++) {
                Eq[i] = new IVectorMutable[0];
                Ep[i] = new IVectorMutable[0];
                mu[i] = new IVectorMutable[0];
            }
        }
        for (int i=0; i<moleculeCount; i++) {
            int nAtoms = molecules.getMolecule(i).getChildList().getAtomCount();
            if (Eq[i].length < nAtoms) {
                Eq[i] = new IVectorMutable[nAtoms];
                mu[i] = new IVectorMutable[nAtoms];
                Ep[i] = new IVectorMutable[nAtoms];
                for (int j=0; j<nAtoms; j++) {
                    Eq[i][j] = space.makeVector();
                    mu[i][j] = space.makeVector();
                    Ep[i][j] = space.makeVector();
                }
            }
            else {
                for (int j=0; j<nAtoms; j++) {
                    Eq[i][j].E(0);
                    mu[i][j].E(0);
                    Ep[i][j].E(0);
                }
            }
        }
        double sqrtpi = Math.sqrt(Math.PI);
        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            IAtomList iLeafAtoms = molecules.getMolecule(i).getChildList();
            for (int ii=0; ii<iLeafAtoms.getAtomCount(); ii++) {
                GCPMAgent agenti = (GCPMAgent)typeManager.getAgent(iLeafAtoms.getAtom(ii).getType());
                double alphaPerp = agenti.alphaPerp;
                double alphaPar = agenti.alphaPar;
                if (alphaPerp == 0 && alphaPar == 0) continue;
                IVectorMutable ri = iLeafAtoms.getAtom(ii).getPosition();

                for (int j=0; j<molecules.getMoleculeCount(); j++) {
                    if (i==j) continue;
                    IAtomList jLeafAtoms = molecules.getMolecule(j).getChildList();

                    IVectorMutable rj = jLeafAtoms.getAtom(0).getPosition();
                    work.Ev1Mv2(ri, rj);
                    shift.Ea1Tv1(-1,work);
                    boundary.nearestImage(work);
                    shift.PE(work);

                    for (int jj=0; jj<jLeafAtoms.getAtomCount(); jj++) {
                        GCPMAgent agentj = (GCPMAgent)typeManager.getAgent(jLeafAtoms.getAtom(jj).getType());
                        double qj = agentj.charge;
                        if (qj==0) continue;
                        GCPMAgent agentij = getPairAgent(iLeafAtoms.getAtom(ii).getType(),jLeafAtoms.getAtom(jj).getType());
                        double tauij = agentij.tau;
                        rj = jLeafAtoms.getAtom(jj).getPosition();
                
                        work.Ev1Mv2(ri, rj);
                        work.PE(shift);
                        double r2 = work.squared();
                        double r1 = Math.sqrt(r2);

                        double fac = qj/(r1*r2)*((1-org.apache.commons.math3.special.Erf.erfc(r1/(2*tauij)))
                                -r1/(sqrtpi*tauij)*Math.exp(-r2/(4*tauij*tauij)));
                        Eq[i][ii].PEa1Tv1(fac, work);
//                        if (i==0) {
//                            System.out.println("after "+j+" "+jj);
//                            System.out.println(Eq[i][ii]);
//                        }
                    }
                }
            }
        }

        int maxIter = 550;
        double mixIter = 0.9;
for (int iter=0; iter<maxIter; iter++) {
        double sumDeltaMu = 0;
        double sumMu = 0;
        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            IAtomList iLeafAtoms = molecules.getMolecule(i).getChildList();
            for (int ii=0; ii<iLeafAtoms.getAtomCount(); ii++) {
                GCPMAgent agenti = (GCPMAgent)typeManager.getAgent(iLeafAtoms.getAtom(ii).getType());
                double alphaPerp = agenti.alphaPerp;
                double alphaPar = agenti.alphaPar;
                if (alphaPerp == 0 && alphaPar == 0) continue;
                Ep[i][ii].PE(Eq[i][ii]);
                oldMu.E(mu[i][ii]);
                IVector parAxis = null;
                double alpha = alphaPerp;
                if (alphaPerp != alphaPar) {
                    parAxis = agenti.getParallelAxis(molecules.getMolecule(i));
                    double cosTheta = Math.abs(parAxis.dot(Ep[i][ii])/Math.sqrt(Ep[i][ii].squared()));
                    alpha = alphaPerp + cosTheta*(alphaPar-alphaPerp);
                }
                mu[i][ii].Ea1Tv1(alpha, Ep[i][ii]);
                mu[i][ii].TE(mixIter);
                mu[i][ii].PEa1Tv1(1-mixIter, oldMu);
                sumDeltaMu += mu[i][ii].Mv1Squared(oldMu);
                sumMu += mu[i][ii].squared();
            }
        }
        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            for (int ii=0; ii<Ep[i].length; ii++) {
                Ep[i][ii].E(0);
            }
        }

        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            IAtomList iLeafAtoms = molecules.getMolecule(i).getChildList();
            for (int ii=0; ii<iLeafAtoms.getAtomCount(); ii++) {
                GCPMAgent agenti = (GCPMAgent)typeManager.getAgent(iLeafAtoms.getAtom(ii).getType());
                if (agenti.alphaPerp == 0 && agenti.alphaPar == 0) continue;
                IVectorMutable ri = iLeafAtoms.getAtom(ii).getPosition();

                for (int j=i+1; j<molecules.getMoleculeCount(); j++) {
                    IAtomList jLeafAtoms = molecules.getMolecule(j).getChildList();
                    IVectorMutable rj = jLeafAtoms.getAtom(0).getPosition();
                    work.Ev1Mv2(ri, rj);
                    shift.Ea1Tv1(-1,work);
                    boundary.nearestImage(work);
                    shift.PE(work);

                    for (int jj=0; jj<jLeafAtoms.getAtomCount(); jj++) {
                        GCPMAgent agentj = (GCPMAgent)typeManager.getAgent(jLeafAtoms.getAtom(jj).getType());
                        if (agentj.alphaPerp == 0 && agentj.alphaPar == 0) continue;

                        GCPMAgent agentij = getPairAgent(iLeafAtoms.getAtom(ii).getType(),jLeafAtoms.getAtom(jj).getType());
                        double tauij = agentij.tau;
                        rj = jLeafAtoms.getAtom(jj).getPosition();
                
                        work.Ev1Mv2(ri, rj);
                        work.PE(shift);
                        double r2 = work.squared();
                        double r1 = Math.sqrt(r2);
                
                        if (r2 < coreFac*agentij.sigma) {
                            return Double.NaN;
                        }

                        double erf = (1-org.apache.commons.math3.special.Erf.erfc(r1/(2*tauij)));
                        double exp = Math.exp(-r2/(4*tauij*tauij));
                
                        double prefac = (r1/(tauij*sqrtpi))*exp;
                
                        double postfac = prefac * 0.666666666666666666666 * r2 / (4*tauij*tauij);
                
                        double fr = erf - prefac;

                        double fpr = fr - postfac;
                
                        Ep[i][ii].PEa1Tv1(-fr/(r1*r2), mu[j][jj]);
                
                        Ep[i][ii].PEa1Tv1(3*work.dot(mu[j][jj])*fpr/(r2*r2*r1), work);
                
                        Ep[j][jj].PEa1Tv1(-fr/(r1*r2), mu[i][ii]);
                        Ep[j][jj].PEa1Tv1(3*work.dot(mu[i][ii])*fpr/(r2*r2*r1), work);
                    }
                }
            }
        }
        
        if (debugme) {
            for (int i=0; i<molecules.getMoleculeCount(); i++) {
                for (int ii=0; ii<molecules.getMolecule(i).getChildList().getAtomCount(); ii++) {
                    if (Ep[i][ii].isZero()) continue;
                    System.out.println(iter+" "+i+" "+ii+" "+Ep[i][ii]+" "+mu[i][ii]);
                }
            }
        }

        if (sumDeltaMu < 1e-20) break;
        if (iter==maxIter-1) {
            System.err.println("we were unable to converge");
            System.err.println("sumDeltaMu "+sumDeltaMu);
            System.err.println("sumMu "+sumMu);
            throw new RuntimeException("bye");
        }
}
        double UpolAtkins = 0;
        for (int i=0; i<molecules.getMoleculeCount(); i++) {
            for (int ii=0; ii<molecules.getMolecule(i).getChildList().getAtomCount(); ii++) {
                UpolAtkins += Eq[i][ii].dot(mu[i][ii]);
            }
        }
        UpolAtkins *= -0.5;
        if (!debugme && Double.isNaN(UpolAtkins)) {
            debugme = true;
            getPolarizationEnergy(molecules);
            throw new RuntimeException("oops");
        }
        //x here represents P (almost).
        //For x to be P, the A of the Ax=b actually needs an extra factor of
        //alphaPol.  We'll add that bit in when we calculate UpolAtkins.  
        return UpolAtkins;
    }
    
    public final double getRange() {
        return Double.POSITIVE_INFINITY;
    }
    
    public void setBox(IBox box) {
    	boundary = box.getBoundary();
    }
    
    public P3GCPMAxilrodTeller makeAxilrodTeller() {
        return new P3GCPMAxilrodTeller(space);
    }

    protected final MoleculePair pair;
    protected IBoundary boundary;
    protected final double coreFac;
    protected IVectorMutable[][] Eq, Ep, mu;
    protected IVectorMutable oldMu;
    protected final IVectorMutable rijVector;
    protected final IVectorMutable work, shift;
    protected Component component;
    protected final AtomTypeAgentManager typeManager;
    protected final int nAtomTypes;
    protected final GCPMAgent[][] pairAgents;
    
    public enum Component { TWO_BODY, INDUCTION, FULL }
    
    public static class GCPMAgent {
        public final double sigma, epsilon, tau, gamma;
        public final double charge, charge2, alphaPar, alphaPerp;
        public final double E;
        
        public GCPMAgent(double sigma, double epsilon, double tau, double gamma, double charge, double alphaPar, double alphaPerp, double E) {
            this.sigma = sigma;
            this.epsilon = epsilon;
            this.tau = tau;
            this.gamma = gamma;
            this.charge = charge;
            this.charge2 = charge*charge;
            this.alphaPar = alphaPar;
            this.alphaPerp = alphaPerp;
            this.E = E;
        }

        public GCPMAgent(double sigma, double epsilon, double tau, double gamma, double charge1, double charge2, double alphaPar, double alphaPerp, double E) {
            this.sigma = sigma;
            this.epsilon = epsilon;
            this.tau = tau;
            this.gamma = gamma;
            this.charge2 = charge1*charge2;
            this.charge = Math.sqrt(this.charge2);
            this.alphaPar = alphaPar;
            this.alphaPerp = alphaPerp;
            this.E = E;
        }
        
        public IVector getParallelAxis(IMolecule mol) {
            return null;
        }
    }
    
    public class P3GCPMAxilrodTeller implements IPotentialMolecular {

        protected final IVectorMutable rij, rik, rjk;
        protected final IVectorMutable bveci, bvecj, bveck;
        protected final double[] cosg;
        protected final IVectorMutable norm;
        protected final IVectorMutable xveci, xvecj, xveck;
        protected final IVectorMutable yveci, yvecj, yveck;
        protected final double[] xx, yy, zz;
        
        public P3GCPMAxilrodTeller(ISpace space) {
            rij = space.makeVector();
            rik = space.makeVector();
            rjk = space.makeVector();
            cosg = new double[4];
            norm = space.makeVector();
            bveci = space.makeVector();
            bvecj = space.makeVector();
            bveck = space.makeVector();
            xveci = space.makeVector();
            xvecj = space.makeVector();
            xveck = space.makeVector();
            yveci = space.makeVector();
            yvecj = space.makeVector();
            yveck = space.makeVector();
            xx = new double[3];
            yy = new double[3];
            zz = new double[3];
        }
        
        public double getRange() {
            return Double.POSITIVE_INFINITY;
        }

        public void setBox(IBox box) {
            
        }

        public int nBody() {
            return 3;
        }

        public double energy(IMoleculeList molecules) {
            
            IAtomList atomsi = molecules.getMolecule(0).getChildList();
            IAtomList atomsj = molecules.getMolecule(1).getChildList();
            IAtomList atomsk = molecules.getMolecule(2).getChildList();
            double usum = 0;
            
for (int ii=0; ii<atomsi.getAtomCount(); ii++) {
    GCPMAgent agenti = (GCPMAgent)typeManager.getAgent(atomsi.getAtom(ii).getType());
    if (agenti.alphaPerp == 0 && agenti.alphaPar == 0) continue;
    double ei = agenti.E;
    IVector ri = atomsi.getAtom(0).getPosition();
    
    double iAlphaPerp = agenti.alphaPerp;
    double iAlphaAn = agenti.alphaPar - iAlphaPerp;
    if (iAlphaAn != 0) bveci.E(agenti.getParallelAxis(molecules.getMolecule(0)));
    
    for (int jj=0; jj<atomsj.getAtomCount(); jj++) {
        GCPMAgent agentj = (GCPMAgent)typeManager.getAgent(atomsj.getAtom(jj).getType());
        if (agentj.alphaPerp == 0 && agentj.alphaPar == 0) continue;
        double epij = ei * agentj.E;
        double esij = ei + agentj.E;
        double ej = agentj.E;
        IVector rj = atomsj.getAtom(0).getPosition();
        double jAlphaPerp = agentj.alphaPerp;
        double jAlphaAn = agentj.alphaPar - jAlphaPerp;
        
        if (jAlphaAn != 0) bvecj.E(agentj.getParallelAxis(molecules.getMolecule(1)));

        rij.Ev1Mv2(rj, ri);
        double drij2 = rij.squared();
        double drij = Math.sqrt(drij2);
        double drij3 = drij2*drij;
        rij.TE(1/drij);

        for (int kk=0; kk<atomsk.getAtomCount(); kk++) {
            GCPMAgent agentk = (GCPMAgent)typeManager.getAgent(atomsk.getAtom(kk).getType());
            if (agentk.alphaPerp == 0 && agentk.alphaPar == 0) continue;
            double epijk = epij * agentk.E;
            if (epijk == 0) continue;
            double esijk = esij + agentk.E;
            double ek = agentk.E;
            double esik = ei + ek;
            double esjk = ej + ek;

            double kAlphaPerp = agentk.alphaPerp;
            double kAlphaAn = agentk.alphaPar - kAlphaPerp;
            if (kAlphaAn!=0) bveck.E(agentk.getParallelAxis(molecules.getMolecule(2)));

            IVector rk = atomsk.getAtom(0).getPosition();
            rik.Ev1Mv2(rk, ri);
            rjk.Ev1Mv2(rk, rj);
            double drik2 = rik.squared();
            double drjk2 = rjk.squared();
            double drik = Math.sqrt(drik2);
            double drjk = Math.sqrt(drjk2);
            double drik3 = drik2*drik;
            double drjk3 = drjk2*drjk;
            rik.TE(1/drik);
            rjk.TE(1/drjk);
            cosg[0] = -rij.dot(rik);
            cosg[1] = -rij.dot(rjk);
            cosg[2] = rjk.dot(rik);
            cosg[3] = cosg[0];
            norm.E(rij);
            norm.XE(rik);
            norm.normalize();

            xveci.Ev1Pv2(rij, rik);
            xveci.normalize();
            yveci.E(norm);
            yveci.XE(xveci);

            xvecj.Ev1Mv2(rjk,rij);
            xvecj.normalize();
            yvecj.E(norm);
            yvecj.XE(xvecj);
            
            xveck.Ev1Pv2(rjk, rik);
            xveck.TE(-1);
            yveck.E(norm);
            yveck.XE(xveck);

            double eadd = 1;
            for (int a=0; a<3; a++) {
                xx[a] = Math.sqrt((1 + cosg[a])*(1 + cosg[a+1])) +
                        Math.sqrt((1 - cosg[a])*(1 - cosg[a+1])) * 0.5;
                eadd *= xx[a];
            }
            double polix = iAlphaPerp;
            if (iAlphaAn!=0) polix += iAlphaAn * Math.abs(bveci.dot(xveci));
            
            double poljx = jAlphaPerp;
            if (jAlphaAn!=0) poljx += jAlphaAn * Math.abs(bvecj.dot(xvecj));
            
            double polkx = kAlphaPerp;
            if (jAlphaAn!=0) polkx += kAlphaAn * Math.abs(bveck.dot(xveck));

            double nufac   = polkx*poljx*polix;
            eadd *= nufac;
            double u = eadd / (drij3*drik3*drjk3);

            // (z, z, z) matrix element
            // the polarizability is here the projection on the normal to the 
            // intermolecular plane. 
            double poliz = iAlphaPerp;
            if (iAlphaAn!=0) poliz += iAlphaAn * Math.abs(bveci.dot(norm));

            double poljz = jAlphaPerp;
            if (jAlphaAn!=0) poljz += jAlphaAn * Math.abs(bvecj.dot(norm));
            
            double polkz = kAlphaPerp;
            if (kAlphaAn!=0) polkz += kAlphaAn * Math.abs(bveck.dot(norm));
            
            nufac   = polkz*poljz*poliz;

            u += nufac / (drij3*drik3*drjk3);

            // (y, y, y) matrix element
            // the polarizability is here the projection on the axis orthogonal both
            // to the bisector axis, and to the normal of the intermolecular plane.
            eadd = 1;
            for (int a=0; a<3; a++) {
                yy[a] = -Math.sqrt((1 - cosg[a])*(1 - cosg[a+1])) -
                         Math.sqrt((1 + cosg[a])*(1 + cosg[a+1])) * 0.5;
                eadd *= yy[a];
            }

            double poliy = iAlphaPerp;
            if (iAlphaAn!=0) poliy += iAlphaAn * Math.abs(bveci.dot(yveci));
            
            double poljy = jAlphaPerp;
            if (jAlphaAn!=0) poljy += jAlphaAn * Math.abs(bvecj.dot(yvecj));
            
            double polky = kAlphaPerp;
            if (kAlphaAn!=0) polky += kAlphaAn * Math.abs(bveck.dot(yveck));

            nufac   = polkx*poljx*polix;
            u += eadd*nufac / (drij3*drik3*drjk3);

            // here come the mixed matrix elements. six in total. three double x's and
            // three double y's.
            // (x, x, y)
            double eprd = (Math.sqrt((1 + cosg[1])*(1 - cosg[2])) - 
                           Math.sqrt((1 - cosg[1])*(1 + cosg[2])) * 0.5) * 
                         (-Math.sqrt((1 + cosg[0])*(1 - cosg[2])) + 
                           Math.sqrt((1 - cosg[0])*(1 + cosg[2])) * 0.5);
            nufac= polix*poljx*polky;
            eadd = xx[0]*eprd*nufac;

            // (x, y, x)
            eprd = (Math.sqrt((1 + cosg[0])*(1 - cosg[1])) - 
                    Math.sqrt((1 - cosg[0])*(1 + cosg[1])) * 0.5) * 
                  (-Math.sqrt((1 + cosg[2])*(1 - cosg[1])) + 
                    Math.sqrt((1 - cosg[2])*(1 + cosg[1])) * 0.5);
            nufac= polix*poljy*polkx;
            eadd += xx[2]*eprd*nufac;

            // (y, x, x)
            eprd = (Math.sqrt((1 + cosg[2])*(1 - cosg[0])) -
                    Math.sqrt((1 - cosg[2])*(1 + cosg[0])) * 0.5) *
                  (-Math.sqrt((1 + cosg[1])*(1 - cosg[0])) +
                    Math.sqrt((1 - cosg[1])*(1 + cosg[0])) * 0.5);
            nufac= poliy*poljx*polkx;
            eadd += xx[1]*eprd*nufac;
            u  += eadd / (drij3*drik3*drjk3);

            // the double y's.
            // (y, y, x)
            eprd = (Math.sqrt((1.0 + cosg[2])*(1 - cosg[0])) -
                    Math.sqrt((1.0 - cosg[2])*(1 + cosg[0])) * 0.5) *
                  (-Math.sqrt((1.0 + cosg[2])*(1 - cosg[1])) +
                    Math.sqrt((1.0 - cosg[2])*(1 + cosg[1])) * 0.5);
            nufac= poliy*poljy*polkx;
            eadd = yy[0]*eprd*nufac;

            // (y, x, y)
            eprd = (Math.sqrt((1.0 + cosg[1])*(1 - cosg[2])) -
                    Math.sqrt((1.0 - cosg[1])*(1 + cosg[2])) * 0.5) *
                  (-Math.sqrt((1.0 + cosg[1])*(1 - cosg[0])) +
                    Math.sqrt((1.0 - cosg[1])*(1 + cosg[0])) * 0.5);
            nufac= poliy*poljx*polky;
            eadd += yy[2]*eprd*nufac;

            // (x, y, y)
            eprd = (Math.sqrt((1.0 + cosg[0])*(1 - cosg[1])) -
                    Math.sqrt((1.0 - cosg[0])*(1 + cosg[1])) * 0.5) *
                  (-Math.sqrt((1.0 + cosg[0])*(1 - cosg[2])) +
                    Math.sqrt((1.0 - cosg[0])*(1 + cosg[2])) * 0.5);
            nufac= polix*poljy*polky;
            eadd = eadd + yy[1]*eprd*nufac;

            u += eadd / (drij3*drik3*drjk3);
            
            if (Double.isNaN(u)) {
                energy(molecules);
                throw new RuntimeException("oops "+u);
            }
            double nufac0 = 1.5 * epijk * esijk / (esij * esik * esjk);
            usum += u*nufac0;
        }
    }
}
            return usum;
        }
    }
    
    public static void main2(String[] args) {
        double x = 0;
        double z = 4.;
        final ISpace space = Space3D.getInstance();
        Simulation sim = new Simulation(space);
        SpeciesSpheresHetero speciesCO2 = new SpeciesSpheresHetero(space, new IElement[]{Carbon.INSTANCE, Oxygen.INSTANCE});
        speciesCO2.setChildCount(new int[]{1,2});
        speciesCO2.setConformation(new IConformation() {
            
            public void initializePositions(IAtomList atomList) {
                atomList.getAtom(0).getPosition().E(0);
                atomList.getAtom(1).getPosition().setX(0,1.161);
                atomList.getAtom(2).getPosition().setX(0,-1.161);
            }
        });
        sim.addSpecies(speciesCO2);
        IBox box = new etomica.box.Box(space);
        sim.addBox(box);
        box.setNMolecules(speciesCO2, 2);
        box.getBoundary().setBoxSize(space.makeVector(new double[]{100,100,100}));
        IMolecule mol0 = box.getMoleculeList().getMolecule(0);
        IMolecule mol1 = box.getMoleculeList().getMolecule(1);
        
        mol0.getChildList().getAtom(0).getPosition().E(space.makeVector(new double[]{0.000000,0,0.000000 }));
        mol0.getChildList().getAtom(1).getPosition().E(space.makeVector(new double[]{-1.161,0,0 }));
        mol0.getChildList().getAtom(2).getPosition().E(space.makeVector(new double[]{1.161,0,0 }));
        mol1.getChildList().getAtom(0).getPosition().E(space.makeVector(new double[]{ 0,0,z }));
        mol1.getChildList().getAtom(1).getPosition().E(space.makeVector(new double[]{ -1.161,0,z}));
        mol1.getChildList().getAtom(2).getPosition().E(space.makeVector(new double[]{ 1.161,0,z }));
        
//        space.makeVector(new double[]{ 1.000000,-11.000000,-5.000000 }) 
//        space.makeVector(new double[]{ 0.732908,-10.699688,-3.910782 }) 
//        space.makeVector(new double[]{ 1.267092,-11.300312,-6.089218 }) 
        
//        MoleculeActionTranslateTo translator = new MoleculeActionTranslateTo(space);
//        translator.setDestination(space.makeVector(new double[]{x,0,z}));
//        translator.actionPerformed(mol1);
        AtomTypeAgentManager typeManager = new AtomTypeAgentManager(null);
        double qC = Electron.UNIT.toSim(0.6642);
        typeManager.setAgent(speciesCO2.getAtomType(0), new GCPMAgent(3.193,Kelvin.UNIT.toSim(71.34),0.61/1.0483,15.5,qC,4.05,1.95,0.0) {
            protected final IVectorMutable r = space.makeVector();
            public IVector getParallelAxis(IMolecule mol) {
                IAtomList atoms = mol.getChildList();
                r.Ev1Mv2(atoms.getAtom(2).getPosition(),atoms.getAtom(1).getPosition());
                r.normalize();
                return r;
            }
        });
        double qO = -0.5*qC;
        typeManager.setAgent(speciesCO2.getAtomType(1), new GCPMAgent(3.193*1.0483,Kelvin.UNIT.toSim(67.72),0.61,15.5,qO,0,0,0));
        PNGCPM p2 = new PNGCPM(space, typeManager, 2);
        p2.setBox(box);
//        p2.setComponent(PNGCPM.Component.INDUCTION);
        IMoleculeList molecules = box.getMoleculeList();
        double u = p2.energy(molecules);
        System.out.println(u);
        
        PNCO2GCPM p2c = new PNCO2GCPM(space);
        p2c.setBox(box);
//        p2c.setComponent(PNCO2GCPM.Component.INDUCTION);
        double uc = p2c.energy(molecules);
        System.out.println(uc);
        
    }
    
    public static void mainCO2(String[] args) {
        double x = 0;
        double z1 = 5.;
        double y2 = 2.;
        final ISpace space = Space3D.getInstance();
        Simulation sim = new Simulation(space);
        SpeciesSpheresHetero speciesCO2 = new SpeciesSpheresHetero(space, new IElement[]{Carbon.INSTANCE, Oxygen.INSTANCE});
        speciesCO2.setChildCount(new int[]{1,2});
        speciesCO2.setConformation(new IConformation() {
            
            public void initializePositions(IAtomList atomList) {
                atomList.getAtom(0).getPosition().E(0);
                atomList.getAtom(1).getPosition().setX(0,1.161);
                atomList.getAtom(2).getPosition().setX(0,-1.161);
            }
        });
        sim.addSpecies(speciesCO2);
        IBox box = new etomica.box.Box(space);
        sim.addBox(box);
        box.setNMolecules(speciesCO2, 3);
        box.getBoundary().setBoxSize(space.makeVector(new double[]{100,100,100}));
        IMolecule mol0 = box.getMoleculeList().getMolecule(0);
        IMolecule mol1 = box.getMoleculeList().getMolecule(1);
        IMolecule mol2 = box.getMoleculeList().getMolecule(2);
        
        mol0.getChildList().getAtom(0).getPosition().E(space.makeVector(new double[]{0.000000,0,0.000000 }));
        mol0.getChildList().getAtom(1).getPosition().E(space.makeVector(new double[]{-1.161,0,0 }));
        mol0.getChildList().getAtom(2).getPosition().E(space.makeVector(new double[]{1.161,0,0 }));
        mol1.getChildList().getAtom(0).getPosition().E(space.makeVector(new double[]{ 0,0,z1 }));
        mol1.getChildList().getAtom(1).getPosition().E(space.makeVector(new double[]{ -1.161,0,z1}));
        mol1.getChildList().getAtom(2).getPosition().E(space.makeVector(new double[]{ 1.161,0,z1 }));
        mol2.getChildList().getAtom(0).getPosition().E(space.makeVector(new double[]{ 0,y2,0}));
        mol2.getChildList().getAtom(1).getPosition().E(space.makeVector(new double[]{ -1.161,y2,0}));
        mol2.getChildList().getAtom(2).getPosition().E(space.makeVector(new double[]{ 1.161,y2,0 }));
        
        AtomTypeAgentManager typeManager = new AtomTypeAgentManager(null);
        double qC = Electron.UNIT.toSim(0.6642);
        typeManager.setAgent(speciesCO2.getAtomType(0), new GCPMAgent(3.193,Kelvin.UNIT.toSim(71.34),0.61/1.0483,15.5,qC,4.05,1.95,16.0/9.0*Kelvin.UNIT.toSim(2.52e4)) {
            protected final IVectorMutable r = space.makeVector();
            public IVector getParallelAxis(IMolecule mol) {
                IAtomList atoms = mol.getChildList();
                r.Ev1Mv2(atoms.getAtom(2).getPosition(),atoms.getAtom(1).getPosition());
                r.normalize();
                return r;
            }
        });
        double qO = -0.5*qC;
        typeManager.setAgent(speciesCO2.getAtomType(1), new GCPMAgent(3.193*1.0483,Kelvin.UNIT.toSim(67.72),0.61,15.5,qO,0,0,0));
        PNGCPM p2 = new PNGCPM(space, typeManager, 2);
        p2.setBox(box);
        P3GCPMAxilrodTeller p3 = p2.makeAxilrodTeller();
        IMoleculeList molecules = box.getMoleculeList();
        double u = p2.energy(molecules);
        System.out.println(u);
        
        PNCO2GCPM p2c = new PNCO2GCPM(space);
        p2c.setBox(box);
        PNCO2GCPM.P3GCPMAxilrodTeller p3c = p2c.makeAxilrodTeller();
        double uc = p2c.energy(molecules);
        System.out.println(uc);
        
    }

    public static void main(String[] args) {
        double x = 0;
        double z1 = 5.;
        double y2 = 7.;
        final ISpace space = Space3D.getInstance();
        Simulation sim = new Simulation(space);
        SpeciesWater4PCOM speciesWaterCOM = new SpeciesWater4PCOM(space);
        sim.addSpecies(speciesWaterCOM);
        IBox box = new etomica.box.Box(space);
        sim.addBox(box);
        box.setNMolecules(speciesWaterCOM, 3);
        box.getBoundary().setBoxSize(space.makeVector(new double[]{100,100,100}));
        IMolecule mol0 = box.getMoleculeList().getMolecule(0);
        IMolecule mol1 = box.getMoleculeList().getMolecule(1);
        IMolecule mol2 = box.getMoleculeList().getMolecule(2);
        
        MoleculeActionTranslateTo translator = new MoleculeActionTranslateTo(space);
        translator.setDestination(space.makeVector(new double[]{0,0,z1}));
        translator.actionPerformed(mol1);
        translator.setDestination(space.makeVector(new double[]{0,y2,0}));
        translator.actionPerformed(mol2);
        
        AtomTypeAgentManager typeManager = new AtomTypeAgentManager(null);
        typeManager.setAgent(speciesWaterCOM.getHydrogenType(), new GCPMAgent(1.0,0,0.455,12.75,Electron.UNIT.toSim(0.6113),0,0,0));
        typeManager.setAgent(speciesWaterCOM.getOxygenType(), new GCPMAgent(3.69,Kelvin.UNIT.toSim(110),0,12.75,0,0,0,0,0));
        typeManager.setAgent(speciesWaterCOM.getMType(), new GCPMAgent(1.0,0,0.610,12.75,Electron.UNIT.toSim(-1.2226),0,0,0));
        typeManager.setAgent(speciesWaterCOM.getCOMType(), new GCPMAgent(1.0,0,0.610,12.75,0,1.444,1.444,0));
        PNGCPM p2 = new PNGCPM(space, typeManager, 4);
        p2.setBox(box);
        IMoleculeList molecules = box.getMoleculeList();
        MoleculePair pair = new MoleculePair(molecules.getMolecule(0), molecules.getMolecule(1));
        double u = p2.energy(molecules);
        System.out.println(u);

        
        sim = new Simulation(space);
        SpeciesWater4P speciesWater = new SpeciesWater4P(space);
        sim.addSpecies(speciesWater);
        box = new etomica.box.Box(space);
        sim.addBox(box);
        box.setNMolecules(speciesWater, 3);
        box.getBoundary().setBoxSize(space.makeVector(new double[]{100,100,100}));
        mol0 = box.getMoleculeList().getMolecule(0);
        mol1 = box.getMoleculeList().getMolecule(1);
        mol2 = box.getMoleculeList().getMolecule(2);
        
        translator.setDestination(space.makeVector(new double[]{0,0,z1}));
        translator.actionPerformed(mol1);
        translator.setDestination(space.makeVector(new double[]{0,y2,0}));
        translator.actionPerformed(mol2);

        PNWaterGCPM p2c = new PNWaterGCPM(space);
        p2c.setBox(box);
        double uc = p2c.energy(molecules);
        System.out.println(uc);
        
    }

}
