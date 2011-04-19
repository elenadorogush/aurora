/**
 * @(#)Slide.java
 */

package aurora.hwc.report;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import aurora.*;

/**
 * Class representing an individual slide
 * @author Gabriel Gomes
 */
public class Slide implements AuroraConfigurable {
	
	public int id;
	public String title;
	public Vector<Plot> plots = new Vector<Plot>();

	public Slide(){};
	
	@SuppressWarnings("unchecked")
	public Slide(String t,Vector<Plot> p,int i){
		id = i;
		title = t;		
		plots = (Vector<Plot>) p.clone();
	}
	
	@Override
	protected Slide clone() {
		Slide s = new Slide(this.title,this.plots,this.id);
		return s;
	}

	@Override
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		int i;
		String name;
				
		if ((p == null) || (!p.hasChildNodes()))
			return false;
		try {
			NamedNodeMap A = p.getAttributes();
			for (i=0; i<A.getLength(); i++){
				name = A.item(i).getNodeName();
				if(name.equals("id"))
					this.id = Integer.parseInt(A.item(i).getTextContent());
				if(name.equals("title"))
					this.title = A.item(i).getTextContent();
			}
			NodeList C = p.getChildNodes();
			for (i=0; i<p.getChildNodes().getLength(); i++){
				name = C.item(i).getNodeName();
				if(name.equals("plot")){
					Plot P = new Plot();
					P.initFromDOM(C.item(i));
					this.plots.add(P);
				}
			}
		}
		catch(Exception e) {
			throw new ExceptionConfiguration(e.getMessage());
		}
		return true;
	}

	@Override
	public void xmlDump(PrintStream out) throws IOException {
		out.print("\t\t\t<slide id=\"" + id + "\" title=\"" + title + "\">\n");
		for(int i=0;i<plots.size();i++)
			plots.get(i).xmlDump(out);
		out.print("\t\t\t</slide>\n");		
	}


	@Override
	public boolean validate() throws ExceptionConfiguration {
		// TODO Auto-generated method stub
		return true;
	}
}
