/**
 * @(#)ProcessManager.java
 */

package aurora.service;


/**
 * Interface for managing wrappers that run Java applications.
 * @author Alex Kurzhanskiy
 */
public interface ProcessManager {
	
	/**
	 * Run Java application with given inputs and specified outputs.
	 * @param input_files buffers with references to input files, or with full contents of input files.
	 * @param output_files buffers with references to output files, or with full contents of output files.
	 * @param updater interface to a worker process updater.
	 * @param period time in seconds indicating how often the updates must be sent.
	 * @return exit text.
	 */
	public String run_application(String[] input_files, String[] output_files, Updatable updater, int period) throws Exception;

	public String whatIsMyProcess();
}
