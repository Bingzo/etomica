/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.action;


/**
 * An Action that supports the capability of pausing/unpausing and terminating
 * on request. Subclasses define the activity performed by implementing the
 * run() method.
 * 
 * @author Andrew Schultz and David Kofke
 */
public abstract class Activity implements IAction, java.io.Serializable {

    /**
     * Create class with a simple default label.
     */
    public Activity() {
    }
    
    /**
     * Copy constructor.
     */
    protected Activity(Activity activity) {
        name = activity.name;
    }

    /**
     * Method defining the behavior of the activity. Implementation should
     * ensure regular checking of doContinue() to permit any requests to pause
     * or halt to be put in effect. If doContinue() returns false, run method
     * should end activity.
     */
    protected abstract void run();

    /**
     * Sets integrator to begin isActive on its own thread. This is the normal
     * way to begin the integrator's activity. Fires an event to listeners
     * indicating that integrator has started, calls the initialize method, and
     * starts a new thread that then enters the integrators run() method. If
     * integrator is already isActive, method call return immediately and has no
     * effect.
     */
    public void actionPerformed() {
        synchronized (this) {
            haltRequested = false;
            pauseRequested = false;
            isPaused = false;
            isActive = true;
        }
        run();
        isActive = false;
        if (haltRequested) {
            synchronized (this) {
                haltRequested = false;
                notifyAll();
            }//release thread waiting for halt to take effect
        }
    }

    protected synchronized boolean doContinue() {
        if (pauseRequested) {
            doWait();
        }
        return !haltRequested;
    }

    /**
     * Method to put activity in a condition of being paused.
     */
    protected synchronized void doWait() {
        try {
            while(pauseRequested) {
                notifyAll(); //release any threads waiting for pause to take effect
                isPaused = true;
                this.wait(); //put in paused state
            }
        } catch (InterruptedException e) {
        }
        isPaused = false;
        notifyAll(); // release any threads waiting for unpause to take effect
    }

    /**
     * Requests that the Activity pause its execution. The actual suspension of
     * execution occurs only after activity notices the pause request. The
     * calling thread is put in a wait state until the pause takes effect.
     */
    public synchronized void pause() {
        try {
            // make thread requesting pause wait until pause is in effect
            while(!isPaused && isActive()) {
                pauseRequested = true;
                this.wait();
            }
        } catch (InterruptedException e) { }
    }

    /**
     * Removes activity from the paused state, resuming execution where it left
     * off.
     */
    public synchronized void unPause() {
        try {
            while(isPaused && isActive()) {
                pauseRequested = false;
                notifyAll();
                this.wait();
            }
        } catch (InterruptedException e) { }
    }

    /**
     * Request that the activity terminate as soon as safely possible. Causes
     * calling thread to wait until the halt is in effect.
     */
    public synchronized void halt() {
        try {
            while(isActive()) {
                haltRequested = true;
                unPause();//in case currently paused
                // check to see if Activity actually halted while we were 
                // for the unpause
                if (haltRequested) {
                    this.wait(); //make thread requesting halt wait until halt is in effect
                }
            }
        } catch (InterruptedException e) { }
    }

    /**
     * Queries whether the integrator is in a state of being paused. This may
     * occur independent of whether the integrator is isActive or not. If paused
     * but not isActive, then pause will take effect upon start.
     */
    public synchronized boolean isPaused() {
        return isPaused;
    }

    /**
     * Indicates if the integrator has been started and has not yet completed.
     * If so, returns true, even if integrator is presently paused (but not
     * halted).
     */
    public synchronized boolean isActive() {
        return isActive;
    }

    protected boolean isActive = false;
    protected boolean haltRequested = false;
    protected boolean pauseRequested = false;
    private boolean isPaused = false;
    private String name;
}
