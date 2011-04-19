/**
 * @(#)PlotElement.java
 */

package aurora.hwc.report;

import java.io.*;
import java.util.Vector;
import org.w3c.dom.*;
import aurora.*;


/**
 * Class representing an individual plot element
 * @author Gabriel Gomes
 */
public class PlotElement implements AuroraConfigurable {

	private Configuration cfg;
	public static enum Type {multiline,contour,scatter,minmax,none};

	// data source
	public Vector<String> scenarios = new Vector<String>();
	public Vector<String> datafiles = new Vector<String>();

	// axis definition
	public Quantity xquantity;
	public Quantity yquantity;
	public Vector<String> xaxis_linkids = new Vector<String>();
	public Vector<String> yaxis_linkids = new Vector<String>();

	// element type
	public Type type;

	// legend string
	public int legend_precision;
	public String legend_units;
	public Vector<String> legend_text = new Vector<String>();
	public Vector<String> legend = new Vector<String>();
	
	// marker
	//public String marker;
	//public int markersize;
	public Vector<String> colors = new Vector<String>();
	public boolean fill;
	
	// for contour plot
	public int keepSlice;
	
	// for scatter plot
	public boolean boxplot;
	public boolean docustomxaxis;
	public float customxvalue;
	
	// actual plot data
	public Vector<Float> xdata= new Vector<Float>();
	public Vector<Vector<Float>> ydata = new Vector<Vector<Float>>();
	public Vector<Vector<Float>> zdata = new Vector<Vector<Float>>();
	public Vector<Float> legendval = new Vector<Float>();;

	public PlotElement(){};
	
	public PlotElement(PlotElement.Type _type,Configuration c){
		cfg = c;
		type = _type;
		keepSlice = -1;
		fill = cfg.cbx_dofill;
	}
	
	public void setxlinks(Vector<String> in){
		xaxis_linkids.clear();
		for(int i=0;i<in.size();i++)
			xaxis_linkids.add(in.get(i));
	}
	
	public void setylinks(Vector<String> in){
		yaxis_linkids.clear();
		for(int i=0;i<in.size();i++)
			yaxis_linkids.add(in.get(i));
	}

	public void setylinks(String in){
		yaxis_linkids.clear();
		yaxis_linkids.add(in);			
	}

	public void setdatafiles(Vector<String> s,Vector<String> d){
		if(s.size()!=d.size())
			return;
		scenarios.clear();
		datafiles.clear();
		for(int i=0;i<s.size();i++){
			scenarios.add(s.get(i));
			datafiles.add(d.get(i));
		}
	}

	public void setdatafiles(String s, String d){
		scenarios.clear();
		datafiles.clear();
		scenarios.add(s);
		datafiles.add(d);
	}
	
	public void setlegendtext(Vector<String> t){
		legend_text.clear();
		for(int i=0;i<t.size();i++)
			legend_text.add(t.get(i));
	}

	public void setlegendtext(String t){
		legend_text.clear();
		legend_text.add(t);
	}

	public PlotElement clone() {
		PlotElement z = new PlotElement(this.type,this.cfg);
		z.setdatafiles(this.scenarios,this.datafiles);
		z.setxlinks(this.xaxis_linkids);
		z.setylinks(this.yaxis_linkids);
		z.xquantity = this.xquantity;
		z.yquantity = this.yquantity;
		z.keepSlice = this.keepSlice;
		z.legend_precision = this.legend_precision;
		z.legend_units = this.legend_units;
		z.legend_text = this.legend_text;
//		z.marker = this.marker;
//		z.markersize = this.markersize;
		z.boxplot = this.boxplot;
		z.docustomxaxis = this.docustomxaxis;
		z.customxvalue = this.customxvalue;
		return z;
	}

	@Override
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		
		int i;
		String name;
		
		NamedNodeMap A = p.getAttributes();
		for (i=0;i<A.getLength();i++){
			name = A.item(i).getNodeName();
			if(name.equals("color"))
				Utils.readMatlabFormattedStringVector(A.item(i).getTextContent(),this.colors) ;
			if(name.equals("fill"))
				this.fill = Boolean.parseBoolean(A.item(i).getTextContent());
			if(name.equals("type"))
				this.type = PlotElement.Type.valueOf(A.item(i).getTextContent());			
		}
		
		NodeList C = p.getChildNodes();
		for (i=0; i<p.getChildNodes().getLength(); i++){
			name = C.item(i).getNodeName();
			if(name.equals("legend")){
				Utils.readMatlabFormattedStringVector(C.item(i).getTextContent(),legend);
			}
			if(name.equals("xdata"))
				Utils.readMatlabFormattedFloatVector(C.item(i).getTextContent(),xdata);
			if(name.equals("ydata"))
				Utils.readMatlabFormattedFloatMatrix(C.item(i).getTextContent(),ydata);
			if(name.equals("zdata"))
				Utils.readMatlabFormattedFloatMatrix(C.item(i).getTextContent(),zdata);
		}
		return true;
	}

	@Override
	public void xmlDump(PrintStream out) throws IOException {
		out.print("\t\t\t\t\t<element color=\"" + Utils.writeMatlabFormattedVector(colors) + "\" fill=\"" + fill + "\" type=\"" + type + "\">\n");
		out.print("\t\t\t\t\t\t<legend>" + Utils.writeMatlabFormattedVector(legend) + "</legend>\n");
		out.print("\t\t\t\t\t\t<xdata>" + Utils.writeMatlabFormattedVector(xdata) + "</xdata>\n");
		out.print("\t\t\t\t\t\t<ydata>" + Utils.writeMatlabFormattedMatrix(ydata) + "</ydata>\n");	
		if(zdata!=null)
			out.print("\t\t\t\t\t\t<zdata>" + Utils.writeMatlabFormattedMatrix(zdata) + "</zdata>\n");
		out.print("\t\t\t\t\t</element>\n");		
	}

	@Override
	public boolean validate() throws ExceptionConfiguration {
		// TODO Auto-generated method stub
		return true;
	}
	
}


