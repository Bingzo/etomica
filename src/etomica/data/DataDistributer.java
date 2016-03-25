/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.data;

import etomica.data.DataSplitter.IDataSinkFactory;
import etomica.data.types.DataDouble;
import etomica.util.Arrays;

/**
 * DataSink that takes a Data object and pushes it to a dataSink chosen
 * by an indexer.
 *
 * @author Andrew Schultz
 */
public class DataDistributer implements IDataSink {

    public DataDistributer(Indexer indexer, IDataSinkFactory dataSinkFactory) {
        tag = new DataTag();
        dataSinks = new IDataSink[0];
        this.indexer = indexer;
        this.dataSinkFactory = dataSinkFactory;
    }
 
    /**
     * Returns the DataTag associated with this DataSplitter
     * @return
     */
    public DataTag getTag() {
        return tag;
    }

    /**
     * Returns the DataSink for the ith output stream (corresponding to the ith
     * numerical value coming in).
     */
    public IDataSink getDataSink(int i) {
        return dataSinks[i];
    }

    public int getNumDataSinks() {
        return dataSinks.length;
    }
    
    /**
     * Sets the DataSink for the ith output stream (corresponding to the ith
     * numerical value coming in).
     */
    public void setDataSink(int i, IDataSink newDataSink) {
        if (i >= dataSinks.length) {
            dataSinks = (IDataSink[])Arrays.resizeArray(dataSinks, i+1);
        }
        dataSinks[i] = newDataSink;
        if (dataSinks[i] != null && dataInfo != null) {
            dataSinks[i].putDataInfo(dataInfo);
        }
    }

    public DataPipe getDataCaster(IEtomicaDataInfo incomingDataInfo) {
        return null;
    }

    public void putData(IData data) {
        int idx = indexer.getIndex();
        if (idx >= dataSinks.length || dataSinks[idx] == null) {
            setDataSink(idx, dataSinkFactory.makeDataSink(idx));
        }
        dataSinks[idx].putData(data);
    }

    public void putDataInfo(IEtomicaDataInfo incomingDataInfo) {
        dataInfo = incomingDataInfo;
        for (int i=0; i<dataSinks.length; i++) {
            if (dataSinks[i] != null) {
                dataSinks[i].putDataInfo(incomingDataInfo);
            }
        }
    }

    protected IDataSink[] dataSinks;
    protected DataDouble[] outData;
    protected IEtomicaDataInfo dataInfo;
    protected final DataTag tag;
    protected final IDataSinkFactory dataSinkFactory;
    protected final Indexer indexer;

    public static interface Indexer {
        public int getIndex();
    }
}
