package org.pangu.tree.generators;

import java.io.OutputStream;

import org.pangu.PanguException;
import org.pangu.tree.GenInfo;

public class BooleanGenerator extends Generator {
   
    public BooleanGenerator() { 
    }

    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        byte[] data = ("" + info.getRandom().nextBoolean()).getBytes();
        print(os,data);
        info.takeFromBytesLeft(data.length);
    }
}
