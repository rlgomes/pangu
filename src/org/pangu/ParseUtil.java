package org.pangu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ParseUtil {

    private static DocumentBuilder db = null;

    static {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Error instantiating DocumentBuilder.",e);
        }
    }
    
    public static Document parse(File xml) throws PanguException { 
        try {
            FileInputStream fis = new FileInputStream(xml);
            return parse(fis);
        } catch (FileNotFoundException e) {
            throw new PanguException("Error parsing XML document",e);
        }
    }
    
    public static Document parse(InputStream is) throws  PanguException { 
        try { 
            return db.parse(is);
        } catch (IOException e) { 
            throw new PanguException("Error parsing XML document",e);
        } catch (SAXException e) {
            throw new PanguException("Error parsing XML document",e);
        }
    }
}
