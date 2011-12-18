/**
 * @(#)gui_mainframe.java
 */

package aurora.hwc.report;

import java.io.File;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;


/**
 * Main window for the Aurora Report Generator.
 * @author Gabriel Gomes
 */
public class gui_mainframe extends JFrame {
	private static final long serialVersionUID = 8742484261790264586L;

	public gui_mainframe(File inputfile){
        setContentPane(new gui_mainpanel(inputfile));
        pack();
		setTitle("Aurora Report Generator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setLocationRelativeTo(null);		
	}
	
	////////////////////////////////////////////////////////////////////////
	// main
	// java -jar gui_mainframe.jar [-view <[on]|off>] [-config <filename>] [-verbose <[on],off>] [-saveto <filename>] [-tempdir <folder>] [-rootdir <folder>]
	////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {	
		
		Configuration.rgguilaunched = true;
		File inputfile = null;
		
		// check usage
		if(args.length%2!=0 | args.length>12){
			System.out.println("Usage: java -jar arg.jar [-view <[on]|off>] [-config <filename>] [-verbose <[on],off>] [-saveto <filename>] [-tempdir <folder>] [-rootdir <folder>]");
			System.out.println("	-view		Whether to show the GUI.");
			System.out.println("	-config		report generator configuration file.");
			System.out.println("	-verbose	Turn on verbose execution mode.");
			System.out.println("	-saveto		Name of the report.");
			System.out.println("	-tempdir	Folder used for storing intermediate files. Default is <user.home>/ARG/tempfiles");
			System.out.println("	-rootdir	Output folder. Default is <user.home>/ARG/files");
			return;
		}

		// parse command
		for(int i=0;i<args.length/2;i++){
			if( args[2*i].equalsIgnoreCase("-view") )
				Configuration.rgguilaunched = args[2*i+1].equalsIgnoreCase("on");
			if( args[2*i].equals("-config") )
				inputfile = new File(args[2*i+1]);
			if( args[2*i].equals("-verbose") )
				Utils.verbose = args[2*i+1].equalsIgnoreCase("on");
			if( args[2*i].equals("-saveto") )
				Utils.outfilename = args[2*i+1];
			if( args[2*i].equals("-tempdir") )
				Configuration.setTempDir(args[2*i+1]);
			if( args[2*i].equals("-rootdir") )
				Configuration.setRootDir(args[2*i+1]);
		}

		if(Configuration.rgguilaunched){
		    JFrame.setDefaultLookAndFeelDecorated(true);
			gui_mainframe window = new gui_mainframe(inputfile);
			window.setVisible(true);
			window.setLocationRelativeTo(null);
		}
		else{

			if(inputfile==null){
				System.out.println("A config file is required to run the command line mode.");
				return;
			}

			if(Utils.outfilename==null){
				System.out.println("An output file is required to run the command line mode.");
				return;
			}
			
			try {
				// load and validate the configuration file
				Configuration config = new Configuration();
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputfile);
				config.initFromDOM(doc.getChildNodes().item(0));

				if(!config.xmlValidate()){
					config.writeToConsole("Invalid configuration file");
					return;
				}

				// if information is complete, start the run
				if( gui_mainpanel.canstart(config))
					gui_mainpanel.run(config);

			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		
	}
}
