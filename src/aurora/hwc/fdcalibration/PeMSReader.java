package aurora.hwc.fdcalibration;

import java.net.*;
import java.io.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import aurora.service.*;

public class PeMSReader {

	public static final ColumnFormat PeMSDataClearingHouse = new ColumnFormat(",",5,8,9,10,8,false);
	public static final ColumnFormat CaltransDbx 			= new ColumnFormat("\t",6,20,22,23,8,true);
	
	public HashMap<String,DataSource> PeMSFileToVDS;

	public PeMSReader(HashMap<String,DataSource> f){
		PeMSFileToVDS=f;
	}

    public void Read5minData(HashMap <Integer,FiveMinuteData> data, Updatable updater) throws Exception {

    	// step through data file
    	if (updater != null)
    		updater.notify_update(5);
    
    	int count = 0;
    	for(DataSource datasource : PeMSFileToVDS.values()){
    		String dataformat = datasource.getFormat();
    		count++;
    		if(dataformat.equalsIgnoreCase("pems") || dataformat.equalsIgnoreCase("pems data clearinghouse"))
    			ReadDataSource(data,datasource,PeMSDataClearingHouse);
    		if(dataformat.equalsIgnoreCase("caltransdbx") || dataformat.equalsIgnoreCase("caltrans dbx"))
    			ReadDataSource(data,datasource,CaltransDbx);
            if (updater != null)
            	updater.notify_update(Math.round(50*(((float)count+1)/ PeMSFileToVDS.size())));
    	}
    	 
    }

    private static Date ConvertTime(final String timestr) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        ParsePosition pp = new ParsePosition(0);
        return formatter.parse(timestr,pp);
    }

    public static void ReadDataSource(HashMap <Integer,FiveMinuteData> data,DataSource datasource, ColumnFormat format) throws NumberFormatException, IOException{
		int lane;
    	String line,str;
    	int indexof;
        Calendar calendar = Calendar.getInstance();
    	float totalflw,totalspd;
    	float val;
    	long time;
    	int actuallanes;
    	boolean hasflw,hasspd,hasocc;
    	    	
		URLConnection uc = datasource.getUrl().openConnection();
		BufferedReader fin = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		if(format.hasheader)
			line=fin.readLine(); 	// discard the header
    	while ((line=fin.readLine()) != null) {
            String f[] = line.split(format.delimiter,-1);
            int vds = Integer.parseInt(f[1]);

            indexof = datasource.getFor_vds().indexOf(vds);
            if(indexof<0)
            	continue;
            
    		calendar.setTime(ConvertTime(f[0]));
    		time = calendar.getTime().getTime()/1000;
    
        	ArrayList<Float> laneflw = new ArrayList<Float>();
        	ArrayList<Float> laneocc = new ArrayList<Float>();
        	ArrayList<Float> lanespd = new ArrayList<Float>();
        
        	// store in lane-wise ArrayList
        	actuallanes = 0;
            totalflw = 0;
            totalspd = 0;
            int index;
            for (lane=0;lane<format.maxlanes;lane++) {
            	
            	index = format.laneblocksize*(lane+1)+format.flwoffset;
            	str = f[index];
            	hasflw = !str.isEmpty();
            	if(hasflw){
            		val = Float.parseFloat(str)*12f;
            		laneflw.add(val);
            		totalflw += val;
            	}
            	else
                	laneflw.add(Float.NaN); 
            	
            	index = format.laneblocksize*(lane+1)+format.occoffset;
            	str = f[index];
            	hasocc = !str.isEmpty();
            	if(hasocc)
            		laneocc.add(Float.parseFloat(str));
            	else
            		laneocc.add(Float.NaN); 
            	
            	index = format.laneblocksize*(lane+1)+format.spdoffset;
            	str = f[index];
            	hasspd = !str.isEmpty();
            	if(hasspd){
            		val = Float.parseFloat(str);
            		lanespd.add(val);
            		totalspd += val;
            	}
            	else
            		lanespd.add(Float.NaN); 
            	if(hasflw || hasocc || hasspd)
            		actuallanes++;
            }

            // find the data structure and store. 
            FiveMinuteData D = data.get(vds);
            if(D.isaggregate && actuallanes>0){
                totalspd /= actuallanes;
                D.addAggFlw(totalflw);
                D.addAggOcc(totalflw/totalspd);
                D.addAggSpd(totalspd);
                D.time.add(time);	
            }
            else{
            
	            D.flwadd(laneflw,0,actuallanes);
	            D.occadd(laneocc,0,actuallanes);
	            D.spdadd(lanespd,0,actuallanes);
	            D.time.add(time);
            }
        }    	
        fin.close();
    }
    
}