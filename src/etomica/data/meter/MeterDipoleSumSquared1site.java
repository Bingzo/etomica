package etomica.data.meter;

import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IMoleculeList;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.atom.IAtomOriented;
import etomica.data.DataSourceScalar;
import etomica.space.ISpace;
import etomica.space3d.Vector3D;
import etomica.units.CompoundDimension;
import etomica.units.Dimension;
import etomica.units.Dipole;
/**
 * meter for (sum dipole)^2, used for dielectric constant calculation
 * 
 * @author shu
 */
public class MeterDipoleSumSquared1site extends DataSourceScalar {
	 
    private IBox box;
    private IVectorMutable dipoleSum;
    private double dipoleMagnitude;
    
	public MeterDipoleSumSquared1site(ISpace space, IBox box, double dipoleMagnitude) {
		super("dipoleSum^2", new CompoundDimension(new Dimension[]{Dipole.DIMENSION},new double[]{2.0}));
		this.box=box;
		this.dipoleMagnitude = dipoleMagnitude;
		dipoleSum = space.makeVector();
	}

	public double getDataAsScalar() {
		dipoleSum = new Vector3D();
		if (box == null) throw new IllegalStateException("no box");
		IMoleculeList moleculeList = box.getMoleculeList();
		int numMolecule = moleculeList.getMoleculeCount();
		for (int i=0;i<numMolecule; i++){
			IAtomList atomList = moleculeList.getMolecule(i).getChildList();
			IAtomOriented atom = (IAtomOriented) atomList.getAtom(0);
	        IVector v = atom.getOrientation().getDirection();
			dipoleSum.PE(v);
        }
        double squared = dipoleMagnitude*dipoleMagnitude*dipoleSum.squared();
        return squared;
	}

    public IBox getBox() {
    	return box;
    }
    public void setBox(IBox _box) {
    	box = _box;
    }

}