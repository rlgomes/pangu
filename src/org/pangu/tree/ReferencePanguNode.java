//package org.pangu.tree;
//
//import java.io.OutputStream;
//import java.util.ArrayList;
//
//import org.pangu.PanguException;
//
//public class ReferencePanguNode extends PanguNode {
//
//    private String refid = null;
//    
//    public ReferencePanguNode(String refid) throws PanguException {
//        super(-1,-1);
//        this.refid = refid;
//    }
//    
//    @Override
//    public void generate(GenInfo info, OutputStream os) throws PanguException {
//        getCache().lookupXMLReference(refid).generate(info, os);
//    }
//
//    @Override
//    public PanguNode duplicate() throws PanguException {
//        return getCache().lookupXMLReference(refid).duplicate();
//    }
//
//    @Override
//    public String toString(ArrayList<PanguNode> visited) {
//        return getCache().lookupXMLReference(refid).toString(visited);
//    }
//
//}
