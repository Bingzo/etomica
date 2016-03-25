/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.integrator.mcmove;
import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IPotentialMaster;
import etomica.api.IRandom;
import etomica.api.IVectorMutable;
import etomica.atom.AtomPositionGeometricCenter;
import etomica.atom.IAtomPositionDefinition;
import etomica.space.ISpace;
import etomica.space.RotationTensor;


public class MCMoveRotateMolecule3D extends MCMoveMolecule {
    
    private static final long serialVersionUID = 2L;
    protected transient IVectorMutable r0;
    protected transient RotationTensor rotationTensor;
    protected IAtomPositionDefinition positionDefinition;
    
    public MCMoveRotateMolecule3D(IPotentialMaster potentialMaster, IRandom random,
    		                      ISpace _space) {
        super(potentialMaster, random, _space, Math.PI/2, Math.PI);
        rotationTensor = _space.makeRotationTensor();
        r0 = _space.makeVector();
        positionDefinition = new AtomPositionGeometricCenter(space);
    }
     
    public IAtomPositionDefinition getPositionDefinition() {
		return positionDefinition;
    }

	public void setPositionDefinition(IAtomPositionDefinition positionDefinition) {
		this.positionDefinition = positionDefinition;
	}

	public boolean doTrial() {
//        System.out.println("doTrial MCMoveRotateMolecule called");
        
        if(box.getMoleculeList().getMoleculeCount()==0) {molecule = null; return false;}
            
        molecule = moleculeSource.getMolecule();
        energyMeter.setTarget(molecule);
        uOld = energyMeter.getDataAsScalar();
        
        if(Double.isInfinite(uOld)) {
            throw new RuntimeException("Overlap in initial state");
        }
        double dTheta = (2*random.nextDouble() - 1.0)*stepSize;
        rotationTensor.setAxial(r0.getD() == 3 ? random.nextInt(3) : 2,dTheta);

        r0.E(positionDefinition.position(molecule));
        doTransform();
        
        energyMeter.setTarget(molecule);
        return true;
    }
    
    protected void doTransform() {
        IAtomList childList = molecule.getChildList();
        for (int iChild = 0; iChild<childList.getAtomCount(); iChild++) {
            IAtom a = childList.getAtom(iChild);
            IVectorMutable r = a.getPosition();
            r.ME(r0);
            box.getBoundary().nearestImage(r);
            rotationTensor.transform(r);
            r.PE(r0);
        }
    }
    
    public void rejectNotify() {
        rotationTensor.invert();
        doTransform();
    }
}
