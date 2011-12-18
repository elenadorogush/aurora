package aurora.hwc.fdcalibration;

import java.net.URL;
import java.util.ArrayList;

public class DataSource {
	private URL url;
	private String format;
	private ArrayList<Integer> for_vds = new ArrayList<Integer>();

	public URL getUrl() {
		return url;
	}

	public String getFormat() {
		return format;
	}
	
	public ArrayList<Integer> getFor_vds() {
		return for_vds;
	}
	
	public void add_to_for_vds(int vds){
		for_vds.add(vds);
	}
	
	public DataSource(String urlname,String f) throws Exception{
		url = new URL(urlname);
		format = f;
	}

}
