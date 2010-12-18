package org.pangu.tree.generators;

import java.io.OutputStream;
import java.util.Random;

import org.pangu.PanguException;
import org.pangu.tree.GenInfo;

public class PositiveIntegerGenerator extends Generator {
   
    public PositiveIntegerGenerator() { 
    }

    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        Random rand = info.getRandom();
        String aux = "" + (1 + Math.abs(rand.nextInt(1000)));
        int limit = (int) info.getBytesLeft();
        
        // can't set limit to zero because an empty string is not a valid
        // integer
        if ( limit <= 0 ) limit = 1;
        if ( aux.length() < limit ) limit = aux.length();
        
        byte[] data = aux.substring(0,limit).getBytes();
        print(os,data);
        info.takeFromBytesLeft(data.length);
    }

}
