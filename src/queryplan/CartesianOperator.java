package queryplan;
import java.util.ArrayList;
import java.util.List;

import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicTuple;
import symbolicdb.Tuple;
import symbolicdb.Variable;


public class CartesianOperator extends QueryOperator.BinaryQueryOperator {
	
	public CartesianOperator(QueryOperator o1, QueryOperator o2, QueryPlan queryPlan){
		this.o1 = o1; 
		this.o2 = o2;
		this.queryPlan = queryPlan; 
		
		//construct schema
		this.currentSchema = new RelationSchema(""); 
		for(ColumnSchema cs : o1.currentSchema.getAttributes()){
			this.currentSchema.addAttribute(cs.columnName()); 
		}
		for(ColumnSchema cs : o2.currentSchema.getAttributes()){
			this.currentSchema.addAttribute(cs.columnName()); 
		}
		
		
	}
	
	@Override
	public void update(boolean print) {
		o1.update(print);
		o2.update(print); 
		setIntermediateResults(cartesianProduct(o1.getIntermediateResults(), o2.getIntermediateResults())); 
	}
	
	private List<Tuple> cartesianProduct(List<Tuple> tuples1, List<Tuple> tuples2){
		List<Tuple> result = new ArrayList<Tuple>();
		
		for(Tuple tx : tuples1){
			for(Tuple ty : tuples2){
				Tuple t = tx.constructNewTupleWithSchema(currentSchema);
				for(int i=0; i<t.arity(); i++){
					if(i < tx.arity()){
						t.setColumn(i, tx.getColumn(i));
					}else{
						t.setColumn(i, ty.getColumn(i-tx.arity())); 
					}
				}
				result.add(t);
			} 
		}
		return result; 
	}

	@Override
	public List<Tuple> resultOf(List<Tuple> tuples) {
		List<Tuple> result1 = o1.resultOf(tuples); 
		System.out.println(result1.get(0).underlyingSchema());
		
		List<Tuple> result2 = o2.resultOf(tuples); 
		System.out.println(result2.get(0).underlyingSchema());
		
		return cartesianProduct(result1, result2); 
	}
	
	//-------------- specific to SymbolicDB ----------------
	

	public void replaceVariableV1WithV2(Variable v1, Variable v2) {
		localVariableRenaming(v1, v2); 
		o1.replaceVariableV1WithV2(v1, v2); 
		o2.replaceVariableV1WithV2(v1, v2); 
	}

	@Override
	public List<SymbolicTuple> translateToAtomicAdds(SymbolicTuple... tuples) {
		List<SymbolicTuple> atomicAdds = new ArrayList<SymbolicTuple>();
		
		SymbolicTuple[] lefts  = leftsOrRights(true, tuples);
		SymbolicTuple[] rights = leftsOrRights(false, tuples);
		
		atomicAdds.addAll(o1.translateToAtomicAdds(lefts));
		atomicAdds.addAll(o2.translateToAtomicAdds(rights));
		
		return atomicAdds; 
		
	}
	
	private SymbolicTuple[] leftsOrRights(boolean getLefts, SymbolicTuple[] tuples){
		SymbolicTuple[] sideTuples = new SymbolicTuple[tuples.length]; 
		
		for(int x=0; x<tuples.length; x++){
			SymbolicTuple tuple = tuples[x]; 
			
			SymbolicTuple t1 = new SymbolicTuple(o1.currentSchema); 
			SymbolicTuple t2 = new SymbolicTuple(o2.currentSchema); 
			
			for(int i=0; i< tuple.arity(); i++){
				if(i < t1.arity()){
					t1.setColumn(i, tuple.getColumn(i)); 
				}else{
					t2.setColumn(i-t1.arity(), tuple.getColumn(i)); 
					
				}
			}
			
			if(getLefts==true){
				sideTuples[x] = t1;
			}else{
				sideTuples[x] = t2; 
			}
		}
		
		return sideTuples; 
	}

	
	
	
}
