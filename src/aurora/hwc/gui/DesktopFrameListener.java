/**
 * @(#)DesktopFrameListener.java 
 */

package aurora.hwc.gui;


import java.awt.event.*;


/**
 * Implementation of a component listener for the action desktop.
 * Must fire on resize.
 * @author Alex Kurzhanskiy
 * @version $Id: DesktopFrameListener.java 38 2010-02-08 22:59:00Z akurzhan $
 */
public class DesktopFrameListener implements ComponentListener {
	private TreePane treePane = null;
	
	
	public DesktopFrameListener() { super(); }
	public DesktopFrameListener(TreePane tp) {
		super();
		treePane = tp;
	}

	
	public void componentHidden(ComponentEvent e) {
		return;
	}

	public void componentMoved(ComponentEvent e) {
		return;
	}

	public void componentResized(ComponentEvent e) {
		treePane.resizeFrames();
		return;
	}

	public void componentShown(ComponentEvent e) {
		return;
	}

}