/**
 * @(#)ExportManager.java
 */

package aurora.service;

import java.io.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import aurora.hwc.report.*;


/**
 * Process manager for report exporting. 
 * @author Alex Kurzhanskiy
 * @version $Id: $
 */
public class ExportManager implements ProcessManager {
	
	/**
	 * Run report exporter with progress reports at required frequency.
	 * @param input_files [0] contains URL pointing to the XML with report data.
	 * @param output_files [0] contains name of the output file. 
	 * @param updater 
	 * @param period
	 * @return <code>Done!</code> if successful, otherwise throw exception.
	 */
	public String run_application(String[] input_files, String[] output_files, Updatable updater, int period) throws Exception {
		if ((input_files == null) || (input_files.length < 1))
			throw new Exception("Error: No input files!");
		if ((output_files == null) || (output_files.length < 1))
			throw new Exception("Error: No output files specified!");
		String report_url = null;
		try {
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(input_files[0]));
	        Node nd = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getChildNodes().item(0);
	        report_url = nd.getTextContent();
		}
		catch(Exception e) {
			throw new Exception("Error: Failed to parse input: " + e.getMessage());
		}

		File output_file = new File(output_files[0]);
		String type = Utils.getExtension(output_file);
		AbstractExporter exporter = null;
		if (type.equals("ppt"))
			exporter = new Export_PPT();
		else if (type.equals("xls"))
			exporter = new Export_XLS();
		else if (type.equals("pdf"))
			exporter = new Export_PDF();
		if (exporter != null) {
			exporter.setUpdater(updater, period);
			exporter.setReportURL(report_url);
			exporter.export(output_file);
		} 
		else {
			throw new Exception("Error: Wrong output file extension!");
		}
		return "Done!";
	}

	/**
	 * My process is Report Exporter.
	 */
	public String whatIsMyProcess() {
		return "Report Exporter";
	}

}
