package org.pangu.tree.decorators;

public interface PanguDecorator {

    public byte[] beforeStartElementName();
    public byte[] startElement(String name);
    public byte[] afterStartElementName();
    
    public byte[] beforeElementValue();
    public byte[] afterElementValue();
    
    public byte[] beforeStopElementName();
    public byte[] stopElement(String name);
    public byte[] afterStopElementName();
    
    public byte[] beforeAttributeName();
    public byte[] afterAttributeName();
    
    public byte[] beforeAttributeValue();
    public byte[] afterAttributeValue();
    
    public byte[] betweenAttributes();
   
    public byte[] beforeSequence();
    public byte[] afterSequence();
    public byte[] betweenSequenceElements();

    public byte[] beforeChoice();
    public byte[] afterChoice();
    public byte[] betweenChoiceElements();
}
