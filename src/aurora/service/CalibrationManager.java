/**
 * @(#)CalibrationManager.java
 */

package aurora.service;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

import org.xml.sax.InputSource;
import org.w3c.dom.*;

import aurora.*;
import aurora.hwc.*;
import aurora.hwc.fdcalibration.*;


/**
 * Process manager for calibration. 
 * @author Alex Kurzhanskiy
 */
public class CalibrationManager implements ProcessManager {
	
	/**
	 * Run calibration with progress reports at required frequency.
	 * @param input_files [0] contains buffered XML configuration; [index > 0] contain URLs to data files.
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
		ContainerHWC mySystem = new ContainerHWC();
		// 1: read configuration and validate
		try {
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(input_files[0]));
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			mySystem.initFromDOM(doc.getChildNodes().item(0));
			mySystem.validate();
		}
		catch(Exception e) {
			throw new Exception("Error: Failed to parse xml: " + e.getMessage());
		}
		// 2: create calibrator and read data
		//ArrayList<String> input_urls = new ArrayList<String>();
		//for (int i = 1; i < input_files.length; i++)
		//	input_urls.add(input_files[i]);
		//FDCalibrator fdc = new FDCalibrator("", input_urls, "");
		FDCalibrator fdc = new FDCalibrator(input_files[0],output_files[0]);
		fdc.setMySystem(mySystem);
		fdc.setUpdater(updater);
		try {
			fdc.readtrafficdata();
		}
		catch(Exception e){
			throw new Exception("Error: Failed to parse data files: " + e.getMessage());
		}
		// 3: run calibration routine
		Vector<AbstractSensor> SensorList = mySystem.getMyNetwork().getSensors();
		long curr_sys_time = System.currentTimeMillis();
		long lst_updt_time = curr_sys_time;
		for (int i = 0; i < SensorList.size(); i++) {
			SensorLoopDetector S = (SensorLoopDetector) SensorList.get(i);
			if ((S.getVDS() != 0) && (S.getLink() != null))
				fdc.calibrate(S);
			curr_sys_time = System.currentTimeMillis();
			if ((updater != null) && (((curr_sys_time-lst_updt_time)/1000) >= period)) {
				updater.notify_update(50+Math.round(30*(((float)i+1)/SensorList.size())));
				lst_updt_time = curr_sys_time;
			}
		}
		// 4: extend parameters to the rest of the network
		fdc.propagate();
		updater.notify_update(98);
		// 5: dump configuration into XML
		if ((output_files != null) && (output_files.length > 0)) {
			PrintStream ps = null;
			try {
				if ((output_files[0] != null) && (!output_files[0].isEmpty())) {
					File o_file = new File(output_files[0]);
					ps = new PrintStream(new FileOutputStream(o_file.getAbsolutePath()));
					mySystem.xmlDump(ps);
				}
				else {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ps = new PrintStream(baos);
					mySystem.xmlDump(ps);
					output_files[0] = baos.toString();
				}
			}
			catch(Exception e) {
				if (ps != null)
					ps.close();
				throw new Exception("Error: Failed to generate configuration file");
			}
			ps.close();
		}
		return "Done!";
	}

	/**
	 * My process is Calibration.
	 */
	public String whatIsMyProcess() {
		return "Calibration";
	}

}
