package queryplan;

import java.util.List;

import constraints.BinaryConstraint;
import constraints.ComparisonOp;
import constraints.NumericConstraint;
import constraints.UnaryConstraint;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

public class QueryPlanIllustrator {
	
	public QueryPlanIllustrator(){
		
	}
	
	public void illustrate(QueryPlan qp){
		//take root operator. illustrate it. 
		
		//iterate to lower nodes. if part of it is not already illustrated - illustrate.
		//recurse.
		
		illustrate(qp.root()); 
		
		
		
	}

	int numops = 0; 
	
	private void illustrate(QueryOperator op){
		if(op instanceof CartesianOperator){
			//just one tuple
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			op.request(posExample); 
			op.update(false); 
			
		}else if(op instanceof FilterOperator){
			FilterOperator filterOp = (FilterOperator) op; 
			
			//one negative example - one tuple in underlyingOp that doesnt make it through
			QueryOperator underlying = filterOp.underlyingOperator(); 
			if(filterOp.unaryConstraint != null){ //indeed a unary constraint
				SymbolicTuple negExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
				Variable failingV = negExample.getColumn(filterOp.unaryConstraint.getColumnName());
				failingV.addConstraint(filterOp.unaryConstraint.getConstraint().negate()); 
				underlying.request(negExample); 
				underlying.update(false); 
			}
			
			
			//one positive example
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			op.request(posExample); 
			op.update(false); 
			
		}else if(op instanceof GroupbyCountOperator){
			SymbolicTuple groupOfTwo = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			groupOfTwo.getColumn(1).addConstraint(new NumericConstraint(new ComparisonOp.EQUALS(), 2));
			op.request(groupOfTwo);
			op.update(false);
						
		}else if(op instanceof ProjectOperator){
			//just one tuple
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			op.request(posExample); 
			op.update(false); 
		}else if(op instanceof TableOperator){
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			op.request(posExample); 
			op.update(false); 
		}
		
		//recursively illustrate
		if(op instanceof QueryOperator.UnaryQueryOperator){
			illustrate(((QueryOperator.UnaryQueryOperator) op).underlyingOperator());
		}else if(op instanceof QueryOperator.BinaryQueryOperator){
			illustrate(((QueryOperator.BinaryQueryOperator) op).o1);
			illustrate(((QueryOperator.BinaryQueryOperator) op).o2); 
		}
		
		
	
	}

	public void testTranslate(QueryOperator op){
		if(op instanceof CartesianOperator){
			//just one tuple
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			printTranslation(posExample, op); 
		}else if(op instanceof FilterOperator){
			FilterOperator filterOp = (FilterOperator) op; 
			
			//one negative example - one tuple in underlyingOp that doesnt make it through
			QueryOperator underlying = filterOp.underlyingOperator(); 
			if(filterOp.unaryConstraint != null){ //indeed a unary constraint
				SymbolicTuple negExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
				Variable failingV = negExample.getColumn(filterOp.unaryConstraint.getColumnName());
				failingV.addConstraint(filterOp.unaryConstraint.getConstraint().negate()); 
				
				printTranslation(negExample, underlying); 
			}
			
			//one positive example
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			printTranslation(posExample, op); 
			
			
		}else if(op instanceof GroupbyCountOperator){
			SymbolicTuple groupOfTwo = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			groupOfTwo.getColumn(1).addConstraint(new NumericConstraint(new ComparisonOp.EQUALS(), 2));
			printTranslation(groupOfTwo, op); 
		}else if(op instanceof ProjectOperator){
			//just one tuple
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			
			printTranslation(posExample, op); 
			
			
		}else if(op instanceof TableOperator){
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			printTranslation(posExample, op); 
		}
		
		//recursively illustrate
		if(op instanceof QueryOperator.UnaryQueryOperator){
			testTranslate(((QueryOperator.UnaryQueryOperator) op).underlyingOperator());
		}else if(op instanceof QueryOperator.BinaryQueryOperator){
			testTranslate(((QueryOperator.BinaryQueryOperator) op).o1);
			testTranslate(((QueryOperator.BinaryQueryOperator) op).o2); 
		}
		
		
	
	}
	
	private void printTranslation(SymbolicTuple requested, QueryOperator op){
		List<SymbolicTuple> atomicAdds = op.translateToAtomicAdds(requested);
		System.out.println(requested + "@" + op.getClass().getSimpleName() + " translates to:"); 
		for(SymbolicTuple t : atomicAdds){
			System.out.println("  " + t + "(" +  t.underlyingSchema().getRelationName() + ")"); 
		}
		System.out.println("-------"); 
	}
	
	
	
}

