/**
 * @(#)NodeTerminal.java
 */

package aurora.hwc;


/**
 * Terminal Node.
 * @author Alex Kurzhanskiy
 * @version $Id: $
 */
public final class NodeTerminal extends AbstractNodeHWC {
	private static final long serialVersionUID = 7523854689596881326L;
	
	
	public NodeTerminal() { }
	public NodeTerminal(int id) { this.id = id; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.NODE_TERMINAL;
	}
	
	/**
	 * Returns letter code of the Node type.
	 */
	public String getTypeLetterCode() {
		return "T";
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Terminal Node";
	}
	
	/**
	 * Returns compatible simple controller type names.
	 */
	public String[] getSimpleControllerTypes2() {
		return null;
	}
	
	/**
	 * Returns compatible simple controller classes.
	 */
	public String[] getSimpleControllerClasses() {
		return null;
	}
	
}