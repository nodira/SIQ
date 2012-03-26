package queryplan;
import java.util.ArrayList;
import java.util.List;

import old.TemplateTuple;

import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;


public class TableOperator extends QueryOperator{
	
	private SymbolicRelation underlyingRelation; 
	
	
	public TableOperator(SymbolicRelation underlyingRelation){
		this.setUnderlyingRelation(underlyingRelation);
		this.currentSchema = new RelationSchema(""); 
		for(ColumnSchema cs : underlyingRelation.relationSchema().getAttributes()){
			this.currentSchema.addAttribute(underlyingRelation.relationSchema().getRelationName() + "_" + cs.columnName()); 
		}
		
		System.out.println("TableOperator: " + currentSchema); 
	}
	
	@Override
	public void replaceVariableV1WithV2(Variable v1, Variable v2) {
		localVariableRenaming(v1, v2); 
		
	}

	@Override
	public void update(boolean print) {
		this.setIntermediateResults(new ArrayList<SymbolicTuple>());
		for(SymbolicTuple t : getUnderlyingRelation().getTuples()){
			this.getIntermediateResults().add(t); 
		}
		
		
	}

	@Override
	public void request(SymbolicTuple tuple, boolean mustBeNewTuple, boolean print) {
		if(print){
			printDebugInfo(tuple, "request"); 
		}
		
		if(mustBeNewTuple == true){
			getUnderlyingRelation().addTuple(tuple); 
			//intermediateResults.add(SymbolicTuple.constructTupleWithNewVariables(currentSchema)); 
		}else{//try to merge with existing tuple
			boolean successfullyMergedWithExistingTuple = false; 
			for(SymbolicTuple t: getUnderlyingRelation().getTuples()){
				if(t.canBeMerged(tuple)){
					t.merge(tuple); 
					for(int i=0; i<t.arity(); i++){
						replaceVariableV1WithV2(tuple.getColumn(i), t.getColumn(i)); 
					}
					successfullyMergedWithExistingTuple = true; 
					break; 
				}
			}
			
			//if nothing matches - add new tuple
			if(successfullyMergedWithExistingTuple == false){
				request(tuple, true, print); 
				//intermediateResults.add(SymbolicTuple.constructTupleWithNewVariables(currentSchema)); 
			}
		}
		
	}

	public void setUnderlyingRelation(SymbolicRelation underlyingRelation) {
		this.underlyingRelation = underlyingRelation;
	}

	public SymbolicRelation getUnderlyingRelation() {
		return underlyingRelation;
	}
	
	
	
	
}
