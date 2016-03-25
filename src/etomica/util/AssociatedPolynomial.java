/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.util;//simulate..Special Functions
    //Refer Numerical Recipes Pg 254

public class AssociatedPolynomial implements java.io.Serializable {
    
    public AssociatedPolynomial(){
    }
    
    public static double plgndr(int l,int m,double theta){
      
      /*Computes the associated Legendre Polynomial Plm(x).Here m and l are integrers
        0 <= m <= l,while x lies in the range -1 <= x <= 1*/
        
        double pll,pmm,pmmp1;
        double fact,somx2,x;
        int    i,ll;
            
        x = Math.cos(theta);
        if(m < 0 || m > l || Math.abs(x) > 1.0){
            System.err.println("Bad arguments in method associatedPolynomial");
        }
        //Compute Pm,m
        pmm = 1.0;
        if(m > 0){
            somx2 = Math.sqrt((1.0-x)*(1.0+x));
            fact  = 1.0;
            for(i = 1;i <= m;i++){
                pmm *= -1.0*fact*somx2;
                fact += 2.0;
            }
        }
        if(l==m){ return pmm;}
        //Compute Pm,m+1
        pmmp1 = x*(2.0*m + 1.0)*pmm;
        if(l==(m+1)) {
            return pmmp1;
        }
        pll = 0.0;
        for(ll = m+2;ll <= l;ll++) {
            pll = (x*(2.0*ll - 1.0)*pmmp1 - (ll + m - 1.0)*pmm)/(ll - m);
            pmm   = pmmp1;
            pmmp1 = pll;
        }
        return pll;
     }
}
