package queryplan;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import constraints.ComparisonOp;
import constraints.NumericConstraint;
import constraints.VariableConstraint;

import realdb.RealTuple;
import realdb.RealValue.DoubleValue;
import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicTuple;
import symbolicdb.Tuple;
import symbolicdb.Variable;


public class GroupbyCountOperator extends QueryOperator.UnaryQueryOperator{
	public List<Integer> groupbyColumns; 
	
	public GroupbyCountOperator(QueryOperator underlyingOperator, QueryPlan queryPlan, int... groupbyColumns){
		List<Integer> groupbyColsList = new ArrayList<Integer>(); 
		for(Integer groupbyColumn : groupbyColumns){
			groupbyColsList.add(groupbyColumn); 
		}
		this.underlyingOperator = underlyingOperator; 
		this.groupbyColumns = groupbyColsList; 
		this.queryPlan = queryPlan; 
		constructSchema(); 
	}
	
	public GroupbyCountOperator(QueryOperator underlyingOperator, List<Integer> groupbyColumns){
		this.underlyingOperator = underlyingOperator;
		this.groupbyColumns = groupbyColumns; 
		constructSchema(); 
		
	}
	
	private void constructSchema(){
		//construct schema
		this.currentSchema = new RelationSchema(""); 
		for(Integer groupbyColumn: groupbyColumns){
			this.currentSchema.addAttribute(underlyingOperator.currentSchema.getAttribute(groupbyColumn).columnName()); 
		}
		this.currentSchema.addAttribute("g_count"); 
	}
	
	@Override
	public void update(boolean print) {
		underlyingOperator.update(print); 
		this.setIntermediateResults(applyOperator(underlyingOperator.getIntermediateResults()));	
	}
	
	@Override
	public List<Tuple> applyOperator(List<Tuple> tuples){
		List<Tuple> result = new ArrayList<Tuple>(); 
		
		Hashtable<String, Integer> groupToCount = new Hashtable<String, Integer>(); 
		for(Tuple t : tuples){
			//construct group string
			StringBuilder sb = new StringBuilder();
			for(Integer groupbyColumn : groupbyColumns){
				sb.append(t.getColumn(groupbyColumn) + ","); 
			}
			
			String group = sb.toString(); 
			
			//if its not in hashtable - add it and add tuple to intermediate results
			if(groupToCount.containsKey(group) == false){
				groupToCount.put(group, 0); 
				
				Tuple groupCount = t.constructNewTupleWithSchema(currentSchema); 
				for(int i=0; i<groupbyColumns.size(); i++){
					groupCount.setColumn(i, t.getColumn(groupbyColumns.get(i))); 
				}
				result.add(groupCount); 
			}
			
			//now it is definitely in hashtable. so increase the count 
			groupToCount.put(group, groupToCount.get(group) + 1); 
			
		}
	
		//iterate through intermediate results and fill in the count column from the hashtable
		for(Tuple t : result){
			//construct group string
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<t.arity()-1; i++){
				sb.append(t.getColumn(i) + ","); 
			}
			String group = sb.toString(); 
			int groupCount = groupToCount.get(group); 
			
			if(t instanceof SymbolicTuple){
				Variable countVar = new Variable(currentSchema.getAttribute(currentSchema.size()-1)); 
				countVar.addConstraint(new NumericConstraint(new ComparisonOp.EQUALS(), groupCount)); 
				t.setColumn(currentSchema.size()-1, countVar); 
			}else if (t instanceof RealTuple){
				DoubleValue countVar = new DoubleValue(groupCount);
				t.setColumn(currentSchema.size()-1, countVar); 
			}
		}
		
		return result; 
	}

	@Override
	public void replaceVariableV1WithV2(Variable v1, Variable v2) {
		localVariableRenaming(v1, v2);
		underlyingOperator.replaceVariableV1WithV2(v1, v2); 	
	}
	
	public String groupByColsString(){
		StringBuilder sb = new StringBuilder(); 
		
		for(Integer groupbyCol : groupbyColumns){
			sb.append(currentSchema.getAttribute(groupbyCol) + ", ");  
		}
		 
		if(sb.length() > 0){
			sb.delete(sb.length() - 2, sb.length()); 
		}
		
		return sb.toString(); 
	}
	
	//-------------- specific to SymbolicDB ----------------
	

	@Override
	public List<SymbolicTuple> translateToAtomicAdds(SymbolicTuple... tuples) {
		SymbolicTuple[] requests = ungrouped(tuples); 
		return underlyingOperator.translateToAtomicAdds(requests); 
	}
	
	private SymbolicTuple[] ungrouped(SymbolicTuple... tuples){
		List<SymbolicTuple[]> ungroups = new ArrayList<SymbolicTuple[]>();
		int totalCount = 0;

		for(SymbolicTuple tuple : tuples){
			int count = -1; 
			//it must either be a fixed value or a >= value. 
			if(tuple.getColumn(currentSchema.size()-1).hasDoubleValue() == false){
				List<VariableConstraint> constraints = tuple.getColumn(currentSchema.size()-1).getConstraints();
				if(constraints.size() == 1){
					VariableConstraint constraint = constraints.get(0);
					
					if(constraint.getOp() instanceof ComparisonOp.EQUALS || 
					   constraint.getOp() instanceof ComparisonOp.GEQ ||
					   constraint.getOp() instanceof ComparisonOp.GT){
						count = (int) Double.parseDouble(constraint.stringValue()); 
					}
					
				}	
			}
			
			if(count == -1){
				System.err.println("For groupby requests - the count must be specified as =, >=, or >. \n" +
						"But continuing anyways without satisfying this request.. ");
				return new SymbolicTuple[]{}; 
			}
			
			//int count = (int) (tuple.getColumn(currentSchema.size()-1).getDoubleValue()); 
			SymbolicTuple[] requests = new SymbolicTuple[count];


			for(int tupleCount = 0; tupleCount < count; tupleCount++){
				SymbolicTuple unprojectedT = new SymbolicTuple(underlyingOperator.currentSchema); 
				for(int i=0; i<groupbyColumns.size(); i++){
					unprojectedT.setColumn(groupbyColumns.get(i), tuple.getColumn(i)); 
				}
				//fill the nulls with new variables
				for(int i=0; i<unprojectedT.arity(); i++){
					if(unprojectedT.getColumn(i) == null){
						Variable v = new Variable(unprojectedT.underlyingSchema().getAttribute(i)); 
						unprojectedT.setColumn(i, v); 
					}
				}

				requests[tupleCount] = unprojectedT; 
			}

			totalCount += count;
			ungroups.add(requests); 
		}

		SymbolicTuple[] allRequests = new SymbolicTuple[totalCount];
		int i=0; 
		for(SymbolicTuple[] requests : ungroups){
			for(SymbolicTuple request : requests){
				allRequests[i++] = request; 
			}
		}

		return allRequests; 

	}




}
