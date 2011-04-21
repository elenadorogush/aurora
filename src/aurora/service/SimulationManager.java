/**
 * @(#)SimulationManager.java
 */

package aurora.service;

import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;
import aurora.hwc.*;


/**
 * Process manager for simulation. 
 * @author Alex Kurzhanskiy
 */
public class SimulationManager implements ProcessManager {
	
	/**
	 * Run simulation with progress reports at required frequency.
	 * @param input_files [0] contains buffered XML configuration.
	 * @param output_files [0] contains name of the output file; [1] (optional) place holder for configuration XML dumped on exit. 
	 * @param updater 
	 * @param period
	 * @return <code>Done!</code> if successful, otherwise string starting with <code>Error:</code>.
	 */
	public String run_application(String[] input_files, String[] output_files, Updatable updater, int period) {
		ContainerHWC mySystem = new ContainerHWC();
		mySystem.batchMode();
		try {
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(input_files[0]));
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			mySystem.initFromDOM(doc.getChildNodes().item(0));
			mySystem.validate();
		}
		catch(Exception e) {
			return "Error: Failed to parse xml: " + e.getMessage();
		}
		File data = null;
		try {
			data = new File(output_files[0]);
			if ((!mySystem.getMySettings().setTmpDataFile(data)) || (!mySystem.getMySettings().createDataHeader())) {
				return "Error: Failed to open data output file!";
			}
		}
		catch(Exception e) {
			return "Error: Failed to open data output file: " + e.getMessage();
		}
		try {
			mySystem.initialize();
		}
		catch(Exception e) {
			mySystem.getMySettings().getTmpDataOutput().close();
			return "Error: Failed to initialize: " + e.getMessage();
		}
		boolean res = true;
		mySystem.getMyStatus().setStopped(false);
		mySystem.getMyStatus().setSaved(false);
		int ts = mySystem.getMyNetwork().getTS();
		int start_ts = ts;
		double total_sim_time = mySystem.getMySettings().getTimeMax() - mySystem.getMyNetwork().getSimTime();
		long curr_sys_time = System.currentTimeMillis();
		long lst_updt_time = curr_sys_time;
		while ((!mySystem.getMyStatus().isStopped()) && res) {
			try {
				res = mySystem.dataUpdate(++ts);
			}
			catch(Exception e) {
				mySystem.getMySettings().getTmpDataOutput().close();
				return "Simulation failed on time step " + ts + ": " + e.getMessage();
			}
			curr_sys_time = System.currentTimeMillis();
			if ((updater != null) && (((curr_sys_time-lst_updt_time)/1000) >= period)) {
				updater.notify_update((int)Math.round(100*(((ts - start_ts)*mySystem.getMyNetwork().getTP()) / total_sim_time)));
				lst_updt_time = curr_sys_time;
			}
		}
		if (!res)
			return "Simulation failed on time step " + ts;
		mySystem.getMySettings().getTmpDataOutput().close();
		if ((output_files != null) && (output_files.length > 1)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			try {
				mySystem.xmlDump(ps);
			}
			catch(Exception e) {
				ps.close();
				return "Error: Failed to generate configuration file";
			}
			output_files[1] = ps.toString();
			ps.close();
		}
		return("Done!");
	}

	/**
	 * My process is Simulation.
	 */
	public String whatIsMyProcess() {
		return "Simulation";
	}

}
