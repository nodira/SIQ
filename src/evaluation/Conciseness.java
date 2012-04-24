package evaluation;

import queryplan.CartesianOperator;
import queryplan.FilterOperator;
import queryplan.GroupbyCountOperator;
import queryplan.ProjectOperator;
import queryplan.QueryOperator;
import queryplan.TableOperator;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

public class Conciseness extends QualityMetric{

	public Conciseness(){
	}
	
	
	@Override
	public double measureAtNode(QueryOperator op) {
		if(op instanceof CartesianOperator){
			return conciseness(1, op.numTuples()); 
		}else if(op instanceof FilterOperator){
			FilterOperator filterOp = (FilterOperator) op; 
			if(filterOp.unaryConstraint != null){ //indeed a unary constraint
				return conciseness(2, filterOp.underlyingOperator().numTuples()); 
			}else{
				return conciseness(1, filterOp.underlyingOperator().numTuples());
			}
			 
		}else if(op instanceof GroupbyCountOperator){
			return conciseness(1, op.numTuples()); 
			
		}else if(op instanceof ProjectOperator){
			return conciseness(1, op.numTuples()); 
			
		}else if(op instanceof TableOperator){
			return conciseness(1, op.numTuples()); 
		}else{
			throw new RuntimeException("Code does not support " + op.getClass().getCanonicalName()); 
		}
	
	}
	
	private double conciseness(int numEquivClasses, int numTuples){
		if(numTuples >= numEquivClasses){
			return ((double) numEquivClasses) / ((double) numTuples); 
		}else{
			return 1.0; 
		}
		
	}

	
	
}
