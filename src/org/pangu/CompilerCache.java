package org.pangu;

import java.util.HashMap;
import java.util.NoSuchElementException;

import org.pangu.tree.PanguNode;
import org.w3c.dom.Node;

/**
 * Classed used internally to cache various Schema references as well as 
 * PanguNode references for the XSDCompiler
 * 
 * @author rlgomes
 */
class CompilerCache {
   
    HashMap<String, Node> elementRefs = null;
    
    HashMap<String, Node> attributeRefs = null;
   
    HashMap<String, PanguNode> xmlrefs = null;
   
    HashMap<String, Node> simpleTypes = null;

    HashMap<String, Node> complexTypes = null;
    
    public CompilerCache() { 
        elementRefs = new HashMap<String, Node>();
        attributeRefs = new HashMap<String, Node>();
        xmlrefs = new HashMap<String, PanguNode>();
        simpleTypes = new HashMap<String, Node>(); 
        complexTypes = new HashMap<String, Node>(); 
    }
  
    public void addXMLReference(String name, PanguNode pn) { 
        xmlrefs.put(name, pn);
    }
    
    public PanguNode lookupXMLReference(String name) { 
        return xmlrefs.get(name);
    }
    
    public Node lookupElementReference(String id) { 
        Node ref = elementRefs.get(id);
        if ( ref == null ) { 
            throw new NoSuchElementException("No reference found for [" + id + "]");
        }
        return ref;
    }
    
    public Node lookupAttributeReference(String id) { 
        Node ref = attributeRefs.get(id);
        if ( ref == null ) { 
            throw new NoSuchElementException("No reference found for [" + id + "]");
        }
        return ref;
    }

    public Node lookupSimpleType(String typename) { 
        return simpleTypes.get(typename);
    }

    public Node lookupComplexType(String typename) { 
        return complexTypes.get(typename);
    }
}
