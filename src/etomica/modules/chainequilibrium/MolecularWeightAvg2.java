/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.modules.chainequilibrium;

import etomica.data.DataPipe;
import etomica.data.DataProcessor;
import etomica.data.IData;
import etomica.data.IEtomicaDataInfo;
import etomica.data.types.DataDouble;
import etomica.data.types.DataDouble.DataInfoDouble;
import etomica.units.Quantity;

/**
 * Takes output from MeterChainLength as input and outputs the weight-average
 * molecular weight.
 * 
 * @author Andrew Schultz
 */
public class MolecularWeightAvg2 extends DataProcessor {

    public MolecularWeightAvg2() {
        data = new DataDouble();
        dataInfo = new DataInfoDouble("Avg MW", Quantity.DIMENSION);
    }

    protected IData processData(IData inputData) {
        double sum = 0, sum2 = 0;
        for (int i=0; i<inputData.getLength(); i++) {
            double v = inputData.getValue(i);
            sum += v * (i+1);
            sum2 += v;
        }
        data.x = sum / sum2;
        return data;
    }

    protected IEtomicaDataInfo processDataInfo(IEtomicaDataInfo inputDataInfo) {
        return dataInfo;
    }

    public DataPipe getDataCaster(IEtomicaDataInfo inputDataInfo) {
        return null;
    }

    protected final DataDouble data;
    protected final DataInfoDouble dataInfo;
}
