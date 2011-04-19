/**
 * @(#)AbstractExporter.java
 */

package aurora.hwc.report;

import java.io.File;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 * Base class for making report exporters
 * @author Gabriel Gomes
 */
public abstract class AbstractExporter {

	protected void openFile(File file){};

	protected void closeFile(){};

	protected void exportSlide(Slide S){};

	protected void exportSectionHeader(ReportSection S){};

	/**
	 * Access function used to export the entire report to a file
	 */
	final public void export(File outfile){
		Vector<ReportSection> report = readxml(gui_mainpanel.reportfile);
		openFile(outfile);
		int i,j;
		for(i=0;i<report.size();i++){
			exportSectionHeader(report.get(i));
			for(j=0;j<report.get(i).slides.size();j++)
				exportSlide(report.get(i).slides.get(j));
		}
		closeFile();
	}
	
	final public static Vector<ReportSection> readxml(File configfile){

		Vector<ReportSection> report = new Vector<ReportSection>();
		int i,j,k;
		String name1;
		String configURI = "file:" + configfile.getAbsolutePath();
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configURI);
			Node p = doc.getChildNodes().item(0);
			if ((p == null) || (!p.hasChildNodes()))
				return null;
			
			report = new Vector<ReportSection>();
			for (i=0; i<p.getChildNodes().getLength(); i++){
				name1 = p.getChildNodes().item(i).getNodeName();
				
				if (name1.equals("ActualData")){
					NodeList c = p.getChildNodes().item(i).getChildNodes();
					for (j=0; j<c.getLength(); j++){
						Node n = c.item(j);
						if(n.getNodeName().equals("Section")){
							ReportSection Sec = new ReportSection();
							NamedNodeMap A = n.getAttributes();
							for(k=0;k<A.getLength();k++){
								if(A.item(k).getNodeName().equals("title")){
									Sec.title = A.item(k).getTextContent();
								}
							}
							NodeList s = n.getChildNodes();
							for(k=0;k<s.getLength();k++){
								if(s.item(k).getNodeName().equals("slide")){
									Slide S = new Slide();
									S.initFromDOM(s.item(k));
									Sec.slides.add(S);
								}
							}
							report.add(Sec);
						}
					}
				}
			}
			return report;
		}
		catch(Exception exc) {
			System.out.println(exc.getMessage());
			return null;
		}
	}
	
}
