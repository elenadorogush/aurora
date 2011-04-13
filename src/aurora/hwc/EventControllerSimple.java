/**
 * @(#)EventControllerSimple.java
 */

package aurora.hwc;

import java.io.*;
import org.w3c.dom.*;

import aurora.*;
import aurora.util.Util;


/**
 * Event that changes local controller for given Link
 * at the assigned Node.
 * @author Alex Kurzhanskiy
 * @version $Id: EventControllerSimple.java 49 2010-03-05 02:12:09Z akurzhan $
 */
public final class EventControllerSimple extends AbstractEvent {
	private static final long serialVersionUID = -4895477288111895032L;
	
	protected AbstractControllerSimple controller = null;
	
	
	public EventControllerSimple() {
		description = "Simple Controller change";
	}
	public EventControllerSimple(int neid) {
		super(neid);
		description = "Simple Controller change";
	}
	
	
	/**
	 * Initializes the event from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		try  {
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("controller")) {
						Node type_attr = pp.item(i).getAttributes().getNamedItem("type");
						String class_name = null;
						if (type_attr != null)
							class_name = myManager.getContainer().ctrType2Classname(type_attr.getNodeValue());
						else
							class_name = pp.item(i).getAttributes().getNamedItem("class").getNodeValue();
						Class c = Class.forName(class_name);
						controller = (AbstractControllerSimple)c.newInstance();
						controller.setMyLink((AbstractLink)myNE);
						res &= controller.initFromDOM(pp.item(i));
					}
				}
			}
			else
				res = false;
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}

	/**
	 * Generates XML description of the max queue Event.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		//out.print("<lkid>" + Integer.toString(linkId) + "</lkid>");
		if (controller != null)
			controller.xmlDump(out);
		out.print("</event>");
		return;
	}
	
	/**
	 * Changes controller for given Link at the assigned simple Node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public final boolean activate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		super.activate(top);
		if (!enabled)
			return enabled;
		AbstractNode nd = ((AbstractLink)myNE).getEndNode();
		if (nd == null)
			throw new ExceptionEvent("Link (" + Integer.toString(myNE.getId()) + ") has no end node.");
		System.out.println("Event! Time " + Util.time2string(tstamp) + ": " + description);
		AbstractControllerSimple ctrl = ((AbstractNodeSimple)nd).getSimpleController((AbstractLink)myNE);
		boolean res = ((AbstractNodeSimple)nd).setSimpleController(controller, (AbstractLink)myNE);
		if (controller != null) {
			try {
				controller.initialize();
			}
			catch(Exception e) { }
		}
		controller = ctrl;
		return res;
	}
	
	/**
	 * Changes controller for given Link at the assigned simple Node back to what it was.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public final boolean deactivate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		if (!enabled)
			return enabled;
		AbstractNode nd = ((AbstractLink)myNE).getEndNode();
		if (nd == null)
			throw new ExceptionEvent("Link (" + Integer.toString(myNE.getId()) + ") has no end node.");
		System.out.println("Event rollback! Time " + Util.time2string(tstamp) + ": " + description);
		AbstractControllerSimple ctrl = ((AbstractNodeSimple)nd).getSimpleController((AbstractLink)myNE);
		boolean res = ((AbstractNodeSimple)nd).setSimpleController(controller, (AbstractLink)myNE);
		controller = ctrl;
		return res;
	}
	
	/**
	 * Returns type description. 
	 */
	public final String getTypeString() {
		return "Local Controller";
	}
	
	/**
	 * Returns letter code of the event type.
	 */
	public final String getTypeLetterCode() {
		return "SCONTROL";
	}
	
	/**
	 * Returns controller.
	 */
	public final AbstractControllerSimple getController() {
		return controller;
	}
	
	/**
	 * Sets controller.<br>
	 * @param x simple controller object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setController(AbstractControllerSimple x) {
		controller = x;
		return true;
	}
	
}
