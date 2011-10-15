package symbolicdb;

import java.util.ArrayList;
import java.util.List;

public class SymbolicRelation {
	
	String relationName; 
	int arity; 
	
	List<SymbolicTuple> tuples = new ArrayList<SymbolicTuple>();
	
	public SymbolicRelation(String relationName, int arity){
		this.relationName = relationName; 
		this.arity = arity; 
	}
	
	
	
	
}
