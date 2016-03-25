/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.meam;

import etomica.units.ElectronVolt;

/**
 * This class provides the MEAM parameters for the SAC alloy constituents
 * (Ag, Cu, and Sn) and for the reference structures required for cross 
 * potentials (the pair potentials between atoms of different types).
 * 
 * The parameters for Ag are those published by Dong et al. in 2005, in a paper
 * studing a Sn-Ag system.  The parameters are the same for Ag as those initially 
 * published by Baskes in 1992.  The parameters for the reference 
 * structure used for the Sn-Ag cross potential (L12 Ag3Sn) are also included 
 * in the 2005 paper by Dong et al.
 * 
 * Note: the scaling parameter for Ag is that developed for a Sn-Ag system, not
 * for a Sn-Ag-Cu system.
 * 
 * The parameters for Cu are those published by Aguilar, Ravelo, and Baskes in 
 * 2000, in a paper studying a Sn-Cu system.  The parameters are essentially the
 * same for Cu as those initially determined by Baskes in 1992.  The parameters for
 * the reference structure used for the Sn-Cu cross potential (L12 Cu3Sn) are also
 * included in 2000 paper by Aguilar et al.  
 * 
 * Note: the scaling parameter for Cu is that developed for a Sn-Cu system, not 
 * for a Sn-Ag-Cu system.
 * 
 * The parameters for Sn were also published by Aguilar, Ravelo, and Baskes in 
 * 2000.  The parameters are essentially the same for Sn as those initially 
 * determined by Ravelo and Baskes in 1997.
 * 
 * Note: Ravelo and Baskes (1997) used the FCC structure as the reference structure for 
 * pure tin.  
 * 
 * Note: the scaling parameter for Sn is that developed for a Sn-Cu system, not 
 * for a Sn-Ag-Cu system.  
 * 
 * Created by K.R. Schadel and D.A. Kofke July 2005.  Modified from a pseudo EAM
 * class to a MEAM class by K.R. Schadel in February 2006.  
 */
public class ParameterSetMEAM {
	
	public ParameterSetMEAM(double Ec, double A, double r0, double a, 
			double b0, double b1, double b2, double b3, 
			double t1, double t2, double t3, double rho0, double Z,
			double Cmin, double Cmax) {
		this.Ec = Ec;
		this.A = A;
		this.r0 = r0;
		this.a = a;
		this.b0 = b0;
		this.b1 = b1;
		this.b2 = b2;
		this.b3 = b3;
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
		this.rho0 = rho0;
		this.Z = Z;
		this.Cmin = Cmin;
		this.Cmax = Cmax;
	}
	
	public final double Ec;  	
		//cohesive energy (sublimation energy) 
		//per atom of reference crystal structure (eV)
	public final double A; 
		//scaling factor for the embedding 
		//energy (unitless)
	public final double r0; 
		//equilibrium nearest-neighbor 
		//distance (Angstroms)
	public final double a; 
		//alpha, exponential decay factor for the 
		//universal energy function (unitless)
	public final double b0; 
		//beta0, exponential decay factor for 
		//s-orbital partial electron densities (unitless)
	public final double b1; 
		//exponential decay factor for 
		//p-orbital partial electron densities (unitless)
	public final double b2; 
		//exponential decay factor for 
		//d-orbital partial electron densities (unitless)
	public final double b3; 
		//exponential decay factor for 
		//f-orbital partial electron densities (unitless)
	public final double t1;
		//weighting factor for p-orbital partial electron densities
	public final double t2;
		//weighting factor for d-orbital partial electron densities
	public final double t3;
		//weighting factor for f-orbital partial electron densities
	public final double rho0;
		//scaling parameter 
	public final double Z; //coordination number for the reference 
		//crystal structure (the number of first nearest neighbors) (unitless)
	public final double Cmin;
	public final double Cmax;
	
	public static final ParameterSetMEAM Sn = new ParameterSetMEAM(ElectronVolt.UNIT.toSim(3.08), 1.0, 3.44, 
			6.20, 6.2, 6.0, 6.0, 6.0, 4.5, 6.5, -0.183, 1.0, 12.0, 0.8, 2.8);
	public static final ParameterSetMEAM Ag = new ParameterSetMEAM(ElectronVolt.UNIT.toSim(2.85), 1.06, 2.89, 
			5.89, 4.46, 2.2, 6.0, 2.2, 5.54, 2.45, 1.29, 1.0, 12.0, 2.0, 2.8);
	//used parameters from Cu-Sn paper (Aguilar, Ravelo, & Baskes 2000)
	public static final ParameterSetMEAM Cu = new ParameterSetMEAM(ElectronVolt.UNIT.toSim(3.62), 1.07, 2.50, 
			5.106, 3.62, 2.2, 6.0, 2.2, 3.14, 2.49, 2.95, 1.0, 12.0, 2.0, 2.8);
	//used paramters from Ni paper (Baskes 1997)
	public static final ParameterSetMEAM Ni = new ParameterSetMEAM(ElectronVolt.UNIT.toSim(4.45), 1.10, 2.49,
			4.99, 2.45, 1.5, 6.0, 1.5, 3.57, 1.60, 3.7, 1.0, 12.0, 2.0, 2.8);
	
	public static final ParameterSetMEAM Ag3Sn = new ParameterSetMEAM(ElectronVolt.UNIT.toSim(2.83), Double.NaN, 2.96,
			6.07, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 
			Double.NaN, Double.NaN, Double.NaN);
	public static final ParameterSetMEAM Cu3Sn = new ParameterSetMEAM(ElectronVolt.UNIT.toSim(3.5), Double.NaN, 2.68, 
			5.38, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
			Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
}
