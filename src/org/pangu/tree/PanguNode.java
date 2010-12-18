package org.pangu.tree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

import org.pangu.PanguException;

public abstract class PanguNode {
    
    private int minoccurs = 0;
    private int maxoccurs = 1;
    
    protected ArrayList<PanguNode> children = null;
    
    private PanguNode parent = null;
    
    public PanguNode(int minoccurs, int maxoccurs) throws PanguException { 
        this.minoccurs = minoccurs;
        this.maxoccurs = maxoccurs;
        
        children = new ArrayList<PanguNode>();
    }
    
    public void addNode(PanguNode node) { 
        children.add(node);
        node.setParent(this);
    }
    
    protected PanguNode getParent() { 
        return parent;
    }
    protected void setParent(PanguNode pn) { 
        parent = pn;
    }
    
    public void setMinOccurs(int minoccurs) { this.minoccurs = minoccurs; }
    public int getMinOccurs() { return minoccurs; }

    public void setMaxOccurs(int maxoccurs) { this.maxoccurs = maxoccurs; }
    public int getMaxOccurs() { return maxoccurs; }
    
    public ArrayList<PanguNode> getChildren() { return children; }
    
    protected String getOccurenceString() { 
        if ( getMaxOccurs() == Integer.MAX_VALUE ) { 
            return "{" + getMinOccurs() + ",*}";
        } else { 
            return "{" + getMinOccurs() + "," + getMaxOccurs() + "}";
        }
    }

    /**
     * 
     * @param info
     * @return
     * @throws PanguException
     */
    public String generate(GenInfo info) throws PanguException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        generate(info,baos);
        return baos.toString();
    }
   
    /**
     * 
     * @param info
     * @param os
     * @throws PanguException
     */
    public abstract void generate(GenInfo info, OutputStream os)
           throws PanguException;
    
    @Override
    public String toString() {
        return toString(new ArrayList<PanguNode>());
    }
   
    public abstract PanguNode duplicate() throws PanguException;
    
    public abstract String toString(ArrayList<PanguNode> visited);
   
    /**
     * Given the current information from info and the number of children tags
     * this tag has what is the "right" amount of children to try and generate.
     * Of course if the children.size() is 1 and the max is set to the the 
     * maximum integer then we'll try to pick a random number between 2 and 10.
     * Otherwise, we'll just go with the number of children we currently have
     * below us.
     * 
     * @param info
     * @return
     */
    protected long calcChildGoal(GenInfo info) { 
        Random rand = info.getRandom();
        int max = getMaxOccurs();
        long childGoal = max;
        if ( max == Integer.MAX_VALUE ) { 
            if ( childGoal > info.getBytesLeft()/8 ) {
                childGoal = info.getBytesLeft()/128;
                if ( childGoal < 8 ) 
                    childGoal = 8;
            }
            childGoal = 2 + Math.abs(rand.nextInt((int)childGoal));
        } else { 
            childGoal = children.size();
        }   
        return childGoal;
    }
    
    protected void print(OutputStream os, byte[] data) throws PanguException { 
        try {
            os.write(data);
        } catch (IOException e) {
            throw new PanguException("Unable to write to output stream.",e);
        }
    }
}