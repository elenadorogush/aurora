/**
 * @(#)gui_color.java
 */

package aurora.hwc.report;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;


/**
 * Color selection dialog
 * @author Gabriel Gomes
 */
public class gui_color extends JDialog implements ActionListener {
	private static final long serialVersionUID = -1509304199667359385L;
	private Vector<JButton> burronarray = new Vector<JButton>();
	private JButton buttonok = new JButton("Ok");
	private JButton buttoncancel = new JButton("Cancel");
	private boolean returnok;
	private Vector<String> incolors = new Vector<String>();

	public gui_color(){
		
		for(int i=0;i<10;i++){
			incolors.add("#FFFFFF");
		}
		makeit();
	}
	
	public gui_color(Vector<String> colors){
		incolors = colors;
        makeit();
	}
	
	private void makeit(){

		JPanel buttonPanel = new JPanel();
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		Vector<Vector<Float>> rgbcolors = new Vector<Vector<Float>>();
		Utils.hex2rgb(incolors,rgbcolors);
		for(int i=0;i<10;i++){
			JButton b = new JButton();
			b.setPreferredSize(new Dimension(30,30));
			b.setBackground(new Color(rgbcolors.get(i).get(0),rgbcolors.get(i).get(1),rgbcolors.get(i).get(2)));
			b.setActionCommand("colorbutton");
			b.addActionListener(this);
			burronarray.add(b);
			buttonPanel.add(b);
		}
		content.add(buttonPanel);
			
		JPanel okcancelPanel = new JPanel();
		
		buttonok.setActionCommand("okbutton");
		buttonok.addActionListener(this);
		okcancelPanel.add(buttonok);
		buttoncancel.setActionCommand("cancelbutton");
		buttoncancel.addActionListener(this);
		okcancelPanel.add(buttoncancel);
        content.add(okcancelPanel);
		
        setContentPane(content);	
        pack();
		setTitle("Color selection");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);
        setModal(true);
	}
		
	public static void main(String[] args) {
		JDialog.setDefaultLookAndFeelDecorated(true);
		gui_color window = new gui_color();
		window.setVisible(true);
	}

	public void showDialog(Vector<String> outcolors) {
	    setVisible(true);
	    
	    if(returnok){
		    Vector<Vector<Float>> rgb = new Vector<Vector<Float>>();
		    for(int i=0;i<burronarray.size();i++){
		    	Vector<Float> z = new Vector<Float>();
		    	Color c = burronarray.get(i).getBackground();
		    	z.add(((float) c.getRed())/255f);
		    	z.add(((float) c.getGreen())/255f);
		    	z.add(((float) c.getBlue())/255f);
		    	rgb.add(z);
		    }
	    	Utils.rgb2hex(rgb,outcolors);
	    }	    	   
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		String command = arg0.getActionCommand();
		
		if(command.equals("colorbutton")){
	        Color newcolor = JColorChooser.showDialog(((Component) arg0.getSource()).getParent(), "Choose a color", Color.blue);
	        if(newcolor!=null)
	        	((JButton) arg0.getSource()).setBackground(newcolor);
		}

		if(command.equals("okbutton")){
			returnok = true;
			setVisible(false);
			dispose();
		}

		if(command.equals("cancelbutton")){
			returnok = false;
			setVisible(false);
			dispose();	
		}
	}
}

