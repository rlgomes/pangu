package org.pangu.tree;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

import org.pangu.PanguException;
import org.pangu.tree.decorators.PanguDecorator;

public class ChoiceNode extends PanguNode {

    public ChoiceNode(int min, int max) throws PanguException {
        super(min,max);
    }
    
    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        PanguDecorator decorator = info.getDecorator();
        ArrayList<PanguNode> children = getChildren();
        Random rand = info.getRandom();
        
        int count = 0;
        int min = getMinOccurs();
        int max = getMaxOccurs();
       
        byte[] BE = decorator.betweenChoiceElements();
        byte[] BC = decorator.beforeChoice();
        byte[] AC = decorator.afterChoice();
        
        info.takeFromBytesLeft(BC.length + AC.length);
        
        long childGoal = calcChildGoal(info);
        
        print(os, BC);
        GenInfo aux = info.duplicate();
        int childrenLength = children.size();
        
        while ( info.getBytesLeft() > 0 || count < min ) {
            int child = rand.nextInt(childrenLength);
            
            PanguNode pn = children.get(child);
            if ( count != 0 ) {
                print(os,BE);
                info.takeFromBytesLeft(BE.length);
            }

            int bytes = (int) (info.getBytesLeft() / childGoal);
            aux.setBytesLeft(bytes);
            
            pn.generate(aux, os);
            info.takeFromBytesLeft(bytes-aux.getBytesLeft());
            count++;
            
            if ( count >= max )
                break;
        }
        print(os, AC);
    }
    
    @Override
    public PanguNode duplicate() throws PanguException {
        ChoiceNode node = new ChoiceNode(getMinOccurs(), getMaxOccurs());
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
                result.append(pn.toString(visited) + " | ");
            }
            return result.substring(0,result.length()-1) + getOccurenceString();
        }
        return "";
    }
}
