/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.math;

import etomica.util.AssociatedPolynomial;


public class SphericalHarmonics implements java.io.Serializable {
             
    private static double factor(int l,int m){
        return Math.sqrt(((2.0*l + 1.0)*SpecialFunctions.factorial(l-m))/(4.0*Math.PI*SpecialFunctions.factorial(l+m)));
    }
    
    private static double y(int l,int m,double theta){
        return factor(l,m)*AssociatedPolynomial.plgndr(l, m, theta);
    }
    
    public static double realYm(int l, int m, double theta, double phi){
        if(m < 0.0){
            m = -m;
            return Math.pow(-1,m)*y(l,m,theta)*Math.cos(m*phi);
        }
        return y(l,m,theta)*Math.cos(m*phi);
    }
    
    public static double imaginaryYm(int l,int m,double theta,double phi){
        if(m < 0.0){
            m = -m;
            return Math.pow(-1,m)*y(l,m,theta)*Math.sin(m*phi)*(-1.0);
        }
        return y(l,m,theta)*Math.sin(m*phi);
    }
}
