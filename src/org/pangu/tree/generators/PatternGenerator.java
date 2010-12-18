package org.pangu.tree.generators;

import java.io.OutputStream;

import nl.flotsam.xeger.Xeger;

import org.pangu.PanguException;
import org.pangu.tree.GenInfo;

public class PatternGenerator extends Generator {
   
    private String pattern = null;
    
    public PatternGenerator(String pattern) { 
        this.pattern = pattern;
    }

    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        Xeger xeger = new Xeger(pattern, info.getRandom());
        byte[] data = xeger.generate().getBytes();
        print(os, data);
    }

}
