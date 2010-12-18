package org.pangu.tree;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.pangu.PanguException;
import org.pangu.tree.decorators.PanguDecorator;

public class ElementNode extends PanguNode {

    private String _name = null;
    
    public ArrayList<AttributeNode> attributes = null;
    
    public ElementNode(String name, int min, int max) throws PanguException {
        super(min,max);
        _name = name;
        attributes = new ArrayList< AttributeNode>();
    }
    
    public void addAttribute(AttributeNode node) { 
        attributes.add(node);
    }
    
    public String getName() { return _name; }
    
    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        PanguDecorator decorator = info.getDecorator();
        
        byte[] E1 = decorator.beforeStartElementName();
        byte[] EA = decorator.startElement(_name);
        byte[] E2 = decorator.afterStartElementName();

        byte[] E3 = decorator.beforeStopElementName();
        byte[] EB = decorator.stopElement(_name);
        byte[] E4 = decorator.afterStopElementName();
       
        Random rand = info.getRandom();
        int spent = E1.length + EA.length + E2.length + E3.length + 
                    EB.length + E4.length;
        info.takeFromBytesLeft(spent);
        byte[] AS = decorator.betweenAttributes(); 
        
        print(os, E1);
        print(os, EA);
        if ( attributes.size() != 0 ) { 
	        for (AttributeNode an : attributes) { 
	            int bytes = (int) (info.getBytesLeft()*0.1f);
	            GenInfo aux = info.duplicate();
	            aux.setBytesLeft(bytes);
	            
		        print(os, AS);
	            an.generate(aux, os);
	            info.takeFromBytesLeft(bytes-aux.getBytesLeft());
	        }
        }

        ArrayList<PanguNode> children = getChildren();
        HashMap<PanguNode, AtomicInteger> counters = 
                                        new HashMap<PanguNode, AtomicInteger>();
        
        for (PanguNode pn : children) 
            counters.put(pn,new AtomicInteger(0));
       
        boolean minReached = false;
        boolean notmaxedout = true;
      
        byte[] BEV = decorator.beforeElementValue();
        byte[] AEV = decorator.afterElementValue();
        spent = BEV.length + AEV.length;
        
        print(os, E2);
        while ( !minReached || (info.getBytesLeft() > 0 && notmaxedout) ) {
            notmaxedout = false;
	        for (PanguNode pn : children) { 
	            if ( counters.get(pn).intValue() >= pn.getMaxOccurs() )
	                continue;

	            notmaxedout = true;

	            if ( pn.getMinOccurs() == 0 && rand.nextBoolean() ) 
	                continue;
	          
	            if ( pn instanceof DataNode ) { 
	                info.takeFromBytesLeft(spent);
	                print(os,BEV);
	                pn.generate(info, os);
	                print(os,AEV);
	            } else { 
	                pn.generate(info, os);
	            }
	            counters.get(pn).incrementAndGet();
	        }
	        
	        minReached = true;
            for (PanguNode pn : children) { 
                int value = counters.get(pn).intValue();
                if ( value < pn.getMinOccurs() ) {
                    minReached = false;
                }
            }
        }
        print(os, E3);
        print(os, EB);
        print(os, E4);
    }
    
    @Override
    public PanguNode duplicate() throws PanguException {
        ElementNode node = new ElementNode(_name, getMinOccurs(), getMaxOccurs());
        node.attributes = attributes;
        node.children = getChildren();
        return node;
    }
    
    @Override
    public String toString(ArrayList<PanguNode> visited) {
        StringBuffer result = new StringBuffer(getName());
       
        if ( visited.contains(this) )
            return getName();
        
        visited.add(this);
        
        if ( attributes.size() != 0 ) { 
            result.append(" < ");
	        for (AttributeNode an: attributes) { 
	            result.append(an.toString(visited));
	        }
	        result.append(" >");
        }
        
        ArrayList<PanguNode> children = getChildren();
        if ( children.size() != 0 ) { 
            result.append(" [ ");
	        for (PanguNode pn: children) { 
	            result.append(pn.toString(visited));
	        }
	        result.append(" ]");
        }
        return result.toString() + getOccurenceString();
    }
}
