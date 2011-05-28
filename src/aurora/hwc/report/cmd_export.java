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

	/**
	 * Return file extension.
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if ((i > 0) && (i < (s.length() - 1)))
			ext = s.substring(i+1).toLowerCase();
		if (ext == null)
			return "";
		return ext;
	}
	
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: java -cp <path_to_aurora.jar> aurora.hwc.report.cmd_export <report_file>.xml <output_file>.[pdf|ppt|xls]");
			return;
		}
		File output_file = new File(args[1]);
		String type = getExtension(output_file);
		AbstractExporter exporter = null;
		if (type.equals("ppt"))
			exporter = new Export_PPT();
		else if (type.equals("xls"))
			exporter = new Export_XLS();
		else if (type.equals("pdf"))
			exporter = new Export_PDF();
		if (exporter != null) {
			exporter.setReportFile(new File(args[0]));
			exporter.export(output_file);
			System.out.println("Done!");
		} 
		else
			System.err.println("Error: wrong output file extension.\nAdmissible extensions are .pdf, .ppt and .xls.");
		return;
	}

}
