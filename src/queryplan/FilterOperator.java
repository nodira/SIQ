package queryplan;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import constraints.BinaryConstraint;
import constraints.UnaryConstraint;
import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;

import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;


public class FilterOperator extends QueryOperator.UnaryQueryOperator{
	F_PredicateInWhere predicate; 
	UnaryConstraint unaryConstraint = null; 
	BinaryConstraint binaryConstraint = null; 
	
	public FilterOperator(QueryOperator underlyingOperator, F_PredicateInWhere predicate, QueryPlan queryPlan){
		this.underlyingOperator = underlyingOperator; 
		this.predicate = predicate; 
		this.queryPlan = queryPlan; 
		
		this.unaryConstraint = UnaryConstraint.constructUnaryConstraint(predicate);
		if(this.unaryConstraint == null){
			this.binaryConstraint = BinaryConstraint.constructBinaryConstraint(predicate); 
		}
		
		//construct schema
		this.currentSchema = new RelationSchema(""); 
		for(ColumnSchema cs : underlyingOperator.currentSchema.getAttributes()){
			this.currentSchema.addAttribute(cs.columnName()); 
		}
	}
	
	
	@Override
	public void update(boolean print) {
		underlyingOperator.update(print);
		this.setIntermediateResults(applyOperator(underlyingOperator.getIntermediateResults()));
	}

	public List<SymbolicTuple> applyOperator(List<SymbolicTuple> tuples){
		List<SymbolicTuple> result = new ArrayList<SymbolicTuple>();
		for(SymbolicTuple t : tuples){
			//if symbolictuple can pass the filter - make it pass
			//AND add the filter 
			if(unaryConstraint!= null){//if its a unary constraint
				Variable v = t.getColumn(unaryConstraint.getColumnName()); 
				if(v.satisfiableWith(unaryConstraint.getConstraint())){
					v.addConstraint(unaryConstraint.getConstraint()); 
					result.add(t); 
				}
			}else{//if its a binary constraint
				Variable v1 = t.getColumn(binaryConstraint.getCol1());
				Variable v2 = t.getColumn(binaryConstraint.getCol2());
				
				if(v1 == v2){
					result.add(t);
				}else if(v1.canBeMerged(v2)){
					v1.merge(v2);
					replaceVariableV1WithV2(v2, v1); 
					t.setColumn(binaryConstraint.getCol2(), v1); 
					result.add(t); 
				}
			}	
		}
		return result; 
	}
	
	private SymbolicTuple[] addPredicateToTuples(boolean print, SymbolicTuple... tuples){
		SymbolicTuple[] requests = new SymbolicTuple[tuples.length];
		
		for(int i=0; i<tuples.length; i++){
			SymbolicTuple tuple = tuples[i]; 
			boolean satisfiedFilter = false; 
			
			if(unaryConstraint!= null){//if its a unary constraint
				Variable v = tuple.getColumn(unaryConstraint.getColumnName()); 
				if(v.satisfiableWith(unaryConstraint.getConstraint())){
					v.addConstraint(unaryConstraint.getConstraint()); 
					satisfiedFilter = true; 
				}
			}else{//if its a binary constraint
				Variable v1 = tuple.getColumn(binaryConstraint.getCol1());
				Variable v2 = tuple.getColumn(binaryConstraint.getCol2());
				
				if(v1.canBeMerged(v2)){
					v1.merge(v2);
					for(SymbolicTuple tt : tuples){
						tt.replaceV1WithV2(v2, v1);
					}
					tuple.setColumn(binaryConstraint.getCol2(), v1); 
					satisfiedFilter = true; 
				}
			}	
			
			if(satisfiedFilter == false){
				throw new RuntimeException("Can not request this tuple because it doesn't pass filter!"); 
			}else{
				requests[i] = tuple; 
			}
		}
		
		return requests; 
	}



	@Override
	public void replaceVariableV1WithV2(Variable v1, Variable v2) {
		localVariableRenaming(v1, v2); 
		underlyingOperator.replaceVariableV1WithV2(v1, v2); 
	}

	public String predicateString(){
		if(unaryConstraint != null){
			return unaryConstraint.toString(); 
		}else{
			return binaryConstraint.toString(); 
		}
	}

	@Override
	public List<SymbolicTuple> translateToAtomicAdds(SymbolicTuple... tuples) {
		return underlyingOperator.translateToAtomicAdds(addPredicateToTuples(false, tuples)); 
	}

	
}
