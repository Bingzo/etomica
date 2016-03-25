/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.potential;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IMoleculeList;
import etomica.api.IPotentialMolecular;
import etomica.api.ISpecies;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.atom.SpeciesAgentManager;
import etomica.space.ISpace;
import etomica.space.Tensor;
import etomica.util.Constants;

/**
 * Effective semiclassical molecular potential using the approach of
 * Takahashi and Imada
 * 
 * http://dx.doi.org/10.1143/JPSJ.53.3765
 * 
 * as described by Schenter
 * 
 * http://dx.doi.org/10.1063/1.1505441
 * 
 * @author Andrew Schultz
 */
public class P2SemiclassicalMolecular implements IPotentialMolecular {

    protected final IPotentialMolecularTorque p2Classy;
    protected double temperature, fac;
    protected final SpeciesAgentManager agents;
    protected final ISpace space;
    
    public P2SemiclassicalMolecular(ISpace space, IPotentialMolecularTorque p2Classy) {
        this.space = space;
        this.p2Classy = p2Classy;
        if (p2Classy.nBody() != 2) throw new RuntimeException("I would really rather have a 2-body potential");
        agents = new SpeciesAgentManager(null);
    }
    
    public void setMoleculeInfo(ISpecies species, MoleculeInfo moleculeInfo) {
        agents.setAgent(species, moleculeInfo);
    }
    
    public void setTemperature(double newTemperature) {
        temperature = newTemperature;
        double hbar = Constants.PLANCK_H/(2*Math.PI);
        fac = hbar*hbar/(24*temperature*temperature);
    }
    
    public double getRange() {
        return p2Classy.getRange();
    }

    public void setBox(IBox box) {
        p2Classy.setBox(box);
    }

    public int nBody() {
        return 2;
    }

    public double energy(IMoleculeList molecules) {
        double uC = p2Classy.energy(molecules);
        IVector[][] gradAndTorque = p2Classy.gradientAndTorque(molecules);
        double sum = 0;
        for (int i=0; i<2; i++) {
            IMolecule iMol = molecules.getMolecule(i);
            MoleculeInfo molInfo = (MoleculeInfo)agents.getAgent(iMol.getType());
            if (molInfo == null) {
                molInfo = new MoleculeInfoBrute(space);
                agents.setAgent(iMol.getType(), molInfo);
            }
            double mi = molInfo.getMass(iMol);
            sum -= gradAndTorque[i][0].squared()/mi;
            IVector[] momentAndAxes = molInfo.getMomentAndAxes(iMol);
            IVector moment = momentAndAxes[0];
            for (int j=0; j<3; j++) {
                if (moment.getX(j) < 1e-10) continue;
                IVector axis = momentAndAxes[j+1];
                double torque = gradAndTorque[i][1].dot(axis);
                sum += torque*torque/moment.getX(j);
            }
        }
        double uFull = uC + fac*sum;
        return uFull;
    }

    public interface MoleculeInfo {
        /**
         * Returns the mass of the molecule.
         */
        public double getMass(IMolecule molecule);
        
        /**
         * Returns the moment of inertia as a vector containing Ix, Iy, Iz
         * and also the principle axes.  The 0 element is the moment of inertia
         * vector while elements 1, 2 and 3 are the principle axes.
         */
        public IVector[] getMomentAndAxes(IMolecule molecule);
    }
    
    public static class MoleculeInfoBrute implements MoleculeInfo {
        protected final IVectorMutable cm, rj;
        protected final Tensor id, moment, momentj, rjrj;
        protected final IVectorMutable[] rv;
        
        public MoleculeInfoBrute(ISpace space) {
            moment = space.makeTensor();
            momentj = space.makeTensor();
            rj = space.makeVector();
            rjrj = space.makeTensor();
            cm = space.makeVector();
            id = space.makeTensor();
            id.setComponent(0,0,1);
            id.setComponent(1,1,1);
            id.setComponent(2,2,1);
            rv = new IVectorMutable[4];
            for (int i=0; i<4; i++) {
                rv[i] = space.makeVector();
            }
        }

        public double getMass(IMolecule molecule) {
            double m = 0;
            moment.E(0);
            IAtomList atoms = molecule.getChildList();
            for (int j=0; j<atoms.getAtomCount(); j++) {
                IAtom a = atoms.getAtom(j);
                double mj = a.getType().getMass();
                m += mj;
            }
            return m;
        }
        
        public IVector[] getMomentAndAxes(IMolecule molecule) {
            double m = 0;
            moment.E(0);
            IAtomList atoms = molecule.getChildList();
            for (int j=0; j<atoms.getAtomCount(); j++) {
                IAtom a = atoms.getAtom(j);
                double mj = a.getType().getMass();
                cm.PEa1Tv1(mj, a.getPosition());
                m += mj;
            }
            cm.TE(1.0/m);
            for (int j=0; j<atoms.getAtomCount(); j++) {
                IAtom a = atoms.getAtom(j);
                double mj = a.getType().getMass();
                rj.Ev1Mv2(a.getPosition(), cm);
                momentj.E(id);
                momentj.TE(rj.squared());
                momentj.PEv1v2(rj, rj);
                momentj.TE(mj);
                moment.PE(momentj);
            }
            Matrix matrix = new Matrix(moment.toArray(), 3);
            EigenvalueDecomposition ed = new EigenvalueDecomposition(matrix);
            double[] evals = ed.getRealEigenvalues();
            double[][] evecs = ed.getV().getArray();
            rv[0].E(evals);
            for (int i=0; i<3; i++) {
                rv[i+1].E(evecs[i]);
            }
            return rv;
        }
    }
}
