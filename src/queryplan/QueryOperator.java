package queryplan;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import old.SimpleView;

import edu.washington.db.cqms.snipsuggest.features.F_TableInFrom;
import edu.washington.db.cqms.snipsuggest.features.QueryFeature;
import query.QuerySession;
import schema.DBSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;


public abstract class  QueryOperator  {
	private List<SymbolicTuple> intermediateResults = new ArrayList<SymbolicTuple>();
	RelationSchema currentSchema; 
	
	public abstract void update(boolean print); 

	public void request(SymbolicTuple tuple, boolean mustBeNewTuple){
		request(tuple, mustBeNewTuple, false); 
	} 
	
	public abstract void request(SymbolicTuple tuple, boolean mustBeNewTuple, boolean print);
	
	protected void printDebugInfo(SymbolicTuple tuple, String method){
		System.err.println(method + " @ " + this.getClass().getSimpleName() + ": " + tuple); 
	}
	
	
	public abstract void replaceVariableV1WithV2(Variable v1, Variable v2);
	
	protected void localVariableRenaming(Variable v1, Variable v2){
		for(SymbolicTuple t: getIntermediateResults()){
			t.replaceV1WithV2(v1, v2); 
		}
	}
	
	protected String intermediateResultsString(){
		StringBuilder sb = new StringBuilder();
		for(SymbolicTuple t : getIntermediateResults()){
			sb.append(t.toString() + "\n"); 
		}
		return sb.toString(); 
		
	}
	
	public RelationSchema schema(){
		return currentSchema; 
	}
	
	public void setIntermediateResults(List<SymbolicTuple> intermediateResults) {
		this.intermediateResults = intermediateResults;
	}

	public List<SymbolicTuple> getIntermediateResults() {
		return intermediateResults;
	}

	public static abstract class UnaryQueryOperator extends QueryOperator{
		QueryOperator underlyingOperator; 
		
		public QueryOperator underlyingOperator(){
			return underlyingOperator; 
		}
	}
	
	public static abstract class BinaryQueryOperator extends QueryOperator{
		QueryOperator o1, o2; 
		
		public QueryOperator op1(){
			return o1; 
		}

		public QueryOperator op2(){
			return o2; 
		}
	}
	
	
	
	

	
	
	
}
