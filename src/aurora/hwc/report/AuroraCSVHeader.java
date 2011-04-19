/**
 * @(#)AuroraCSVHeader.java
 */

package aurora.hwc.report;

import java.util.Vector;


/**
 * Aurora CSV header information
 * @author Gabriel Gomes
 */
public class AuroraCSVHeader {

	public String description = "";
	public float simT;
	public Vector<String> vehicletype = new Vector<String>();	
	public Vector<AuroraCSVLink> links = new Vector<AuroraCSVLink>();
	public Vector<AuroraCSVRoute> routes = new Vector<AuroraCSVRoute>();

	public void getRouteNames(Vector<String> names){
		names.clear();
		for(int i=0;i<routes.size();i++)
			names.add(routes.get(i).name);
	}		
	
	@Override
	public boolean equals(Object obj) {
		AuroraCSVHeader that = (AuroraCSVHeader) obj;
		
		if(this.simT!=that.simT)
			return false;

		if(this.vehicletype.size()!=that.vehicletype.size())
			return false;
		
		if(this.links.size()!=that.links.size())
			return false;

		if(this.routes.size()!=that.routes.size())
			return false;
		
		int i,j;
		for(i=0;i<this.links.size();i++){
			AuroraCSVLink L1 = this.links.get(i);
			AuroraCSVLink L2 = that.links.get(i);
			
		    if(!L1.id.equals(L2.id))
		        return false;		    

		    if(L1.type!=L2.type)
		        return false;
		    
		    if(L1.length!=L2.length)
		        return false;

		    if(L1.lanes!=L2.lanes)
		        return false;

		    if(L1.issource!=L2.issource)
		        return false;
		}

		for(i=0;i<this.routes.size();i++){
			AuroraCSVRoute R1 = this.routes.get(i);
			AuroraCSVRoute R2 = that.routes.get(i);
		    if( !R1.name.equals(R2.name) )
		        return false;
		    if( R1.linkids.size()!=R1.linkids.size())
		    	return false;
		    for(j=0;j<R1.linkids.size();j++){
		    	if(!R1.linkids.get(j).equals(R2.linkids.get(j)))
		    		return false;
		    }
		}	
		return true;
	}
	
	public void deepcopy(AuroraCSVHeader z) {
		z.simT = this.simT;
		z.description = this.description;
		int i;
		z.vehicletype.clear();
		for(i=0;i<this.vehicletype.size();i++)
			z.vehicletype.add(this.vehicletype.get(i));
		z.links.clear();
		for(i=0;i<this.links.size();i++)
			z.links.add(this.links.get(i).clone());
		z.routes.clear();
		for(i=0;i<this.routes.size();i++)
			z.routes.add(this.routes.get(i).clone());
	}
	
	public void clear(){
		links.clear();
		routes.clear();
		description = "";
		simT = Float.NaN;
		vehicletype.clear();	
	}
}