/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.normalmode;

import etomica.action.IAction;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IMolecule;
import etomica.api.IMoleculeList;
import etomica.api.ISpecies;
import etomica.api.IVectorMutable;
import etomica.data.DataSourceIndependent;
import etomica.data.DataTag;
import etomica.data.IData;
import etomica.data.IEtomicaDataInfo;
import etomica.data.IEtomicaDataSource;
import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataDoubleArray.DataInfoDoubleArray;
import etomica.data.types.DataFunction;
import etomica.data.types.DataFunction.DataInfoFunction;
import etomica.space.ISpace;
import etomica.units.Angle;
import etomica.util.DoubleRange;
import etomica.util.HistogramNotSoSimple;

/**
 * Meter that measures the average tilt angle (not the angle of average tilt!)
 *
 * @author Andrew Schultz
 */
public class MeterTiltHistogram implements IAction, IEtomicaDataSource, DataSourceIndependent {

    public MeterTiltHistogram(ISpace space, ISpecies species) {
        this.species = species;
        int nData = 180;
        dr = space.makeVector();
        histogram = new HistogramNotSoSimple(nData, new DoubleRange(0, Math.PI/2));
        histogram.setDoAveraging(false);
        xData = new DataDoubleArray(new int[]{nData}, histogram.xValues());
        xDataInfo = new DataInfoDoubleArray("angle", Angle.DIMENSION, new int[]{nData});
        data = new DataFunction(new int[]{nData}, histogram.getHistogram());
        dataInfo = new DataInfoFunction("tilt", Angle.DIMENSION, this);
        tag = new DataTag();
        dataInfo.addTag(tag);
        xTag = new DataTag();
        xDataInfo.addTag(xTag);
    }
    
    public void setBox(IBox newBox) {
        box = newBox;
    }
    
    public void reset() {
        histogram.reset();
    }

    public void actionPerformed() {
        IMoleculeList molecules = box.getMoleculeList(species);
        int nMolecules = molecules.getMoleculeCount();
        for (int i=0; i<nMolecules; i++) {
            IMolecule molecule = molecules.getMolecule(i);
            IAtomList atomList = molecule.getChildList();
            int leafCount = atomList.getAtomCount();
            dr.E(atomList.getAtom(leafCount-1).getPosition());
            dr.ME(atomList.getAtom(0).getPosition());
            dr.normalize();
            double sintheta = Math.sqrt(dr.getX(0)*dr.getX(0) + dr.getX(1)*dr.getX(1));
            double costheta = dr.getX(2);
            double theta = Math.atan2(sintheta, costheta);
            histogram.addValue(theta, 1/sintheta);
        }
    }

    public IData getData() {
        histogram.getHistogram();
        return data;
    }

    public IEtomicaDataInfo getDataInfo() {
        return dataInfo;
    }

    public DataTag getTag() {
        return tag;
    }
    
    public DataDoubleArray getIndependentData(int i) {
        return xData;
    }

    public DataInfoDoubleArray getIndependentDataInfo(int i) {
        return xDataInfo;
    }

    public int getIndependentArrayDimension() {
        return 1;
    }

    public DataTag getIndependentTag() {
        return xTag;
    }

    private static final long serialVersionUID = 1L;
    protected final ISpecies species;
    protected IBox box;
    protected final IVectorMutable dr;
    protected final DataFunction data;
    protected final DataInfoFunction dataInfo;
    protected final DataDoubleArray xData;
    protected final DataInfoDoubleArray xDataInfo;
    protected final DataTag tag, xTag;
    protected final HistogramNotSoSimple histogram;
}
