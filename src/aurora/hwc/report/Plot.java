/**
 * @(#)Plot.java
 */

package aurora.hwc.report;

import java.io.*;
import java.util.Vector;
import org.w3c.dom.*;
import aurora.*;


/**
 * Class representing an individual plot
 * @author Gabriel Gomes
 */
public class Plot implements AuroraConfigurable {
	
	public static enum Type {twoD,threeD,none};

	public boolean showlegend;
	public Vector<PlotElement> elements = new Vector<PlotElement>();
	public String xlabel;
	public String ylabel;
	public String zlabel;
	public Plot.Type type;
	
	public Plot(){};
	
	public Plot(Vector<PlotElement> e,Plot.Type t){
		type = t;
		elements.clear();
		for(int i=0;i<e.size();i++)
			elements.add(e.get(i).clone());
	}

	public void makelabels(PlotElement e){
		
		switch(e.type){
		
		case contour:
			xlabel = "Link index";
			ylabel = "Time [hr]";
	        // Define z label
	        switch(e.yquantity){
            case tvh:
                zlabel = "Vehicles per link [veh]";
                break;
            case qoverride:
                zlabel = "Override proportion [-]";
                break;
            case density:
            	zlabel = "Density [veh/mile]";
                break;
            case speed:
            	zlabel = "Speed [mile/hr]";
            	break;
            case flow:
            	zlabel = "Flow [veh/hr]";
            	break;
	        }
			break;
			
		case scatter:

			switch(e.xquantity){
            case tvh:
            	xlabel = "Vehicles hours [veh.hr]";
            	break;
            case tvm:
            	xlabel = "Vehicles miles [veh.mile]";
            	break;
            case delay:
            	xlabel = "Delayed vehicle hours [veh.hr]";
            	break;
            case lostprod:
            	xlabel = "Lost mainline productivity [lane.mile.hr]";
            	break;
			}

			switch(e.yquantity){
            case tvh:
            	ylabel = "Vehicles hours [veh.hr]";
            	break;
            case tvm:
            	ylabel = "Vehicles miles [veh.mile]";
            	break;
            case delay:
            	ylabel = "Delayed vehicle hours [veh.hr]";
            	break;
            case lostprod:
            	ylabel = "Lost mainline productivity [lane.mile.hr]";
            	break;
			}
			break;
			
		case multiline:
		case minmax:
			
			switch(e.xquantity){
			    case space:          // AGGREGATE OVER TIME
			        xlabel = "Link index";
			        
			        // Define y label
			        switch(e.yquantity){
		            case tvh:
		                ylabel = "Total vehicles hours [veh.hr]";
		                break;
		            case mlvh:
		            	ylabel = "Mainline vehicles hours [veh.hr]";
		                break;
		            case tvm:
		            	ylabel = "Vehicle miles [veh.mile]";
		                break;
		            case mldelay:
		            	ylabel = "Delayed mainline vehicle hours [veh.hr]";
		                break;
		            case delay:
		            	ylabel = "Delayed vehicle hours [veh.hr]";
		                break;
		            case lostprod:
		            	ylabel = "Lost mainline productivity [lane.mile.hr]";
		                break;
		            case qoverride:
		                ylabel = "Override time [hr]";
		                break;
		            case routetraveltime:
		            	ylabel = "Travel time [hr]";
		                break;
		            case time:
		            	ylabel = "Time [hr]";
			        }
			        break;
			        
			    case time:          // AGGREGATE OVER SPACE
			        xlabel = "Time [hr]";
			        
			        // Define y label
			        switch(e.yquantity){
		            case tvh:
		                ylabel = "Total vehicles [veh]";
		                break;
		            case mlvh:
		                ylabel = "Mainline vehicles [veh]";
		                break;
		            case tvm:
		                ylabel = "Flux [veh.mile/hr]";
		                break;
		            case mldelay:
		                ylabel = "Delayed mainline vehicles [veh]";
		                break;
		            case delay:
		                ylabel = "Delayed vehicles [veh]";
		                break;
		            case lostprod:
		                ylabel = "Unused pavement [lane.mile]";
		                break;
		            case qoverride:
		                ylabel = "Overriden onramps [#]";
		                break;
		            case routetraveltime:
		                ylabel = "Travel time [hr]";
		                break;
		            case density:
		                ylabel = "Density [veh/mile]";
		                break;
		            case speed:
		                ylabel = "Speed [mile/hr]";
		                break;
		            case flow:
		                ylabel = "Flow [veh/hr]";
		                break;
		            case space:
		            	ylabel = "Link index";
			        }
			        break;
			}
			break;
		
		}


	}

	@Override
	protected Plot clone() {
		Plot p = new Plot(this.elements,this.type);
		p.xlabel = this.xlabel;
		p.ylabel = this.ylabel;
		return p;
	}

	@Override
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {

		int i;
		String name;
				
		if ((p == null) || (!p.hasChildNodes()))
			return false;
		try {
			
			NamedNodeMap A = p.getAttributes();
			for (i=0; i<A.getLength(); i++){
				name = A.item(i).getNodeName();
				if(name.equals("showlegend"))
					this.showlegend = Boolean.parseBoolean(A.item(i).getTextContent());
				if(name.equals("type"))
					this.type = Plot.Type.valueOf(A.item(i).getTextContent());
				if(name.equals("xlabel"))
					this.xlabel = A.item(i).getTextContent();
				if(name.equals("ylabel"))
					this.ylabel = A.item(i).getTextContent();
				if(name.equals("zlabel"))
					this.zlabel = A.item(i).getTextContent();
			}

			NodeList C = p.getChildNodes();
			for (i=0; i<p.getChildNodes().getLength(); i++){
				name = C.item(i).getNodeName();
				if(name.equals("element")){
					PlotElement E = new PlotElement();
					E.initFromDOM(C.item(i));					
					this.elements.add(E);
				}
			}
		}
		catch(Exception e) {
			throw new ExceptionConfiguration(e.getMessage());
		}
		return true;
		
	}

	@Override
	public void xmlDump(PrintStream out) throws IOException {		
		out.print("\t\t\t\t<plot showlegend=\"" + showlegend 
				+ "\" type=\"" + type 
				+ "\" xlabel=\"" + xlabel 
				+ "\" ylabel=\"" + ylabel + "\" ");
		if(zlabel!=null)
			out.print("zlabel=\"" + zlabel + "\"");
		
		out.print(">\n");
		for(int i=0;i<elements.size();i++)
			elements.get(i).xmlDump(out);
		out.print("\t\t\t\t</plot>\n");		
	}

	@Override
	public boolean validate() throws ExceptionConfiguration {
		// TODO Auto-generated method stub
		return true;
	}
		
}
