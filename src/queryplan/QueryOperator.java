package queryplan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import edu.washington.db.cqms.common.sqlparser.Pair;
import schema.RelationSchema;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;


public abstract class  QueryOperator  {
	QueryPlan queryPlan; 
	RelationSchema currentSchema; 
	
	private List<SymbolicTuple> intermediateResults = new ArrayList<SymbolicTuple>();
	
	//returns the result of having the argument tuples as base tuples
	public abstract List<SymbolicTuple> resultOf(List<SymbolicTuple> tuples); 
	
	public abstract void update(boolean print); 
	
	public void request(SymbolicTuple... requests){
		List<SymbolicTuple> tuplesToAdd = translateToAtomicAdds(requests);
		List<List<AddAction>> candidateLists = new ArrayList<List<AddAction>>();
		
		//for each tupleToAdd t - we want to construct find merge candidates c1, ..., ck and 
		//construct [c1, ..., ck, ]  
		for(SymbolicTuple t : tuplesToAdd){
			List<SymbolicTuple> mergeCandidates = queryPlan.db().allTuplesThatMergeWith(t);
			List<AddAction> addActionCandidates = new ArrayList<AddAction>();
			for(SymbolicTuple mergeCandidate : mergeCandidates){
				addActionCandidates.add(new AddAction(t, mergeCandidate)); 
			}
			addActionCandidates.add(new AddAction(t, null)); 
			candidateLists.add(addActionCandidates); 
		}
		
		//construct configurations
		List<List<AddAction>> potentialAddConfigurations = SymbolicTuple.product(candidateLists);
		List<Configuration> configurations = new ArrayList<Configuration>(); 
		for(List<AddAction> config : potentialAddConfigurations){
			Configuration c = new Configuration(config);
			//two tuples cant be merged with the same underlying tuple - so remove configurations with repetitions
			if(c.hasRepeatedMergeCandidates() == false){
				configurations.add(c); 
			}
		}
		
		//sort configurations so that configurations with more merges appear first
		Collections.sort(configurations); 
		
		//iterate through until we find a configuration that works. apply it. 
		for(Configuration config : configurations){
			if(this.queryPlan.db().canBeApplied(config)){
				this.queryPlan.db().applyConfiguration(config);
				return; 
			}
		}
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
	
	public abstract List<SymbolicTuple> translateToAtomicAdds(SymbolicTuple... tuples); 
	
	
	
	//-------------------------------------------------------------------------

	public static abstract class UnaryQueryOperator extends QueryOperator{
		QueryOperator underlyingOperator; 
		
		public QueryOperator underlyingOperator(){
			return underlyingOperator; 
		}
		
		public abstract List<SymbolicTuple> applyOperator(List<SymbolicTuple> resultBelow); 
		
		public List<SymbolicTuple> resultOf(List<SymbolicTuple> tuples) {
			List<SymbolicTuple> resultBelow = underlyingOperator.resultOf(tuples);
			return applyOperator(resultBelow); 
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
