/**
 * @(#)ReportManager.java
 */

package aurora.service;

import java.io.*;

import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;
import aurora.hwc.report.*;


/**
 * Process manager for report generation. 
 * @author Alex Kurzhanskiy
 * @version $Id: $
 */
public class ReportManager implements ProcessManager {
	
	/**
	 * Run report generator with progress reports at required frequency.
	 * @param input_files [0] contains buffered XML report request.
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
		Configuration config = new Configuration();
		File temp = null;
		// 1: create temporary folder
		try {
			temp = File.createTempFile(".AuroraReport", Long.toString(System.nanoTime()));
			if (!temp.delete())
				throw new Exception("Error: Failed to create temporary folder");
			if (!temp.mkdir())
				throw new Exception("Error: Failed to create temporary folder");
			temp.deleteOnExit();
			Configuration.setTempDir(temp.getAbsolutePath());
		}
		catch (Exception e) {
			throw new Exception("Error: Failed to create temporary folder");
		}
		// 2: parse and validate report request
		try {
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(input_files[0]));
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			config.initFromDOM(doc.getChildNodes().item(0));
			if (!config.xmlValidate())
				throw new Exception("Error: Failed to parse request");
		}
		catch(Exception e) {
			throw new Exception("Error: Failed to parse xml: " + e.getMessage());
		}
		// 3: check report request for consistency
		if (!(((config.reporttype != ReportType.vehicletypes) && (config.datafiles.size() > 0))
				|| ((config.reporttype == ReportType.vehicletypes) && (config.datafiles.size() == 1))))
			throw new Exception("Error: Consistency check of the reuest failed");
		try {
			config.check();
		}
		catch(Exception e) {
			throw new Exception("Error: Consistency check of the reuest failed");
		}
		// 4: generate report
		if ((output_files[0] != null) && (!output_files[0].isEmpty())) {
			ReportGenerator rg = new ReportGenerator(config);
			rg.setReportFile(new File(output_files[0]));
			rg.setUpdater(updater, period);
			rg.run(config);
		}
		// 5: clean up temporary files
		File[] files = temp.listFiles();
		for (File file : files)
			file.delete();
		temp.delete();
		return "Done!";
	}

	/**
	 * My process is Report Generator.
	 */
	public String whatIsMyProcess() {
		return "Report Generator";
	}

}
