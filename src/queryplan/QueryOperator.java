package queryplan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import edu.washington.db.cqms.common.sqlparser.Pair;
import realdb.RealTuple;
import schema.RelationSchema;
import symbolicdb.Assignment;
import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicTuple;
import symbolicdb.Tuple;
import symbolicdb.Variable;


public abstract class  QueryOperator  {
	QueryPlan queryPlan; 
	RelationSchema currentSchema; 
	
	private List<Tuple> intermediateResults = new ArrayList<Tuple>();
	
	//updates the intermediateResults of this node (but first updates underlying ops)
	public abstract void update(boolean print); 
	
	//returns the result of having the argument tuples as base tuples
	public abstract List<Tuple> resultOf(List<Tuple> tuples); 
	
	
	public int numTuples(){
		return intermediateResults.size(); 
	}
	
	protected String intermediateResultsString(){
		StringBuilder sb = new StringBuilder();
		for(Tuple t : getIntermediateResults()){
			sb.append(t.toString() + "\n"); 
		}
		return sb.toString(); 
		
	}
	
	public RelationSchema schema(){
		return currentSchema; 
	}
	
	public void setIntermediateResults(List<Tuple> intermediateResults) {
		this.intermediateResults = intermediateResults;
	}

	public List<Tuple> getIntermediateResults() {
		return intermediateResults;
	}
	
	
	public boolean hasTuples(SymbolicTuple... tuples){
		List<SymbolicTuple> symbolicTuples = new ArrayList<SymbolicTuple>();
		for(SymbolicTuple t : tuples){
			symbolicTuples.add(t); 
		}
		List<RealTuple> realTuples = new ArrayList<RealTuple>();
		for(Tuple t : intermediateResults){
			realTuples.add((RealTuple) t); 
		}
		return hasTuples(realTuples, symbolicTuples, new Assignment()); 
	}
	
	private boolean hasTuples(List<RealTuple> realTuples, List<SymbolicTuple> symbolicTuples, Assignment asg){
		if(symbolicTuples.isEmpty()){
			return true; 
		}else{
			SymbolicTuple st = symbolicTuples.get(0); 
			for(RealTuple rt : realTuples){
				if(rt.matches(st)){
					Assignment st2rt = rt.makeAssignment(st); 
					if(asg.consistentWith(st2rt)){
						Assignment combinedAsg = asg.combineWith(st2rt);
						List<RealTuple> rtsMinusRt = new ArrayList<RealTuple>();
						rtsMinusRt.addAll(realTuples);
						rtsMinusRt.remove(rt); 
						if(hasTuples(rtsMinusRt, symbolicTuples.subList(1, symbolicTuples.size()), combinedAsg)){
							return true; 
						}
						
					}
				}
			}
			return false; 
			
		}
		
	}
	
	
	//methods that are specific to symbolic-db. 
	
	public abstract void replaceVariableV1WithV2(Variable v1, Variable v2);

	protected void localVariableRenaming(Variable v1, Variable v2){
		for(Tuple t: getIntermediateResults()){
			((SymbolicTuple)t).replaceV1WithV2(v1, v2); 
		}
	}
	
	public abstract List<SymbolicTuple> translateToAtomicAdds(SymbolicTuple... tuples); 
	
	public void request(SymbolicTuple... requests){
		List<SymbolicTuple> tuplesToAdd = translateToAtomicAdds(requests);
		List<List<AddAction>> candidateLists = new ArrayList<List<AddAction>>();
		
		//for each tupleToAdd t - we want to construct find merge candidates c1, ..., ck and 
		//construct [c1, ..., ck, ]  
		for(SymbolicTuple t : tuplesToAdd){
			List<SymbolicTuple> mergeCandidates = ((SymbolicDB) queryPlan.db()).allTuplesThatMergeWith(t);
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
			if(((SymbolicDB)this.queryPlan.db()).canBeApplied(config)){
				((SymbolicDB)this.queryPlan.db()).applyConfiguration(config);
				return; 
			}
		}
	}
	
	
	
	//-------------------------------------------------------------------------

	public static abstract class UnaryQueryOperator extends QueryOperator{
		QueryOperator underlyingOperator; 
		
		public QueryOperator underlyingOperator(){
			return underlyingOperator; 
		}
		
		public abstract List<Tuple> applyOperator(List<Tuple> resultBelow); 
		
		public List<Tuple> resultOf(List<Tuple> tuples) {
			List<Tuple> resultBelow = underlyingOperator.resultOf(tuples);
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
