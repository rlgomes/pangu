package org.pangu.tree;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.pangu.PanguException;
import org.pangu.tree.decorators.PanguDecorator;

/**
 * 
 * @author rlgomes
 */
public class AttributeNode extends PanguNode {

    private String name = null;
    private String use = null;
    private DataNode data = null;
    
    public AttributeNode(String name,
                         DataNode data,
                         String use,
                         int min,
                         int max)
            throws PanguException {
        super(min, max);
        this.name = name;
        this.use = use;
        this.data = data;
    }
    
    @Override
    public PanguNode duplicate() throws PanguException {
        AttributeNode node = new AttributeNode(name,
                                               data,
                                               use,
                                               getMinOccurs(),
                                               getMaxOccurs());
        return node;
    }
    
    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        PanguDecorator decorator = info.getDecorator();
        byte[] A1 = decorator.beforeAttributeName();
        byte[] A2 = decorator.afterAttributeName();
        byte[] A3 = decorator.beforeAttributeValue();
        byte[] A4 = decorator.afterAttributeValue();
        byte[] N = name.getBytes();
        int length = A1.length + N.length + A2.length + A3.length;
        ByteBuffer buff = ByteBuffer.allocate(length);
        
        buff.put(A1);
        buff.put(N);
        buff.put(A2);
        buff.put(A3);
        print(os,buff.array());
        info.takeFromBytesLeft(length + A4.length);
        
        data.generate(info,os);
        print(os,A4);
    }
    
    public String getUse() { return use; }
    public void setUse(String use) { this.use = use; }

    @Override
    public String toString(ArrayList<PanguNode> visited) {
        return name + ": " + data.toString();
    }
}
