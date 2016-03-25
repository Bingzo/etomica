package etomica.liquidLJ;
import etomica.api.IBox;
import etomica.api.IPotentialMaster;
import etomica.atom.iterator.IteratorDirective;
import etomica.data.DataTag;
import etomica.data.IData;
import etomica.data.IEtomicaDataInfo;
import etomica.data.IEtomicaDataSource;
import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataDoubleArray.DataInfoDoubleArray;
import etomica.space.ISpace;
import etomica.units.Energy;

/**
 * Meter for evaluation of the potential energy in a box.
 * Includes several related methods for computing the potential energy of a single
 * atom or molecule with all neighboring atoms
 *
 * @author David Kofke
 */
 
public class MeterPotentialEnergyCutoff implements IEtomicaDataSource {
    
    public MeterPotentialEnergyCutoff(IPotentialMaster potentialMaster, ISpace space, double[] cutoffs) {
        dataInfo = new DataInfoDoubleArray("energy", Energy.DIMENSION, new int[]{cutoffs.length});
        tag = new DataTag();
        data = new DataDoubleArray(cutoffs.length);
        dataInfo.addTag(tag);
        iteratorDirective.includeLrc = false;
        potential = potentialMaster;
        iteratorDirective.setDirection(IteratorDirective.Direction.UP);
        energy = new PotentialCalculationEnergySumCutoff(space, cutoffs);
    }
    
    public IEtomicaDataInfo getDataInfo() {
        return dataInfo;
    }

    public DataTag getTag() {
        return tag;
    }

   /**
    * Computes total potential energy for box.
    * Currently, does not include long-range correction to truncation of energy
    */
    public IData getData() {
        if (box == null) throw new IllegalStateException("must call setBox before using meter");
        energy.setBox(box);
    	energy.zeroSum();
        potential.calculate(box, iteratorDirective, energy);
        System.arraycopy(energy.getSums(), 0, data.getData(), 0, dataInfo.getLength());
        return data;
    }
    /**
     * @return Returns the box.
     */
    public IBox getBox() {
        return box;
    }
    /**
     * @param box The box to set.
     */
    public void setBox(IBox box) {
        this.box = box;
    }

    protected IBox box;
    protected final DataInfoDoubleArray dataInfo;
    protected final DataTag tag;
    protected final DataDoubleArray data;
    protected final IteratorDirective iteratorDirective = new IteratorDirective();
    protected final PotentialCalculationEnergySumCutoff energy;
    protected final IPotentialMaster potential;
}
