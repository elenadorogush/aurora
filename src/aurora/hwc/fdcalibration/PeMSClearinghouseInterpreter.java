package aurora.hwc.fdcalibration;

import java.net.*;
import java.io.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import aurora.service.*;


public class PeMSClearinghouseInterpreter {

	public ArrayList<URL> PeMSCH_5min;

	public PeMSClearinghouseInterpreter(ArrayList<URL> f){
		PeMSCH_5min=f;
	}

    public void Read5minData(ArrayList<Integer> selectedvds,ArrayList<ArrayList<Integer>> selectedlanes,HashMap <Integer,FiveMinuteData> data, Updatable updater) throws Exception {
		int lane;
    	String line,str;
    	int indexof;
    	ArrayList<Integer> lanes;
    	ArrayList<Float> laneflw = new ArrayList<Float>();
    	ArrayList<Float> laneocc = new ArrayList<Float>();
    	ArrayList<Float> lanespd = new ArrayList<Float>();
        Calendar calendar = Calendar.getInstance();
    	float flw,dty,spd;
    	long time;
    
    	// step through data file
    	if (updater != null)
    		updater.notify_update(5);
    	for (int i = 0; i < PeMSCH_5min.size(); i++) {
    		URLConnection uc = PeMSCH_5min.get(i).openConnection();
    		BufferedReader fin = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            while ((line=fin.readLine()) != null) {
                String f[] = line.split(",");
                int vds = Integer.parseInt(f[1]);

                indexof = selectedvds.indexOf(vds);
                if(indexof<0)
                	continue;
                
        		calendar.setTime(ConvertTime(f[0]));
        		time = calendar.getTime().getTime()/1000;
        
                lanes = selectedlanes.get(indexof);
            	laneflw.clear();
            	laneocc.clear();
            	lanespd.clear();
            
            	// store in lane-wise ArrayList
                for (int j = 0; j < lanes.size(); j++) {
                	lane = lanes.get(j)-1;
                	str = f[5*(lane+1)+8];
                	if(str.isEmpty())
                    	laneflw.add(Float.NaN); 
                	else
                		laneflw.add(Float.parseFloat(str)); 
                	str = f[5*(lane+1)+9];
                	if(str.isEmpty())
                		laneocc.add(Float.NaN); 
                	else
                		laneocc.add(Float.parseFloat(str)); 
                	str = f[5*(lane+1)+10];
                	if(str.isEmpty())
                		lanespd.add(Float.NaN); 
                	else
                		lanespd.add(Float.parseFloat(str));
                }

                // compute totals
                flw = 0;
                dty = 0;
                spd = 0;
                for (int j = 0; j < lanes.size(); j++) {
                	flw += laneflw.get(j)*12f;
                	spd += lanespd.get(j);
                }
                spd /= lanes.size();
                dty = flw/spd;
                
                // find the data structure and store. 
                FiveMinuteData D = data.get(vds);
                D.flw.add(flw);
                D.dty.add(dty);
                D.spd.add(spd);
                D.time.add(time);
            }
            fin.close();
            if (updater != null)
            	updater.notify_update(Math.round(50*(((float)i+1)/PeMSCH_5min.size())));
    	}
    	 
    }

    private Date ConvertTime(final String timestr) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        ParsePosition pp = new ParsePosition(0);
        return formatter.parse(timestr,pp);
    }
    
}