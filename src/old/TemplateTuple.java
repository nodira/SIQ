package old;

import schema.RelationSchema;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

public class TemplateTuple extends SymbolicTuple{

	public TemplateTuple(RelationSchema underlyingSchema) {
		super(underlyingSchema);	
	}
	
	public TemplateTuple clone(){
		TemplateTuple t = new TemplateTuple(super.underlyingSchema); 
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

}
