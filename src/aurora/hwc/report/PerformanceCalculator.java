/**
 * @(#)PerformanceCalculator.java
 */

package aurora.hwc.report;

import java.util.Vector;

/**
 * Class used by the report generator to process and run performance calculations on individual plot elements.
 * @author Gabriel Gomes
 */
public class PerformanceCalculator {

	public Configuration cfg;
	private AuroraCSVFile csv;
	
	public Vector<String> alllinkids = new Vector<String>();
	public Vector<Float> alllinklength = new Vector<Float>();
	public Vector<Float> alllinklengthlanes = new Vector<Float>();
	public Vector<Boolean> alllinkissource = new Vector<Boolean>();
	public Vector<Integer> alllinktype = new Vector<Integer>();
	
	public PerformanceCalculator(Configuration c,AuroraCSVHeader header){
		cfg = c;
		for(int i=0;i<header.links.size();i++){
			AuroraCSVLink L = header.links.get(i);
			alllinkids.add( L.id );
			alllinklength.add( L.length );
			alllinklengthlanes.add( L.length*L.lanes );
			alllinkissource.add( L.issource );
			alllinktype.add( L.type );
		}
		csv = new AuroraCSVFile(header);
	}
	
	@SuppressWarnings("unchecked")
	public void processElements(Vector<PlotElement> E){

		int i,j,k,ndim1=0;
		Vector<Float> subsetlinklength = new Vector<Float>();
		Vector<Boolean> subsetlinkissource = new Vector<Boolean>();
		Vector<Float> subsetlinklengthlanes = new Vector<Float>();

		int colorcount = 0;
		for(i=0;i<E.size();i++){

			PlotElement e = E.get(i);
			cfg.writeToConsole("\t\t\t\t+ Element " + i);
			
			subsetlinklength.clear();
			subsetlinkissource.clear();
			subsetlinklengthlanes.clear();
			
			if(cfg.reporttype==ReportType.scatter){
				processScatter(e);
				e.colors.add(cfg.colors.get(colorcount%cfg.colors.size()));
				colorcount++;
				continue;
			}
						
			// Define space subset
			Vector<String> subsetlinkids = new Vector<String>();
			if(e.yaxis_linkids.isEmpty())
				subsetlinkids = alllinkids;
			else
				subsetlinkids = Utils.vectorintersect(e.yaxis_linkids,alllinkids);
			
			for(j=0;j<subsetlinkids.size();j++){
				int ind = alllinkids.indexOf(subsetlinkids.get(j));
				subsetlinklength.add(alllinklength.get(ind));
				subsetlinkissource.add(alllinkissource.get(ind));
				subsetlinklengthlanes.add(alllinklengthlanes.get(ind));
			}
			
			Vector<Quantity> getq = new Vector<Quantity>();
			Vector<Vector<Vector<Float>>> phi = new Vector<Vector<Vector<Float>>>();
		    Float dt = Float.NaN;
			
		    for(j=0;j<e.scenarios.size();j++){
	
				// make list of quantities to retrieve from data files
				getq.clear();
			    switch(e.yquantity){
			    
			    case flow:
			    case tvm:
			    	getq.add(Quantity.flow);
			    	break;
			    
			    case density:
			    case tvh:
			    case mlvh:
			    	getq.add(Quantity.density);
			    	break;
			    
			    case speed:
			    case delay:
			    case mldelay:
			    case lostprod:
			    case routetraveltime:
			    	getq.add(Quantity.capacity);
			    	getq.add(Quantity.critical_density);
			    	getq.add(Quantity.flow);
			    	getq.add(Quantity.density);
			    	break;
			    	
			    case routetrajectories:
			    	getq.add(Quantity.capacity);
			    	getq.add(Quantity.critical_density);
			    	getq.add(Quantity.flow);
			    	getq.add(Quantity.density);
			    	break;
			    
			    case qoverride:
			    	getq.add(Quantity.density);
			    	getq.add(Quantity.queue_limit);
			    	break;
			    }
		    	
			    cfg.writeToConsole("\t\t\t\t+ Loading " + e.datafiles.get(j));
			    csv.loadData(getq,e.scenarios.get(j),e.datafiles.get(j),subsetlinkids,this);
	
			    if(!csv.hasdata){
			    	phi.add(null);
			    	continue;
			    }
			    
			    // compute dt, check that it is the same for all scenarios
			    Float newdt = csv.time.get(1)-csv.time.get(0);
			    if(!dt.isNaN() && newdt!=dt){
			    	// ERROR dt must be the same for all scenarios
			    }
			    dt = newdt;
			    
			    // size of 1st dimension of Density and/or OutFlow data blocks
			    switch(e.yquantity){
			    case flow:
			    case tvm:
			    	ndim1 = csv.data.getOutFlow().size();
			    	break;
			    
			    case density:
			    case tvh:
			    case mlvh:
			    case speed:
			    case delay:
			    case mldelay:
			    case lostprod:
			    case routetraveltime:
			    case routetrajectories:
			    case qoverride:
			    	ndim1 = csv.data.getDensity().size();
			    	break;
			    }
			    
			    // process Density and/or OutFlow to time/space matrix, append to phi
			    switch(e.yquantity){
			    
		        case density:
				    for(k=0;k<ndim1;k++)
				    	phi.add(csv.data.getDensity().get(k));
		            break;
		            
		        case speed:
				    for(k=0;k<ndim1;k++)
		        		phi.add(csv.data.evaluateSpeed(k,subsetlinkissource));
		            break;
		            
		        case flow:
				    for(k=0;k<ndim1;k++)
				    	phi.add(csv.data.getOutFlow().get(k));
		            break;
		            
		        case tvh:
		        case mlvh: 
				    for(k=0;k<ndim1;k++)
				    	phi.add(csv.data.evaluateVehicles(k,subsetlinklength));
		            break;
		            
		        case tvm:
				    for(k=0;k<ndim1;k++)
				    	phi.add(csv.data.evaluateFlux(k,subsetlinklength));
		            break;
		            
		        case delay:
		        case mldelay:	
				    for(k=0;k<ndim1;k++)
				    	phi.add(csv.data.evaluateDelayedVehicles(k,subsetlinklength));
		            break;
		            
		        case lostprod:       // unused pavement in [lane.mile]
				    for(k=0;k<ndim1;k++)
				    	phi.add(csv.data.evaluateUnutilizedPavement(k,subsetlinklengthlanes,subsetlinkissource,cfg.congspeed));
		            break;
		            
		        case qoverride:
				    for(k=0;k<ndim1;k++)
				    	phi.add(csv.data.evaluateQueueOverride(k));
			    	break;
		            
		        //case routetraveltime:
				//    for(int k=0;k<ndim3;k++)
				//    	Vector<Vector<Float>> v = csv.data.evaluateSpeed(k,subsetlinkissource);
		        //    //phi = computeRouteTravelTime(Length,time,v);
		        //    break;
		            
		        //case routetrajectories:
		            //v = evaluateSpeed(Cap,CritDens,F,D,Issource,false);
		            //phi = computeRouteTravelTime(Length,time,v);
		        //    break;
		        }
			    
			} // loop scenarios
	
			if(e.type==PlotElement.Type.contour){
		        // matrix in 3rd dimension case (for contours)
		        e.zdata = (Vector<Vector<Float>>) phi.get(e.keepSlice).clone();
		        e.xdata = Utils.cumsum(subsetlinklength);
		        e.ydata = new Vector<Vector<Float>>();
		        e.ydata.add((Vector<Float>) csv.time.clone());
			}
			else{
			    switch(e.xquantity){
		        case space:
		        	e.ydata = Utils.sumoverdimension3(phi,Quantity.time);
		        	Utils.multiplyscalar2(e.ydata,dt);
		            e.xdata = new Vector<Float>();
		            for(k=0;k<subsetlinklength.size();k++)
		            	e.xdata.add((float) k);
		            for(k=0;k<e.ydata.size();k++)
		            	e.legendval.add(Utils.sum(e.ydata.get(k)));
		            break;
		        case time:
 		        	e.ydata = Utils.sumoverdimension3(phi,Quantity.space);
		            e.xdata = (Vector<Float>) csv.time.clone();
		            for(k=0;k<e.ydata.size();k++)
		            	e.legendval.add(Utils.sum(e.ydata.get(k))*dt);
	//		        if(strcmp(E.yquantity,'routetraveltime'))  % exception for route travel time plot
	//		            e.legendval = meanwithnan(phi,1);
		            break;
		    	}
			}

		    // determine color list for this element
			for(j=0;j<e.ydata.size();j++){
				e.colors.add(cfg.colors.get(colorcount%cfg.colors.size()));
				colorcount++;
			}

			// construct legend
			e.legend.clear();
			for (j = 0; j < e.legend_text.size(); j++)
				if (e.legend_units!=null) {
					e.legend.add(String.format(e.legend_text.get(j) + " (%." + e.legend_precision + "f " + e.legend_units + ")", j<e.legendval.size() ? e.legendval.get(j):-1));
				}
				else
					e.legend.add( String.format(e.legend_text.get(j)) );
		}

	}

	public void processScatter(PlotElement e){
		
		int i;
		Vector<Float> xdata = new Vector<Float>();
		Vector<Float> ydata = new Vector<Float>();
		
		for(i=0;i<e.scenarios.size();i++){

		    cfg.writeToConsole("\t\t\t\t+ Loading " + e.datafiles.get(i));

		    csv.loadDataScatter(e.scenarios.get(i),e.datafiles.get(i),e.yaxis_linkids,this);		    
		    
		    switch(e.yquantity){
	            
	        case tvh:
			    ydata.add(Utils.sum(csv.data.getVh()));
	            break;
	            
	        case tvm:
	        	ydata.add(Utils.sum(csv.data.getVm()));
	            break;
	            
	        case delay:
	        	ydata.add(Utils.sum(csv.data.getDelay()));
	            break;
	            
	        case lostprod:       // unused pavement in [lane.mile]
	        	ydata.add(Utils.sum(csv.data.getLostprod()));
	            break;
	            
	        }
		    
		    csv.loadDataScatter(e.scenarios.get(i),e.datafiles.get(i),e.xaxis_linkids,this);		    
		    
		    switch(e.xquantity){
	            
	        case tvh:
			    xdata.add(Utils.sum(csv.data.getVh()));
	            break;
	            
	        case tvm:
	        	xdata.add(Utils.sum(csv.data.getVm()));
	            break;
	            
	        case delay:
	        	xdata.add(Utils.sum(csv.data.getDelay()));
	            break;
	            
	        case lostprod:       // unused pavement in [lane.mile]
	        	xdata.add(Utils.sum(csv.data.getLostprod()));
	            break;
	            
	        }		    
		}
		e.xdata = xdata;
		e.ydata = new Vector<Vector<Float>>();
		e.ydata.add(ydata);
		e.legend.add(e.legend_text.get(0));
	}
	
}
