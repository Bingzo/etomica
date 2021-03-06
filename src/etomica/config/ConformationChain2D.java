/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/*
 * Created on Mar 24, 2005
 */
package etomica.config;

import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.space.ISpace;

/**
 * @author nancycribbin
 *
 */
public class ConformationChain2D extends ConformationChain {
	
    public ConformationChain2D(ISpace space, IVector[] vex){
		super(space);
		vectors = new IVectorMutable[vex.length];
		for (int i=0; i<vex.length; i++) {
		    vectors[i] = space.makeVector();
		}
		for(int i = 0; i < vex.length; i++){
			vectors[i].E(vex[i]);
		}
		tracker = 0;
	}
	
	/* (non-Javadoc)
	 * @see etomica.ConformationChain#reset()
	 */
	public void reset() { 
		tracker = 0;
	}

	/* (non-Javadoc)
	 * @see etomica.ConformationChain#nextVector()
	 */
	public IVector nextVector() {
		if(tracker>=vectors.length){
		    reset();
		}
	    tracker += 1;
	    return vectors[tracker-1];
	}

    private static final long serialVersionUID = 1L;
	protected final IVectorMutable[] vectors;
	protected int tracker;			//Tracker is used to track which vector the counter is on.
}
