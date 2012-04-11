package queryplan;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;


public class TableOperator extends QueryOperator{
	
	private SymbolicDB underlyingDB; 
	private SymbolicRelation underlyingRelation; 
	
	
	public TableOperator(SymbolicDB underlyingDB, String relationName, QueryPlan queryPlan){
		this.underlyingDB = underlyingDB; 
		this.underlyingRelation = underlyingDB.getRelation(relationName);
		this.queryPlan = queryPlan;
		
		this.currentSchema = new RelationSchema(""); 
		for(ColumnSchema cs : underlyingRelation.relationSchema().getAttributes()){
			this.currentSchema.addAttribute(underlyingRelation.relationSchema().getRelationName() + "_" + cs.columnName()); 
		}
		
		System.out.println("TableOperator: " + currentSchema); 
	}
	
	@Override
	public void replaceVariableV1WithV2(Variable v1, Variable v2) {
		localVariableRenaming(v1, v2); 
		underlyingDB.replaceVariableV1WithV2(v1, v2); 
	}

	@Override
	public void update(boolean print) {
		this.setIntermediateResults(new ArrayList<SymbolicTuple>());
		for(SymbolicTuple t : getUnderlyingRelation().getTuples()){
			SymbolicTuple tRenamed = renameFromUnderlyingSchema(t); 
			this.getIntermediateResults().add(tRenamed);  
		}	
	}

	
	private SymbolicTuple renameFromUnderlyingSchema(SymbolicTuple tuple){
		SymbolicTuple tupleRenamed = SymbolicTuple.constructTupleWithNewVariables(currentSchema);
		for(int i=0; i<tuple.arity(); i++){
			tupleRenamed.setColumn(i, tuple.getColumn(i)); 
		}
		return tupleRenamed; 
	}
	
	private SymbolicTuple renameToUnderlyingSchema(SymbolicTuple tuple){
		SymbolicTuple tupleRenamed = SymbolicTuple.constructTupleWithNewVariables(underlyingRelation.relationSchema());
		for(int i=0; i<tuple.arity(); i++){
			tupleRenamed.setColumn(i, tuple.getColumn(i)); 
		}
		return tupleRenamed; 
	}
	
	private SymbolicTuple addTupleWithRenaming(SymbolicTuple tuple){
		SymbolicTuple tupleRenamed = renameToUnderlyingSchema(tuple); 
		underlyingDB.addTuple(underlyingRelation, tupleRenamed);  
		
		return tupleRenamed; 
	}
	
	public SymbolicRelation getUnderlyingRelation() {
		return underlyingRelation;
	}

	//this is BEST EFFORT. if we cant add all - add some subset. 
	@Override
	public List<SymbolicTuple> translateToAtomicAdds(SymbolicTuple... tuples) {
		List<SymbolicTuple> atomicAdds = new ArrayList<SymbolicTuple>();
		for(SymbolicTuple tuple : tuples){
			SymbolicTuple renamedTuple = renameToUnderlyingSchema(tuple);  
			atomicAdds.add(renamedTuple); 
		}
		
		List<SymbolicTuple> dontAdd = new ArrayList<SymbolicTuple>();
		
		//if breaks primary key? 
		for(int a=0; a<underlyingRelation.arity(); a++){
			if(underlyingRelation.relationSchema().getAttribute(a).isKey()){
				HashSet<Variable> seen = new HashSet<Variable>();
				for(SymbolicTuple t: atomicAdds){
					if(seen.contains(t.getColumn(a))){
						dontAdd.add(t); 
					}else{
						seen.add(t.getColumn(a));
					}
				}
			}
			
		}
		
		atomicAdds.removeAll(dontAdd); 
		return atomicAdds ; 
	}

	@Override
	public List<SymbolicTuple> resultOf(List<SymbolicTuple> tuples) {
		List<SymbolicTuple> tuplesForThisTable = new ArrayList<SymbolicTuple>();
		for(SymbolicTuple tuple : tuples){
			if(tuple.underlyingSchema() == underlyingRelation.relationSchema()){
				tuplesForThisTable.add(renameFromUnderlyingSchema(tuple));
			}
		}
		return tuplesForThisTable; 
	}
	
	
	
	
}
