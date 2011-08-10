package aurora.hwc.fdcalibration;

import java.net.URL;
import java.util.ArrayList;

public class DataSource {
	private URL url;
	private ArrayList<Integer> for_vds = new ArrayList<Integer>();
	private ArrayList<ArrayList<Integer>> for_vdslanes = new ArrayList<ArrayList<Integer>>();

	public URL getUrl() {
		return url;
	}

	public ArrayList<Integer> getFor_vds() {
		return for_vds;
	}

	public ArrayList<ArrayList<Integer>> getFor_vdslanes() {
		return for_vdslanes;
	}
	
	public void add_to_for_vds(int vds){
		for_vds.add(vds);
	}

	public void add_to_for_vdslanes(ArrayList<Integer> vdslanes){
		for_vdslanes.add(vdslanes);
	}
	
	public DataSource(String urlname) throws Exception{
		url = new URL(urlname);
	}
	
	public void addVDS(int vds,ArrayList<Integer> vdslanes){
		for_vds.add(vds);
		for_vdslanes.add(vdslanes);
	}

}
