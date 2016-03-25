/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.surfacetension;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import etomica.data.DataPipe;
import etomica.data.DataProcessor;
import etomica.data.IData;
import etomica.data.IEtomicaDataInfo;
import etomica.data.IEtomicaDataInfoFactory;
import etomica.data.types.DataDoubleArray;
import etomica.data.types.DataFunction;
import etomica.data.types.DataFunction.DataInfoFunction;

public class FitTanh extends DataProcessor {

    private boolean debug = false;    

    public void setTolerance(double newTolerance) {
        tolerance = newTolerance;
    }
    
    public double getTolerance() {
        return tolerance;
    }
    
    public void setMaxIterations(int newMaxIterations) {
        maxIterations = newMaxIterations;
    }

    public double[] doFit(double[] x, double[] y) {
        double[] dx = new double[]{0.001, 0.001, 0.01, 0.1};

        // if we failed or were just reinitialized, reset parameters
        // otherwise start with old parameters
        if (Double.isNaN(param[0])) {
            param[0] = 0.0;  // vapor density
            param[1] = 0.8;  // liquid density
            param[2] = 3;  // interface width
            param[3] = x[x.length*2/3];  // location of interface
        }
//        debug = true;
        double sumSqErr = getSqErr(x, y);
//        debug = false;
//        System.out.println(Arrays.toString(param));
//        System.out.println((-1)+" "+sumSqErr);

        for (int cycle=0; cycle<maxIterations; cycle++) {
            for (int paramNow=0; paramNow<4; paramNow++) {
                double sumSqErr1 = getSqErr(x, y);
                param[paramNow] += dx[paramNow];
                double sumSqErr2 = getSqErr(x, y);
                param[paramNow] -= 2*dx[paramNow];
                double sumSqErr0 = getSqErr(x, y);
                param[paramNow] += dx[paramNow];
                double a = (sumSqErr2 - 2*sumSqErr1 + sumSqErr0) / (dx[paramNow]*dx[paramNow]);
                double b = (sumSqErr2 - sumSqErr0) / (2*dx[paramNow]) - a*(2*param[paramNow]);
                double newParam = - 0.5 * b / a;
//                System.out.println(sumSqErr0+" "+sumSqErr1+" "+sumSqErr2);
//                System.out.println("a: "+a+" b: "+b);
                if (a > 0) {
//                    System.out.println(paramNow+" "+param[paramNow]+" => "+newParam);
                }
                else {
                    if (sumSqErr0 < sumSqErr2) {
                        newParam = param[paramNow] - 100*dx[paramNow];
                    }
                    else {
                        newParam = param[paramNow] + 100*dx[paramNow];
                    }
//                    System.out.println(paramNow+" fit failed, "+param[paramNow]+" => "+newParam);
                }
                param[paramNow] = newParam;
                if (paramNow==0 && param[0] < 0) param[0] = 0;
                if (paramNow==1 && param[1] < param[0]*1.5) param[1] = param[0]*1.5;
                if (paramNow==2) {
                    if (param[2] < 0.1) param[2] = 0.1;
                    else if (param[2] > x[x.length-1]*0.5) param[2] = x[x.length-1]*0.5;
                }
                if (paramNow==3) {
                    if (param[3] < 2) param[3] = 2;
                    else if (param[3] > x[x.length-1]-param[2]*0.5) param[3] = x[x.length-1]-param[2]*0.5;
                }
            }
//            debug = true;
//            System.out.println(Arrays.toString(param));
            double newSumSqErr = getSqErr(x, y);
//            System.out.println(cycle+" "+newSumSqErr);
//            System.exit(1);
            if (sumSqErr - newSumSqErr < tolerance) {
                // bail if we didn't get better by at least tolerance (or got worse)
                break;
            }
            sumSqErr = newSumSqErr;
        }
        
        return param;
    }
    
    protected double getSqErr(double[] x, double[] y) {
        int nValues = x.length;
        double sumSqErr = 0;
        for (int i=0; i<nValues; i++) {
//            double yCalc = 0.5 * ((param[1] + param[0]) - (param[1] - param[0]) * Math.tanh(2 * (Math.abs(x[i]) - param[3]) / param[2]));
//            if (debug) System.out.println(x[i]+" "+Math.tanh(2 * (x[i] + param[3]) / param[2])+" "+Math.tanh(2 * (x[i] - param[3]) / param[2]));
            double yCalc = param[0] + 0.5 * (param[1] - param[0]) * (Math.tanh(2 * (x[i] + param[3]) / param[2]) - Math.tanh(2 * (x[i] - param[3]) / param[2]));
            if (debug) System.out.println(x[i]+" "+yCalc+" "+y[i]);
            double err = yCalc - y[i];
            sumSqErr += err*err;
        }
        return sumSqErr;
    }
    
    public double[] getLastBestParam() {
        return param;
    }
    
    protected double tolerance = 1.e-10;
    protected int maxIterations = 20;
    
    public static void main(String[] args) {
        FileReader fileReader;
        try {
            fileReader = new FileReader("d.dat");
        }catch(IOException e) {
            throw new RuntimeException("Cannot open d.dat, caught IOException: " + e.getMessage());
        }
        ArrayList<Double> xList = new ArrayList<Double>();
        ArrayList<Double> yList = new ArrayList<Double>();
        try {
            BufferedReader bufReader = new BufferedReader(fileReader);
            while (true) {
                String line = bufReader.readLine();
                if (line == null) {
                    break;
                }
                String[] xy = line.split(" +");
                xList.add(Double.parseDouble(xy[0]));
                yList.add(Double.parseDouble(xy[1]));
            }
            fileReader.close();
        } catch(IOException e) {
            throw new RuntimeException("Problem reading d.dat, caught IOException: " + e.getMessage());
        }

        double[] x = new double[xList.size()];
        double[] y = new double[yList.size()];
        for (int i=0; i<x.length; i++) {
            x[i] = xList.get(i);
            y[i] = yList.get(i);
        }
        FitTanh fitter = new FitTanh();
        double[] params = fitter.doFit(x, y);
        System.out.println("Final: "+params[0]+" "+params[1]+" "+params[2]);
    }

    protected IData processData(IData inputData) {
        double[] x = fitDataInfo.getXDataSource().getIndependentData(0).getData();
        double[] y = ((DataDoubleArray)inputData).getData();
        doFit(x, y);
        if (param[0] < 0) {
            param[0] = 0;
        }
        if (param[0] > param[1] || param[2] <= 0 || param[2] > x[x.length-1] || param[3] <= 0 || param[3] > x[x.length-1]) {
            fitData.E(Double.NaN);
            param[0] = Double.NaN;
            param[1] = Double.NaN;
            param[2] = Double.NaN;
            param[3] = Double.NaN;
            return fitData;
        }
        double[] yCalc = fitData.getData();
        int nValues = x.length;
        for (int i=0; i<nValues; i++) {
            yCalc[i] = 0.5 * ((param[1] + param[0]) - (param[1] - param[0]) * Math.tanh(2 * (Math.abs(x[i]) - param[3]) / param[2]));
        }
        return fitData;
    }

    protected IEtomicaDataInfo processDataInfo(IEtomicaDataInfo inputDataInfo) {
        param[0] = Double.NaN;
        IEtomicaDataInfoFactory factory = inputDataInfo.getFactory();
        factory.setLabel("fit");
        fitDataInfo = (DataInfoFunction)factory.makeDataInfo();
        fitDataInfo.addTag(tag);
        fitData = new DataFunction(fitDataInfo.getArrayShape());
        return fitDataInfo;
    }

    public DataPipe getDataCaster(IEtomicaDataInfo intputDataInfo) {
        return null;
    }
    
    protected DataFunction fitData;
    protected DataInfoFunction fitDataInfo;
    protected double[] param = new double[4];
}
