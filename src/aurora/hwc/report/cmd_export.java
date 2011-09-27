/**
 * cmd_export.java
 */

package aurora.hwc.report;

import java.io.*;

/**
 * Main class for exporting report.
 * @author Alex Kurzhanskiy
 * @version $Id: $
 */
public class cmd_export {
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: java -cp <path_to_aurora.jar> aurora.hwc.report.cmd_export <xml_report_file_url> <output_file>.[pdf|ppt|xls]");
			return;
		}
		File output_file = new File(args[1]);
		String type = Utils.getExtension(output_file);
		AbstractExporter exporter = null;
		if (type.equals("ppt"))
			exporter = new Export_PPT();
		else if (type.equals("xls"))
			exporter = new Export_XLS();
		else if (type.equals("pdf"))
			exporter = new Export_PDF();
		if (exporter != null) {
			exporter.setReportURL(args[0]);
			exporter.export(null, output_file);
			System.out.println("Done!");
		} 
		else
			System.err.println("Error: Wrong output file extension.\nAdmissible extensions are .pdf, .ppt and .xls.");
		return;
	}

}
