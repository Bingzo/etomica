/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package etomica.virial.simulations;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import org.json.simple.JSONObject;
import etomica.api.IAtom;
import etomica.api.IAtomList;
import etomica.api.IAtomType;
import etomica.api.IIntegratorEvent;
import etomica.api.IIntegratorListener;
import etomica.api.IMoleculeList;
import etomica.api.IPotentialAtomic;
import etomica.api.IVector;
import etomica.api.IVectorMutable;
import etomica.atom.AtomHydrogen;
import etomica.atom.AtomTypeOrientedSphere;
import etomica.atom.IAtomOriented;
import etomica.atom.IAtomTypeOriented;
import etomica.atom.iterator.ApiIntergroupCoupled;
import etomica.chem.elements.Nitrogen;
import etomica.config.ConformationLinear;
import etomica.data.IData;
import etomica.data.types.DataGroup;
import etomica.integrator.mcmove.MCMove;
import etomica.potential.P2NitrogenHellmann;
import etomica.potential.P2SemiclassicalAtomic;
import etomica.potential.P3NitrogenHellmannNonAdditive;
import etomica.potential.PotentialMolecularMonatomic;
import etomica.potential.P2SemiclassicalAtomic.AtomInfo;
import etomica.space.Space;
import etomica.space3d.Space3D;
import etomica.species.SpeciesSpheresHetero;
import etomica.units.Kelvin;
import etomica.util.Constants;
import etomica.util.ParameterBase;
import etomica.util.ParseArgs;
import etomica.virial.ClusterWheatleyHS;
import etomica.virial.ClusterWheatleyMultibody;
import etomica.virial.ClusterWheatleySoft;
import etomica.virial.MCMoveClusterRingRegrow;
import etomica.virial.MCMoveClusterRingRegrowOrientation;
import etomica.virial.MayerFunctionMolecularThreeBody;
import etomica.virial.MayerFunctionNonAdditive;
import etomica.virial.MayerGeneral;
import etomica.virial.MayerHardSphere;
import etomica.virial.PotentialGroupPI;
import etomica.virial.cluster.Standard;

public class VirialN2PI {
    public static void main(String[] args) {
        VirialN2Param params = new VirialN2Param();
        boolean isCommandLine = args.length > 0;
        if (isCommandLine) {
            ParseArgs parseArgs = new ParseArgs(params);
            parseArgs.parseArgs(args, true);
        }
        else {
            // default options - choose these before committing to CVS
//            params.nBeads = 8;
//            params.temperatureK = 500;
//            params.numSteps = (long)1E6;
//            params.pN2HellmannA = false;
            

            // runtime options - make changes in these and not the default options above
            params.nPoints = 3;
            params.nBeads = 8;
            params.temperatureK = 500;
            params.numSteps = (long)1E6;
            params.scBeads = true;
            params.nonAdditive = true;
        }
        
        final int nPoints = params.nPoints;
        final double temperatureK = params.temperatureK;
        final double temperature = Kelvin.UNIT.toSim(temperatureK);
        final boolean pairOnly = params.pairOnly;
        long steps = params.numSteps;
        double sigmaHSRef = params.sigmaHSRef;
        double refFrac = params.refFrac;
        final boolean p2N2HellmannA = params.pN2HellmannA;
        final int nBeads = params.nBeads;
        final int beadFac = params.beadFac;
        final boolean scBeads = params.scBeads;
        final boolean nonAdditive = params.nonAdditive;
        final boolean p3N2HellmannA = params.p3N2HellmannA;

        final double[] HSB = new double[8];
        HSB[2] = Standard.B2HS(sigmaHSRef);
        HSB[3] = Standard.B3HS(sigmaHSRef);
        HSB[4] = Standard.B4HS(sigmaHSRef);
        HSB[5] = Standard.B5HS(sigmaHSRef);
        HSB[6] = Standard.B6HS(sigmaHSRef);
        HSB[7] = Standard.B7HS(sigmaHSRef);
        
        System.out.println("Overlap sampling for N2 pair potential of Hellmann (2013) at " + temperatureK + " K");
        System.out.println("Path Integral Monte Carlo (PIMC) calculation with P = "+nBeads);
        System.out.println("Semi-classical beads: "+scBeads);
        if (nPoints == 3) System.out.println("Non-additive: "+nonAdditive);
        
        System.out.println("Reference diagram: B"+nPoints+" for hard spheres with diameter " + sigmaHSRef + " Angstroms");
        
        System.out.println("B"+nPoints+"HS: "+HSB[nPoints]);
        if (steps%1000 != 0) {
            throw new RuntimeException("steps should be a multiple of 1000");
        }
        System.out.println(steps+" steps (1000 IntegratorOverlap steps of "+(steps/1000)+")");
        
        Space space = Space3D.getInstance();
        // make ref and tar clusters
        MayerHardSphere fRef = new MayerHardSphere(sigmaHSRef);
        ClusterWheatleyHS refCluster = new ClusterWheatleyHS(nPoints, fRef);
        refCluster.setTemperature(temperature);
        
        final P2NitrogenHellmann p2Full = new P2NitrogenHellmann(space);
        if (p2N2HellmannA) p2Full.parametersB = false;
        final P2SemiclassicalAtomic p2PISC = new P2SemiclassicalAtomic(space, p2Full, temperature*nBeads*nBeads);
        final IPotentialAtomic p2;
        if (scBeads) {
            p2 = p2PISC;
        }
        else {
            p2 = p2Full;
        }        
        PotentialGroupPI pTarGroup = new PotentialGroupPI(beadFac);
        pTarGroup.addPotential(p2, new ApiIntergroupCoupled());
        MayerGeneral fTar = new MayerGeneral(pTarGroup) {
            public double f(IMoleculeList pair, double r2, double beta) {                
                return super.f(pair, r2, beta/nBeads);
            }
        };
        
        ClusterWheatleySoft tarCluster = new ClusterWheatleySoft(nPoints, fTar, 1e-12);
        if (nPoints == 3 && nonAdditive) {
            final P3NitrogenHellmannNonAdditive p3N2NonAdditive = new P3NitrogenHellmannNonAdditive(space);
            if (p3N2HellmannA) p3N2NonAdditive.parametersB = false;
            final IPotentialAtomic p3 = p3N2NonAdditive;
            PotentialMolecularMonatomic p3N2Molecular = new PotentialMolecularMonatomic(space, p3);
            MayerFunctionMolecularThreeBody f3Tar = new MayerFunctionMolecularThreeBody(p3N2Molecular);            
            MayerFunctionNonAdditive [] m1 = new MayerFunctionNonAdditive[]{null,null,null,f3Tar};
            tarCluster = new ClusterWheatleyMultibody(nPoints, fTar, m1);
        }
        tarCluster.setTemperature(temperature);
        
        // make species        
        IAtomTypeOriented atype = new AtomTypeOrientedSphere(Nitrogen.INSTANCE, space);
        SpeciesSpheresHetero speciesN2 = null;        
        speciesN2 = new SpeciesSpheresHetero(space,new IAtomTypeOriented [] {atype}) {
            protected IAtom makeLeafAtom(IAtomType leafType) {
                return new AtomHydrogen(space,(IAtomTypeOriented)leafType,blN2);
            }
        };
        speciesN2.setChildCount(new int [] {nBeads});
        speciesN2.setConformation(new ConformationLinear(space, 0));        
        // make simulation
        final SimulationVirialOverlap2 sim = new SimulationVirialOverlap2(space, speciesN2, temperature, refCluster, tarCluster);
//        sim.init();
        sim.integratorOS.setNumSubSteps(1000);
        steps /= 1000;
        final IVectorMutable[] rv = space.makeVectorArray(4);
        rv[0].setX(0, massN*blN2*blN2*0.25);
        rv[0].setX(1, massN*blN2*blN2*0.25);
        p2PISC.setAtomInfo(speciesN2.getAtomType(0), new AtomInfo() {
            public IVector[] getMomentAndAxes(IAtomOriented molecule) {
                
                // rv[0,2] = 0
                // rv[3] is the orientation
                rv[3].E(molecule.getOrientation().getDirection());
                // rv[1] is an axis perpendicular to rv[3]
                rv[1].E(0);
                if (Math.abs(rv[3].getX(0)) < 0.5) {
                    rv[1].setX(0, 1);
                }
                else if (Math.abs(rv[3].getX(1)) < 0.5) {
                    rv[1].setX(1, 1);
                }
                else {
                    rv[1].setX(2, 1);
                }
                rv[2].Ea1Tv1(rv[1].dot(rv[3]), rv[3]);
                rv[1].ME(rv[2]);
                // rv[2] is an axis perpendicular to rv[3] and rv[1]
                rv[2].E(rv[1]);
                rv[2].XE(rv[3]);
                return rv;
            }
        });
//        add additional moves here, simulation already has translation and rotation moves
//        rotation is a bit pointless when we can regrow the ring completely

        if (nBeads != 1) {
            sim.integrators[0].getMoveManager().removeMCMove(sim.mcMoveRotate[0]);
            sim.integrators[1].getMoveManager().removeMCMove(sim.mcMoveRotate[1]);
        }        
        
//        ring regrow translation
        MCMoveClusterRingRegrow refTr = new MCMoveClusterRingRegrow(sim.getRandom(), space);
        double lambda = Constants.PLANCK_H/Math.sqrt(2*Math.PI*massN*temperature);
        refTr.setEnergyFactor(2*nBeads*Math.PI/(lambda*lambda));

        MCMoveClusterRingRegrow tarTr = new MCMoveClusterRingRegrow(sim.getRandom(), space);
        tarTr.setEnergyFactor(2*nBeads*Math.PI/(lambda*lambda));

        if (nBeads != 1) {
            sim.integrators[0].getMoveManager().addMCMove(refTr);
            sim.integrators[1].getMoveManager().addMCMove(tarTr);
        }
//        ring regrow orientation
        MCMoveClusterRingRegrowOrientation refOr = new MCMoveClusterRingRegrowOrientation(sim.getRandom(), space, nBeads);        
        MCMoveClusterRingRegrowOrientation tarOr = new MCMoveClusterRingRegrowOrientation(sim.getRandom(), space, nBeads);
        refOr.setStiffness(temperature, massN);
        tarOr.setStiffness(temperature, massN);
        
        if (nBeads != 1) {
            sim.integrators[0].getMoveManager().addMCMove(refOr);
            sim.integrators[1].getMoveManager().addMCMove(tarOr);
        }
        
        System.out.println();
        String refFileName = null;
        if (isCommandLine) {
            String tempString = ""+temperatureK;
            if (temperatureK == (int)temperatureK) {
                // temperature is an integer, use "200" instead of "200.0"
                tempString = ""+(int)temperatureK;
            }
            refFileName = "refpref"+nPoints;
            refFileName += pairOnly ? "_2b" : "_3b";
            refFileName += "_"+tempString+"_PI";
        }
        long t1 = System.currentTimeMillis();
        // if using 3-body potential for B3, we must select initial configuration
        // that is not overlapping for any two molecules
        if (nPoints == 3 && nonAdditive) {
            IAtomList tarList = sim.box[1].getLeafList();        
            for (int i=0; i<nBeads; i++) {
                for (int j=0; j<3; j++) {
                    int k = j*nBeads + i;
                    IVectorMutable p = tarList.getAtom(k).getPosition();
                    p.setX(j, 4.0);
                }
            }            
            sim.box[1].trialNotify();
            sim.box[1].acceptNotify();
            sim.box[1].getSampleCluster().value(sim.box[1]);
        }        
        
        sim.initRefPref(refFileName, steps/40);
        if (refFrac >= 0) {
            sim.integratorOS.setRefStepFraction(refFrac);
            sim.integratorOS.setAdjustStepFraction(false);
        }
        sim.equilibrate(refFileName, steps/10);
        System.out.println("equilibration finished");
        
        sim.integratorOS.setNumSubSteps((int)steps);
        sim.setAccumulatorBlockSize(steps);
        sim.integratorOS.setAggressiveAdjustStepFraction(true);
        sim.ai.setMaxSteps(1000);
        
        System.out.println("MC Move step sizes (ref)    "+sim.mcMoveTranslate[0].getStepSize());
        System.out.println("MC Move step sizes (target) "+sim.mcMoveTranslate[1].getStepSize());
        
        final double refIntegralF = HSB[nPoints];
        if (! isCommandLine) {
            IIntegratorListener progressReport = new IIntegratorListener() {        
                public void integratorInitialized(IIntegratorEvent e) {}
                public void integratorStepStarted(IIntegratorEvent e) {}
                public void integratorStepFinished(IIntegratorEvent e) {
                    if ((sim.integratorOS.getStepCount()*10) % sim.ai.getMaxSteps() != 0) return;
                    System.out.print(sim.integratorOS.getStepCount()+" steps: ");
                    double[] ratioAndError = sim.dvo.getAverageAndError();
                    double ratio = ratioAndError[0];
                    double error = ratioAndError[1];
                    //                    System.out.println("abs average: "+ratio*refIntegralF+" error: "+error*refIntegralF);
                    System.out.println("abs average: "+ratio*refIntegralF+" error: "+error*refIntegralF);
                    if (ratio == 0 || Double.isNaN(ratio)) {
                        throw new RuntimeException("oops");
                    }
                }
            };
            sim.integratorOS.getEventManager().addListener(progressReport);
        }
        // this is where the simulation takes place
        sim.getController().actionPerformed();
        //end of simulation
        long t2 = System.currentTimeMillis();
        
        
        System.out.println(" ");
        System.out.println("final reference step fraction "+sim.integratorOS.getIdealRefStepFraction());
        System.out.println("actual reference step fraction "+sim.integratorOS.getRefStepFraction());
        System.out.println(" ");
        
        System.out.println("Reference system: ");
        List<MCMove> refMoves = sim.integrators[0].getMoveManager().getMCMoves();
        for (MCMove m : refMoves) {
            double acc = m.getTracker().acceptanceRatio();
            System.out.println(m.toString()+" acceptance ratio: "+acc);            
        }
        System.out.println("Target system: ");
        List<MCMove> tarMoves = sim.integrators[1].getMoveManager().getMCMoves();        
        for (MCMove m : tarMoves) {
            double acc = m.getTracker().acceptanceRatio();

//            if (acc == 1) {
//                throw new RuntimeException("something seems fishy");
//            }
            System.out.println(m.toString()+" acceptance ratio: "+acc);
        }
     // Printing results here
        double[] ratioAndError = sim.dvo.getAverageAndError();
        double ratio = ratioAndError[0];
        double error = ratioAndError[1];
        double bn = ratio*HSB[nPoints];
        double bnError = error*Math.abs(HSB[nPoints]);
        
        System.out.println("ratio average: "+ratio+" error: "+error);
        System.out.println("abs average: "+bn+" error: "+bnError);
        DataGroup allYourBase = (DataGroup)sim.accumulators[0].getData();
        IData ratioData = allYourBase.getData(sim.accumulators[0].RATIO.index);
        IData ratioErrorData = allYourBase.getData(sim.accumulators[0].RATIO_ERROR.index);
        IData averageData = allYourBase.getData(sim.accumulators[0].AVERAGE.index);
        IData stdevData = allYourBase.getData(sim.accumulators[0].STANDARD_DEVIATION.index);
        IData errorData = allYourBase.getData(sim.accumulators[0].ERROR.index);
        IData correlationData = allYourBase.getData(sim.accumulators[0].BLOCK_CORRELATION.index);
        IData covarianceData = allYourBase.getData(sim.accumulators[0].BLOCK_COVARIANCE.index);
        double correlationCoef = covarianceData.getValue(1)/Math.sqrt(covarianceData.getValue(0)*covarianceData.getValue(3));
        correlationCoef = (Double.isNaN(correlationCoef) || Double.isInfinite(correlationCoef)) ? 0 : correlationCoef;
        double refAvg = averageData.getValue(0);
        double refOvAvg = averageData.getValue(1);
        System.out.print(String.format("reference ratio average: %20.15e error:  %10.5e  cor: %6.4f\n", ratioData.getValue(1), ratioErrorData.getValue(1), correlationCoef));
        System.out.print(String.format("reference average: %20.15e stdev: %9.4e error: %9.4e cor: %6.4f\n",
                              averageData.getValue(0), stdevData.getValue(0), errorData.getValue(0), correlationData.getValue(0)));
        System.out.print(String.format("reference overlap average: %20.15e stdev: %9.4e error: %9.3e cor: %6.4f\n",
                              averageData.getValue(1), stdevData.getValue(1), errorData.getValue(1), correlationData.getValue(1)));
        
        allYourBase = (DataGroup)sim.accumulators[1].getData();
        ratioData = allYourBase.getData(sim.accumulators[1].RATIO.index);
        ratioErrorData = allYourBase.getData(sim.accumulators[1].RATIO_ERROR.index);
        averageData = allYourBase.getData(sim.accumulators[1].AVERAGE.index);
        stdevData = allYourBase.getData(sim.accumulators[1].STANDARD_DEVIATION.index);
        errorData = allYourBase.getData(sim.accumulators[1].ERROR.index);
        correlationData = allYourBase.getData(sim.accumulators[1].BLOCK_CORRELATION.index);
        covarianceData = allYourBase.getData(sim.accumulators[1].BLOCK_COVARIANCE.index);
        int n = sim.numExtraTargetClusters;
        correlationCoef = covarianceData.getValue(n+1)/Math.sqrt(covarianceData.getValue(0)*covarianceData.getValue((n+2)*(n+2)-1));
        correlationCoef = (Double.isNaN(correlationCoef) || Double.isInfinite(correlationCoef)) ? 0 : correlationCoef;
        double tarAvg = averageData.getValue(0);
        double tarOvAvg = averageData.getValue(1);
        double tarCorr = correlationData.getValue(0);
        System.out.print(String.format("target ratio average: %20.15e  error: %10.5e  cor: %6.4f\n", ratioData.getValue(n+1), ratioErrorData.getValue(n+1), correlationCoef));
        System.out.print(String.format("target average: %20.15e stdev: %9.4e error: %9.4e cor: %6.4f\n",
                              averageData.getValue(0), stdevData.getValue(0), errorData.getValue(0), correlationData.getValue(0)));
        System.out.print(String.format("target overlap average: %20.15e stdev: %9.4e error: %9.4e cor: %6.4f\n",
                              averageData.getValue(n+1), stdevData.getValue(n+1), errorData.getValue(n+1), correlationData.getValue(n+1)));
        if (isCommandLine) {
            LinkedHashMap resultsMap = new LinkedHashMap();
            resultsMap.put("temperature", temperatureK);
            resultsMap.put("P", nBeads);
            resultsMap.put("bn", bn);
            resultsMap.put("bnError", bnError);
            resultsMap.put("refAvg", refAvg);
            resultsMap.put("refOvAvg", refOvAvg);
            resultsMap.put("tarAvg", tarAvg);
            resultsMap.put("tarOvAvg", tarOvAvg);
            resultsMap.put("tarCorr", tarCorr);
            for (MCMove m : tarMoves) {
                double acc = m.getTracker().acceptanceRatio();
                resultsMap.put(m.toString(), acc);
            }

            if ((t2-t1)/1000.0 > 24*3600) {
                resultsMap.put("time",(t2-t1)/(24*3600*1000.0));
                resultsMap.put("unit","days");
                System.out.println("time: "+(t2-t1)/(24*3600*1000.0)+" days");
            }
            else if ((t2-t1)/1000.0 > 3600) {
                resultsMap.put("time",(t2-t1)/(3600*1000.0));
                resultsMap.put("unit","hrs");
                System.out.println("time: "+(t2-t1)/(3600*1000.0)+" hrs");
            }
            else if ((t2-t1)/1000.0 > 60) {
                resultsMap.put("time",(t2-t1)/(60*1000.0));
                resultsMap.put("unit","mins");
                System.out.println("time: "+(t2-t1)/(60*1000.0)+" mins");
            }
            else {
                resultsMap.put("time",(t2-t1)/(1000.0));
                resultsMap.put("unit","secs");
                System.out.println("time: "+(t2-t1)/1000.0+" secs");
            }                    
            String jsonFileName = params.jarFile;
            jsonFileName += "B"+nPoints+"PI";
            if (scBeads) jsonFileName += "SC";
            if (nPoints == 3) {
                if (nonAdditive) jsonFileName += "N";
                jsonFileName += "A";
            }            
            jsonFileName += (int)Math.log10((double)params.numSteps)+"s";
            if (temperatureK == (int) temperatureK) { 
                jsonFileName += (int)temperatureK+"K";
            }
            else {
                jsonFileName += temperatureK+"K";
            }            
            jsonFileName += nBeads+"nb";
            jsonFileName += ".json";
            try {
                FileWriter jsonFile = new FileWriter(jsonFileName);
                jsonFile.write(JSONObject.toJSONString(resultsMap));
                jsonFile.write("\n");
                jsonFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }                
        }
//        sim.printResults(HSB[nPoints]);        
        
        if ((t2-t1)/1000.0 > 24*3600) {
            System.out.println("time: "+(t2-t1)/(24*3600*1000.0)+" days");
        }
        else if ((t2-t1)/1000.0 > 3600) {
            System.out.println("time: "+(t2-t1)/(3600*1000.0)+" hrs");
        }
        else if ((t2-t1)/1000.0 > 60) {
            System.out.println("time: "+(t2-t1)/(60*1000.0)+" mins");
        }
        else {
            System.out.println("time: "+(t2-t1)/1000.0+" secs");
        }
    }    
    public static final double massN = Nitrogen.INSTANCE.getMass();
    public static final double blN2 = 1.1014;
    /**
     * Inner class for parameters
     */
    public static class VirialN2Param extends ParameterBase {
        public int nPoints = 2;
        public int nBeads = 8;
        public double temperatureK = 500.0;   // Kelvin
        public long numSteps = 1000000;
        public double refFrac = -1;        
        public double sigmaHSRef = 4.50; // -1 means use equation for sigmaHSRef
        public boolean pairOnly = true;
        public boolean pN2HellmannA = true;
        public int beadFac = 2;
        public boolean scBeads = false;
        public String jarFile = "";
        public boolean nonAdditive = false;
        public boolean p3N2HellmannA = false;
    }

}
