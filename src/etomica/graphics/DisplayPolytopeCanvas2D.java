/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.graphics;
import java.awt.Color;
import java.awt.Graphics;

import etomica.math.geometry.LineSegment;
import etomica.math.geometry.Polygon;

//Class used to define canvas onto which polytope is drawn
public class DisplayPolytopeCanvas2D extends DisplayCanvas {
    
    private DisplayPolytope displayPolytope;

    public DisplayPolytopeCanvas2D(DisplayPolytope _polytope) {
        super(null);
        displayPolytope = _polytope;
    }
    
    public void initialize() {}
    
    /**
     * Sets the size of the display to a new value and scales the image so that
     * the box fits in the canvas in the same proportion as before.
     */
    public void scaleSetSize(int width, int height) {
        if(getBounds().width * getBounds().height != 0) {  //reset scale based on larger size change
            double ratio1 = (double)width/(double)getBounds().width;
            double ratio2 = (double)height/(double)getBounds().height;
            double factor = Math.min(ratio1, ratio2);
            displayPolytope.setScale(displayPolytope.getScale()*factor);
            setSize(width, height);
        }
    }
          
    //Override superclass methods for changing size so that scale is reset with any size change  
    // this setBounds is ultimately called by all other setSize, setBounds methods
    public void setBounds(int x, int y, int width, int height) {
        if(width == 0 || height == 0) return;
        super.setBounds(x,y,width,height);
        createOffScreen(width,height);
    }
       
    /**
     * doPaint is the method that handles the drawing of the box to the screen.
     * Several variables and conditions affect how the image is drawn.  First,
     * the Unit.Length.Sim class variable <code>TO_PIXELS</code> performs the conversion 
     * between polytope dimensions and pixels.  The default value is 10 pixels/unit length
     * reflecting the default size of the box (300 pixels by 300 pixels) and the
     * default polytope size (30 by 30).
     *
     * @param g The graphic object to which the image of the box is drawn
     */
    public void doPaint(Graphics g) {
        if(!isVisible() || displayPolytope.getPolytope() == null) {return;}
        int w = getSize().width;
        int h = getSize().height;
        
        g.setColor(getBackground());
        g.fillRect(0,0,w,h);
        displayPolytope.computeImageParameters2(w, h);

        //Draw other features if indicated
        if(drawBoundary>DRAW_BOUNDARY_NONE) {
            g.setColor(Color.gray);
            double toPixels = displayPolytope.getScale()*pixel.toPixels();
            Polygon shape = (Polygon)displayPolytope.getPolytope();
            LineSegment[] edges = shape.getEdges();
            int ox = displayPolytope.getOrigin()[0];
            int oy = displayPolytope.getOrigin()[1];
            for(int i=0; i<edges.length; i++) {
                int x1 = ox + (int)(toPixels*edges[i].getVertices()[0].getX(0));
                int y1 = oy + (int)(toPixels*edges[i].getVertices()[0].getX(1));
                int x2 = ox + (int)(toPixels*edges[i].getVertices()[1].getX(0));
                int y2 = oy + (int)(toPixels*edges[i].getVertices()[1].getX(1));
                g.drawLine(x1,y1,x2,y2);
            }
        }

    }
}
