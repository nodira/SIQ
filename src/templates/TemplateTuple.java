package templates;

import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

public class TemplateTuple {

	SymbolicTuple t;
	String relationName; 
	
	public TemplateTuple(String relationName, SymbolicTuple t){
		this.relationName = relationName; 
		this.t = t; 
	}
	
	public TemplateTuple(String relationName, Variable... variables){
		this.relationName = relationName; 
		this.t = new SymbolicTuple(null, variables); 
	}
	
	public SymbolicTuple getSymbolicTuple(){
		return t; 
	}
	
	public String getRelationName(){
		return relationName; 
	}
	
	public TemplateTuple clone(){
		return new TemplateTuple(relationName, t.clone()); 
	}
	
	public static TemplateTuple constructTupleWithNewVariables(String relationName, int arity){
		return new TemplateTuple(relationName, SymbolicTuple.constructTupleWithNewVariables(null, arity)); 
	}

}
