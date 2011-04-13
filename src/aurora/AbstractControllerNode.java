/**
 * @(#)AbstractControllerNode.java
 */

package aurora;

import org.w3c.dom.Node;


/**
 * This class is a base for Node Controllers.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractControllerNode.java 38 2010-02-08 22:59:00Z akurzhan $
 */
public abstract class AbstractControllerNode extends AbstractController {
	private static final long serialVersionUID = -7845344291835328109L;
	
	AbstractNodeSimple myNode = null;
	
	
	/**
	 * Initializes controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		myNode.setNodeController(this);
		return res;
	}
	
	/**
	 * Updates the state of controller.
	 * @param ts new time step.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		if (ts < 1)
			throw new ExceptionSimulation(null, "Nonpositive time step (" + Integer.toString(ts) + ").");
		if (this.ts == 0) {
			this.ts = ts;
			return true;
		}
		int period = (int)Math.round((double)(tp/myNode.getTop().getTP()));
		if (period == 0)
			period = 1;
		if ((ts - this.ts) < period)
			return false;
		this.ts = ts;
		return true;
	}
	
	/**
	 * Returns <code>false</code> indicating that it is not a simple controller.
	 */
	public final boolean isSimple() {
		return false;
	}
	
	/**
	 * Returns <code>true</code> indicating that it is a node controller.
	 */
	public final boolean isNode() {
		return true;
	}
	
	/**
	 * Returns <code>false</code> indicating that it is not a complex controller.
	 */
	public final boolean isComplex() {
		return false;
	}
	
	/**
	 * Returns the Network Element it belongs to.
	 */
	public final AbstractNetworkElement getMyNE() {
		return getMyNode();
	}
	
	/**
	 * Returns node to which this controller belongs.
	 */
	public final AbstractNodeSimple getMyNode() {
		return myNode;
	}
	
	/**
	 * Assigns node to which this controller should belong.
	 * @param x monitor.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyNode(AbstractNodeSimple x) {
		if (x == null)
			return false;
		myNode = x;
		return true;
	}

}
