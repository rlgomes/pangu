package org.pangu;

import java.io.IOException;
import java.io.OutputStream;

import org.pangu.tree.GenInfo;
import org.pangu.tree.PanguNode;

public class PanguGen {
    
    private PanguNode _root = null;
    
    PanguGen(PanguNode root) { 
        _root = root;
    }

    public void generate(OutputStream os, GenInfo gi) throws PanguException {
        _root.generate(gi,os);
        try {
            os.close();
        } catch (IOException e) {
            throw new PanguException("Unable to close output stream.",e);
        }
    }
}
