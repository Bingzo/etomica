/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */


// Support for a PropertyEditor that uses text.

package etomica.graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;

import javax.swing.JTextField;

public class PropertyText extends JTextField implements KeyListener, FocusListener, PropertyChangeListener {

    public PropertyText(PropertyEditor pe) {
	    super(pe.getAsText());
	    editor = pe;
	    addKeyListener(this);
	    addFocusListener(this);
	    editor.addPropertyChangeListener(this);
//    	setBorder(PropertySheet.EMPTY_BORDER);
    }

    public void repaint() {}

    protected void updateEditor() {
	    try {
	        editor.setAsText(getText());
	    } 
	    catch (IllegalArgumentException ex) {
	        // Quietly ignore.
	    }
    }
    
    /**
     * Listen to update display if editor changes value in some other way.
     * For example, display of dimensioned property values can be changed with a change of the units.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        setText(editor.getAsText());
    }
    
    //----------------------------------------------------------------------
    // Focus listener methods.

    public void focusGained(FocusEvent e) {}

    public void focusLost(FocusEvent e) {
    	updateEditor();
    }
    
    //----------------------------------------------------------------------
    // Keyboard listener methods.

    public void keyReleased(KeyEvent e) {
 	    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
	        updateEditor();
	    }
    }

    public void keyPressed(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    //----------------------------------------------------------------------
    private transient PropertyEditor editor;
}
