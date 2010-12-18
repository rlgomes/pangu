package org.pangu.tree.generators;

import java.io.OutputStream;

import org.pangu.PanguException;
import org.pangu.tree.GenInfo;

public class StringGenerator extends Generator {
    
    private static byte[] DATA = 
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ".
                                                                     getBytes();
    
    private int desiredLength = -1;
    
    public StringGenerator(int length) { 
        desiredLength = length;
    }

    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        int length = (int) info.getBytesLeft();
        if ( desiredLength != -1 ) { 
            length = desiredLength;
        }
        
        int total = 0;
        
        while ( total < length ) { 
            int howmuch = DATA.length;
            byte[] data = DATA;
           
            if ( total + howmuch > length ) { 
                howmuch = length-total;
                byte[] aux = new byte[howmuch];
                System.arraycopy(DATA, 0, aux, 0, howmuch);
                data = aux;
            }
            
            print(os, data);
            info.takeFromBytesLeft(data.length);
            
            total+=howmuch;
        }
    }
}