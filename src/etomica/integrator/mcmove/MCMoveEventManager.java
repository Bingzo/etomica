/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.integrator.mcmove;

import etomica.util.EventManager;
import etomica.util.IEvent;
import etomica.util.IListener;

public class MCMoveEventManager extends EventManager {

    public MCMoveEventManager() {
        super();
    }

    public void fireEvent(IEvent event) {
        for(Linker link=first; link!=null; link=link.next) {
            ((IListener)link.listener).actionPerformed(event);
        }
    }

}
