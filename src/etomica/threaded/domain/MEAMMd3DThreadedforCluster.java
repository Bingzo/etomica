/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.threaded.domain;



import etomica.threaded.IntegratorVelocityVerletThreaded;


public class MEAMMd3DThreadedforCluster {

    public static void main(String[] args) {
        
        // On-the-fly input - Number of Atoms
        int n;
        n = Integer.parseInt(args[0]);
        
        // On-the-fly input - Number of Threads
        int t;
        t = Integer.parseInt(args[1]);
        
        // On-the-fly input - Number of Timesteps
        int s;
        s = Integer.parseInt(args[2]);
                
        MEAMMd3DThreaded sim = new MEAMMd3DThreaded(n, t);
               
       
        sim.activityIntegrate.setMaxSteps(s);
      
        
        // Timer
        double time1 = System.currentTimeMillis();
        
        sim.getController().actionPerformed();
        
        double time2 = System.currentTimeMillis();
        time2 = (time2 - time1)/1000;
        
        
        
        // Output
        System.out.println(t+"_thread(s) - "+n+"_atoms - "+s+"_timesteps");
        

        //System.out.println("force sum time: "+PotentialCalculationForceSumThreaded.forceSumTime/1000+" seconds.");
        
      //  for(int i=0;i<((PotentialMasterListThreaded)sim.getPotentialMaster()).threads.length; i++){
      //  System.out.println("thread"+i+" calculate time: "+((PotentialMasterListThreaded)sim.getPotentialMaster()).threads[i].threadCalculate /1000+" seconds.");
        // }
        
      //  System.out.println("threads time: "+IntegratorVelocityVerletThreaded.threadtime/1000+" seconds.");
        System.out.println("total time: "+time2+" seconds.");
    }

   
}