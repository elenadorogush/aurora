/**
 * @(#)gui_mainpanel.java
 */

package aurora.hwc.report;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.*;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * Main panel for the Aurora Report Generator.
 * @author Gabriel Gomes
 */
public class gui_mainpanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -7405218897970299291L;

	public static String homePath = System.getProperty("user.home") + "\\ARG";
	public static String tempPath = homePath + "\\tempfiles";
	
	private Configuration config = new Configuration();
	private Vector<String> colors = new Vector<String>();
	
	private JTabbedPane tabbedPane 				= new JTabbedPane();
	private GPanel panelcontents;

	private JTree scenariostree;
	private gui_CheckTreeManager checkTreeManager;

	private JTable table_scenariosgroups 		= new JTable();
	private JTable table_groups 				= new JTable();

	private JButton button_save 				= new JButton("Save");
	private JButton button_load 				= new JButton("Load");
	private JButton button_start 				= new JButton("Start");
	private JButton button_loadroutes 			= new JButton("Load routes");
	private JButton button_colors 				= new JButton();

	private JComboBox cmb_reporttype 			= new JComboBox();
	private JComboBox cmb_export				= new JComboBox();
	private JComboBox cmb_xaxis_subnetwork 		= new JComboBox();
	private JComboBox cmb_yaxis_subnetwork 		= new JComboBox();
	private JComboBox cmb_xaxis_quantity 		= new JComboBox();
	private JComboBox cmb_yaxis_quantity 		= new JComboBox();

	private JTextArea  txt_console 				= new JTextArea(5,20);
	private JTextField txt_customxaxislabel 	= new JTextField(15);
	private JTextField txt_maxdatapoints 		= new JTextField(5);
	private JTextField txt_congspeed 			= new JTextField(5);
	private JTextField txt_outputfile 			= new JTextField(20);
	private JTextField txt_fromtime				= new JTextField(3);
	private JTextField txt_totime				= new JTextField(3);
	
	private JCheckBox cb_fillplot 				= new JCheckBox();
	private JCheckBox cb_legend 				= new JCheckBox();
	private JCheckBox cb_linkstate_time 		= new JCheckBox();
	private JCheckBox cb_sysperf_time   		= new JCheckBox();
	private JCheckBox cb_onperf_time 			= new JCheckBox();
	private JCheckBox cb_routeperf_time 		= new JCheckBox();
	private JCheckBox cb_routetraveltime_time 	= new JCheckBox();
	private JCheckBox cb_onperf_space 			= new JCheckBox();
	private JCheckBox cb_routeperf_space 		= new JCheckBox();
	private JCheckBox cb_onperf_contour 		= new JCheckBox();
	private JCheckBox cb_routeperf_contour 		= new JCheckBox();
	private JCheckBox cb_routetraveltime_contour = new JCheckBox();	
	private JCheckBox cb_boxplot 				= new JCheckBox("Box plot");

	public gui_mainpanel(File configfile){

		colors.add("#69A7D6");
		colors.add("#980D92");
		colors.add("#D32A64");
		colors.add("#707826");
		colors.add("#DFD555");
		colors.add("#E17A8F");
		colors.add("#2B478E");
		colors.add("#4CDA1D");
		colors.add("#F432C6");
		colors.add("#07CAFF");
		colors.add("#1C9F22");
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// tabbed pane ............
		tabbedPane.addTab("Basic"         ,null,new BasicPanel(this)        ,null);
		tabbedPane.addTab("Sections"      ,null,new SectionsPanel(this)       ,null);
		tabbedPane.addTab("Options"       ,null,new OptionsPanel(this)      ,null);
		tabbedPane.addTab("Scatter Groups",null,new ScatterGroupsPanel(this),null);
		tabbedPane.addTab("Scatter Plots" ,null,new ScatterPlotsPanel(this) ,null);
		tabbedPane.setSelectedIndex(0);
		tabbedPane.setPreferredSize(new Dimension(500,400));
		this.add(tabbedPane);

		// message console .....................
		txt_console.setCaretPosition(txt_console.getDocument().getLength());
		txt_console.setEditable(false);
		txt_console.setTabSize(1);
		this.add(new JScrollPane(txt_console));
		
		// buttons .....................
		button_save.setActionCommand("savebutton");
		button_save.addActionListener(this);
		button_save.setAlignmentX(Component.CENTER_ALIGNMENT);
		button_load.setActionCommand("loadbutton");
		button_load.addActionListener(this);
		button_load.setAlignmentX(Component.CENTER_ALIGNMENT);
		button_start.addActionListener(new startbuttonListener(this));
		button_start.setAlignmentX(Component.CENTER_ALIGNMENT);
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(button_save);
		buttonsPanel.add(button_load);
		buttonsPanel.add(button_start);
		buttonsPanel.setMaximumSize(new Dimension(1000, 40));
		buttonsPanel.setAlignmentX(0f);
		this.add(buttonsPanel);
		        
        // disable tabs and start button
        tabbedPane.setEnabledAt(1,false);
        tabbedPane.setEnabledAt(2,false);
        tabbedPane.setEnabledAt(3,false);
        tabbedPane.setEnabledAt(4,false);
        button_start.setEnabled(false);

        System.setOut(new PrintStream(new JTextAreaOutputStream(txt_console)));
        
        // load configuration
        if(configfile!=null)
        	loadconfigfile(configfile);
        
        Utils.writeToConsole("Welcome to the TOPL Report Generator.");
	}
	

	////////////////////////////////////////////////////////////////////////
	// Panels
	////////////////////////////////////////////////////////////////////////

	private class BasicPanel extends GPanel {
		private static final long serialVersionUID = -8136865529407860949L;

		public BasicPanel(JPanel p){
			
			super(p);
			JPanel comp = new JPanel();
			BoxLayout aa = new BoxLayout(comp, BoxLayout.Y_AXIS);
			comp.setLayout(aa);

			// Panel exporter ..........
			JPanel panelexporter = createBorderedPanel("Export format");
			
			Iterator<String> it = Utils.exportString2Type.keySet().iterator();
		    while(it.hasNext()){
		    	String rtype = (String) it.next();
		    	cmb_export.addItem(rtype);
		    }
		    cmb_export.setMaximumSize(new Dimension(1000,0));
		    panelexporter.add(cmb_export);
			comp.add(panelexporter);

			// Panel type of report ..........
			JPanel panelreporttype = createBorderedPanel("Type of report");
		    Iterator<String> it1 = Utils.reportString2Type.keySet().iterator();
		    while(it1.hasNext()){
		    	String rtype = (String) it1.next();
		    	cmb_reporttype.addItem(rtype);
		    }
			cmb_reporttype.setMaximumSize(new Dimension(1000,0));
			cmb_reporttype.setActionCommand("reporttype");
			cmb_reporttype.addActionListener((gui_mainpanel) p);
			
			panelreporttype.add(cmb_reporttype);
			comp.add(panelreporttype);

			// panel scenarios tree ................ 
			JPanel paneltree = createBorderedPanel("Available simulation files");
			DefaultMutableTreeNode top = new DefaultMutableTreeNode("Scenarios");
		    File file = new File(homePath + "\\files");
		    int i,j;
		    if (file.isDirectory()) {
		    	File [] list = file.listFiles();
		    	for (i = 0; i < list.length; i++){
		    		if(list[i].isDirectory()){
		    			DefaultMutableTreeNode newbranch = new DefaultMutableTreeNode(list[i].getName());
		    			File [] sublist = list[i].listFiles();
		    			for(j=0;j<sublist.length;j++){
		    				if(sublist[j].isFile() & sublist[j].getName().endsWith(".csv"))
		    					newbranch.add(new DefaultMutableTreeNode(sublist[j].getName()));
		    			}
		    			top.add(newbranch);			    			
		    		}
		    	}
		    }
			scenariostree = new JTree(top);
			checkTreeManager = new gui_CheckTreeManager(scenariostree,mypanel); 			
			paneltree.add(new JScrollPane(scenariostree));
			comp.add(paneltree);

			// panel output file ....................
			JPanel paneloutputfile = createBorderedPanel("Output file");
			paneloutputfile.add(txt_outputfile);
			comp.add(paneloutputfile);

	        add(comp);
		}
	}

	private class SectionsPanel extends GPanel {
		private static final long serialVersionUID = -5116473932964910983L;

		public SectionsPanel(JPanel p){
			
			super(p);
			JPanel comp = new JPanel();
			BoxLayout x = new BoxLayout(comp, BoxLayout.Y_AXIS);
			comp.setLayout(x);
			
			// panel time range .......................
			txt_fromtime.setText("0");
			txt_totime.setText("24");
			JPanel paneltimerange = createBorderedPanel("Time range");
			paneltimerange.setLayout(new BoxLayout(paneltimerange, BoxLayout.X_AXIS));
			paneltimerange.add(new JLabel("From "));
			paneltimerange.add(txt_fromtime);
			paneltimerange.add(new JLabel("To "));
			paneltimerange.add(txt_totime);
			comp.add(paneltimerange);

			// panel contents ........................
			panelcontents = createBorderedPanel("Contents");
			panelcontents.setLayout(new GridLayout(0,4));			
			panelcontents.add(new JLabel(""));
			panelcontents.add(new JLabel("over time"));
			panelcontents.add(new JLabel("over space"));
			panelcontents.add(new JLabel("contour"));
			panelcontents.add(new JLabel("Each Link State"));
			panelcontents.add(cb_linkstate_time);
			panelcontents.add(new JLabel(""));
			panelcontents.add(new JLabel(""));
			panelcontents.add(new JLabel("System performance"));
			panelcontents.add(cb_sysperf_time);
			panelcontents.add(new JLabel(""));
			panelcontents.add(new JLabel(""));
			panelcontents.add(new JLabel("Onramp performance"));
			panelcontents.add(cb_onperf_time);
			panelcontents.add(cb_onperf_space);
			panelcontents.add(cb_onperf_contour);
			panelcontents.add(new JLabel("Route performance"));
			panelcontents.add(cb_routeperf_time);
			panelcontents.add(cb_routeperf_space);
			panelcontents.add(cb_routeperf_contour);
			panelcontents.add(new JLabel("Route travel time"));
			panelcontents.add(cb_routetraveltime_time);
			panelcontents.add(new JLabel(""));
			panelcontents.add(cb_routetraveltime_contour);
			comp.add(panelcontents);
			
			add(comp);			
		}
	}
	
	private class OptionsPanel extends GPanel {
		private static final long serialVersionUID = 3914963064623725909L;

		public OptionsPanel(JPanel p){

			super(p);
			JPanel comp = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			c.anchor = GridBagConstraints.EAST;
			c.gridx = 0;
			c.gridy = 0;
			comp.add(new JLabel("Congestion speed [mph] "),c);
			
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 1;
			c.gridy = 0;
			txt_congspeed.setText("55");
			comp.add(txt_congspeed,c);

			c.anchor = GridBagConstraints.EAST;
			c.gridx = 0;
			c.gridy = 1;
			comp.add(new JLabel("Maximum # data points to read "),c);
			
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 1;
			c.gridy = 1;
			txt_maxdatapoints.setText("200");
			comp.add(txt_maxdatapoints,c);

			c.anchor = GridBagConstraints.EAST;
			c.gridx = 0;
			c.gridy = 2;
			comp.add(new JLabel("Colors "),c);
			
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 1;
			c.gridy = 2;
			button_colors.setActionCommand("colorsbutton");
			button_colors.addActionListener((gui_mainpanel) p);
			comp.add(button_colors,c);			

			c.anchor = GridBagConstraints.EAST;
			c.gridx = 0;
			c.gridy = 3;
			comp.add(new JLabel("Fill plots"),c);
			
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 1;
			c.gridy = 3;
			comp.add(cb_fillplot,c);

			c.anchor = GridBagConstraints.EAST;
			c.gridx = 0;
			c.gridy = 4;
			comp.add(new JLabel("Include legend"),c);
			
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 1;
			c.gridy = 4;
			comp.add(cb_legend,c);
			
			add(comp);	
		}
	}

	private class ScatterGroupsPanel extends GPanel {
		private static final long serialVersionUID = -5047368978431994762L;

		public ScatterGroupsPanel(JPanel p){
			super(p);
			table_scenariosgroups.setModel(new scenariosgroupsTableModel());
			table_scenariosgroups.setPreferredScrollableViewportSize(new Dimension(500, 70));
			table_scenariosgroups.setFillsViewportHeight(true);
			table_scenariosgroups.getTableHeader().setReorderingAllowed(false);
	        add(new JScrollPane(table_scenariosgroups));
		}
	}

	private class ScatterPlotsPanel extends GPanel {
		private static final long serialVersionUID = 1589996087091613616L;

		public ScatterPlotsPanel(JPanel p){

			super(p);
			JPanel comp = new JPanel();
			comp.setLayout(new BoxLayout(comp, BoxLayout.Y_AXIS));
			
			// Panel performance measures ..........
			JPanel panelmeasures = createBorderedPanel("Performance measures");
			panelmeasures.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			
			c.anchor = GridBagConstraints.WEST;
			c.gridx = 0;
			c.gridy = 0;
			button_loadroutes.setActionCommand("loadroutesbutton");
			button_loadroutes.addActionListener((gui_mainpanel) p);
			panelmeasures.add(button_loadroutes,c);

			JPanel panelcustomxaxis = createBorderedPanel("Custom X axis");
			panelcustomxaxis.setLayout(new BoxLayout(panelcustomxaxis, BoxLayout.Y_AXIS));
			txt_customxaxislabel.setActionCommand("editcustomxaxis");
			txt_customxaxislabel.addActionListener((gui_mainpanel) p);
			panelcustomxaxis.add(txt_customxaxislabel);
			cb_boxplot.setEnabled(false);
			panelcustomxaxis.add(cb_boxplot);

			c.gridx = 0;
			c.gridy = 1;
			panelmeasures.add(panelcustomxaxis,c);
			
			c.gridx = 0;
			c.gridy = 2;
			panelmeasures.add(new JLabel("X-axis subnetwork"),c);

			c.gridx = 1;
			c.gridy = 2;
			panelmeasures.add(new JLabel("Y-axis subnetwork"),c);
			
			c.gridx = 0;
			c.gridy = 3;
			cmb_xaxis_subnetwork.addItem("Network");
			cmb_xaxis_subnetwork.addItem("Onramps");
			cmb_xaxis_subnetwork.addItem("Mainline");
			panelmeasures.add(cmb_xaxis_subnetwork,c);

			c.gridx = 1;
			c.gridy = 3;
			cmb_yaxis_subnetwork.addItem("Network");
			cmb_yaxis_subnetwork.addItem("Onramps");
			cmb_yaxis_subnetwork.addItem("Mainline");
			panelmeasures.add(cmb_yaxis_subnetwork,c);

			c.gridx = 0;
			c.gridy = 4;
			panelmeasures.add(new JLabel("X-axis quantity"),c);

			c.gridx = 1;
			c.gridy = 4;
			panelmeasures.add(new JLabel("Y-axis quantity"),c);

			c.gridx = 0;
			c.gridy = 5;
			cmb_xaxis_quantity.addItem("Vehicle hours");
			cmb_xaxis_quantity.addItem("Vehicle miles");
			cmb_xaxis_quantity.addItem("Delay");
			panelmeasures.add(cmb_xaxis_quantity,c);

			c.gridx = 1;
			c.gridy = 5;
			cmb_yaxis_quantity.addItem("Vehicle hours");
			cmb_yaxis_quantity.addItem("Vehicle miles");
			cmb_yaxis_quantity.addItem("Delay");
			panelmeasures.add(cmb_yaxis_quantity,c);
			comp.add(panelmeasures);
			
			// Panel groups ..........
			JPanel panelgroups = createBorderedPanel("Groups");
			table_groups.setModel(new groupsTableModel());
	    	table_groups.setFillsViewportHeight(true);
	    	table_groups.getTableHeader().setReorderingAllowed(false);
	    	table_groups.setAutoCreateColumnsFromModel(false);
	        panelgroups.add(new JScrollPane(table_groups));
			comp.add(panelgroups);

			add(comp);
		}
	}

	private GPanel createBorderedPanel(String title){
		TitledBorder titled;
		titled = BorderFactory.createTitledBorder(title);
		titled.setTitlePosition(TitledBorder.CENTER);
		GPanel x = new GPanel(this); 
		x.setBorder(titled);
		return x;
	}

	////////////////////////////////////////////////////////////////////////
	// Listeners
	////////////////////////////////////////////////////////////////////////
	
	public void actionPerformed(ActionEvent event) {

		int i;
		
		String command = event.getActionCommand();
		
		// report type selected ............................................
		if(command.equals("reporttype")){
			
			enabletoggglebuttons();

			ReportType rtype = Utils.reportString2Type.get((String) cmb_reporttype.getSelectedItem());
			
			switch(rtype){
			case vehicletypes:
				panelcontents.setEnabled(true);
				cb_routeperf_contour.setEnabled(true);
				cb_onperf_contour.setEnabled(true);
				cb_fillplot.setEnabled(true);
				cb_legend.setEnabled(true);
				cb_routetraveltime_contour.setEnabled(false);
				cb_routetraveltime_time.setEnabled(false);
				break;
			case aggvehicletypes:
				panelcontents.setEnabled(true);
				cb_routeperf_contour.setEnabled(true);
				cb_onperf_contour.setEnabled(true);
				cb_fillplot.setEnabled(true);
				cb_legend.setEnabled(true);
				cb_routetraveltime_contour.setEnabled(true);
				cb_routetraveltime_time.setEnabled(true);
				break;
			case bestworst:
				panelcontents.setEnabled(true);
				cb_routeperf_contour.setEnabled(false);
				cb_onperf_contour.setEnabled(false);
				cb_fillplot.setEnabled(false);
				cb_legend.setEnabled(false);
				cb_routetraveltime_contour.setEnabled(true);
				cb_routetraveltime_time.setEnabled(true);
				break;
			case scatter:
				panelcontents.setEnabled(false);
				cb_fillplot.setEnabled(false);
				cb_routetraveltime_contour.setEnabled(false);
				cb_routetraveltime_time.setEnabled(false);
				break;
			}		
		}

		// Color button pressed ............................................
		if(command.equals("colorsbutton")){			
			gui_color window = new gui_color(colors);
			window.showDialog(colors);			
		}
		
		// Save button pressed ............................................
		if(command.equals("savebutton")){

			String ext = "xml";
			JFileChooser fc = new JFileChooser("Save Configuration");
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File fp = fc.getSelectedFile();
				if (fp.exists()) {
					int res = JOptionPane.showConfirmDialog(this, "File '" + fp.getName() + "' exists. Overwrite?", "Confirmation", JOptionPane.YES_NO_OPTION);
					if (res == JOptionPane.NO_OPTION)
						return;
				}
				try {
					String fpath = fp.getAbsolutePath();
					if (!fp.getName().endsWith(ext))
						fpath += "." + ext;
					PrintStream oos = new PrintStream(new FileOutputStream(fpath));
					writeConfiguration();
					config.xmlDump(oos);
					oos.close();
				}
				catch(Exception exc) {
					JOptionPane.showMessageDialog(this, exc.getMessage(), exc.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		// Load button pressed ............................................
		if(command.equals("loadbutton")){
			JFileChooser fc = new JFileChooser("Open File");
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				loadconfigfile(fc.getSelectedFile());
			}
		}
		
		// Load routes button pressed ............................................
		if(command.equals("loadroutesbutton")){
			Utils.writeToConsole("Loading and comparing routes...");
			AuroraCSVHeader commonHeader = new AuroraCSVHeader();
			if(!AuroraCSVFile.loadCommonHeader(config, commonHeader)){
				//System.out.println("Error: headers in selected files are not all the same");
			}
			else{
				Vector<String> routenames = new Vector<String>();
				Vector<String> newlist = new Vector<String>();
				commonHeader.getRouteNames(routenames);
				
				// modify cmb_xaxis_subnetwork
				for(i=0;i<cmb_xaxis_subnetwork.getItemCount();i++){
					String listitem = (String) cmb_xaxis_subnetwork.getItemAt(i);
					if(routenames.indexOf(listitem)<0)
						newlist.add(listitem);
				}
				for(i=0;i<routenames.size();i++)
					newlist.add(routenames.get(i));
				cmb_xaxis_subnetwork.removeAllItems();
				for(i=0;i<newlist.size();i++)
					cmb_xaxis_subnetwork.addItem(newlist.get(i));
				
				newlist.clear();
				
				// modify cmb_yaxis_subnetwork
				for(i=0;i<cmb_yaxis_subnetwork.getItemCount();i++){
					String listitem = (String) cmb_yaxis_subnetwork.getItemAt(i);
					if(routenames.indexOf(listitem)<0)
						newlist.add(listitem);
				}
				for(i=0;i<routenames.size();i++)
					newlist.add(routenames.get(i));
				cmb_yaxis_subnetwork.removeAllItems();
				for(i=0;i<newlist.size();i++)
					cmb_yaxis_subnetwork.addItem(newlist.get(i));
			}
			Utils.writeToConsole("\t+ Done.");
		}		

		// edit custom x label ............................................
		if(command.equals("editcustomxaxis")){
			
			String xlab = txt_customxaxislabel.getText();			
			ArrayList<String> liststr = new ArrayList<String>();
			for(i=0;i<cmb_xaxis_quantity.getItemCount();i++)
				liststr.add( (String) cmb_xaxis_quantity.getItemAt(i) );
			boolean havexlab = !liststr.get(0).equals("Vehicle hours");
			
			if(havexlab){
			    if( xlab.isEmpty() ){
			        liststr.remove(0);
			        havexlab = false;
			    }
			    else{
			    	liststr.set(0, xlab);
			        havexlab = true;
			    }
			}
			else{
			    if(liststr.indexOf(xlab)>=0){
			        //disp('invalid name')
			        havexlab = false;
			    }
			    else if( !xlab.isEmpty() ){
			    	liststr.add(0,xlab);
			        havexlab = true;
			    }
			}

			cmb_xaxis_quantity.removeAllItems();
			for(i=0;i<liststr.size();i++)
				cmb_xaxis_quantity.addItem(liststr.get(i));

			groupsTableModel z = (groupsTableModel) table_groups.getModel();
			if(havexlab){
				cb_boxplot.setEnabled(true);
				z.setColumnEditable(1,true);
				z.setxvaluecolname(xlab);
			}
			else{
				cb_boxplot.setEnabled(false);
				z.setColumnEditable(1,false);
				z.setxvaluecolname("-");
				z.clearxvalue();
			}
		}
	}
	
	public class startbuttonListener implements ActionListener {
		
		gui_mainpanel window;

		public startbuttonListener(){}
		
		public startbuttonListener(gui_mainpanel w){
			window = w;
		}
		
		public void actionPerformed(ActionEvent arg0) {
			writeConfiguration();
			gui_mainpanel.run(window.config);
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////
	// table models
	////////////////////////////////////////////////////////////////////////
	
	public class scenariosgroupsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -1470317929527636468L;
		
		private Vector<String> scenarionames  = new Vector<String>();
		private Vector<String> scenariogroups = new Vector<String>();

		public int getColumnCount() { return 2; }
		public int getRowCount() { return scenarionames.size(); }
		public boolean isCellEditable(int rowIndex, int columnIndex) {	
			return columnIndex>0;
		}
		public String getColumnName(int column) {
			String str = new String();
			switch(column){
			case 0:
				return str = "Scenario";
			case 1:
				return str = "Group";
			}
			return str;
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if(rowIndex>=getRowCount())
				return;
			switch(columnIndex){
			case 0:
				scenarionames.set(rowIndex, (String) aValue);
				break;
			case 1:
				scenariogroups.set(rowIndex, (String) aValue);
				break;
			}
			updategroupstable();	
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(columnIndex){
			case 0:
				return scenarionames.get(rowIndex);
			case 1:
				return scenariogroups.get(rowIndex);
			default:
				return null;
			}
		}
		
		public void setData(Vector<String> newnames,Vector<String> newgroups){
			int i,j;
			for(i=0;i<getRowCount();i++){
				for(j=0;j<getColumnCount();j++){
					setValueAt("",i,j);
				}
			}
			scenarionames.clear();
			scenariogroups.clear();
			for(i=0;i<newnames.size();i++){
				scenarionames.add(newnames.get(i));
				scenariogroups.add(newgroups.get(i));
			}
			fireTableDataChanged();
		}
		
		public void clear(){
			scenarionames.clear();
			scenariogroups.clear();
			fireTableDataChanged();
		}
				
	}
	
	public class groupsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 2311840563375746796L;
		
		private Vector<String> groupnames  = new Vector<String>();
		private Vector<String> xvalue = new Vector<String>();
		private String xvaluecolname = "-";
		private boolean [] iscolumneditable = {false,false};

		public int getColumnCount() { return 2; }
		public int getRowCount() { return groupnames.size(); }
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return iscolumneditable[columnIndex];
		}
		
		public String getColumnName(int column) {
			String str = new String();
			switch(column){
			case 0:
				return str = "Name";
			case 1:
				return str = xvaluecolname;
			}
			return str;
		}
		
		public void setxvaluecolname(String x) { 
			xvaluecolname = x;
			table_groups.getColumnModel().getColumn(1).setHeaderValue(x);
			//this.repaint();
		}
		
		public void clearxvalue(){ 
			for(int i=0;i<xvalue.size();i++)
				xvalue.set(i,"");
			fireTableDataChanged();
		}
		
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if(rowIndex>=getRowCount())
				return;
			switch(columnIndex){
			case 0:
				groupnames.set(rowIndex, (String) aValue);
				break;
			case 1:
				xvalue.set(rowIndex, (String) aValue);
				break;
			}
		}
		
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch(columnIndex){
			case 0:
				return groupnames.get(rowIndex);
			case 1:
				return xvalue.get(rowIndex);
			default:
				return null;
			}
		}
		
		public void setData(Vector<String> g,Vector<String> xv){ 
			int i,j;
			for(i=0;i<getRowCount();i++){
				for(j=0;j<getColumnCount();j++){
					setValueAt("",i,j);
				}
			}
			groupnames.clear();
			xvalue.clear();
			for(i=0;i<g.size();i++){
				groupnames.add(g.get(i));
				xvalue.add(xv.get(i));
			}
			fireTableDataChanged();
		}
		
		public void clear(){
			groupnames.clear();
			xvalue.clear();
			fireTableDataChanged();
		}
		
		public void setColumnEditable(int col,boolean e){
			iscolumneditable[col]=e;
			fireTableStructureChanged();
		}		
	}

	////////////////////////////////////////////////////////////////////////
	// Auxiliary methods
	////////////////////////////////////////////////////////////////////////
	
	public static boolean canstart(Configuration cfg){
		return (cfg.reporttype!=ReportType.vehicletypes & cfg.datafiles.size()>0) | (cfg.reporttype==ReportType.vehicletypes & cfg.datafiles.size()==1);
	}
	
	public void enabletoggglebuttons(){

		writeConfiguration();
			
		if(canstart(config)){	
			button_start.setEnabled(true);
			tabbedPane.setEnabledAt(2, true);
			tabbedPane.setEnabledAt(1, true);
			ReportType rtype = Utils.reportString2Type.get((String) cmb_reporttype.getSelectedItem());
		    if(rtype==ReportType.scatter){
				tabbedPane.setEnabledAt(3, true);
				tabbedPane.setEnabledAt(4, true);
		    }
		    else{
				tabbedPane.setEnabledAt(3, false);
				tabbedPane.setEnabledAt(4, false);
		    }
		}
		else{
			button_start.setEnabled(false);
			tabbedPane.setEnabledAt(1, false);
			tabbedPane.setEnabledAt(2, false);
			tabbedPane.setEnabledAt(3, false);
			tabbedPane.setEnabledAt(4, false);
		}
	}
	
	public void updatescenariosgroupstable(){
		int i,ind;
		String newname;
		Vector<ScenarioFiles> scenarios = new Vector<ScenarioFiles>();
		checkTreeManager.readTree(scenarios);
		Vector<String> oldnames = new Vector<String>();
		Vector<String> oldgroups = new Vector<String>();
		for(i=0;i<table_scenariosgroups.getRowCount();i++){
			oldnames.add( (String) table_scenariosgroups.getValueAt(i,0) );
			oldgroups.add( (String) table_scenariosgroups.getValueAt(i,1) );
		}
		Vector<String> newnames = new Vector<String>();
		Vector<String> newgroups = new Vector<String>();
		for(i=0;i<scenarios.size();i++){
		    newname = scenarios.get(i).scenarioName;
		    ind = oldnames.indexOf(newname);
		    newnames.add(newname);
		    if(ind<0)
		    	newgroups.add("");
		    else
		    	newgroups.add(oldgroups.get(ind));		    
		}
		((scenariosgroupsTableModel) table_scenariosgroups.getModel()).setData(newnames,newgroups);
	}
	
	public void updategroupstable(){
		
		int i,ind;
		String str, newname;

		// read all groups from scenariosgroupstable
		int numscenarios = table_scenariosgroups.getRowCount();
		TreeSet<String> newgroups = new TreeSet<String>();
		for(i=0;i<numscenarios;i++){
			str = (String) table_scenariosgroups.getValueAt(i,1);
			if(!str.isEmpty())
				newgroups.add(str);
		}
		
		// read old data from groupstable
		Vector<String> oldgroups = new Vector<String>();
		Vector<String> oldxvalue = new Vector<String>();
		for(i=0;i<table_groups.getRowCount();i++){
			oldgroups.add( (String) table_groups.getValueAt(i,0) );
			oldxvalue.add( (String) table_groups.getValueAt(i,1) );
		}

		// define new data matrix
		Vector<String> groups = new Vector<String>();
		Vector<String> xvalues = new Vector<String>();
	    Iterator<String> it =newgroups.iterator();
	    while(it.hasNext()){
	    	newname = (String) it.next();
	    	ind = oldgroups.indexOf(newname);
	    	groups.add(newname);
	    	if(ind<0){
	    		table_groups.setValueAt(newname, i, 0);
				xvalues.add("");
	    	}
	    	else{
	    		xvalues.add(oldxvalue.get(ind));
	    	}
	    }
		((groupsTableModel) table_groups.getModel()).setData(groups,xvalues);
	}
	
	private void loadconfigfile(File configfile){
		if (configfile.getName().endsWith("xml")) {
			String configURI = "file:" + configfile.getAbsolutePath();
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configURI);
				config.initFromDOM(doc.getChildNodes().item(0));
				if(!config.xmlValidate()){
					Utils.writeToConsole("Invalid configuration file");
					return;
				}
				readConfiguration();
				enabletoggglebuttons();
			}
			catch(Exception exc) {
				String buf = exc.getMessage();
				if ((buf == null) || (buf.equals("")))
					buf = "Unknown error...";
				JOptionPane.showMessageDialog(this, buf, exc.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public static void cleantempfolder(){
		File folder = new File(tempPath);
		File[] files = folder.listFiles();
		for (File file : files)
			file.delete();
		
	}
	
	public static void run(Configuration C){

		// check the configuration
		try {
			C.check();
		} catch (Exception e) {
			Utils.writeToConsole(e.getMessage());
			return;
		}
        
		// select an output file if not already specified
		if(Utils.outfilename==null){
			JFileChooser fc = new JFileChooser();
		    myFileFilter filter = new myFileFilter(C.exporttype);
		    fc.setFileFilter(filter);
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File fp = fc.getSelectedFile();
				if (fp.exists()) {
					int res = JOptionPane.showConfirmDialog(null, "File '" + fp.getName() + "' exists. Overwrite?", "Confirmation", JOptionPane.YES_NO_OPTION);
					if (res == JOptionPane.NO_OPTION)
						return;
				}
				Utils.outfilename = fp.getAbsolutePath();
			}
			else{
				Utils.writeToConsole("Run aborted.");
				return;
			}	
		}
		
		// create the export files
		File export_pdf = new File(Utils.outfilename + ".pdf");
		File export_ppt = new File(Utils.outfilename + ".ppt");
		File export_xls = new File(Utils.outfilename + ".xls");
		File reportfile = new File(Utils.outfilename + ".xml");

		// clean out old files
		gui_mainpanel.cleantempfolder();

		// generate the report
		ReportGenerator rg = new ReportGenerator(C);
		rg.setReportFile(reportfile);
		rg.run(C);
		
		// export
		(new Export_PDF()).export(reportfile, export_pdf);
		(new Export_PPT()).export(reportfile, export_ppt);
		(new Export_XLS()).export(reportfile, export_xls);

		return;
	}
	
	////////////////////////////////////////////////////////////////////////
	// Configuration
	////////////////////////////////////////////////////////////////////////

	private void writeConfiguration() {
		int i;
		String str;
		
		str = txt_congspeed.getText();
		try {
			config.congspeed = Float.parseFloat(str);
		} catch (Exception e) {
			config.congspeed = 55f;
		}

		str = txt_maxdatapoints.getText();
		try {
			config.maxpointspercurve = Integer.parseInt(str);
		} catch (Exception e) {
			config.maxpointspercurve = 200;
		}

		str = txt_fromtime.getText();
		try {
			config.timefrom = Float.parseFloat(str);
		} catch (Exception e) {
			config.timefrom = Float.NEGATIVE_INFINITY;
		}

		str = txt_totime.getText();
		try {
			config.timeto = Float.parseFloat(str);
		} catch (Exception e) {
			config.timeto = Float.POSITIVE_INFINITY;
		}

		config.exporttype = Utils.exportString2Type.get((String) cmb_export.getSelectedItem());
		config.reporttype = Utils.reportString2Type.get((String) cmb_reporttype.getSelectedItem());
		config.cbx_orperf_time = cb_onperf_time.isSelected();
		config.cbx_orperf_space = cb_onperf_space.isSelected(); 
		config.cbx_orperf_contour = cb_onperf_contour.isSelected();
		config.cbx_routeperf_time = cb_routeperf_time.isSelected();
		config.cbx_routeperf_space = cb_routeperf_space.isSelected();
		config.cbx_routeperf_contour = cb_routeperf_contour.isSelected(); 
		config.cbx_routetraveltime =  cb_routetraveltime_contour.isSelected();
		config.cbx_routetrajectories = cb_routetraveltime_time.isSelected();
		config.cbx_dolegend = cb_legend.isSelected();
		config.cbx_dofill = cb_fillplot.isSelected(); 
		config.cbx_linkstate = cb_linkstate_time.isSelected(); 
		config.cbx_sysperf = cb_sysperf_time.isSelected();
		config.txt_outputfile = txt_outputfile.getText();
		config.txt_customxaxis = txt_customxaxislabel.getText(); 
		config.cbx_boxplot = cb_boxplot.isSelected();
		config.colors.clear();
		for(i=0;i<colors.size();i++)	
			config.colors.add(colors.get(i));

		config.table_groups_group.clear();
		config.table_groups_xvalue.clear();
		for(i=0;i<table_groups.getRowCount();i++){
			config.table_groups_group.add( (String) table_groups.getValueAt(i,0) );
			config.table_groups_xvalue.add( (String) table_groups.getValueAt(i,1) );
		}
		
		config.table_scenariogroups_scenario.clear();
		config.table_scenariogroups_group.clear();
		for(i=0;i< table_scenariosgroups.getRowCount();i++){
			config.table_scenariogroups_scenario.add( (String) table_scenariosgroups.getValueAt(i,0) );
			config.table_scenariogroups_group.add( (String) table_scenariosgroups.getValueAt(i,1) );
		}

		config.cmb_xaxis_subnetwork_selected = cmb_xaxis_subnetwork.getSelectedIndex();
		config.cmb_xaxis_subnetwork.clear();
		for(i=0;i<cmb_xaxis_subnetwork.getItemCount();i++)
			config.cmb_xaxis_subnetwork.add((String)cmb_xaxis_subnetwork.getItemAt(i));
		
		config.cmb_xaxis_quantity_selected = cmb_xaxis_quantity.getSelectedIndex();
		config.cmb_xaxis_quantity.clear();
		for(i=0;i<cmb_xaxis_quantity.getItemCount();i++)
			config.cmb_xaxis_quantity.add((String)cmb_xaxis_quantity.getItemAt(i));
		
		config.cmb_yaxis_subnetwork_selected = cmb_yaxis_subnetwork.getSelectedIndex();
		config.cmb_yaxis_subnetwork.clear();
		for(i=0;i<cmb_yaxis_subnetwork.getItemCount();i++)
			config.cmb_yaxis_subnetwork.add((String)cmb_yaxis_subnetwork.getItemAt(i));
		
		config.cmb_yaxis_quantity_selected = cmb_yaxis_quantity.getSelectedIndex();
		config.cmb_yaxis_quantity.clear();
		for(i=0;i<cmb_yaxis_quantity.getItemCount();i++)
			config.cmb_yaxis_quantity.add((String)cmb_yaxis_quantity.getItemAt(i));

		TreePath[] selectionPaths = checkTreeManager.getSelectionModel().getSelectionPaths();
		config.chk_tree.clear();
		if(selectionPaths!=null)
			for(i=0;i<selectionPaths.length;i++)
				config.chk_tree.add(selectionPaths[i].getLastPathComponent().toString());
		
		checkTreeManager.readTree(config.scenarios,config.datafiles);

	}

	private void readConfiguration() {

		int i;
		wipeclean();
				
		txt_congspeed.setText( String.format("%.1f",config.congspeed));
		txt_maxdatapoints.setText( String.format("%d",config.maxpointspercurve));
		cb_onperf_time.setSelected(config.cbx_orperf_time);
		cb_onperf_space.setSelected(config.cbx_orperf_space);
		cb_onperf_contour.setSelected(config.cbx_orperf_contour);
		cb_routeperf_time.setSelected(config.cbx_routeperf_time);
		cb_routeperf_space.setSelected(config.cbx_routeperf_space);
		cb_routeperf_contour.setSelected(config.cbx_routeperf_contour);
		cb_routetraveltime_contour.setSelected(config.cbx_routetraveltime);
		cb_routetraveltime_time.setSelected(config.cbx_routetrajectories);
		cb_legend.setSelected(config.cbx_dolegend);
		cb_fillplot.setSelected(config.cbx_dofill);
		cb_linkstate_time.setSelected(config.cbx_linkstate);
		cb_sysperf_time.setSelected(config.cbx_sysperf);
		txt_outputfile.setText(config.txt_outputfile);
		txt_fromtime.setText(String.format("%.1f",config.timefrom));
		txt_totime.setText(String.format("%.1f",config.timeto));
		txt_customxaxislabel.setText(config.txt_customxaxis);
		cb_boxplot.setSelected(config.cbx_boxplot);
		
		colors.clear();
		for(i=0;i<config.colors.size();i++)
			colors.add(config.colors.get(i));
		
		((scenariosgroupsTableModel) table_scenariosgroups.getModel()).setData(config.table_scenariogroups_scenario,config.table_scenariogroups_group);
		((groupsTableModel) table_groups.getModel()).setData(config.table_groups_group,config.table_groups_xvalue);	

		
		for(i=0;i<config.cmb_xaxis_subnetwork.size();i++)
			cmb_xaxis_subnetwork.addItem(config.cmb_xaxis_subnetwork.get(i));
		if(config.cmb_xaxis_subnetwork_selected>=0)
			cmb_xaxis_subnetwork.setSelectedIndex(config.cmb_xaxis_subnetwork_selected);
			
		for(i=0;i<config.cmb_xaxis_quantity.size();i++)
			cmb_xaxis_quantity.addItem(config.cmb_xaxis_quantity.get(i));
		if(config.cmb_xaxis_quantity_selected>=0)
			cmb_xaxis_quantity.setSelectedIndex(config.cmb_xaxis_quantity_selected);

		for(i=0;i<config.cmb_yaxis_subnetwork.size();i++)
			cmb_yaxis_subnetwork.addItem(config.cmb_yaxis_subnetwork.get(i));
		if(config.cmb_yaxis_subnetwork_selected>=0)
			cmb_yaxis_subnetwork.setSelectedIndex(config.cmb_yaxis_subnetwork_selected);
			
		for(i=0;i<config.cmb_yaxis_quantity.size();i++)
			cmb_yaxis_quantity.addItem(config.cmb_yaxis_quantity.get(i));
		if(config.cmb_yaxis_quantity_selected>=0)
			cmb_yaxis_quantity.setSelectedIndex(config.cmb_yaxis_quantity_selected);

		TreePath [] treepaths = new TreePath[config.chk_tree.size()];
		for(i=0;i<config.chk_tree.size();i++){
			DefaultMutableTreeNode n = checkTreeManager.getDecendantByName(null,config.chk_tree.get(i));
			if(n!=null){
				TreeNode[] z = ((DefaultTreeModel) scenariostree.getModel()).getPathToRoot(n);
				treepaths[i] = new TreePath(z);
			}
			else
				treepaths[i] = null;
		}
		checkTreeManager.selectionModel.setSelectionPaths(treepaths);
		
		cmb_export.setSelectedItem( Utils.exportType2String.get(config.exporttype) );
		cmb_reporttype.setSelectedItem( Utils.reportType2String.get(config.reporttype) );

	}

	private void wipeclean(){

		// clear ui
		checkTreeManager.selectionModel.clearSelection();
		
		txt_outputfile.setText("");
		txt_fromtime.setText("");
		txt_totime.setText("");
		cb_linkstate_time.setSelected(false);
		cb_sysperf_time.setSelected(false);
		cb_onperf_time.setSelected(false);
		cb_onperf_space.setSelected(false);
		cb_onperf_contour.setSelected(false);
		cb_routeperf_time.setSelected(false);
		cb_routeperf_space.setSelected(false);
		cb_routeperf_contour.setSelected(false);
		cb_routetraveltime_time.setSelected(false);
		cb_routetraveltime_contour.setSelected(false);
		txt_congspeed.setText("");
		txt_maxdatapoints.setText("");
		cb_legend.setSelected(false);
		cb_fillplot.setSelected(false);
		colors.clear();
		((scenariosgroupsTableModel) table_scenariosgroups.getModel()).clear();
		((groupsTableModel) table_groups.getModel()).clear();
		txt_customxaxislabel.setText("");
		cb_boxplot.setSelected(false);
		cmb_xaxis_subnetwork.removeAllItems();
		cmb_xaxis_quantity.removeAllItems();
		cmb_yaxis_subnetwork.removeAllItems();
		cmb_yaxis_quantity.removeAllItems();
		
	}

	////////////////////////////////////////////////////////////////////////
	// local classes
	////////////////////////////////////////////////////////////////////////

	private class GPanel extends JPanel {
		private static final long serialVersionUID = -7861857220993680328L;
		public JPanel mypanel;
		public GPanel(JPanel p){ mypanel = p; }
		public void setEnabled(boolean arg0) {
			super.setEnabled(arg0);
			Component [] z = getComponents();
			for(int i=0;i<z.length;i++){
				z[i].setEnabled(arg0);
			}
		}
	}

	public class JTextAreaOutputStream extends OutputStream {
		private JTextArea ta;
		
		public JTextAreaOutputStream(JTextArea t) {
			super();
			ta = t;
		}
		
		public void write(int i) {
			ta.append(Character.toString((char)i));
		}
		
		public void write(char[] buf, int off, int len) {
			String s = new String(buf, off, len);
			ta.append(s);
		}
  }	

	public static class myFileFilter extends FileFilter {
		private Utils.ExporterType exportertype;

		public myFileFilter(Utils.ExporterType d){ exportertype = d; }
		
		@Override
		public boolean accept(File f) {
			String filename = f.getName();
			int dotPlace = f.getName().lastIndexOf ('.');
			if(dotPlace<0)
				return true;
			String ext = filename.substring( dotPlace + 1 );
			return ext.equalsIgnoreCase(exportertype.toString());
		}

		@Override
		public String getDescription() {
			return Utils.exportType2String.get(exportertype);
		}
		
	}
}
