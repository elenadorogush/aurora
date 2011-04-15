/**
 * @(#)DirectionsCache.java
 */

package aurora.hwc;

import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;
import aurora.*;


public class DirectionsCache  implements AuroraConfigurable, Serializable {
	private static final long serialVersionUID = 6299884863280488079L;
	
	private String all;
	
	
	// basic initialization
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(p),new StreamResult(buffer));
			all = buffer.toString();
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}

	// dump all
	public void xmlDump(PrintStream out) throws IOException {
		out.print("\n" + all + "\n");
	}

	// no validation needed
	public boolean validate() throws ExceptionConfiguration {
		return true;
	}

}
