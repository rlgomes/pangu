package org.pangu.tree;

import java.io.OutputStream;
import java.util.ArrayList;

import org.pangu.PanguException;

public class RootNode extends PanguNode {

    public RootNode() throws PanguException { 
        super(1,1);
    }
    
    public RootNode(int min, int max) throws PanguException {
        super(min,max);
    }
    
    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        String rootname = info.getDesiredRoot();
        if ( rootname != null ) { 
            ArrayList<PanguNode> children = getChildren();
            ElementNode node = null;
            for (PanguNode pn : children) { 
                if ( pn instanceof ElementNode ) { 
                    if ( ((ElementNode)pn).getName().equals(rootname) ) {
                        node = (ElementNode)pn;
                        break;
                    }
                }
            }
            
            if ( node != null ) { 
                node.generate(info, os);
            } else { 
                throw new PanguException("Unable to find node [" + rootname + "]");
            }
        } else {
            int which = info.getRandom().nextInt(getChildren().size());
	        PanguNode node = getChildren().get(which);
	        node.generate(info,os);
        }
    }
    
    @Override
    public PanguNode duplicate() throws PanguException {
        RootNode node = new RootNode();
        node.children = getChildren();
        return node;
    }
    
    @Override
    public String toString(ArrayList<PanguNode> visited) {
        StringBuffer result = new StringBuffer();

        if ( visited.contains(this) )
            return "";
        
        visited.add(this);
        
        ArrayList<PanguNode> children = getChildren();
        for (PanguNode pn: children) { 
            result.append(pn.toString(visited) + "\n");
        }
        return result.toString();
    }
}
