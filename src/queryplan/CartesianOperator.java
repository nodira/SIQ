package queryplan;
import java.util.ArrayList;

import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;


public class CartesianOperator extends QueryOperator.BinaryQueryOperator {
	
	public CartesianOperator(QueryOperator o1, QueryOperator o2){
		this.o1 = o1; 
		this.o2 = o2;
		
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
		
		setIntermediateResults(new ArrayList<SymbolicTuple>()); 
		
		for(SymbolicTuple tx : o1.getIntermediateResults()){
			for(SymbolicTuple ty : o2.getIntermediateResults()){
				SymbolicTuple t = new SymbolicTuple(currentSchema);
				for(int i=0; i<t.arity(); i++){
					if(i < tx.arity()){
						t.setColumn(i, tx.getColumn(i));
					}else{
						t.setColumn(i, ty.getColumn(i-tx.arity())); 
					}
				}
				getIntermediateResults().add(t);
			} 
		}
		
	}



	@Override
	public void request(SymbolicTuple tuple, boolean mustBeNewTuple, boolean print) {
		if(print){
			printDebugInfo(tuple, "request"); 
		}
		
		SymbolicTuple t1 = new SymbolicTuple(o1.currentSchema); 
		SymbolicTuple t2 = new SymbolicTuple(o2.currentSchema); 
		
		for(int i=0; i< tuple.arity(); i++){
			if(i < t1.arity()){
				t1.setColumn(i, tuple.getColumn(i)); 
			}else{
				t2.setColumn(i-t1.arity(), tuple.getColumn(i)); 
				
			}
		}
		
		o1.request(t1, mustBeNewTuple, print);
		o2.request(t2, mustBeNewTuple, print); 
		
		//do we need to check if it exists here? or can this be done at the very bottom level? 
		
		
	}

	
}
