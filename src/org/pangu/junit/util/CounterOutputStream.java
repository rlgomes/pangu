package org.pangu.junit.util;

import java.io.IOException;
import java.io.OutputStream;

public class CounterOutputStream extends OutputStream {

    private long _count = 0;
    
    @Override
    public void write(int b) throws IOException {
        _count++;
    }
    
    public long numberOfWrittenBytes() { return _count; }
    public void reset() { _count = 0; }
}
