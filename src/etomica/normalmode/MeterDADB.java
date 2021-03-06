/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.normalmode;

import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IBox;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.atom.AtomLeafAgentManager;
import etomica.atom.AtomLeafAgentManager.AgentSource;
import etomica.atom.iterator.IteratorDirective;
import etomica.data.DataSourceScalar;
import etomica.data.DataTag;
import etomica.data.IData;
import etomica.data.IEtomicaDataInfo;
import etomica.data.IEtomicaDataSource;
import etomica.data.meter.MeterPotentialEnergy;
import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataDoubleArray.DataInfoDoubleArray;
import etomica.integrator.IntegratorVelocityVerlet.MyAgent;
import etomica.potential.PotentialCalculationForceSum;
import etomica.potential.PotentialMaster;
import etomica.space.ISpace;
import etomica.units.Null;

public class MeterDADB implements IEtomicaDataSource, AgentSource<MyAgent> {

    protected final DataDoubleArray data;
    protected final DataInfoDoubleArray dataInfo;
    protected final DataTag tag;
    protected final ISpace space;
    protected final CoordinateDefinition coordinateDefinition;
    protected final DataSourceScalar meterPE;
    protected final PotentialCalculationForceSum pcForceSum;
    protected final PotentialMaster potentialMaster;
    protected final AtomLeafAgentManager<MyAgent> forceManager;
    protected final IteratorDirective id;
    protected final IVectorMutable dr;
    protected double latticeEnergy;
    protected final double temperature;
    public static boolean justDADB = true;
    public static boolean justU = false;
    
    public MeterDADB(ISpace space, DataSourceScalar meterPE, PotentialMaster potentialMaster, CoordinateDefinition coordinateDefinition, double temperature) {
        int nData = justDADB ? 1 : 9;
        data = new DataDoubleArray(nData);
        dataInfo = new DataInfoDoubleArray("stuff", Null.DIMENSION, new int[]{nData});
        tag = new DataTag();
        dataInfo.addTag(tag);
        this.space = space;
        this.coordinateDefinition = coordinateDefinition;
        this.meterPE = meterPE;
        this.potentialMaster = potentialMaster;
        id = new IteratorDirective();
        pcForceSum = new PotentialCalculationForceSum();
        forceManager = new AtomLeafAgentManager<MyAgent>(this, coordinateDefinition.getBox(), MyAgent.class);
        pcForceSum.setAgentManager(forceManager);
        dr = space.makeVector();
        MeterPotentialEnergy meterPE2 = new MeterPotentialEnergy(potentialMaster);
        meterPE2.setBox(coordinateDefinition.getBox());
        latticeEnergy = meterPE2.getDataAsScalar();
        this.temperature = temperature;
    }
    
    public void setLatticeEnergy(double newLatticeEnergy) {
        latticeEnergy = newLatticeEnergy;
    }
    
    public IData getData() {
        IBox box = coordinateDefinition.getBox();
        
        pcForceSum.reset();
        
        double[] x = data.getData();
        double x0 = meterPE.getDataAsScalar() - latticeEnergy;
        potentialMaster.calculate(box, id, pcForceSum);
        IAtomList atoms = box.getLeafList();
        double sum = 0;
        for (int i=0; i<atoms.getAtomCount(); i++) {
            IAtom atom = atoms.getAtom(i);
            IVector lPos = coordinateDefinition.getLatticePosition(atom);
            IVector pos = atom.getPosition();
            dr.Ev1Mv2(pos, lPos);
            IVector force = forceManager.getAgent(atom).force;
            sum += force.dot(dr);
        }
        if (justDADB) {
            if (justU) {
                x[0] = (x0+latticeEnergy) + (atoms.getAtomCount()*1.5*temperature) + 0.5*sum;
            }
            else {
//                System.out.println(x0+" "+(0.5*sum)+" "+(x0+0.5*sum)/atoms.getAtomCount());
                x[0] = x0 + 0.5*sum;
            }
            
//            if (Math.random() < 0.01) {
//                System.out.println(0+" "+(x0+latticeEnergy));
//                for (int j=0; j<99; j++) {
//                    for (int i=0; i<atoms.getAtomCount(); i++) {
//                        IAtom atom = atoms.getAtom(i);
//                        IVector lPos = coordinateDefinition.getLatticePosition(atom);
//                        IVectorMutable pos = atom.getPosition();
//                        dr.Ev1Mv2(pos, lPos);
//                        pos.PEa1Tv1(-1.0/(100-j), dr);
//                    }
//                    double u = meterPE.getDataAsScalar();
//                    System.out.println(j+1+" "+(u-latticeEnergy));
//                }
//                System.exit(1);
//            }
            
            return data;
        }
        x[0] = x0;
        x[1] = sum;
        x[2] = x[0]+0.5*x[1];
        x[3] = (2*temperature-x[0])*x[0];   // x[0]*x[0] = 2*T*x[0] - x[3] 
        x[4] = (2*temperature-x[0])*x[2];
//        x[5] = (x[0]*x[0]+2*temperature*temperature)*x[2];
//        x[5] = (-6*temperature*(temperature - x[0]) - x[0]*x[0])*x[2];
        x[5] = x[0]*x[0];
        x[6] = x[0]*x[0]*x[2];
//        x[7] = x[0]*x[1];
        //dAc/dT = -(uAvg+0.5*fdrAvg))/ T^2
        //       = -<x2>/T^2
        //d2Ac/dT2 = ( (2*temperature*(uAvg + 0.5*fdrAvg) - u2Avg + uAvg*uAvg + 0.5*(uAvg*fdrAvg - ufdrAvg) ) / T^4
        //         = (<x4> + <x0>*<x2>)/T^4;
        //d3Ac/dT3 = ( -6*T*T*(uAvg+0.5fdrAvg) + 6*T*(u2Avg - uAvg + 0.5*(ufdrAvg - uAvg*fdrAvg)) - uuu# - 0.5*uuf#)
        //  xyz# = <xyz> - <x><yz> - <y><xz> - <z><xy> + 2<x><y><z>
        //d3Ac/dT3 = (<x5> - 6T*<x0><x2> + 3*<u2>*<x2> + 2*<x0>*(-<x0>*<x2> + 0.5*<uF>) ) / T^6
        //d3Ac/dT3 = (<x5> - 6T*<x0><x2> + 3*<x6>*<x2> + 2*<x0>*(-<x0>*<x2> + 0.5*<x7>) ) / T^6
        return data;
    }

    public DataTag getTag() {
        return tag;
    }

    public IEtomicaDataInfo getDataInfo() {
        return dataInfo;
    }

    public final MyAgent makeAgent(IAtom a) {
        return new MyAgent(space);
    }
    
    public void releaseAgent(MyAgent agent, IAtom atom) {}
}
