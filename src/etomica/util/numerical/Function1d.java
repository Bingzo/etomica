/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.util.numerical;

import etomica.util.FunctionMultiDimensionalDifferentiable;

public class Function1d implements etomica.util.FunctionDifferentiable{

	public Function1d(){
		
	}
	
	protected int ncom;
	protected double[] pcom, xicom; 
	protected FunctionMultiDimensionalDifferentiable function;
	
	
	public double f(double t){

		double f;
		double[] xt = new double[ncom];
		
		for (int j=0; j<ncom; j++){
			xt[j] = pcom[j] + t*xicom[j];
			
		}
		f = function.f(xt);
		
		return f;
	}
	
	public double df(int d, double t){
	
		double df1 =0.0;
		double[] xt = new double[ncom];
		int[] derivative = new int[ncom];
		
		for (int j=0; j<ncom; j++){
			xt[j] = pcom[j] + t*xicom[j];
		}
	
		for (int j=0; j<ncom; j++){
			
			derivative[j] = d;
			
			double dfj = function.df(derivative,xt);
			df1 += dfj*xicom[j];
			derivative[j] = 0;
		}
		return df1;
	}

	public double[] getPcom() {
		return pcom;
	}

	public void setPcom(double[] pcom) {
		this.pcom = pcom;
		ncom = pcom.length;
	}

	public double[] getXicom() {
		return xicom;
	}

	public void setXicom(double[] xicom) {
		this.xicom = xicom;
	}
	
	public FunctionMultiDimensionalDifferentiable getFunction(){
		return this.function;
	}
	
	public void setFunction(FunctionMultiDimensionalDifferentiable function){
		this.function = function;
	}
}
