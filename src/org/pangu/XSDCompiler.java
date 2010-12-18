package org.pangu;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.pangu.tree.AttributeNode;
import org.pangu.tree.ChoiceNode;
import org.pangu.tree.DataNode;
import org.pangu.tree.ElementNode;
import org.pangu.tree.PanguNode;
import org.pangu.tree.RootNode;
import org.pangu.tree.SequenceNode;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XSD Compiler does what the name says and compiles any XSD into a pangu 
 * grammar tree which can be used by pangu to generate documents in various
 * formats.
 * 
 * @author rlgomes
 */
public class XSDCompiler {
    
    private static final Logger _logger = Logger.getLogger(XSDCompiler.class);

    private final static String XS_ELEMENT          = "xs:element";
    private final static String XS_ATTRIBUTE        = "xs:attribute";

    private final static String XS_SEQUENCE         = "xs:sequence";
    private final static String XS_CHOICE           = "xs:choice";
    
    private final static String XS_COMPLEXTYPE      = "xs:complexType";
    private final static String XS_SIMPLETYPE       = "xs:simpleType";

    private final static String XS_RESTRICTION      = "xs:restriction";
    private final static String XS_LIST             = "xs:list";
    private final static String XS_UNION            = "xs:union";

    public XSDCompiler() throws PanguException { 
        
    }
   
    /**
     * Compiles the supplied XSD document into a PanguNode 
     * 
     * @param doc
     * @return
     * @throws PanguException
     */
    public static PanguGen compile(Document doc) throws PanguException { 
        CompilerCache cache = init(doc);
        PanguNode node = compile(doc.getDocumentElement(),cache);
        PanguGen gen = new PanguGen(node);
        return gen;
    }

    /**
     * Compiles the supplied XSD document into a PanguNode 
     * 
     * @param doc
     * @return
     * @throws PanguException
     */
    public static PanguGen compile(InputStream is) throws PanguException { 
        Document doc = ParseUtil.parse(is);
        return compile(doc);
    }
    
    private static PanguNode compile(Node root,
                                     CompilerCache cache)
            throws PanguException { 
        return compile(root, new RootNode(), cache);
    }
    
    private static PanguNode compile(Node root,
                                     PanguNode rnode,
                                     CompilerCache cache)
            throws PanguException { 
        NodeList nodes = root.getChildNodes();

        for (int n = 0; n < nodes.getLength(); n++) { 
            Node node = nodes.item(n);
            NamedNodeMap attributes = node.getAttributes();
     
            /**
             * None of these XML elements are of use to generating documents 
             * from the XSD at hand.
             */
            if ( node.getNodeType() == Node.TEXT_NODE ||
                 node.getNodeType() == Node.COMMENT_NODE ||
                 node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE ) { 
                continue;
            }

            /*
             * Keep the min/max occurences from the reference
             */
            int max = getMaxOccurs(node);
            int min = getMinOccurs(node);

            Node namenode = (attributes != null ? 
                                        attributes.getNamedItem("name") : null);
            String nodename = node.getNodeName();
            Node ref = attributes.getNamedItem("ref");

            Node useNode = attributes.getNamedItem("use");
	        String use = (useNode != null ? useNode.getNodeValue() : "optional");
           
            if ( nodename.equals(XS_ELEMENT) ) { 
	            if ( ref != null ) { 
	                String refid = ref.getNodeValue();
	                node = cache.lookupElementReference(refid);
	                attributes = node.getAttributes();
	            }
            }

            if ( nodename.equals(XS_ATTRIBUTE) ) { 
	            if ( ref != null ) { 
	                String refid = ref.getNodeValue();
	                node = cache.lookupAttributeReference(refid);
	                attributes = node.getAttributes();
	            }
            }

            namenode = (attributes != null ? attributes.getNamedItem("name") : null);
            String name = ( namenode != null ? namenode.getNodeValue() : null);
            
            // if this node is already in the cache then just grab it and add 
            // here without trying to re-parse.
            PanguNode xnode = cache.lookupXMLReference(name);
           
            if ( xnode != null ) { 
                _logger.debug("fcache: " + nodename + " " + name + " " + min + " " + max);
                xnode = xnode.duplicate();
                xnode.setMinOccurs(min);
                xnode.setMaxOccurs(max);
                if ( xnode instanceof AttributeNode ) {
                    ((AttributeNode) xnode).setUse(use);
                }
            } else { 
                _logger.debug("creatn: " + nodename + " " + name + " " + min + " " + max);
	            if ( nodename.equals(XS_ELEMENT) ) {
	                xnode = new ElementNode(name, min, max);
	                
	                Node typeAttr = attributes.getNamedItem("type");
	                if ( typeAttr != null ) {
	                    String typename = typeAttr.getNodeValue();
	                    PanguNode tn = null;
	                    
	                    if ( !typename.startsWith("xs:") ) { 
	                        // could be a custom type
	                        Node type = cache.lookupSimpleType(typename);

	                        if ( type == null ) { 
	                            type = cache.lookupComplexType(typename);
	                            
	                            if ( type == null ) { 
	                                throw new PanguException("Unable to resolve type [" + typename + "]");
	                            } else { 
	                                compile(type, xnode, cache);
	                            }
	                        } else { 
		                        type = getNodeOfType(type, Node.ELEMENT_NODE);
		                        tn = parseSimpleType(type, 1, 1);
	                        }
	                    } else { 
		                    typename = typename.replaceAll("xs:", "");
		                    tn = new DataNode(typename, 1, 1, new String[0]);
	                    }
	                    if ( tn != null ) {
	                        xnode.addNode(tn);
	                    }
	                }

                    cache.addXMLReference(name, xnode);
                    compile(node, xnode, cache);
	            } else if ( nodename.equals(XS_ATTRIBUTE) ) {
	                /**
	                 * <attribute default=string
	                 *            fixed=string
	                 *            form=qualified|unqualified
	                 *            id=ID
	                 *            name=NCName
	                 *            ref=QName
	                 *            type=QName
	                 *            use=optional|prohibited|required
	                 *            any attributes
	                 *            >
	                 *    (annotation?,(simpleType?))
	                 * </attribute>    
	                 */
	                if ( rnode instanceof ElementNode ) { 
	                    String attrName = attributes.getNamedItem("name").getNodeValue();
	                    Node typeNode = attributes.getNamedItem("type"); 
	                    
	                    String typename = (typeNode != null ? typeNode.getNodeValue() : null);
	                    DataNode data = null;
	                    
	                    if ( typeNode == null ) { 
	                        Node type = getNodeOfType(node, Node.ELEMENT_NODE);
	                        
                            if ( type == null ) { 
                                throw new PanguException("Error!");
                            } else { 
                                type = getNodeOfType(type, Node.ELEMENT_NODE);
                                data = parseSimpleType(type, 1, 1);
                            } 
	                    } else if ( !typename.startsWith("xs:") ) { 
                            // could be a custom type
                            Node type = cache.lookupSimpleType(typename);

                            if ( type == null ) { 
                                type = cache.lookupComplexType(typename);
                                
                                if ( type == null ) { 
                                    throw new PanguException("Unable to resolve type [" + typename + "]");
                                } else { 
                                    compile(type, xnode, cache);
                                }
                            } else { 
                                type = getNodeOfType(type, Node.ELEMENT_NODE);
                                data = parseSimpleType(type, 1, 1);
                            }
                        } else { 
                            typename = typename.replaceAll("xs:", "");
                            data = new DataNode(typename, 1, 1, new String[0]);
                        }
	                    
	                    AttributeNode an = new AttributeNode(attrName,
	                                                         data,
	                                                         use,
	                                                         min,
	                                                         max);
	                    
	                    ((ElementNode) rnode).addAttribute(an);
	                    continue;
	                } else { 
	                    // just continue since this should be to reference.
	                    continue;
	                }
	            } else if ( nodename.equals(XS_CHOICE) ) { 
	                xnode = new ChoiceNode(min, max);
	                compile(node, xnode, cache);
	            } else if ( nodename.equals(XS_SEQUENCE) ) { 
	                xnode = new SequenceNode(min, max);
	                compile(node, xnode, cache);
	            } else if ( nodename.equals(XS_COMPLEXTYPE) ) { 
	                // skip over this element in xsd's
	                compile(node, rnode, cache);
	            } else if ( nodename.equals(XS_SIMPLETYPE) ) { 
	                // need to figure this out
	                Node type = getNodeOfType(node, Node.ELEMENT_NODE);
	                xnode = parseSimpleType(type,min,max);
	                compile(node, rnode, cache);
	            }
            }
                
            if ( xnode != null ) { 
                rnode.addNode(xnode);
            }
        }
       
        return rnode;
    }
    
    private static DataNode parseSimpleType(Node type, int min, int max) 
            throws PanguException { 
        NamedNodeMap tattribs = type.getAttributes();
        String tname = type.getNodeName();
        ArrayList<String> targs = new ArrayList<String>();
        String basetype = null;
        
        if ( tname.equals(XS_RESTRICTION) ) {
            /*
             * <restriction base = QName
             *              id = ID >
             *  Content: (annotation?, 
             *              (simpleType?, 
             *                  (minExclusive | 
             *                   minInclusive | 
             *                   maxExclusive | 
             *                   maxInclusive | 
             *                   totalDigits | 
             *                   fractionDigits | 
             *                   length | 
             *                   minLength | 
             *                   maxLength | 
             *                   enumeration | 
             *                   whiteSpace | 
             *                   pattern)*))
             * </restriction>
             */
            Node bnode = tattribs.getNamedItem("base");
           
            if ( bnode != null ) { 
                basetype = bnode.getNodeValue();
                basetype = basetype.replaceAll("xs:", "restriction:");
            }
           
            Node aux = type.getFirstChild();
            while ( aux != null ) { 
                if ( aux.getNodeType() == Node.ELEMENT_NODE ) { 
                    NamedNodeMap aattribs = aux.getAttributes();
                    String value = aattribs.getNamedItem("value").getNodeValue();
                    String nname = aux.getNodeName();
                    nname = nname.replaceAll("xs:", "");
                    
                    targs.add(nname + ":" + value);
                }
                aux = aux.getNextSibling();
            }
        } else if ( tname.equals(XS_LIST) ) { 
            /*
             * <list id = ID
             *       itemType = QName>
             *  Content: (annotation?, simpleType?)
             * </list>
             */
            Node itemType = tattribs.getNamedItem("itemType");
            basetype = itemType.getNodeValue();
            basetype = basetype.replaceAll("xs:", "list:");
        } else if ( tname.equals(XS_UNION) ) { 
            /*
             * <union id = ID
             *        memberTypes = List of QName >
             *  Content: (annotation?, simpleType*)
             * </union>
             */
            
        } else { 
            throw new PanguException("Unexpected node [" + tname + "]");
        }
        
        return new DataNode(basetype,
                             min,
                             max,
                             targs.toArray(new String[0]));
    }
    
    /*
     * We initialize the cache elements and then make sure to lookup all 
     * of the elements that may be refered to or that are types in the grammar
     * that will be referenced as well.
     * 
     * With this cache we can easily put together the full grammar tree and
     * preserve all of the references between elements correctly.
     */
    private static CompilerCache init(Document doc) { 
        CompilerCache cache = new CompilerCache();
        init(doc.getDocumentElement(),cache);
        return cache;
    }

    /**
     * need to cache all xml references 
     *  
     * @param root
     * @param cache
     */
    private static void init(Node root, CompilerCache cache) {
        NodeList nodes = root.getChildNodes();
        
        for (int n = 0; n < nodes.getLength(); n++) { 
            Node node = nodes.item(n);
            String nodename = node.getNodeName();

            if ( nodename.equals(XS_ELEMENT) ) {
                NamedNodeMap attributes = node.getAttributes();
                Node nameattr = attributes.getNamedItem("name");
                
                if ( nameattr != null ) {
                    cache.elementRefs.put(nameattr.getNodeValue(), node);
                }
            } else if ( nodename.equals(XS_ATTRIBUTE) ) {
                NamedNodeMap attributes = node.getAttributes();
                Node nameattr = attributes.getNamedItem("name");
                
                if ( nameattr != null ) {
                    cache.attributeRefs.put(nameattr.getNodeValue(), node);
                }
            } else if ( nodename.equals(XS_SIMPLETYPE) ) {
                NamedNodeMap attributes = node.getAttributes();
                Node nameattr = attributes.getNamedItem("name");
                
                if ( nameattr != null )
                    cache.simpleTypes.put(nameattr.getNodeValue(), node);
            } else if ( nodename.equals(XS_COMPLEXTYPE) ) {
                NamedNodeMap attributes = node.getAttributes();
                Node nameattr = attributes.getNamedItem("name");
                
                if ( nameattr != null )
                    cache.complexTypes.put(nameattr.getNodeValue(), node);
            }
            
            init(node, cache);
        }
    }
    
    private static int getMaxOccurs(Node node) { 
        int result = 1;
        NamedNodeMap attributes = node.getAttributes();
        if ( attributes != null ) {
	        Node maxattr = attributes.getNamedItem("maxOccurs");
	        if ( maxattr != null ) { 
	            String value = maxattr.getNodeValue();
	            
	            if ( value.equalsIgnoreCase("unbounded") ) {
	                result = Integer.MAX_VALUE;
	            } else { 
	                result = Integer.valueOf(value);
	            }
	        }
        }
       
        return result;
    }
    
    private static Node getNodeOfType(Node root, short element_type ) { 
        Node result = root.getFirstChild();
        while ( result != null && result.getNodeType() != element_type) {
            result = result.getNextSibling();
        }
        return result;
    }

    private static int getMinOccurs(Node node) { 
        int result = 1;
        NamedNodeMap attributes = node.getAttributes();
        if ( attributes != null ) {
	        Node minattr = attributes.getNamedItem("minOccurs");
	        if ( minattr != null ) { 
	            String value = minattr.getNodeValue();
	            result = Integer.valueOf(value);
	        }
        }
       
        return result;
    }
}