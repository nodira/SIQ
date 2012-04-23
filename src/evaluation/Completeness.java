package evaluation;

import constraints.ComparisonOp;
import constraints.NumericConstraint;
import queryplan.CartesianOperator;
import queryplan.FilterOperator;
import queryplan.GroupbyCountOperator;
import queryplan.ProjectOperator;
import queryplan.QueryOperator;
import queryplan.QueryPlan;
import queryplan.TableOperator;
import symbolicdb.Assignment;
import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

public class Completeness implements QualityMetric {
	public Completeness(){
		
	}
	
	public double measureAtNode(QueryOperator op){
		if(op instanceof CartesianOperator){
			//just one tuple
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			if(op.hasTuples(posExample)){
				return 1.0;
			}else{
				return 0.0; 
			}
			
		}else if(op instanceof FilterOperator){
			FilterOperator filterOp = (FilterOperator) op; 
			
			//one negative example - one tuple in underlyingOp that doesnt make it through
			double numFilled = 0;
			double numEquivClasses = 0; 
			
			if(filterOp.unaryConstraint != null){ //indeed a unary constraint
				SymbolicTuple negExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
				Variable failingV = negExample.getColumn(filterOp.unaryConstraint.getColumnName());
				failingV.addConstraint(filterOp.unaryConstraint.getConstraint().negate()); 
				
				numEquivClasses++; 
				if(filterOp.underlyingOperator().hasTuples(negExample)){
					numFilled++; 
				}	
			}
			
			//one positive example
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			if(filterOp.hasTuples(posExample)){
				numFilled++;
			}
			numEquivClasses++; 
			
			return numFilled / numEquivClasses; 
		}else if(op instanceof GroupbyCountOperator){
			SymbolicTuple tuple1 = SymbolicTuple.constructTupleWithNewVariables(((GroupbyCountOperator) op).underlyingOperator().schema()); 
			SymbolicTuple tuple2 = SymbolicTuple.constructTupleWithNewVariables(((GroupbyCountOperator) op).underlyingOperator().schema()); 
			for(Integer groupbyColumn : ((GroupbyCountOperator) op).groupbyColumns){
				tuple2.setColumn(groupbyColumn, tuple1.getColumn(groupbyColumn));
			}
			
			if(((GroupbyCountOperator) op).underlyingOperator().hasTuples(tuple1, tuple2)){
				return 1;
			}else{
				return 0; 
			}
						
		}else if(op instanceof ProjectOperator){
			//just one tuple
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			if(op.hasTuples(posExample)){
				return 1.0;
			}else{
				return 0; 
			}
		}else if(op instanceof TableOperator){
			SymbolicTuple posExample = SymbolicTuple.constructTupleWithNewVariables(op.schema()); 
			if(op.hasTuples(posExample)){
				return 1.0;
			}else{
				return 0; 
			}
		}else{
			throw new RuntimeException("Code does not support " + op.getClass().getCanonicalName()); 
		}
		
		
	}
	
}
