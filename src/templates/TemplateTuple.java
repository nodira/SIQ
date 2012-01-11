package templates;

import schema.RelationSchema;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

public class TemplateTuple extends SymbolicTuple{

	public TemplateTuple(SymbolicRelation underlyingRelation) {
		super(underlyingRelation);	
	}
	
	public TemplateTuple clone(){
		TemplateTuple t = new TemplateTuple(super.underlyingRelation); 
		for(int i=0; i<arity(); i++){
			t.setColumn(i, this.getColumn(i).clone()); 
		}
		return t; 
	}
	
	public void setColumn(int colIndex, Variable v){
		variables[colIndex] = v; 
		//we remove THIS line coz it's a template tuple. 
		//v.addColumnSchema(underlyingRelation.relationSchema().getAttribute(colIndex)); 
	}

	
	
	/*SymbolicTuple t;
	SymbolicRelation underlyingRelation; 
	
	public TemplateTuple(SymbolicRelation underlyingRelation, SymbolicTuple t){
		this.underlyingRelation = underlyingRelation; 
		this.t = t; 
	}
	
	public TemplateTuple(SymbolicRelation underlyingRelation, Variable... variables){
		this.underlyingRelation = underlyingRelation; 
		this.t = new SymbolicTuple(null, variables); 
	}
	
	public String getRelationName(){
		return underlyingRelation.relationSchema().getRelationName(); 
	}
	
	public TemplateTuple clone(){
		return new TemplateTuple(underlyingRelation, t.clone()); 
	}
	
	public static TemplateTuple constructTupleWithNewVariables(SymbolicRelation underlyingRelation){
		return new TemplateTuple(underlyingRelation, SymbolicTuple.constructTemplateTupleWithNewVariables(underlyingRelation)); 
	}*/

}
