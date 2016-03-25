/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.listener;

import etomica.action.IAction;
import etomica.api.IIntegratorEvent;
import etomica.api.IIntegratorListener;

public class IntegratorListenerAction implements IIntegratorListener {

    private IAction action;
    private int interval;
    private int intervalCount;
    
    public IntegratorListenerAction(IAction a) {
        this(a, 1);
    }
    
    public IntegratorListenerAction(IAction a, int interval) {
        action = a;
        this.interval = interval;
        intervalCount = 0;
    }
    
    public void integratorInitialized(IIntegratorEvent e) {
        if(intervalCount >= interval) {
            
        }
    }
    
    public void integratorStepStarted(IIntegratorEvent e) {
        if(++intervalCount >= interval) {
            
        }
        
    }
    
    public void integratorStepFinished(IIntegratorEvent e) {
        if(intervalCount >= interval) {
            intervalCount = 0;
            action.actionPerformed();
        }
    }
    
    public void setInterval(int i) {
        interval = i;
    }
    
    public int getInterval() {
        return interval;
    }
    
    public IAction getAction() {
        return action;
    }
    
}
