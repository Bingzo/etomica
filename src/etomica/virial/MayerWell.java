package etomica.virial;

import etomica.api.IBox;
import etomica.api.IMoleculeList;
import etomica.api.IPotential;

public class MayerWell implements MayerFunction {

    protected double sigma2, well2;
    
    public MayerWell(double sigma, double lambda) {
        sigma2 = sigma*sigma;
        well2 = sigma2*lambda*lambda;
    }

    public double f(IMoleculeList pair, double r2, double beta) {
        if (r2 < sigma2 || r2 > well2) return 0;
        return 1;
    }

    public IPotential getPotential() {
        return null;
    }

    public void setBox(IBox box) {}

}
