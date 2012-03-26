package queryplan;
import java.util.ArrayList;
import java.util.List;

import schema.RelationSchema;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;


public class ProjectOperator extends QueryOperator.UnaryQueryOperator{
	
	List<Integer> selectedColumns; 
	
	public ProjectOperator(QueryOperator underlyingOperator, List<Integer> selectedColumns){
		this.underlyingOperator = underlyingOperator; 
		this.selectedColumns = selectedColumns; 
		
		this.currentSchema = new RelationSchema(""); 
		for(Integer selectedColumn: selectedColumns){
			this.currentSchema.addAttribute(underlyingOperator.currentSchema.getAttribute(selectedColumn).columnName()); 
		}
		
		
	}

	@Override
	public void update(boolean print) {
		underlyingOperator.update(print);
		
		this.setIntermediateResults(new ArrayList<SymbolicTuple>()); 
		
		for(SymbolicTuple t : underlyingOperator.getIntermediateResults()){
			SymbolicTuple projectedT = new SymbolicTuple(currentSchema); 
			for(int i=0; i<selectedColumns.size(); i++){
				projectedT.setColumn(i, t.getColumn(selectedColumns.get(i))); 
			}
			getIntermediateResults().add(projectedT); 
		}
	}

	@Override
	public void request(SymbolicTuple tuple, boolean mustBeNewTuple, boolean print) {
		if(print){
			printDebugInfo(tuple, "request"); 
		}
		
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
		underlyingOperator.request(unprojectedT, mustBeNewTuple, print); 
	}

	@Override
	public void replaceVariableV1WithV2(Variable v1, Variable v2) {
		localVariableRenaming(v1, v2); 
		underlyingOperator.replaceVariableV1WithV2(v1, v2); 
	}

	
	
	
}
