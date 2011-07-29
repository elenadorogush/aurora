/**
 * @(#)gui_CheckTreeManager.java
 */

package aurora.hwc.report;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;


public class gui_CheckTreeManager extends MouseAdapter implements TreeSelectionListener{ 
    public gui_CheckTreeSelectionModel selectionModel; 
    private JTree tree = new JTree(); 
    int hotspot = new JCheckBox().getPreferredSize().width; 
    JPanel mypanel;
    
    public gui_CheckTreeManager(JTree tree,JPanel p){
    	this.mypanel = p;
        this.tree = tree; 
        selectionModel = new gui_CheckTreeSelectionModel(tree.getModel()); 
        tree.setCellRenderer(new gui_CheckTreeCellRenderer(tree.getCellRenderer(), selectionModel)); 
        tree.addMouseListener(this); 
        selectionModel.addTreeSelectionListener(this); 
    } 
 
    public void mouseClicked(MouseEvent me){
        TreePath path = tree.getPathForLocation(me.getX(), me.getY()); 
        if(path==null) 
            return; 
        if(me.getX()>tree.getPathBounds(path).x+hotspot) 
            return; 
 
        boolean selected = selectionModel.isPathSelected(path, true); 
        selectionModel.removeTreeSelectionListener(this);   
        try{ 
            if(selected) 
                selectionModel.removeSelectionPath(path); 
            else 
                selectionModel.addSelectionPath(path); 
        } finally{ 
            selectionModel.addTreeSelectionListener(this); 
            tree.treeDidChange(); 
        } 
        
        ((gui_mainpanel) mypanel).enabletoggglebuttons();
        ((gui_mainpanel) mypanel).updatescenariosgroupstable();
        ((gui_mainpanel) mypanel).updategroupstable();
        
    } 
 
    public gui_CheckTreeSelectionModel getSelectionModel(){ 
        return selectionModel; 
    } 
 
    public void valueChanged(TreeSelectionEvent e){ 
        tree.treeDidChange(); 
    } 
    
    public int readTree(Vector<ScenarioFiles> scenarios){
    	int i,j,k,ind;
    	DefaultMutableTreeNode node;
    	String scenarioname;
    	String filename;
    	int numfiles = 0;
    	scenarios.clear();
    	
    	TreePath checkedPaths[] = selectionModel.getSelectionPaths(); 
    	if(checkedPaths==null)
    		return 0;
    	
    	// step through checked paths, populate scenarios
    	for(i=0;i<checkedPaths.length;i++){
    		switch(checkedPaths[i].getPathCount()){
    		case 1:		// "Scenario" checked
    			node = (DefaultMutableTreeNode) checkedPaths[i].getLastPathComponent();
    			for(j=0;j<node.getChildCount();j++){
    				scenarioname = node.getChildAt(j).toString();
        			scenarios.add(new ScenarioFiles(scenarioname));
    				for(k=0;k<node.getChildAt(j).getChildCount();k++){
        				filename = node.getChildAt(j).getChildAt(k).toString();
            			filename = "file:" + Configuration.getFilesDir() + "\\" + scenarioname + "\\" + filename;
        				scenarios.lastElement().datafiles.add(filename);
        				numfiles++;
    				}
    			}
    			break;
    		case 2:		// case scenario checked
    			node = (DefaultMutableTreeNode) checkedPaths[i].getLastPathComponent();
    			scenarioname = checkedPaths[i].getLastPathComponent().toString();
    			scenarios.add(new ScenarioFiles(scenarioname));
    			for(j=0;j<node.getChildCount();j++){
    				filename = node.getChildAt(j).toString();
        			filename = "file:" + Configuration.getFilesDir() + "\\" + scenarioname + "\\" + filename;
    				scenarios.lastElement().datafiles.add(filename);
    				numfiles++;
    			}
    			break;
    		case 3:		// case file checked
    			scenarioname = checkedPaths[i].getPathComponent(1).toString();
    			filename = checkedPaths[i].getLastPathComponent().toString();
    			filename = "file:" + Configuration.getFilesDir() + "\\" + scenarioname + "\\" + filename;
    			
    			ind = -1;
    			for(j=0;j<scenarios.size();j++)
    				if(scenarios.get(j).scenarioName.equals(scenarioname)){
    					ind = j;
    					break;
    				}
    			if(ind<0)
    				scenarios.add(new ScenarioFiles(scenarioname,filename));
    			else
    				scenarios.get(ind).datafiles.add(filename);
    			numfiles++;
    			break;
    		default:
    			break;
    		}
    	}
    	return numfiles;
    }

    public int readTree(Vector<String> scenarios,Vector<String> datafiles){
    	Vector<ScenarioFiles> scenariofiles = new Vector<ScenarioFiles>();
    	int numfiles = readTree(scenariofiles);
    	int i,j;
    	scenarios.clear();
    	datafiles.clear();
    	for(i=0;i<scenariofiles.size();i++){
    		for(j=0;j<scenariofiles.get(i).datafiles.size();j++){
    			scenarios.add(scenariofiles.get(i).scenarioName);
    			datafiles.add(scenariofiles.get(i).datafiles.get(j));
    		}
    	}
    	return numfiles;
    }
    
    public DefaultMutableTreeNode getDecendantByName(DefaultMutableTreeNode node,String name){
    	if(node==null)
    		node = (DefaultMutableTreeNode) tree.getModel().getRoot();
    	if(node.toString().equals(name))
    		return node;
        int cc = node.getChildCount();
        for( int i=0; i < cc; i++) {
        	DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
        	if(child.toString().equals(name))
        		return child;
        	if(!child.isLeaf()){
        		DefaultMutableTreeNode z = getDecendantByName(child,name);
        		if(z!=null)
        			return z;
        	}
        }
        return null;
    }
}