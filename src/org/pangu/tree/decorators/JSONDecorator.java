package org.pangu.tree.decorators;

/**
 * A simple XML decorator for generating XML documents with the pangu streaming
 * library.
 * 
 * @author rlgomes
 */
public class JSONDecorator implements PanguDecorator {
    
    private final static byte[] BSEN = "{\"".getBytes();
    private final static byte[] ASEN = "\":".getBytes();
    private final static byte[] NOTHING = "".getBytes();
    private final static byte[] CLOSESQUIG = "}".getBytes(); 
    private final static byte[] PARENTHESIS = "\"".getBytes();
    private final static byte[] ABN = "\":".getBytes();
    private final static byte[] COMMA = ",".getBytes();
    
    private final static byte[] LEFTBRACKET = "[".getBytes(); 
    private final static byte[] RIGHTBRACKET = "]".getBytes(); 

    @Override public byte[] beforeStartElementName() { return BSEN; }
    @Override public byte[] startElement(String name) { return name.getBytes(); }

    @Override public byte[] afterStartElementName() { return ASEN; }
    @Override public byte[] beforeStopElementName() { return NOTHING; }

    @Override public byte[] stopElement(String name) { return NOTHING; }
    @Override public byte[] afterStopElementName() { return CLOSESQUIG; }
    
    @Override public byte[] afterElementValue() { return PARENTHESIS; }
    @Override public byte[] beforeElementValue() { return PARENTHESIS; }

    @Override public byte[] beforeAttributeName() { return PARENTHESIS; }
    @Override public byte[] afterAttributeName() { return ABN; }

    @Override public byte[] beforeAttributeValue() { return PARENTHESIS; }
    @Override public byte[] afterAttributeValue() { return PARENTHESIS; }

    @Override public byte[] betweenAttributes() { return COMMA; }
    
    @Override public byte[] beforeChoice() { return LEFTBRACKET; }
    @Override public byte[] afterChoice() { return RIGHTBRACKET; }
    @Override public byte[] betweenChoiceElements() { return COMMA; }
    
    @Override public byte[] beforeSequence() { return LEFTBRACKET; }
    @Override public byte[] afterSequence() { return RIGHTBRACKET; }
    @Override public byte[] betweenSequenceElements() { return COMMA; }
}
