package aurora.hwc.fdcalibration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/* This is a standalone class for extracting data from PeMS DH or 
 * Caltrans dbx files and putting it in smaller text files. 
 */
public class ExtractStationData {

	private String vdsfile = "i80vds.txt";
	private String outfolder = "pemsdataclean";
	
	private Vector<Integer> vds = new Vector<Integer>();
	protected HashMap <Integer,FiveMinuteData> data = new HashMap <Integer,FiveMinuteData> ();
	
	/* Input: 1) name of file with list of VDS stations
	 * 2) input file url
	 * 3) input file format
	 * 4) output folder
	 */
	public static void main(String[] args) {

		int i;
		String informat = "";;
		String inURL = "";;

		ExtractStationData X = new ExtractStationData();
		
		if(args.length>0)
			X.vdsfile = args[0];

		if(args.length>1)
			inURL = args[1];

		if(args.length>2)
		   informat = args[2];
		
		if(args.length>3)
			X.outfolder = args[3];
		
		try {
			// read detector stations from file
			X.ReadVDSFile();

			// Create single data source
			DataSource datasource = new DataSource(inURL,informat);

			// Create location for data and add to map
			for(i=0;i<X.vds.size();i++){
				int thisvds = X.vds.get(i);
				X.data.put(thisvds, new FiveMinuteData(thisvds,false));
				datasource.add_to_for_vds(thisvds);
			}
			
			if(informat.equalsIgnoreCase("caltransdbx"))
				PeMSReader.ReadDataSource(X.data, datasource,PeMSReader.CaltransDbx);
			else if(informat.equalsIgnoreCase("pemsdataclearinghouse"))
				PeMSReader.ReadDataSource(X.data,datasource,PeMSReader.PeMSDataClearingHouse);
			else
				System.out.println("ERROR");
				
			// Write to text file
			X.WritePerLaneDataToFile();
						
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private void ReadVDSFile() throws Exception{
		String strLine;
		BufferedReader ba = new BufferedReader(new FileReader(vdsfile));
		while( (strLine = ba.readLine())!=null)
			vds.add( Integer.parseInt(strLine));
	}
	
	private void WritePerLaneDataToFile() throws Exception{
		
		int i;
		
		for(Integer thisvds : data.keySet()) {
			FiveMinuteData D = data.get(thisvds);

			PrintStream flw_out = new PrintStream(new FileOutputStream(outfolder + File.separator + thisvds + "_flw.txt"));
            for(i=0;i<D.flw.size();i++)
            	flw_out.print(tabformat(D.flw.get(i)));
            flw_out.close();

//			PrintStream occ_out = new PrintStream(new FileOutputStream(outfolder + File.separator + thisvds + "_occ.txt"));
//            for(i=0;i<D.occ.size();i++)
//            	occ_out.print(tabformat(D.occ.get(i)));
//            occ_out.close();
            
			PrintStream spd_out = new PrintStream(new FileOutputStream(outfolder + File.separator + thisvds + "_spd.txt"));
            for(i=0;i<D.spd.size();i++)
            	spd_out.print(tabformat(D.spd.get(i)));
            spd_out.close();
			
		}		
	}
	
	private static String tabformat(ArrayList<Float> V){
		String out = "";
		for(int i=0;i<V.size();i++)
			out = out + String.format("%f", V.get(i))+ "\t";
		return out+"\n";
	}	
	
	
}
