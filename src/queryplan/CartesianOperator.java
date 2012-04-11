package queryplan;
import java.util.ArrayList;
import java.util.List;

import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicTuple;
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

	public void replaceVariableV1WithV2(Variable v1, Variable v2) {
		localVariableRenaming(v1, v2); 
		o1.replaceVariableV1WithV2(v1, v2); 
		o2.replaceVariableV1WithV2(v1, v2); 
	}

	@Override
	public void update(boolean print) {
		o1.update(print);
		o2.update(print); 
		setIntermediateResults(cartesianProduct(o1.getIntermediateResults(), o2.getIntermediateResults())); 
	}
	
	private List<SymbolicTuple> cartesianProduct(List<SymbolicTuple> tuples1, List<SymbolicTuple> tuples2){
		List<SymbolicTuple> result = new ArrayList<SymbolicTuple>();
		
		for(SymbolicTuple tx : tuples1){
			for(SymbolicTuple ty : tuples2){
				SymbolicTuple t = new SymbolicTuple(currentSchema);
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
	
	private SymbolicTuple[] minimizeList(SymbolicTuple[] l, boolean doClone){
		List<SymbolicTuple> uniqTuples = new ArrayList<SymbolicTuple>();
		for(SymbolicTuple t : l){
			if(doClone){
				t = t.cloneWithColumnSchemas(); 
			}
			
			boolean merged = false;
			for(SymbolicTuple t1 : uniqTuples){
				if(t.canBeMerged(t1)){
					merged = true; 
					t1.merge(t); 
					for(int i=0; i<t.arity(); i++){
						replaceVariableV1WithV2(t.getColumn(i), t1.getColumn(i)); 
					}
					break; 
				}
			}
			if(merged == false){
				uniqTuples.add(t); 
			}
		}
		
		SymbolicTuple[] uniq = new SymbolicTuple[uniqTuples.size()];
		for(int i=0; i<uniqTuples.size(); i++){
			uniq[i] = uniqTuples.get(i); 
		}
		return uniq; 
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

	@Override
	public List<SymbolicTuple> resultOf(List<SymbolicTuple> tuples) {
		List<SymbolicTuple> result1 = o1.resultOf(tuples); 
		System.out.println(result1.get(0).underlyingSchema());
		
		List<SymbolicTuple> result2 = o2.resultOf(tuples); 
		System.out.println(result2.get(0).underlyingSchema());
		
		return cartesianProduct(result1, result2); 
	}

	
}
