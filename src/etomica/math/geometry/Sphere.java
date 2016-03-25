/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.math.geometry;


/**
 * A sphere in 3D.
 * 
 * @author David Kofke
 *  
 */

/*
 * History Created on Jun 28, 2005 by kofke
 */
public class Sphere extends Hypersphere {

    /**
     * Creates sphere with unit radius
     */
    public Sphere() {
        this(1.0);
    }

    /**
     * Creates sphere of the given radius.
     */
    public Sphere(double radius) {
        super(3, radius);
    }
    
    /**
     * Returns the volume for the present sphere radius.
     */
    public double getVolume() {
        return 4. * Math.PI / 3. * radius * radius * radius;
    }
}
