/**
 * @(#)AbstractEvent.java
 */

package aurora;

import java.io.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.hwc.TypesHWC;


/**
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractEvent.java 50 2010-03-05 02:18:47Z akurzhan $
 */
public abstract class AbstractEvent implements AuroraConfigurable, Serializable {
	private static final long serialVersionUID = 4334310994251406284L;
	
	protected int id = 0;
	protected String description = "";
	protected double tstamp = 0.0; // timestamp
	protected AbstractNetworkElement myNE = null; // network element
	protected boolean enabled = true; // to fire or not
	
	protected EventManager myManager = null;
	
	protected AbstractEvent() { }
	protected AbstractEvent(int neid) {
		myNE = myManager.getContainer().getMyNetwork().getMonitorById(neid);
		if (myNE == null)
    		myNE = myManager.getContainer().getMyNetwork().getNetworkById(neid);
    	if (myNE == null)
    		myNE = myManager.getContainer().getMyNetwork().getNodeById(neid);
    	if (myNE == null)
    		myNE = myManager.getContainer().getMyNetwork().getLinkById(neid);
	}

	
	/**
	 * Initializes the event from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try  {
			Node myid_attr = p.getAttributes().getNamedItem("id");
			if (myid_attr != null)
				id = Integer.parseInt(myid_attr.getNodeValue());
			Node id_attr = p.getAttributes().getNamedItem("network_id");
			if (id_attr != null)
				myNE = myManager.getContainer().getMyNetwork().getNetworkById(Integer.parseInt(id_attr.getNodeValue()));
			else {
				id_attr = p.getAttributes().getNamedItem("node_id");
				if (id_attr != null)
					myNE = myManager.getContainer().getMyNetwork().getNodeById(Integer.parseInt(id_attr.getNodeValue()));
				else {
					id_attr = p.getAttributes().getNamedItem("link_id");
					if (id_attr != null)
						myNE = myManager.getContainer().getMyNetwork().getLinkById(Integer.parseInt(id_attr.getNodeValue()));
					else {
						id_attr = p.getAttributes().getNamedItem("sensor_id");
						if (id_attr != null)
							myNE = myManager.getContainer().getMyNetwork().getSensorById(Integer.parseInt(id_attr.getNodeValue()));
						else {
							id_attr = p.getAttributes().getNamedItem("neid");
							if (id_attr != null) {
								int id = Integer.parseInt(id_attr.getNodeValue());
								myNE = myManager.getContainer().getMyNetwork().getMonitorById(id);
								if (myNE == null)
						    		myNE = myManager.getContainer().getMyNetwork().getNetworkById(id);
						    	if (myNE == null)
						    		myNE = myManager.getContainer().getMyNetwork().getNodeById(id);
						    	if (myNE == null)
						    		myNE = myManager.getContainer().getMyNetwork().getLinkById(id);
							}
						}
					}
				}
			}
			tstamp = Double.parseDouble(p.getAttributes().getNamedItem("tstamp").getNodeValue());
			if (tstamp >= 24)
				tstamp = tstamp / 3600;
			enabled = Boolean.parseBoolean(p.getAttributes().getNamedItem("enabled").getNodeValue());
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("description")) {
						String desc = pp.item(i).getTextContent();
						if (!desc.equals("null"))
							description = desc;
					}
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Generates XML description of an Event.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		if (myNE == null)
			return;
		String buf = "link_id";
		if ((myNE.getType() & TypesHWC.MASK_NETWORK) > 0)
			buf = "network_id";
		else if ((myNE.getType() & TypesHWC.MASK_NODE) > 0)
			buf = "node_id";
		else if ((myNE.getType() & TypesHWC.MASK_SENSOR) > 0)
			buf = "sensor_id";
		out.print("\n<event id=\"" + id + "\" type=\"" + getTypeLetterCode() + "\" " + buf + "=\"" + myNE.getId() + "\" tstamp=\"" + Double.toString(3600*tstamp) + "\" enabled=\"" + Boolean.toString(enabled) + "\">");
		out.print("<description>" + description + "</description>");
		return;
	}
	
	/**
	 * Bogus function that always returns <code>true</code>.
	 */
	public boolean validate() throws ExceptionConfiguration {
		return true;
	}
	
	/**
	 * Activates the event.
	 * @param top top level complex Node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public boolean activate(AbstractNodeComplex top) throws  ExceptionEvent {
		double currentT = top.getTop().getTP() * top.getTS();
		tstamp = Math.max(tstamp, currentT);
		return true;
	}
	
	/**
	 * Deactivates the event.
	 * @param top top level complex Node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public abstract boolean deactivate(AbstractNodeComplex top) throws  ExceptionEvent;
	
	/**
	 * Checks if event is enabled.
	 */
	public final boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Returns event description.
	 */
	public final String getDescription() {
		return description;
	}
	
	/**
	 * Returns time stamp
	 */
	public final double getTime() {
		return tstamp;
	}
	
	/**
	 * Returns NE on which the event is to happen.
	 */
	public final AbstractNetworkElement getNE() {
		return myNE;
	}
	
	/**
	 * Returns type description. 
	 */
	public abstract String getTypeString();
	
	/**
	 * Returns letter code of the event type.
	 */
	public abstract String getTypeLetterCode();
	
	/**
	 * Returns event manager.
	 */
	public final EventManager getEventManager() {
		return myManager;
	}
	
	/**
	 * Sets the Network Element for the event.
	 * @param x Network Element (<code>true</code> to enable, <code>false</code> to disable).
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setNE(AbstractNetworkElement x) {
		if (x == null)
			return false;
		myNE = x;
		return true;
	}
	
	/**
	 * Enables or disables the event.
	 * @param x boolean value (<code>true</code> to enable, <code>false</code> to disable).
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setEnabled(boolean x) {
		enabled = x;
		return true;
	}
	
	/**
	 * Sets event description.
	 * @param x time stamp.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDescription(String x) {
		description = x;
		return true;
	}
	
	/**
	 * Sets timestamp.
	 * @param x timestamp.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setTime(double x) {
		if (x < 0.0)
			tstamp = 0.0;
		else
			tstamp = x;
		return true;
	}
	
	/**
	 * Sets event manager to which the event belongs.
	 * @param x event manager.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setEventManager(EventManager x) {
		if (x == null)
			return false;
		myManager = x;
		return true;
	}
	
}