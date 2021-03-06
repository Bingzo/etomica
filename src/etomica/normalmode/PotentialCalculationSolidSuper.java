package etomica.normalmode;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBoundary;
import etomica.api.IBox;
import etomica.api.IPotentialAtomic;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.potential.Potential2SoftSpherical;
import etomica.potential.PotentialCalculation;
import etomica.space.ISpace;

/**
 * Sums the force on each iterated atom and adds it to the integrator agent
 * associated with the atom.
 */
public class PotentialCalculationSolidSuper implements PotentialCalculation {
        
    protected final CoordinateDefinition coordinateDefinition;
    protected final IVectorMutable drSite0, drSite1, drA, dr, drB;
    protected final ISpace space;
    protected double sum1, virialSum;
    protected double energySum, dadbSum;
    protected double fac1;
    protected IBox box;
    protected IBoundary boundary;
    protected boolean doD2;
    protected double d2sum;
    
    public PotentialCalculationSolidSuper(ISpace space, CoordinateDefinition coordinateDefinition) {
        this.coordinateDefinition = coordinateDefinition;
        this.space = space;
        drSite0 = space.makeVector();
        drSite1 = space.makeVector();
        drA = space.makeVector();
        dr = space.makeVector();
        drB = space.makeVector();
        setBox(coordinateDefinition.getBox());
    }
    
    public void setDoSecondDerivative(boolean doD2) {
        this.doD2 = doD2;
    }

    /**
     * Adds forces due to given potential acting on the atoms produced by the iterator.
     * Implemented for only 1- and 2-body potentials.
     */
    public void doCalculation(IAtomList atoms, IPotentialAtomic potential) {
        IAtom atom0 = atoms.getAtom(0);
        IAtom atom1 = atoms.getAtom(1);
//        if (atom0.getLeafIndex() >0 || atom1.getLeafIndex() > 1) return;
        energySum += potential.energy(atoms);
//        int i0 = atoms.getAtom(0).getLeafIndex();
//        int i1 = atoms.getAtom(1).getLeafIndex();
//        if (i0*i1 > 0 || i0+i1 != 8) return;
//        dr.Ev1Mv2(atom0.getPosition(), atom1.getPosition());
//        boundary.nearestImage(dr);
//        double r = Math.sqrt(dr.squared()); // distance between atoms
        IVector site0 = coordinateDefinition.getLatticePosition(atom0);
        IVector site1 = coordinateDefinition.getLatticePosition(atom1);
//        if (debug) {
//            System.out.println(site0+" "+atom0.getPosition());
//            System.out.println(site1+" "+atom1.getPosition());
//        }

        dr.Ev1Mv2(atom1.getPosition(), atom0.getPosition());
        boundary.nearestImage(dr);
        double r2 = dr.squared();

        drB.Ev1Mv2(site1, site0);
        boundary.nearestImage(drB);
        drB.TE(fac1);

        drSite0.Ev1Mv2(atom0.getPosition(), site0);
        drSite1.Ev1Mv2(atom1.getPosition(), site1);
        drA.Ev1Mv2(drSite1, drSite0);

        Potential2SoftSpherical potentialSoft = (Potential2SoftSpherical)potential;
        
        double du = potentialSoft.du(r2);
        sum1 -= du/r2*dr.dot(drB);
        virialSum += du;

//        dadbSum -= f[0].dot(drSite0);
//        dadbSum -= f[1].dot(drSite1);
//        dadbSum -= f[1].dot(drA);
//        System.out.println(f[1].getX(0)+" "+Math.sqrt(f[1].squared()/r2)*dr.getX(0)+" "+potentialSoft.du(r2)/r2*dr.getX(0));
        double dot = dr.dot(drA);
        dadbSum -= du/r2*dot;
//        if (atoms.getAtom(0).getLeafIndex()==0 && atoms.getAtom(1).getLeafIndex()==8) {
//            System.out.println("new 0 8 "+(dr.dot(f[0])+drA.dot(f[0])*fac1));
//        }
        //dr.PEa1Tv1(fac2, drA);
        //if (debug) System.out.print(drA+"\n "+dr+"\n");
        
        //sum += dr.dot(f[0]);
        
        // dr is vector between i and j
        // drA is vector between sites i and j
        if (doD2) {
            double r2A = drA.squared();
            double d2u = potentialSoft.d2u(r2);
//            System.out.println("rij "+Math.sqrt(r2));
//            System.out.println("dudr "+du/Math.sqrt(r2));
//            System.out.println("dot "+dot);
//            System.out.println();
//            System.out.println("drdT "+dot/Math.sqrt(r2));
//            System.out.println("d2udrdT "+d2u*dot/(r2*Math.sqrt(r2)));
//            System.out.println("ddotdT "+(r2A+dot));
//            System.out.println();
            d2sum -= d2u*dot*dot/(r2*r2) - du*dot*dot/(r2*r2)
                   + du*(r2A + dot)/r2;
//            System.out.println("gradient");
//            System.out.println(du*dr.getX(0)/r2+" "+du*dr.getX(1)/r2+" "+du*dr.getX(2)/r2);
//            double dud2u = (d2u + -du)/(r2*r2);
//            for (int i=0; i<3; i++) {
//                d2sum += du*drA.getX(i)*drA.getX(i);
//                for (int j=0; j<3; j++) {
//                    d2sum += dud2u * dr.getX(i)*dr.getX(j)*drA.getX(i)*drA.getX(j);
//                }
//            }
        }
    }
    
    public double getPressure1() {
        return sum1;
    }
    
    public double getVirialSum() {
        return virialSum;
    }

    public double getEnergySum() {
      return energySum;
    }
    
    /**
     * Returns sum of Fi dot ri
     */
    public double getDADBSum() {
      return dadbSum;
    }
    
    public double getD2Sum() {
        return d2sum;
    }
    
    public void zeroSum() {
        sum1 = virialSum = energySum = dadbSum = d2sum = 0;
        boundary = box.getBoundary();
        int D = boundary.getBoxSize().getD();
        fac1 = 1.0/(D*boundary.volume());
    }
    
    public void setBox(IBox box) {
        this.box = box;
        boundary = box.getBoundary();
    }
}
