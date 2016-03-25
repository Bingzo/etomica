/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.data.meter;

import etomica.api.IAtomList;
import etomica.api.IBoundary;
import etomica.api.IBox;
import etomica.api.IVectorMutable;
import etomica.atom.iterator.ApiLeafAtoms;
import etomica.atom.iterator.AtomsetIteratorBoxDependent;
import etomica.data.DataSourceScalar;
import etomica.math.SphericalHarmonics;
import etomica.math.geometry.coordinate.CoordinateConverter;
import etomica.space.ISpace;
import etomica.units.Undefined;

 /** The Bond Order Parameter Ql provides a metric that indicates the crystallinity of a box.
   * Appropriate for 3-dimensional system only.
   * Refer: Journal of Chemical Physics Vol.104 No.24,22nd June,1996__ Rate of crystal nucleation
   *
   * @author Jhumpa Adhikari
   */

public class MeterBondOrderParameterQ  extends DataSourceScalar {
	
    public MeterBondOrderParameterQ(ISpace _space) {
        this(_space, 5.0);
    }
    
    public MeterBondOrderParameterQ(ISpace _space, double rCut) {
        super("Bond Q Order Parameter", Undefined.DIMENSION);
        setL(6);
        setR2Cut(rCut*rCut);
        dr = _space.makeVector();
    }

    /**
     * Returns the value of the bond-order parameter for the given box
     * in its current configuration.  Returned array has only one element.
     */
    public double getDataAsScalar() {
        if (box == null) throw new IllegalStateException("must call setBox before using meter");
        int nbSum = 0;
        for(int m=-L; m<=L; m++) {
            int idx = m+L;
            Qreal[idx] = 0.0;
            Qimag[idx] = 0.0;
        }
        IBoundary boundary = box.getBoundary();
        pairIterator.setBox(box);
        pairIterator.reset();
        for (IAtomList pair = pairIterator.next(); pair != null;
             pair = pairIterator.next()) {
            dr.Ev1Mv2(pair.getAtom(1).getPosition(),pair.getAtom(0).getPosition());
            boundary.nearestImage(dr);
        	double r2 = dr.squared();
            if(r2 < r2Cut) {
                nbSum += 2;
                CoordinateConverter.toSpherical(dr,rThetaPhi);
                double theta = rThetaPhi[1];
                double phi = rThetaPhi[2];
                for(int m=-L; m<=L; m++) {
                    int idx = m+L;
                    double thetaC = Math.PI - theta;
                    double phiC = Math.PI + phi;
                    Qreal[idx] += SphericalHarmonics.realYm(L, m, theta, phi);
                    Qimag[idx] += SphericalHarmonics.imaginaryYm(L, m, theta, phi);
                    Qreal[idx] += SphericalHarmonics.realYm(L, m, thetaC, phiC);
                    Qimag[idx] += SphericalHarmonics.imaginaryYm(L, m, thetaC, phiC);
                }//end for
            }//end if
        }//end while
        double QL = 0;
        for(int m=-L; m<=L; m++) {
            int idx = m+L;
            QL += Qreal[idx]*Qreal[idx] - Qimag[idx]*Qimag[idx];
        }
        return Math.sqrt(coeff*QL)/nbSum;
    }
    
    public int getL(){return L;}
    /**
     * Sets the value of the parameter l, to indicate if Q4 or Q6 is to be computed.
     * Input of any value other than 4 causes L to be set to 6.
     */
    public void setL(int L){
        if(L != 4) L = 6;
        this.L = L;
        Qreal = new double[2*L + 1];
        Qimag = new double[2*L + 1];
        coeff = 4*Math.PI/(2*L + 1);
    }

    /**
     * Sets the iterator that gives the atoms over which the order parameter
     * will be calculated.  Default iterator is ApiLeafAtoms.
     * @param iter
     */
    public void setIterator(AtomsetIteratorBoxDependent iter) {
    	if(iter.nBody() != 2) throw new IllegalArgumentException("Illegal attempt to use a non-pair iterator");
    	pairIterator = iter;
    }
    /**
     * @return the iterator giving the atoms over which the order parameter
     * is defined.
     */
    public AtomsetIteratorBoxDependent getIterator() {
    	return pairIterator;
    }
    
    public double getR2Cut(){
    	return r2Cut;
    }
    
    public void setR2Cut(double r2c){
    	r2Cut = r2c;
    }
    
    /**
     * @return Returns the box.
     */
    public IBox getBox() {
        return box;
    }
    /**
     * @param box The box to set.
     */
    public void setBox(IBox box) {
        this.box = box;
    }

    private static final long serialVersionUID = 1L;
    private IBox box;
    private double[] Qreal, Qimag;
    private int L;
    private AtomsetIteratorBoxDependent pairIterator = new ApiLeafAtoms();
    private double r2Cut;
    private double[] rThetaPhi = new double[3];
    private double coeff;
    private final IVectorMutable dr;
}
