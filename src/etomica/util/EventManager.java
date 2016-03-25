/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.util;
import java.io.IOException;


/**
 * Class to take care of listener lists and event firing for simulation elements.
 * A class can make an instance of this manager as a field, and delegate any 
 * listener management functions to it.  
 */
public abstract class EventManager implements IEventManager, java.io.Serializable  {
    
    /* (non-Javadoc)
	 * @see etomica.util.IEventManager#addListener(java.lang.Object)
	 */
    public synchronized void addListener(IListener listener) {
        addListener(listener, true);
    }
    
    /* (non-Javadoc)
	 * @see etomica.util.IEventManager#addListener(java.lang.Object, boolean)
	 */
    public synchronized void addListener(IListener listener, boolean doSerialize) {
        //add listener to beginning of list 
        //placement at end causes problem if a listener removes and then adds itself to the list as part of its response to the event
        first = new Linker(listener, first, doSerialize);
    }

    /* (non-Javadoc)
	 * @see etomica.util.IEventManager#removeListener(java.lang.Object)
	 */
    public synchronized void removeListener(IListener listener) {
        Linker previous = null;
        for(Linker link=first; link!=null; link=link.next) {
            if(link.listener == listener) {
                if(link == first) {first = link.next;}
                if(previous != null) previous.next = link.next;
                return;
            }
            previous = link;
        }
    }

    protected transient Linker first;
    
    private void writeObject(java.io.ObjectOutputStream out)
    throws IOException
    {
        // do nothing
        out.defaultWriteObject();
        
        // write # of listeners that will be serialized
        int count = 0;
        for (Linker link = first; link != null; link = link.next) {
            //look for non-transient listeners
            if (link.doSerialize)
                count++;
        }
        out.writeInt(count);

        for (Linker link = first; link != null; link = link.next) {
            //skip transient listeners
            if (link.doSerialize)
                out.writeObject(link.listener);
        }
    }

    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException
    {
        // do nothing
        in.defaultReadObject();
        // read the listener count
        int count = in.readInt();

        for (int i=0; i<count; i++) {
            addListener((IListener)in.readObject());
        }
    }
    
    /**
     * Class used to construct a two-way linked list of listeners.
     */
    protected static class Linker {
        public Object listener;
        public Linker next;
        protected boolean doSerialize;
        public Linker(Object listener, Linker next, boolean doSerialize) {
            this.listener = listener;
            this.next = next;
            this.doSerialize = doSerialize;
        }
    }

}
