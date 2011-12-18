/**
 * @(#)HistoricalDataSource.java 
 */

package aurora.hwc.fdcalibration;

import java.io.*;
import org.w3c.dom.Node;
import aurora.*;


/**
 * This class describes the location and flormat of the historical measurement data.
 * @author Alex Kurzhanskiy
 * @version $Id: $
 */
public class HistoricalDataSource implements AuroraConfigurable, Serializable, Cloneable {
	private static final long serialVersionUID = -2422231136210891765L;
	
	protected String url = ""; // pointer to the data file
	protected double dt = 1/12; // 5 minute sampling period
	protected String format = "pems"; // file format

	/**
	 * Initializes the data source from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		if (p == null)
			return false;
		Node url_attr = p.getAttributes().getNamedItem("url");
		if (url_attr != null)
			url = url_attr.getNodeValue();
		Node dt_attr = p.getAttributes().getNamedItem("dt");
		if (dt_attr != null)
			dt = Double.parseDouble(dt_attr.getNodeValue())/3600;
		Node format_attr = p.getAttributes().getNamedItem("format");
		if (format_attr != null)
			format = format_attr.getNodeValue();
		return true;
	}

	/**
	 * Generates XML description of a data source.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<source url=\"" + url + "\" dt=\"" + Math.round(3600*dt) + "\" format=\"" + format + "\" />\n");
		return;
	}
	
	/**
	 * Return URL.
	 */
	public String getURL() {
		return url;
	}
	
	/**
	 * Return sampling period.
	 */
	public double getTP() {
		return dt;
	}

	/**
	 * Return file format.
	 */
	public String getFormat() {
		return format;
	}

	// No validation necessary
	public boolean validate() throws ExceptionConfiguration {
		return true;
	}

	/**
	 * Set URL.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	
	
}
