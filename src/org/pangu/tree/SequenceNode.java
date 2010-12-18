package org.pangu.tree;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.pangu.PanguException;
import org.pangu.tree.decorators.PanguDecorator;

public class SequenceNode extends PanguNode {

    public SequenceNode(int min, int max) throws PanguException {
        super(min, max);
    }
    
    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        PanguDecorator decorator = info.getDecorator();
        ArrayList<PanguNode> children = getChildren();
        Random rand = info.getRandom();
        int max = getMaxOccurs();
        int count = 0;
        
        HashMap<PanguNode, AtomicInteger> counters = 
                                        new HashMap<PanguNode, AtomicInteger>();
        
        for (PanguNode pn : children) 
            counters.put(pn,new AtomicInteger(0));
        
        byte[] BE = decorator.betweenSequenceElements();
        byte[] BS = decorator.beforeSequence();
        byte[] AS = decorator.afterSequence();
        info.takeFromBytesLeft(BS.length + AS.length);

        boolean minReached = false;
        long childGoal = calcChildGoal(info);
       
        print(os, BS);
        GenInfo aux = info.duplicate();
        while ( !minReached || info.getBytesLeft() > 0 ) {
	        for (PanguNode pn : children) { 
	            if ( pn.getMinOccurs() == 0 && rand.nextBoolean() ) 
	                continue;
	            
	            if ( counters.get(pn).intValue() < pn.getMaxOccurs() || 
	                 (max > 1 && count < max) ) {
	                
	                if ( count != 0 ) {
	                    print(os,BE);
	                    info.takeFromBytesLeft(BE.length);
	                }

	                int bytes = (int) (info.getBytesLeft() / childGoal);
	                aux.setBytesLeft(bytes);
	                
	                pn.generate(aux, os);
	                info.takeFromBytesLeft(bytes-aux.getBytesLeft());
	                counters.get(pn).incrementAndGet();
	                count++;
	            }
	            childGoal--;
	        }
	       
            minReached = true;
	        for (PanguNode pn : children) { 
	            int value = counters.get(pn).intValue();
	            if ( value < pn.getMinOccurs() ) {
	                minReached = false;
	            }
	        }

	        if ( count >= max ) 
	            break;
        }
        print(os, AS);
    }
    
    @Override
    public PanguNode duplicate() throws PanguException {
        SequenceNode node = new SequenceNode(getMinOccurs(), getMaxOccurs());
        node.children = getChildren();
        return node;
    }
    
    @Override
    public String toString(ArrayList<PanguNode> visited) {
        ArrayList<PanguNode> children = getChildren();
        
        if ( visited.contains(this) )
            return "";
        
        visited.add(this);
        
        if ( children.size() != 0 ) { 
            StringBuffer result = new StringBuffer();
	        for (PanguNode pn: children) { 
	            
	            result.append(pn.toString(visited) + ",");
	        }
	        return result.substring(0,result.length()-1) + getOccurenceString();
        }
        return "";
    }
}
