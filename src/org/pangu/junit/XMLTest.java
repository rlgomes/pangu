package org.pangu.junit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.pangu.PanguException;
import org.pangu.PanguGen;
import org.pangu.ParseUtil;
import org.pangu.XSDCompiler;
import org.pangu.junit.util.BaseTest;
import org.pangu.junit.util.CounterOutputStream;
import org.pangu.junit.util.TestException;
import org.pangu.tree.GenInfo;
import org.pangu.tree.decorators.PanguDecorator;
import org.pangu.tree.decorators.XMLDecorator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A test to validate that JSON Generation is working as desired.
 * 
 * @author rlgomes
 */
public class XMLTest extends BaseTest {
    
    private int[] sizes = { 128, 256, 1024, 10240 };
    private String[] files = { "list.xsd", "mlist.xsd", "product.xsd" };
    private String[] roots = { "list", "list", "product" };

    private DocumentBuilder db = null;
    
    private PanguDecorator formatter = new XMLDecorator();
    
    @Before
    public void setup() throws TestException { 
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new TestException("Error instantiating DocumentBuilder.",e);
        }
    }
   
    /**
     * Validate for a well known group of XSDs that after generating the XML, 
     * that the generated XML validates correctly against the original schema. 
     * 
     * @throws TestException
     * @throws PanguException
     */
    @Test
    public void validateSchema() throws TestException, PanguException { 
        logTestStarted("validate generated data is valid with schema");
        
        int[] sizes = { 128, 256, 1024, 10240 };
        String[] files = { "list.xsd", "mlist.xsd", "product.xsd", "order1.xsd" };
        String[] roots = { "list", "list", "product", "order" };

        for (int f = 0; f < files.length; f++) {
            _logger.info("[" + files[f] + "]");

            File schema = new File("test/xsd/" + files[f]);
            Document doc = parse(schema);
            PanguGen gen = XSDCompiler.compile(doc);
                
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GenInfo gi = new GenInfo(formatter);
            gi.setDesiredRoot(roots[f]);
            
            for (int i = 0; i < sizes.length; i++) {
                gi.setBytesLeft(sizes[i]);
                gen.generate(baos, gi);
                ByteArrayInputStream bais = 
                                  new ByteArrayInputStream(baos.toByteArray());
                // validate the generated XML against the original schema
                validate(bais, schema);
                baos.reset();
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
            Document doc = parse(file);
            PanguGen gen = XSDCompiler.compile(doc);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GenInfo gi = new GenInfo(formatter);
            gi.setDesiredRoot(roots[f]);
            
            for (int i = 0; i < sizes.length; i++) {
                double accdiff = 0;
                for (int x = 0; x < ITERATIONS; x++ ) { 
                    gi.setBytesLeft(sizes[i]);
                    gen.generate(baos, gi);
                    double got = baos.size();
                    double diff = ((got - sizes[i]) / got);
                    accdiff += diff;
                    baos.reset();
                }
                
                accdiff = accdiff/ITERATIONS;
                _logger.info(sizes[i] + " off by " + accdiff*100);
                assert( accdiff <= MAX_DIFF );
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
        
        int[] sizes = { 128, 1024 };
        String[] files = { "order1.xsd", "order2.xsd", "order3.xsd" };
        String[] roots = { "order", "order", "order" };
       
        String[] data = new String[files.length];
        for (int i = 0; i < sizes.length; i++) {
	        long seed = System.currentTimeMillis();
	        
	        for (int f = 0; f < files.length; f++) {
	            File file = new File("test/xsd/" + files[f]);
	            Document doc = parse(file);
	            PanguGen gen = XSDCompiler.compile(doc);
	            GenInfo gi = new GenInfo(formatter,sizes[i],seed);
	            gi.setDesiredRoot(roots[f]);
	            
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                gen.generate(baos, gi);
                data[f] = baos.toString();
	        }
	        
	        for (int f = 1; f < files.length; f++) { 
	            TestCase.assertEquals(data[0], data[f]);
	        }
        }
    }
   
    /**
     * A small object performance test that for now just outputs the average 
     * number of generated documents per second that pangu can hit.
     * 
     * @throws TestException
     * @throws PanguException
     */
    @Test
    public void performanceSmallObjects() throws TestException, PanguException {
        logTestStarted("performance of generating small objects");
       
        int size = 128;
        double ITERATIONS = 200000;
        for (int f = 0; f < files.length; f++) {
            _logger.info("[" + files[f] + "]");

            File file = new File("test/xsd/" + files[f]);
            Document doc = parse(file);
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
       
        int size = 1024*1024*200;
        double ITERATIONS = 3;
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
   
    
    private Document parse(File xml) throws TestException { 
        try { 
            FileInputStream fis = new FileInputStream(xml);
            return db.parse(fis);
        } catch (IOException e) { 
            throw new TestException("Unable to parse XML document [" + xml + "]",e);
        } catch (SAXException e) {
            throw new TestException("Unable to parse XML document [" + xml + "]",e);
        }
    }
    
    private void validate(InputStream is, File schema) throws PanguException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(true);

        dbf.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema");
        dbf.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaSource",
                "" + schema);
        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();
            parser.parse(is);
        } catch (ParserConfigurationException e) {
            throw new PanguException("Validation failed.",e);
        } catch (SAXException e) {
            throw new PanguException("Validation failed.",e);
        } catch (IOException e) {
            throw new PanguException("Validation failed.",e);
        }
    }
 
    /**
     * info utility formatting method.
     * 
     * @param unformattedXML
     * @return
     * @throws PanguException
     */
    private String format(String unformattedXML) throws PanguException {
        try { 
	        // Instantiate transformer input
	        Source xmlInput = new StreamSource(new StringReader(unformattedXML));
	        StreamResult xmlOutput = new StreamResult(new StringWriter());
	
	        // Configure transformer
	        Transformer transformer = TransformerFactory.newInstance()
	                                                              .newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	        transformer.transform(xmlInput, xmlOutput);
	        
	        return xmlOutput.getWriter().toString();
        } catch (TransformerException e) { 
            throw new PanguException("Unable to format XML data.",e);
        }
    }
}