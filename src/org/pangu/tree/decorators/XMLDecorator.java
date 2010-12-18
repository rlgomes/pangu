package org.pangu.tree.decorators;

/**
 * A simple XML decorator for generating XML documents 
 * @author rlgomes
 */
public class XMLDecorator implements PanguDecorator {
    
    private final static byte[] LT = "<".getBytes();
    private final static byte[] GT = ">".getBytes();
    private final static byte[] ENDXML = "</".getBytes();
    private final static byte[] NOTHING  = "".getBytes();
    private final static byte[] SPACE  = " ".getBytes();
    private final static byte[] PARENTHESIS = "\"".getBytes();
    private final static byte[] BAV = "=\"".getBytes();
    
    @Override public byte[] beforeStartElementName() { return LT; }
    @Override public byte[] startElement(String name) { return name.getBytes(); }

    @Override public byte[] stopElement(String name) { return name.getBytes(); }
    @Override public byte[] afterStartElementName() { return GT; }

    @Override public byte[] beforeStopElementName() { return ENDXML; }
    @Override public byte[] afterStopElementName() { return GT; }
   
    @Override public byte[] afterElementValue() { return NOTHING; }
    @Override public byte[] beforeElementValue() { return NOTHING; }

    @Override public byte[] beforeAttributeName() { return NOTHING; }
    @Override public byte[] afterAttributeName() { return NOTHING; }

    @Override public byte[] beforeAttributeValue() { return BAV; }
    @Override public byte[] afterAttributeValue() { return PARENTHESIS; }

    @Override public byte[] betweenAttributes() { return SPACE; }
    
    @Override public byte[] beforeChoice() { return NOTHING; }
    @Override public byte[] afterChoice() { return NOTHING; }
    @Override public byte[] betweenChoiceElements() { return NOTHING; }
    
    @Override public byte[] beforeSequence() { return NOTHING; }
    @Override public byte[] afterSequence() { return NOTHING; }
    @Override public byte[] betweenSequenceElements() { return NOTHING; }
}
