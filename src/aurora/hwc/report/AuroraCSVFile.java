/**
 * AuroraCSVFile.java
 */

package aurora.hwc.report;

import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;


/**
 * Class for reading and processing Aurora CSV files.
 * @author Gabriel Gomes
 */
public class AuroraCSVFile {

	public AuroraCSVData data;
	public AuroraCSVHeader header;
	public Vector<Float> time = new Vector<Float>();
	public Vector<String> alllinkids = new Vector<String>();

	
	public AuroraCSVFile(){
		data = new AuroraCSVData();
		header = new AuroraCSVHeader();
	}
	
	public AuroraCSVFile(AuroraCSVHeader h){
		header = new AuroraCSVHeader();
		h.deepcopy(header);
		data = new AuroraCSVData();
		for(int i=0;i<h.links.size();i++)
			alllinkids.add(h.links.get(i).id);
	}
	
	////////////////////////////////////////////////////////////////////////
	// CSV parsing methods
	////////////////////////////////////////////////////////////////////////
	
	public void readHeader(String filename){	
		
		header.clear();

		try{
			BufferedReader br = new BufferedReader( new FileReader(filename));
			String strLine = "";
			Vector<String> vecstr = new Vector<String>();
			
			while( (strLine = br.readLine()) != null){
				
				// .......................................................
				if(strLine.contains("Entry Format")){
					strLine = br.readLine();
					StringTokenizer strtok = new StringTokenizer(strLine, ", ");
					strtok.nextToken();
					Utils.splitbydelimiter(strtok.nextToken(),":",header.vehicletype);
			        continue;
				}
				
				// .......................................................
				if(strLine.contains("Sampling Period")){					
					while(!strLine.contains("seconds")){
						strLine = br.readLine();
					}
					Utils.splitbydelimiter(strLine,",",vecstr);
					header.simT = Float.parseFloat(vecstr.get(0));
					continue;
				}
				
				// .......................................................
				if(strLine.contains("Description")){
					strLine = br.readLine();
					while(!strLine.isEmpty()){
						Utils.splitbydelimiter(strLine,",",vecstr);
					    header.description = header.description + " " + vecstr.get(0);
					    strLine = br.readLine();
					}
					continue;
				}
				
				// .......................................................
				if(strLine.contains("Routes")){
					strLine = br.readLine();
					while(!strLine.isEmpty()){
						Utils.splitbydelimiter(strLine,",",vecstr);
						AuroraCSVRoute r = new AuroraCSVRoute();
						r.setname(vecstr.get(0));
						vecstr.remove(0);
						r.setlinks(vecstr);
						header.routes.add(r);
						strLine = br.readLine();
					}					
					continue;
				}
				
				// .......................................................
				if(strLine.contains("Link ID")){
					Vector<String> linkid = new Vector<String>();
					Vector<String> linkname = new Vector<String>();
					Vector<String> linktype = new Vector<String>();
					Vector<String> linklength = new Vector<String>();
					Vector<String> linklanes = new Vector<String>();
					Vector<String> linksource = new Vector<String>();
					Utils.splitbydelimiter(strLine,",",linkid);
					strLine = br.readLine();
					Utils.splitbydelimiter(strLine,",",linkname);
					strLine = br.readLine();
					Utils.splitbydelimiter(strLine,",",linktype);
					strLine = br.readLine();
					Utils.splitbydelimiter(strLine,",",linklength);
					strLine = br.readLine();
					Utils.splitbydelimiter(strLine,",",linklanes);
					strLine = br.readLine();
					Utils.splitbydelimiter(strLine,",",linksource);
					
					linkid.remove(0);
					linkname.remove(0);
					linktype.remove(0);
					linklength.remove(0);
					linklanes.remove(0);
					linksource.remove(0);
					
					for(int i=0;i<linkid.size();i++){
						AuroraCSVLink L = new AuroraCSVLink();
						L.setid(linkid.get(i));
						L.setname(linkname.get(i));
						L.settype(linktype.get(i));
						L.setlength(linklength.get(i));
						L.setlanes(linklanes.get(i));
						L.setsource(linksource.get(i));
						header.links.add(L);
					}
					continue;
				}

				// .......................................................
			    if(strLine.contains("Time Step")){
			    	break;
			    }
			}
		}
		catch(Exception e){
			System.out.println("Exception while reading csv file: " + e);
		}
	}

	public void loadData(Vector<Quantity> getq,String foldername,String filename,Vector<String> subsetlinkids,PerformanceCalculator p){
		
		File outfile = new File(gui_mainpanel.homePath + "\\tempfiles\\" + foldername + "_" + filename + ".xml");

		// check whether the xml version exists, if so read and return
		if(outfile.exists()){
			ReadTempFile(getq,outfile,subsetlinkids); 
		}
		else{
		    // otherwise read the csv file
			readData(foldername,filename,p.cfg);
		  
			if(p.cfg.reporttype!=ReportType.vehicletypes)
				aggregatefirstdimension();
			
		    // write the xml file
		    try {
				WriteTempFile(outfile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		    
		    // and try again
		    loadData(getq,foldername,filename,subsetlinkids,p);
		}
	}

	public void loadDataScatter(String foldername,String filename,Vector<String> subsetlinkids,PerformanceCalculator p){
		
		File outfile = new File(gui_mainpanel.homePath + "\\tempfiles\\" + foldername + "_" + filename + ".xml");

		// check whether the xml version exists, if so read and return
		if(outfile.exists()){
			ReadTempFileScatter(outfile,subsetlinkids);
		}
		else{
		    // otherwise read the csv file
			readData(foldername,filename,p.cfg);
		  
			aggregatefirstdimension();
			
			// performance measures
			float dt = time.get(1)-time.get(0);
			
			Vector<Float> vh = Utils.sumoverdimension2(data.evaluateVehicles(0,p.alllinklength),Quantity.time);
			Utils.multiplyscalar1(vh,dt);

			Vector<Float> vm = Utils.sumoverdimension2(data.evaluateFlux(0,p.alllinklength),Quantity.time);
			Utils.multiplyscalar1(vm,dt);
			
			Vector<Float> delay = Utils.sumoverdimension2(data.evaluateDelayedVehicles(0,p.alllinklength),Quantity.time);
			Utils.multiplyscalar1(delay,dt);

			//Vector<Float> lostprod = Utils.sumoverdimension2(data.evaluateLostProductivity(0,p.alllinklength),Quantity.time);
			//Utils.multiplyscalar1(lostprod,dt);

		    // write the xml file
		    try {
				WriteTempFileScatter(outfile,vh,vm,delay);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		    
		    // and try again
			loadDataScatter(foldername,filename,subsetlinkids,p);
		}
	}

	public void readData(String foldername,String filename,Configuration cfg){

		boolean ready = false;
		int numtime = 0;
		float newtime;
		Vector<String> vecstr 	= new Vector<String>();
		Vector<String> celldata = new Vector<String>();
		Vector<String> dens		= new Vector<String>();
		Vector<String> outflow	= new Vector<String>();
		Vector<String> fd		= new Vector<String>();
		Vector<String> qlimit	= new Vector<String>();
		String strLine = "";
		
		String inputfile = gui_mainpanel.homePath + "\\files\\" + foldername + "\\" + filename;
		
		try{
			readHeader(inputfile);
			BufferedReader br1 = new BufferedReader( new FileReader(inputfile));
			BufferedReader br2 = new BufferedReader( new FileReader(inputfile));
			
 			while( (strLine = br1.readLine())!=null){

			    if(strLine.contains("Time Step")){
			    	strLine = br1.readLine();
			        ready = true;
			    }
			    
			    if(!ready){
			    	br2.readLine();
			        continue;
			    }

 				// fromtime , totime
			    Utils.splitbydelimiter(strLine,",",vecstr);
			    newtime = Float.parseFloat(vecstr.get(0));
			    if(newtime*header.simT/3600f<cfg.timefrom)
			        continue;
			    if(newtime*header.simT/3600f>cfg.timeto)
			        break;
			    
			    numtime++;
			}
 
 			br1.close();
 			strLine = br2.readLine();
 			
 			int skip = (int) Math.ceil(((float) numtime)/((float) cfg.maxpointspercurve));
 			int n=-1;
 			int numvehtypes = header.vehicletype.size();
 			int numlinks = header.links.size();
 			time.clear();
 			data.clear();
 			data.allocate(numvehtypes, numlinks);
 			int i,j;
 			
 			while( (strLine = br2.readLine()) != null){
 			    
 				// fromtime , totime
 				Utils.splitbydelimiter(strLine,",",vecstr);
			    newtime = Float.parseFloat(vecstr.get(0));
			    if(newtime*header.simT/3600f<cfg.timefrom)
			        continue;
			    if(newtime*header.simT/3600f>cfg.timeto)
			        break;
			    
 			    n++;
 			    if(n%skip!=0)
 			        continue;
 			    
 			    time.add(newtime*header.simT/3600f);
 			  			    
 			    vecstr.remove(0);
  			    for(j=0;j<vecstr.size();j++){
  			    	Utils.splitbydelimiter(vecstr.get(j),";",celldata);
  			    	Utils.splitbydelimiter(celldata.get(0),":",dens);
 					Utils.splitbydelimiter(celldata.get(2),":",outflow);
 					Utils.splitbydelimiter(celldata.get(3),":",fd);
 					Utils.splitbydelimiter(celldata.get(4),":",qlimit);				
 					for(i=0;i<dens.size();i++){
 						data.Density.get(i).get(j).add( Float.parseFloat((dens.get(i))) );
 						data.OutFlow.get(i).get(j).add( Float.parseFloat((outflow.get(i))) );
 					}
 					data.Capacity.get(j).add( Float.parseFloat(fd.get(0)) );
 					data.Critical_Density.get(j).add( Float.parseFloat(fd.get(1)) );
 					data.QueueLimit.get(j).add( Float.parseFloat(qlimit.get(0)) );
 			    }
 			}
 			br2.close();
		}
		catch(Exception e){
			System.out.println("Exception while reading csv file: " + e);
		}
	}

	private void WriteTempFile(File outputfile) throws FileNotFoundException{
		PrintStream oos = new PrintStream(new FileOutputStream(outputfile));
		oos.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		oos.print("<Data>\n");
		oos.print("\t<time>" + Utils.writeMatlabFormattedVector(time) + "\t</time>\n");
		oos.print("\t<Density>\n");
		for(int i=0;i<data.Density.size();i++)
			oos.print("\t\t<vehtype>" + Utils.writeMatlabFormattedMatrix(data.Density.get(i)) + "</vehtype>\n");		
		oos.print("\t</Density>\n");
		oos.print("\t<OutFlow>\n");		
		for(int i=0;i<data.OutFlow.size();i++)
			oos.print("\t\t<vehtype>" + Utils.writeMatlabFormattedMatrix(data.OutFlow.get(i)) + "</vehtype>\n");		
		oos.print("\t</OutFlow>\n");
		oos.print("\t<Capacity>" + Utils.writeMatlabFormattedMatrix(data.Capacity) + "</Capacity>\n");	
		oos.print("\t<Critical_Density>" + Utils.writeMatlabFormattedMatrix(data.Critical_Density) + "</Critical_Density>\n");	
		oos.print("\t<QueueLimit>" + Utils.writeMatlabFormattedMatrix(data.QueueLimit) + "</QueueLimit>\n");	
		oos.print("</Data>\n");
		oos.close();
	}
	
	private void WriteTempFileScatter(File outputfile,Vector<Float> vh,Vector<Float> vm,Vector<Float> delay) throws FileNotFoundException{
		PrintStream oos = new PrintStream(new FileOutputStream(outputfile));
		oos.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		oos.print("<Data>\n");
		oos.print("\t<vh>" + Utils.writeMatlabFormattedVector(vh) + "\t</vh>\n");
		oos.print("\t<vm>" + Utils.writeMatlabFormattedVector(vm) + "\t</vm>\n");
		oos.print("\t<delay>" + Utils.writeMatlabFormattedVector(delay) + "\t</delay>\n");
		//oos.print("\t<lostprod>" + Utils.writeMatlabFormattedVector(lostprod) + "\t</lostprod>\n");
		oos.print("</Data>\n");
		oos.close();
	}

	private void ReadTempFile(Vector<Quantity> getq,File outputfile,Vector<String> subsetlinkids){
		int i,j;

		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("file:" + outputfile.getAbsolutePath());

			time.clear();
			data.clear();
			
			// convert link ids to vector indices
			Vector<Integer> subsetlinkindex = new Vector<Integer>();
			for(i=0;i<subsetlinkids.size();i++)
				subsetlinkindex.add( alllinkids.indexOf(subsetlinkids.get(i)) );
			
			Node p = doc.getChildNodes().item(0);
				
			for (i=0; i<p.getChildNodes().getLength(); i++){
				Node q = p.getChildNodes().item(i);
				
				if(q.getNodeName().equals("time")){
					String str = q.getTextContent();
					Utils.readMatlabFormattedFloatVector(str,time);
				}

				if(getq.contains(Quantity.density) & q.getNodeName().equals("Density")){
					for (j=0; j<q.getChildNodes().getLength(); j++)
						if(q.getChildNodes().item(j).getNodeName().equals("vehtype")){
							Node r = q.getChildNodes().item(j);
							Vector<Vector<Float>> M = new Vector<Vector<Float>>();
							Utils.readMatlabFormattedFloatMatrix(r.getTextContent(),M);
							Utils.keepSubsetFirstDimension(M,subsetlinkindex);
							data.Density.add(M);
						}
				}

				if(getq.contains(Quantity.flow) & q.getNodeName().equals("OutFlow")){
					for (j=0; j<q.getChildNodes().getLength(); j++)
						if(q.getChildNodes().item(j).getNodeName().equals("vehtype")){
							Node r = q.getChildNodes().item(j);
							Vector<Vector<Float>> M = new Vector<Vector<Float>>();
							Utils.readMatlabFormattedFloatMatrix(r.getTextContent(),M);
							Utils.keepSubsetFirstDimension(M,subsetlinkindex);
							data.OutFlow.add(M);
						}
				}

				if(getq.contains(Quantity.capacity) & q.getNodeName().equals("Capacity")){
					Utils.readMatlabFormattedFloatMatrix(q.getTextContent(),data.Capacity);
					Utils.keepSubsetFirstDimension(data.Capacity,subsetlinkindex);
				}

				if(getq.contains(Quantity.critical_density) & q.getNodeName().equals("Critical_Density")){
					Utils.readMatlabFormattedFloatMatrix(q.getTextContent(),data.Critical_Density);
					Utils.keepSubsetFirstDimension(data.Critical_Density,subsetlinkindex);
				}
				
				if(getq.contains(Quantity.queue_limit) & q.getNodeName().equals("QueueLimit")){
					Utils.readMatlabFormattedFloatMatrix(q.getTextContent(),data.QueueLimit);
					Utils.keepSubsetFirstDimension(data.QueueLimit,subsetlinkindex);
				}
			}	
				
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}
	
	@SuppressWarnings("unchecked")
	private void ReadTempFileScatter(File outputfile,Vector<String> subsetlinkids){
		int i;
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("file:" + outputfile.getAbsolutePath());

			time.clear();
			data.clear();
			
			// convert link ids to vector indices
			Vector<Integer> subsetlinkindex = new Vector<Integer>();
			for(i=0;i<subsetlinkids.size();i++)
				subsetlinkindex.add( alllinkids.indexOf(subsetlinkids.get(i)) );
			
			Node p = doc.getChildNodes().item(0);
				
			for (i=0; i<p.getChildNodes().getLength(); i++){
				Node q = p.getChildNodes().item(i);
				
				if(q.getNodeName().equals("vh")){
					Vector<Float> V = new Vector<Float>();
					Utils.readMatlabFormattedFloatVector(q.getTextContent(),V);
					Utils.keepSubset(V,subsetlinkindex);
					data.vh = (Vector<Float>) V.clone();
				}

				if(q.getNodeName().equals("vm")){
					Vector<Float> V = new Vector<Float>();
					Utils.readMatlabFormattedFloatVector(q.getTextContent(),V);
					Utils.keepSubset(V,subsetlinkindex);
					data.vm = (Vector<Float>) V.clone();
				}

				if(q.getNodeName().equals("delay")){
					Vector<Float> V = new Vector<Float>();
					Utils.readMatlabFormattedFloatVector(q.getTextContent(),V);
					Utils.keepSubset(V,subsetlinkindex);
					data.delay = (Vector<Float>) V.clone();
				}

				if(q.getNodeName().equals("lostprod")){
					Vector<Float> V = new Vector<Float>();
					Utils.readMatlabFormattedFloatVector(q.getTextContent(),V);
					Utils.keepSubset(V,subsetlinkindex);
					data.lostprod = (Vector<Float>) V.clone();
				}
			}		
		
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////
	// auxiliary methods
	////////////////////////////////////////////////////////////////////////
	
	private void aggregatefirstdimension(){
		int i,j,k;
		float f;
		
		int size1 = data.Density.size();
		int size2 = data.Density.get(0).size();		
		int size3 = data.Density.get(0).get(0).size();
		
		Vector<Vector<Float>> sumdensity = new Vector<Vector<Float>>();	// [link][time]
		for(j=0;j<size2;j++)
			sumdensity.add(new Vector<Float>());
		for(j=0;j<size2;j++){
			for(k=0;k<size3;k++){
				f = 0f;
				for(i=0;i<size1;i++)
					f += data.Density.get(i).get(j).get(k);
				sumdensity.get(j).add(f);
			}
		}
		data.Density.clear();
		data.Density.add(sumdensity);
			
		Vector<Vector<Float>> sumflow = new Vector<Vector<Float>>();	// [link][time]
		for(j=0;j<size2;j++)
			sumflow.add(new Vector<Float>());
		for(j=0;j<size2;j++){
			for(k=0;k<size3;k++){
				f = 0f;
				for(i=0;i<size1;i++)
					f += data.OutFlow.get(i).get(j).get(k);
				sumflow.get(j).add(f);
			}
		}
		data.OutFlow.clear();
		data.OutFlow.add(sumflow);
		
	}

	////////////////////////////////////////////////////////////////////////
	// static methods
	////////////////////////////////////////////////////////////////////////

	public static boolean loadCommonHeader(Configuration cfg,AuroraCSVHeader commonHeader){

		int i;
		AuroraCSVFile firstFile = new AuroraCSVFile();
		AuroraCSVFile newFile = new AuroraCSVFile();

		Utils.writeToConsole("\t+ Comparing networks.");

		for(i=0;i<cfg.scenarios.size();i++){
			String filename = gui_mainpanel.homePath + "\\files\\" + cfg.scenarios.get(i) + "\\" + cfg.datafiles.get(i);				
			Utils.writeToConsole("\t\t+ " + cfg.datafiles.get(i));
			if(i==0)
				firstFile.readHeader(filename);
			else{
				newFile.readHeader(filename);
				if( !newFile.header.equals( firstFile.header ) )
					return false;
			}
		}

		/*
		Vector<struct_ScenarioFiles> scenarios = new Vector<struct_ScenarioFiles>();
		guidata.checkTreeManager.readTree(scenarios);
		for(i=0;i<scenarios.size();i++){
			for(j=0;j<scenarios.get(i).datafiles.size();j++){
				String filename = gui_reportgenerator.homePath + "\\files\\" + scenarios.get(i).scenarioName + "\\" + scenarios.get(i).datafiles.get(j);				
				Utils.writeToConsole("\t\t+ " + scenarios.get(i).datafiles.get(j));
				if(i==0 & j==0)
					firstFile.readHeader(filename);
				else{
					newFile.readHeader(filename);
					if( !newFile.header.equals( firstFile.header ) )
						return false;
				}
			}
		}
		*/
		
		firstFile.header.deepcopy(commonHeader);
		return true;		
	}	
	
	////////////////////////////////////////////////////////////////////////
	// internal classes
	////////////////////////////////////////////////////////////////////////
	/**
	 * Class used to read Aurora CSV files and compute basic performance quantities
	 * @author Gabriel Gomes
	 */
	public class AuroraCSVData {
		
		private Vector<Vector<Vector<Float>>> Density = new Vector<Vector<Vector<Float>>>();		// [vehicletype][link][time]
		private Vector<Vector<Vector<Float>>> OutFlow = new Vector<Vector<Vector<Float>>>();		// [vehicletype][link][time]
		private Vector<Vector<Float>> Capacity = new Vector<Vector<Float>>();						// [link][time]
		private Vector<Vector<Float>> Critical_Density = new Vector<Vector<Float>>();				// [link][time]
		private Vector<Vector<Float>> QueueLimit = new Vector<Vector<Float>>();						// [link][time]
		
		private Vector<Float> vh = new Vector<Float>();				// [link]
		private Vector<Float> vm = new Vector<Float>();				// [link]
		private Vector<Float> delay = new Vector<Float>();			// [link]
		private Vector<Float> lostprod = new Vector<Float>();		// [link]
		
		public Vector<Float> getVh() {
			return vh;
		}

		public Vector<Float> getVm() {
			return vm;
		}

		public Vector<Float> getDelay() {
			return delay;
		}

		public Vector<Float> getLostprod() {
			return lostprod;
		}

		public Vector<Vector<Vector<Float>>> getDensity() {
			return Density;
		}

		public Vector<Vector<Vector<Float>>> getOutFlow() {
			return OutFlow;
		}
		
		private void clear(){
			Density.clear();
			OutFlow.clear();
			Capacity.clear();
			Critical_Density.clear();
			QueueLimit.clear();
			vh.clear();
			vm.clear();
			delay.clear();
			lostprod.clear();
		}

		/**
		 * Initialize vectors once the number of vehicle types and number of links is known
		 */
		private void allocate(int numvehtypes,int numlinks){

			int i,j;
			for(i=0;i<numvehtypes;i++){
				Density.add( new Vector<Vector<Float>>() );
				OutFlow.add( new Vector<Vector<Float>>() );
				for(j=0;j<numlinks;j++){
					Density.get(i).add(new Vector<Float>());
					OutFlow.get(i).add(new Vector<Float>());
				}
			}
			for(j=0;j<numlinks;j++){
				Capacity.add(new Vector<Float>());
				Critical_Density.add(new Vector<Float>());
				QueueLimit.add(new Vector<Float>());
			}
		}
		
		/**
		 * Compute speed over space and time, in [mph]
		 */
		public Vector<Vector<Float>> evaluateSpeed(int ind,Vector<Boolean> issource){
			Vector<Vector<Float>> v = Utils.divide(OutFlow.get(ind),Density.get(ind));
			// Speed does not apply to source links
			int i,j;
			for(i=0;i<v.size();i++){
				if(issource.get(i)){
					for(j=0;j<v.get(i).size();j++){
						v.get(i).set(j,Float.NaN);
					}
				}
			}
			return v;
		}
		
		/**
		 * Compute freeflow speed over space and time, in [mph]
		 */
		public Vector<Vector<Float>> evaluateFreeflowSpeed(int ind){
			return Utils.divide(Capacity,Density.get(ind));	
		}
		
		/**
		 * Compute number of vehicles per link, per time step
		 */
		public Vector<Vector<Float>> evaluateVehicles(int ind,Vector<Float> subsetlinklength){
			return Utils.multiplyoverspace(Density.get(ind),subsetlinklength);
		}
		
		/**
		 * Compute vehicle flux in space and time, in [veh*mile/hr]
		 */
		public Vector<Vector<Float>> evaluateFlux(int ind,Vector<Float> subsetlinklength){
			return Utils.multiplyoverspace(OutFlow.get(ind),subsetlinklength);
		}
		
		/**
		 * Compute number of delayed vehicles per link, per time step
		 */
		public Vector<Vector<Float>> evaluateDelayedVehicles(int ind,Vector<Float> subsetlinklength){
			Vector<Vector<Float>> FoverVf = Utils.divide(OutFlow.get(ind),evaluateFreeflowSpeed(ind));
			Vector<Vector<Float>> z = Utils.subtract(Density.get(ind),FoverVf);
			Utils.boundbelow(z,0f);
			return Utils.multiplyoverspace(z,subsetlinklength);
		}
		
		/**
		 * Compute total unutilized pavement in space and time
		 */
		public Vector<Vector<Float>> evaluateUnutilizedPavement(int ind,Vector<Float> subsetlinklengthlanes,Vector<Boolean> subsetlinkissource,float congspeed){
	    	Vector<Vector<Float>> FoverCap = Utils.divide(OutFlow.get(ind),Capacity);
	    	Vector<Vector<Float>> OneMinusFoverCap = Utils.oneMinus(FoverCap);
	    	Vector<Vector<Float>> OneMinusFoverCapLL = Utils.multiplyoverspace(OneMinusFoverCap,subsetlinklengthlanes);
	    	Vector<Vector<Float>> v = evaluateSpeed(ind,subsetlinkissource);
			for(int ii=0;ii<OneMinusFoverCapLL.size();ii++){
				for(int jj=0;jj<OneMinusFoverCapLL.get(ii).size();jj++){
					if(v.get(ii).get(jj)>congspeed)
						OneMinusFoverCapLL.get(ii).set(jj,0f);
				}
			}
	    	return OneMinusFoverCapLL;
		}
		
		/**
		 * Compute queue override activation [1|0]
		 */
		public Vector<Vector<Float>> evaluateQueueOverride(int ind){
			return Utils.isgreater(Density.get(ind),QueueLimit);
		}
		
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
