/**
 * @(#)AbstractExporter.java
 */

package aurora.hwc.report;

import java.io.*;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import aurora.service.*;

/**
 * Base class for making report exporters
 * @author Gabriel Gomes
 */
public abstract class AbstractExporter {
	protected String reportURL = "file:" + Configuration.getTempDir() + "/detailed.xml";
	protected Updatable updater = null;
	protected int update_period = 5;
	protected int max_percent = 100;
	protected int offset_percent = 0;
	

	protected void openFile(File file){};

	protected void closeFile(){};

	protected void exportSlide(Slide S){};

	protected void exportSectionHeader(ReportSection S){};

	/**
	 * Access function used to export the entire report to a file
	 */
	final public void export(File infile, File outfile){
		Vector<ReportSection> report = readxml(infile);
		openFile(outfile);
		int total_slides = 0;
		int slide_count = 0;
		for (int i = 0; i < report.size(); i++)
			total_slides += report.get(i).slides.size();
		long sys_curr_time = System.currentTimeMillis();
		long sys_lst_time = sys_curr_time;
		for (int i = 0; i < report.size(); i++) {
			exportSectionHeader(report.get(i));
			for (int j = 0; j < report.get(i).slides.size(); j++) {
				exportSlide(report.get(i).slides.get(j));
				slide_count++;
				sys_curr_time = System.currentTimeMillis();
				if ((sys_curr_time - sys_lst_time) >= 1000*update_period) {
					sys_lst_time = sys_curr_time;
					if (updater != null)
						updater.notify_update(offset_percent + Math.round(max_percent*(float)slide_count/total_slides));
				}
			}
		}
		closeFile();
	}
	
	final public Vector<ReportSection> readxml(File configfile) {
		Vector<ReportSection> report = new Vector<ReportSection>();
		int i,j,k;
		String name1;
		try {
			Document doc = null;
			if (configfile != null) {
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("file:" + configfile.getAbsolutePath());
			}
			else
				doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(reportURL);
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
	
	/**
	 * Set destination for the report URL.
	 */
	public synchronized void setReportURL(String fn) {
		if ((fn != null) && (!fn.isEmpty()))
			reportURL = fn;
		return;
	}
	
	/**
	 * Sets progress updater.
	 */
	public synchronized void setUpdater(Updatable upd, int prd, int offset_prgr, int max_prgr) {
		updater = upd;
		update_period = Math.max(1, prd);
		offset_percent = offset_prgr;
		max_percent = max_prgr;
		return;
	}
}
