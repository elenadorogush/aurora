/**
 * @(#)gui_CheckTreeCellRenderer.java
 */

package aurora.hwc.report;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.*;
import javax.swing.tree.*;


public class gui_CheckTreeCellRenderer extends JPanel implements TreeCellRenderer{ 
	private static final long serialVersionUID = -3064324754439391291L;
	
	private gui_CheckTreeSelectionModel selectionModel; 
    private TreeCellRenderer delegate; 
    private gui_TristateCheckBox checkBox = new gui_TristateCheckBox(); 
 
    public gui_CheckTreeCellRenderer(TreeCellRenderer delegate, gui_CheckTreeSelectionModel selectionModel){ 
        this.delegate = delegate; 
        this.selectionModel = selectionModel; 
        setLayout(new BorderLayout()); 
        setOpaque(false); 
        checkBox.setOpaque(false); 
    } 
 
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus){ 
        Component renderer = delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus); 
 
        TreePath path = tree.getPathForRow(row); 
        if(path!=null){ 
            if(selectionModel.isPathSelected(path, true)) 
                checkBox.setState(gui_TristateCheckBox.SELECTED); //checkBox.setState(Boolean.TRUE); 
            else {
            	if(selectionModel.isPartiallySelected(path))
            		checkBox.setState(gui_TristateCheckBox.DONT_CARE);
            	else
            		checkBox.setState(gui_TristateCheckBox.NOT_SELECTED);
                //checkBox.setState(selectionModel.isPartiallySelected(path) ? null : Boolean.FALSE); 
            }
        } 
        removeAll(); 
        add(checkBox, BorderLayout.WEST); 
        add(renderer, BorderLayout.CENTER); 
        return this; 
    } 
} 
