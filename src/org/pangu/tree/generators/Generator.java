package org.pangu.tree.generators;

import java.io.IOException;
import java.io.OutputStream;

import org.pangu.PanguException;
import org.pangu.tree.GenInfo;

public abstract class Generator {
    
    public abstract void generate(GenInfo info, OutputStream os)
           throws PanguException;
    
    protected void print(OutputStream os, byte[] data) throws PanguException { 
        try {
            os.write(data);
        } catch (IOException e) {
            throw new PanguException("Unable to write to output stream.",e);
        }
    }
}
