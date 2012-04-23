package queryplan;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import realdb.GeneralDB;
import realdb.GeneralRelation;
import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Tuple;
import symbolicdb.Variable;


public class TableOperator extends QueryOperator{
	
	private GeneralDB underlyingDB; 
	private GeneralRelation underlyingRelation; 
	
	
	public TableOperator(GeneralDB underlyingDB, String relationName, QueryPlan queryPlan){
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
	public void update(boolean print) {
		this.setIntermediateResults(new ArrayList<Tuple>());
		for(Tuple t : getUnderlyingRelation().getTuples()){
			Tuple tRenamed = t.reconstructForNewSchema(currentSchema); 
			this.getIntermediateResults().add(tRenamed);  
		}	
	}
	
	@Override
	public List<Tuple> resultOf(List<Tuple> tuples) {
		List<Tuple> tuplesForThisTable = new ArrayList<Tuple>();
		for(Tuple tuple : tuples){
			if(tuple.underlyingSchema() == underlyingRelation.relationSchema()){
				tuplesForThisTable.add(tuple.reconstructForNewSchema(currentSchema));
			}
		}
		return tuplesForThisTable; 
	}
	
	public GeneralRelation getUnderlyingRelation() {
		return underlyingRelation;
	}

	//-------------- specific to SymbolicDB ----------------
	
	@Override
	public void replaceVariableV1WithV2(Variable v1, Variable v2) {
		localVariableRenaming(v1, v2); 
		((SymbolicDB)underlyingDB).replaceVariableV1WithV2(v1, v2); 
	}

	//this is BEST EFFORT. if we cant add all - add some subset. 
	//@Override
	public List<SymbolicTuple> translateToAtomicAdds(SymbolicTuple... tuples) {
		List<SymbolicTuple> atomicAdds = new ArrayList<SymbolicTuple>();
		for(SymbolicTuple tuple : tuples){
			SymbolicTuple renamedTuple = (SymbolicTuple) tuple.reconstructForNewSchema(underlyingRelation.relationSchema());  
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

	
	
	
	
	
}
