package etomica.mappedvirial;

 import etomica.action.IAction;
import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IPotentialMaster;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.atom.AtomLeafAgentManager;
import etomica.atom.AtomLeafAgentManager.AgentSource;
import etomica.atom.iterator.IteratorDirective;
import etomica.data.DataSourceIndependent;
import etomica.data.DataTag;
import etomica.data.IData;
import etomica.data.IEtomicaDataInfo;
import etomica.data.IEtomicaDataSource;
import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataDoubleArray.DataInfoDoubleArray;
import etomica.data.types.DataFunction;
import etomica.data.types.DataFunction.DataInfoFunction;
import etomica.integrator.IntegratorVelocityVerlet;
import etomica.integrator.IntegratorVelocityVerlet.MyAgent;
import etomica.potential.Potential2SoftSpherical;
import etomica.potential.PotentialCalculationForceSum;
import etomica.space.ISpace;
import etomica.units.Force;
import etomica.units.Length;
import etomica.util.DoubleRange;
import etomica.util.Histogram;
import etomica.util.HistogramNotSoSimple;

public class MeterMeanForce implements IEtomicaDataSource, AgentSource<MyAgent>, DataSourceIndependent, IAction {

    protected final IPotentialMaster potentialMaster;
    protected final PotentialCalculationForceSum pcForce;
    protected final IBox box;
    protected final IteratorDirective allAtoms;
    protected final AtomLeafAgentManager<MyAgent> forceManager;
    protected final ISpace space;
    protected final Potential2SoftSpherical p2;
    protected final IVectorMutable dr, fij;
    protected final DataFunction data;
    protected final DataInfoFunction dataInfo;
    protected final DataTag tag, xTag;
    protected final HistogramNotSoSimple hist, hist2;
    protected final DataDoubleArray xData;
    protected final DataInfoDoubleArray xDataInfo;
    
    public MeterMeanForce(ISpace space, IPotentialMaster potentialMaster, Potential2SoftSpherical p2, IBox box, int nbins) {
        this.space = space;
        this.p2 = p2;
        this.box = box;
        this.potentialMaster = potentialMaster;
        pcForce = new PotentialCalculationForceSum();
        if (box != null) {
            forceManager = new AtomLeafAgentManager<MyAgent>(this, box, MyAgent.class);
            pcForce.setAgentManager(forceManager);
        }
        else {
            forceManager = null;
        }
        allAtoms = new IteratorDirective();
        dr = space.makeVector();
        fij = space.makeVector();

        xData = new DataDoubleArray(nbins);
        xDataInfo = new DataInfoDoubleArray("r", Length.DIMENSION, new int[]{nbins});
        xTag = new DataTag();
        xDataInfo.addTag(xTag);
        
        data = new DataFunction(new int[]{nbins});
        dataInfo = new DataInfoFunction("mean force", Force.DIMENSION, this);
        tag = new DataTag();
        dataInfo.addTag(tag);
        
        hist = new HistogramNotSoSimple(nbins, new DoubleRange(0,p2.getRange()));
        hist2 = new HistogramNotSoSimple(nbins, new DoubleRange(0,p2.getRange()));
    }
    
    public Histogram getHistogram() {
        return hist;
    }
    
    public Histogram getHistogram2() {
        return hist2;
    }

    public MyAgent makeAgent(IAtom a) {
        return new MyAgent(space);
    }
    
    public void releaseAgent(MyAgent agent, IAtom atom) {}

    public void actionPerformed() {
        pcForce.reset();
        potentialMaster.calculate(box, allAtoms, pcForce);
        IAtomList list = box.getLeafList();

        int n = list.getAtomCount();
        for (int i=0; i<n; i++) {
            IAtom a = list.getAtom(i);
            IVector fi = forceManager.getAgent(a).force;
            for (int j=i+1; j<n; j++) {
                IAtom b = list.getAtom(j);
                dr.Ev1Mv2(b.getPosition(),a.getPosition());
                box.getBoundary().nearestImage(dr);
                double r2 = dr.squared();
                double r = Math.sqrt(r2);
                if (r > p2.getRange()) continue;
                IVector fj = forceManager.getAgent(b).force;
                fij.Ev1Mv2(fj,fi);
                double fdr = 0.5*fij.dot(dr)/r;
                hist.addValue(r, fdr);
                hist2.addValue(r, fdr*fdr);
            }
        }
    }
    
    public IData getData() {
        double[] h = hist.getHistogram();
        double[] y = data.getData();
        System.arraycopy(h, 0, y, 0, h.length);
        return data;
    }

    public DataTag getTag() {
        return tag;
    }

    public IEtomicaDataInfo getDataInfo() {
        return dataInfo;
    }

    public DataDoubleArray getIndependentData(int i) {
        double[] r = hist.xValues();
        double[] x = xData.getData();
        System.arraycopy(r, 0, x, 0, r.length);
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
}
