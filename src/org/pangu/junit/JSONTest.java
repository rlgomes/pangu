package org.pangu.junit;

import java.io.ByteArrayOutputStream;
import java.io.File;

import junit.framework.TestCase;

import org.json.simple.JSONValue;
import org.junit.Test;
import org.pangu.PanguException;
import org.pangu.PanguGen;
import org.pangu.ParseUtil;
import org.pangu.XSDCompiler;
import org.pangu.junit.util.BaseTest;
import org.pangu.junit.util.CounterOutputStream;
import org.pangu.junit.util.TestException;
import org.pangu.tree.GenInfo;
import org.pangu.tree.decorators.JSONDecorator;
import org.pangu.tree.decorators.PanguDecorator;
import org.w3c.dom.Document;

/**
 * A test to validate that JSON Generation is working as desired.
 * 
 * @author rlgomes
 */
public class JSONTest extends BaseTest {
    
    private int[] sizes = { 128, 256, 1024, 10240 };
    private String[] files = { "list.xsd", "mlist.xsd", "product.xsd" };
    private String[] roots = { "list", "list", "product" };

    private PanguDecorator formatter = new JSONDecorator();
    
    /**
     * Validate for a well known group of XSDs that the generated JSON data is 
     * in fact a valid JSON object.
     * 
     * @throws TestException
     * @throws PanguException
     */
    @Test
    public void validateSchema() throws TestException, PanguException { 
        logTestStarted("validate generated data is a valid json");
        
        int[] sizes = { 128, 256, 1024, 10240 };
        String[] files = { "list.xsd", "mlist.xsd", "product.xsd", "order1.xsd" };
        String[] roots = { "list", "list", "product", "order" };
     
        for (int f = 0; f < files.length; f++) {
            _logger.info("[" + files[f] + "]");

            File schema = new File("test/xsd/" + files[f]);
            Document doc = ParseUtil.parse(schema);
            PanguGen gen = XSDCompiler.compile(doc);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GenInfo gi = new GenInfo(formatter);
            for (int i = 0; i < sizes.length; i++) {
                gi.setBytesLeft(sizes[i]);
                gen.generate(baos, gi);
                JSONValue.parse(baos.toString());
            }
        }
    }
   
    /**
     * This test will just calculate what the average size difference between 
     * the desired size and generated size isn't off by more than 10%.
     * 
     * @throws TestException
     * @throws PanguException
     */
    @Test
    public void analysisTest() throws TestException, PanguException {
        logTestStarted("generated size analysis");
        
        int ITERATIONS = 1000;
        for (int f = 0; f < files.length; f++) {
            _logger.info("[" + files[f] + "]");

            File file = new File("test/xsd/" + files[f]);
            Document doc = ParseUtil.parse(file);
            PanguGen gen = XSDCompiler.compile(doc);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GenInfo gi = new GenInfo(formatter);
            gi.setDesiredRoot(roots[f]);
            
            for (int i = 0; i < sizes.length; i++) {
                double accdiff = 0;
                for (int x = 0; x < ITERATIONS; x++ ) { 
                    gi.setBytesLeft(sizes[i]);
                    gen.generate(baos, gi);
                    
                    if ( _logger.isDebugEnabled() )
                        _logger.debug(baos.toString());

                    double got = baos.size();
                    double diff = ((got - sizes[i]) / got);
                    accdiff += diff;
                    baos.reset();
                }
              
                _logger.info(sizes[i] + " off by " + (accdiff/ITERATIONS)*100);
                assert( accdiff/ITERATIONS <= MAX_DIFF );
            }
        }
    }
   
    /**
     * This test validates that even though the schemas are defined in slightly 
     * different manners they end up having the same generated XML because the
     * underlying schema structure is the same.
     * 
     * @throws TestException
     * @throws PanguException
     */
    @Test
    public void validateOrderSchemas() throws TestException, PanguException {
        logTestStarted("validate order schemas");
        
        long[] sizes = { 128, 1024 };
        String[] files = { "order1.xsd", "order2.xsd", "order3.xsd" };
        String[] roots = { "order", "order", "order" };
       
        String[] data = new String[files.length];
        for (int i = 0; i < sizes.length; i++) {
	        long seed = System.currentTimeMillis();
	        
	        for (int f = 0; f < files.length; f++) {
	            File file = new File("test/xsd/" + files[f]);
	            Document doc = ParseUtil.parse(file);
	            PanguGen gen = XSDCompiler.compile(doc);
	            GenInfo gi = new GenInfo(formatter, sizes[i], seed);
	            gi.setDesiredRoot(roots[f]);
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                gen.generate(baos,gi);
                data[f] = baos.toString();
                baos.reset();
	        }
	        
	        for (int f = 1; f < files.length; f++) { 
	            TestCase.assertEquals(data[0], data[f]);
	        }
        }
    }
    
    @Test
    public void performanceSmallObjects() throws TestException, PanguException {
        logTestStarted("performance of generating small objects");
       
        int size = 128;
        double ITERATIONS = 100000;
        for (int f = 0; f < files.length; f++) {
            _logger.info("[" + files[f] + "]");

            File file = new File("test/xsd/" + files[f]);
            Document doc = ParseUtil.parse(file);
            PanguGen gen = XSDCompiler.compile(doc);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            GenInfo gi = new GenInfo(formatter);
            gi.setDesiredRoot(roots[f]);
            
            long start = System.currentTimeMillis();
            for (int x = 0; x < ITERATIONS; x++ ) { 
                gi.setBytesLeft(size);
                gen.generate(baos, gi);
                baos.reset();
            }
            long stop = System.currentTimeMillis();
            double ops = ITERATIONS/((double)(stop-start)/1000.0f);
            _logger.info("gen/s: " + ops);
        }
    }
    
    @Test
    public void performanceThroughPut() throws TestException, PanguException {
        logTestStarted("performance of throughput");
       
        int size = 1024*1024*150;
        double ITERATIONS = 1;
        for (int f = 0; f < files.length; f++) {
            _logger.info("[" + files[f] + "]");

            File file = new File("test/xsd/" + files[f]);
            Document doc = ParseUtil.parse(file);
            PanguGen gen = XSDCompiler.compile(doc);
            CounterOutputStream os = new CounterOutputStream();
            GenInfo gi = new GenInfo(formatter);
            gi.setDesiredRoot(roots[f]);
            
            long start = System.currentTimeMillis();
            for (int x = 0; x < ITERATIONS; x++ ) { 
                gi.setBytesLeft(size);
                gen.generate(os, gi);
                os.reset();
            }
            long stop = System.currentTimeMillis();
            double ops = ITERATIONS/((double)(stop-start)/1000.0f);
            long mbs = (long)(ops*size)/(1024*1024);
            _logger.info("MB/s: " + mbs);
        }
    }

}