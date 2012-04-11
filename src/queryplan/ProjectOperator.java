package queryplan;
import java.util.ArrayList;
import java.util.List;

import schema.RelationSchema;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;


public class ProjectOperator extends QueryOperator.UnaryQueryOperator{
	
	List<Integer> selectedColumns; 
	
	public ProjectOperator(QueryOperator underlyingOperator, List<Integer> selectedColumns, QueryPlan queryPlan){
		this.underlyingOperator = underlyingOperator; 
		this.selectedColumns = selectedColumns; 
		this.queryPlan = queryPlan;
		this.currentSchema = new RelationSchema(""); 
		for(Integer selectedColumn: selectedColumns){
			this.currentSchema.addAttribute(underlyingOperator.currentSchema.getAttribute(selectedColumn).columnName()); 
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
			SymbolicTuple projectedT = new SymbolicTuple(currentSchema); 
			for(int i=0; i<selectedColumns.size(); i++){
				projectedT.setColumn(i, t.getColumn(selectedColumns.get(i))); 
			}
			result.add(projectedT); 
		}	
		return result; 
	}
	
	private SymbolicTuple[] unprojected(SymbolicTuple[] tuples){
		SymbolicTuple[] unprojected = new SymbolicTuple[tuples.length];
		
		for(int x=0; x < tuples.length; x++){
			SymbolicTuple tuple = tuples[x];
			
			SymbolicTuple unprojectedT = new SymbolicTuple(underlyingOperator.currentSchema); 
			for(int i=0; i<selectedColumns.size(); i++){
				unprojectedT.setColumn(selectedColumns.get(i), tuple.getColumn(i)); 
			}
			//fill the nulls with new variables
			for(int i=0; i<unprojectedT.arity(); i++){
				if(unprojectedT.getColumn(i) == null){
					Variable v = new Variable(unprojectedT.underlyingSchema().getAttribute(i)); 
					unprojectedT.setColumn(i, v); 
				}
			}
			unprojected[x] = unprojectedT; 
		}
		
		return unprojected; 
	}

	@Override
	public void replaceVariableV1WithV2(Variable v1, Variable v2) {
		localVariableRenaming(v1, v2); 
		underlyingOperator.replaceVariableV1WithV2(v1, v2); 
	}
	
	public String selectedColumnsString(){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<currentSchema.size(); i++){
			sb.append(currentSchema.getAttribute(i) + ", "); 
		}
		
		if(sb.length() > 0){
			sb.delete(sb.length() - 2, sb.length()); 
		}
		return sb.toString(); 
	}

	@Override
	public List<SymbolicTuple> translateToAtomicAdds(SymbolicTuple... tuples) {
		SymbolicTuple[] requests = unprojected(tuples); 
		return underlyingOperator.translateToAtomicAdds(requests); 
	}

	
	

	
	
	
}
