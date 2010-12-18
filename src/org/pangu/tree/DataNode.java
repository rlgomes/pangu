package org.pangu.tree;

import java.io.OutputStream;
import java.util.ArrayList;

import org.pangu.PanguException;
import org.pangu.tree.generators.BooleanGenerator;
import org.pangu.tree.generators.DecimalGenerator;
import org.pangu.tree.generators.Generator;
import org.pangu.tree.generators.IntegerGenerator;
import org.pangu.tree.generators.PatternGenerator;
import org.pangu.tree.generators.PositiveIntegerGenerator;
import org.pangu.tree.generators.StringGenerator;

public class DataNode extends PanguNode {
    
    private ArrayList<String> _args = null;
    protected String type= null;
    
    private Generator generator = null;
    
    public DataNode(String type,
                    int min,
                    int max,
                    String[] arguments) throws PanguException {
        super(min, max);
        
        this.type = type;
        _args = new ArrayList<String>();
        for ( int i = 0; i < arguments.length; i++) { 
            _args.add(arguments[i]);
        }
       
        String basetype = null;
        int desiredLength = -1;
        
        if ( type.startsWith("restriction:") ) { 
            basetype = type.replace("restriction:","");
        } else { 
            basetype = type;
        }
        
        for (String arg : _args) { 
            if ( arg.startsWith("length:") ) { 
                desiredLength = Integer.valueOf(arg.replaceAll("length:",""));
            } else if ( arg.startsWith("pattern:") ) { 
                arg = arg.replaceAll("pattern:", "");
                generator = new PatternGenerator(arg);
            }
        }    
        
        if ( generator == null ) { 
	        if ( basetype.equals("string") ) {
	            generator = new StringGenerator(desiredLength);
	        } else if ( basetype.equals("integer") ) {
	            generator = new IntegerGenerator();
	        } else if ( basetype.equals("positiveInteger") ) {
	            generator = new PositiveIntegerGenerator();
	        } else if ( basetype.equals("decimal") ) {
	            generator = new DecimalGenerator();
	        } else if ( basetype.equals("boolean") ) {
	            generator = new BooleanGenerator();
	        }
        }
        
        if ( generator == null ) { 
	        throw new PanguException("Unsupported type [" + basetype + 
	                                 "(" + _args + ") ]");
        }
    }
    
    @Override
    public void generate(GenInfo info, OutputStream os) throws PanguException {
        generator.generate(info,os);
    }
    
    @Override
    public PanguNode duplicate() throws PanguException {
        DataNode node = new DataNode(type, getMinOccurs(),getMaxOccurs(),_args.toArray(new String[0]));
        node.children = getChildren();
        node._args = _args;
        return node;
    }
    
    @Override
    public String toString(ArrayList<PanguNode> visited) {
        StringBuffer args = new StringBuffer();

        if ( visited.contains(this) )
            return "";
        
        visited.add(this);
        
        if ( _args.size() != 0 ) { 
            for (String arg : _args) args.append(arg + ",");
            args = new StringBuffer(args.substring(0,args.length()-1));
        }
        return this.type+ " {" + args + "}";
    }

    public boolean canBeEmpty(GenInfo info) {
        return ( generator instanceof StringGenerator );
    }

}
