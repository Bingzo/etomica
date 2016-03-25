package etomica.metastable;

import etomica.data.DataDump;
import etomica.data.DataPipe;
import etomica.data.DataProcessor;
import etomica.data.DataSourceIndependent;
import etomica.data.DataTag;
import etomica.data.IData;
import etomica.data.IEtomicaDataInfo;
import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataDoubleArray.DataInfoDoubleArray;
import etomica.data.types.DataFunction;
import etomica.units.Null;

public class DataProcessorXY extends DataProcessor implements DataSourceIndependent {

    protected final DataDump dumpX;
    protected DataFunction data;
    
    
    public DataProcessorXY(DataDump dumpX) {
        this.dumpX = dumpX;
    }

    public DataPipe getDataCaster(IEtomicaDataInfo inputDataInfo) {
        return null;
    }

    protected IData processData(IData inputData) {
        double[] y = data.getData();
        for (int i=0; i<inputData.getLength(); i++) {
            y[i] = inputData.getValue(i);
        }
        return data;
    }

    protected IEtomicaDataInfo processDataInfo(IEtomicaDataInfo inputDataInfo) {
        dataInfo = new DataFunction.DataInfoFunction("XY", Null.DIMENSION, this);
        data = new DataFunction(new int[]{dumpX.getDataInfo().getLength()});
        return dataInfo;
    }

    public DataDoubleArray getIndependentData(int i) {
        return (DataDoubleArray)dumpX.getData();
    }

    public DataInfoDoubleArray getIndependentDataInfo(int i) {
        return (DataInfoDoubleArray)dumpX.getDataInfo();
    }

    public int getIndependentArrayDimension() {
        return 1;
    }

    public DataTag getIndependentTag() {
        return dumpX.getTag();
    }

}
