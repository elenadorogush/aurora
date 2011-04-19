/**
 * @(#)ReportSection.java
 */

package aurora.hwc.report;

import java.util.Vector;


/**
 * Class representing an individual report section
 * @author Gabriel Gomes 
 */
public class ReportSection {
	public String title;
	public Vector<Slide> slides = new Vector<Slide>();	
	public ReportSection(){};		
	@SuppressWarnings("unchecked")
	public ReportSection(String t,Vector<Slide> s){
		title = t;
		slides = (Vector<Slide>) s.clone();
	}
}
