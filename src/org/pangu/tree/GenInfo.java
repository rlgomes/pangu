package org.pangu.tree;

import java.util.Random;

import org.pangu.tree.decorators.PanguDecorator;

/**
 * This object contains the generation information required to generate 
 * documents by pangu. This class is responsible for giving the pangu library
 * access to a pseudo random number generator as well as the desire root of
 * your grammar to start at. 
 * 
 * Later we can add a simple key/value lookup mechanism here which can then be
 * used by the end user to pass information to the decorator through the pangu
 * generator.
 *  
 * @author rlgomes
 */
public class GenInfo {

    private Random _random = null;
   
    private long _seed = -1;
    
    private long _bytesleft = 0;
    
    private String _desiredRoot = null;
    
    private PanguDecorator _formatter = null;

    GenInfo() {
        
    }

    public GenInfo(PanguDecorator formatter) { 
        _formatter = formatter;
        _random = new Random();
    }

    public GenInfo(PanguDecorator formatter, long length, long seed) { 
        _formatter = formatter;
        _bytesleft = length;
        _random = new Random(seed);
    }

    public GenInfo(PanguDecorator formatter, long length) { 
        _formatter = formatter;
        _bytesleft = length;
        _random = new Random();
    }
    
    public void setDesiredRoot(String name) { _desiredRoot = name; } 
    public String getDesiredRoot() { return _desiredRoot; } 
    
    public long getBytesLeft() { return _bytesleft; }
    public void takeFromBytesLeft(long amount) { _bytesleft -= amount; }
    public void addToBytesLeft(long amount) { _bytesleft += amount; }
    public void setBytesLeft(long bytesLeft) { _bytesleft = bytesLeft; }

    public PanguDecorator getDecorator() { return _formatter; }

    public Random getRandom() {  return _random; }
    
    public GenInfo duplicate() { 
        GenInfo info = new GenInfo();
        info._random = _random;
        info._seed = _seed;
        info._bytesleft = _bytesleft;
        info._desiredRoot = _desiredRoot;
        info._formatter = _formatter;
        return info;
    }
}
