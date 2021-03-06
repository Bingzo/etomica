/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.action;

import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IMoleculeList;
import etomica.api.IVectorMutable;
import etomica.space.BoundaryDeformablePeriodic;
import etomica.space.ISpace;
import etomica.space3d.Space3D;

/**
 * Class that perform anisotropic angle fluctuation 
 * 
 * @author Tai Boon Tan
 *
 */
public class BoxInflateAnisotropic extends BoxInflate{
    
    public BoxInflateAnisotropic(ISpace space){
    	super(space);
    	
        cVector = space.makeVector();
        cVectorOld = space.makeVector();
        
    }
    
    public BoxInflateAnisotropic(IBox box, ISpace space){
    	this(space);
    	setBox(box);
    	deltaX = new double[box.getMoleculeList().getMoleculeCount()];
    }
    
    /**
     * Performs anisotropic inflation.
     */
    public void actionPerformed() {
        if(box == null) throw new RuntimeException("oops");
        
        translationVector = translator.getTranslationVector();
        
        cVectorOld.E(box.getBoundary().getEdgeVector(2));
        
        double cx = cVector.getX(0);
        double deltacx = (cx-cVectorOld.getX(0));
        double cz = cVector.getX(2);
        double slope = deltacx/cz;
        
        IMoleculeList molecules = box.getMoleculeList();
        IVectorMutable comVector = Space3D.makeVector(3);
        for(int i=0; i<molecules.getMoleculeCount(); i++) {
            IMolecule molecule = molecules.getMolecule(i);
            comVector.E(moleculeCenter.position(molecule));
            
            // delta_x = slope * z
            double h = comVector.getX(2);
            deltaX[i] = slope*h;
            translationVector.setX(0, deltaX[i]);
            groupScaler.actionPerformed(molecule);
        }

        // set the edgeVectors according to the scaling before passing it to BoundaryDeformablePeriodic
        // only scale the x-, y- and z-axes
        // for the x-component of edgeVector[2], it is not being scale. This is done so as to fluctuate
        // the beta-angle
        
        ((BoundaryDeformablePeriodic)box.getBoundary()).setEdgeVector(2, cVector);
    }
    
    
    public void undo(){
    	IMoleculeList molecules = box.getMoleculeList();
    	
    	for(int i=0; i<molecules.getMoleculeCount(); i++) {
    		IMolecule molecule = molecules.getMolecule(i);
    		translationVector.E(new double[]{-deltaX[i], 0.0, 0.0});
    		groupScaler.actionPerformed(molecule);
    	}
    	
        ((BoundaryDeformablePeriodic)box.getBoundary()).setEdgeVector(2, cVectorOld);
    }
        
    public void setCVector(IVectorMutable cVec){
    	cVector = cVec;
    }
    
    protected IVectorMutable cVector, cVectorOld, translationVector;
    protected double[] deltaX;
	private static final long serialVersionUID = 1L;
}
