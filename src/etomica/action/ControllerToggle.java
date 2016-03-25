/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.action;

import etomica.action.activity.Controller;

/**
 * Switches controller between paused and unpaused conditions, or starts
 * it if not yet active.
 */
public class ControllerToggle extends ControllerActionAdapter {
        
    public void actionPerformed() {
    	if(controller == null) return;
        if(!((Controller)controller).isActive()) {
            Thread runner = new Thread(new Runnable() {
                public void run() {
                    try {
                        controller.actionPerformed();
                    }
                    catch (RuntimeException e) {
                        // perhaps this should open a dialog or something
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            runner.start();
        }
        else if(!((Controller)controller).isPaused()) ((Controller)controller).pause();
        else ((Controller)controller).unPause();
    }

    private static final long serialVersionUID = 1L;
}
