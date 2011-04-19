/**
 * @(#)ScenarioFiles.java
 */

package aurora.hwc.report;

import java.util.Vector;


/**
 * Description of a simulation scenario
 * @author Gabriel Gomes
 */
public class ScenarioFiles {
	
	public String scenarioName;
	public Vector<String> datafiles =  new Vector<String>();
	
	public ScenarioFiles(String name){
		scenarioName = name;
	}
	
	public ScenarioFiles(String name,String filename){
		scenarioName = name;
		datafiles.add(filename);
	}
	
	public String toString() {
		return scenarioName + ": " + datafiles.toString();
	}
	
	public ScenarioFiles clone(){
		ScenarioFiles z = new ScenarioFiles(this.scenarioName);
		z.datafiles.clear();
		for(int i=0;i<this.datafiles.size();i++)
			z.datafiles.add(this.datafiles.get(i));
		return z;
	}
}