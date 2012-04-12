package queryplan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;


public class Configuration implements Comparable<Configuration>{
	List<AddAction> addActions;
	
	
	public Configuration(List<AddAction> addActions){
		this.addActions = addActions; 
	}
	
	public boolean hasRepeatedMergeCandidates(){
		HashSet<SymbolicTuple> mergeCandidates = new HashSet<SymbolicTuple>();
		for(AddAction a : addActions){
			if(a.mergeCandidate == null){
				continue; 
			}else{
				if(mergeCandidates.contains(a.mergeCandidate)){
					return true; 
				}
				mergeCandidates.add(a.mergeCandidate); 
			}
		}
		return false; 
	}
	
	public int numUnmerged(){
		int count = 0; 
		for(AddAction a : addActions){
			if(a.mergeCandidate == null){
				count++; 
			}
		}
		return count; 
	}

	@Override
	public int compareTo(Configuration o) {
		return this.numUnmerged() - o.numUnmerged(); //or the opposite? 
	}
	
	public int size(){
		return addActions.size(); 
	}
	
	public AddAction get(int i){
		return addActions.get(i); 
	}
	
	
	public void replaceV1WithV2(Variable v1, Variable v2){
		for(AddAction a : addActions){
			a.t.replaceV1WithV2(v1, v2);
			if(a.mergeCandidate != null){
				a.mergeCandidate.replaceV1WithV2(v1, v2); 
			}
		}
	}
	
	public Configuration cloneWithMapping(Hashtable<Variable, Variable> v2Clone ){
List<AddAction> newAddActions = new ArrayList<AddAction>(); 
		
		for(AddAction a : addActions){
			SymbolicTuple t1 = a.t.cloneWithColumnSchemas();

			
			for(int i=0; i<t1.arity(); i++){
				Variable v = a.t.getColumn(i); 
				Variable v1 = t1.getColumn(i); 
				if(v2Clone.containsKey(v)==false){
					v2Clone.put(v, v1);
				}
				t1.setColumn(i, v2Clone.get(v)); 
			}
			
			SymbolicTuple m1 = null;
			
			if(a.mergeCandidate != null){
				m1 = a.mergeCandidate.cloneWithColumnSchemas();
				for(int i=0; i<m1.arity(); i++){
					Variable v = a.mergeCandidate.getColumn(i); 
					Variable v1 = m1.getColumn(i); 
					
					if(v2Clone.containsKey(v)==false){
						v2Clone.put(v, v1);
					}
					m1.setColumn(i, v2Clone.get(v)); 
				}
			}
			newAddActions.add(new AddAction(t1, m1));
			
		}
		return new Configuration(newAddActions); 
	}
	public Configuration clone(){
		return cloneWithMapping( new Hashtable<Variable, Variable>()); 
		
	}
	
	public String toString(){
		String s = "[";
		for(AddAction a : addActions){
			s += a + ", "; 
		}
		
		if(addActions.size() > 0){
			s = s.substring(0, s.length()-2);
		}
		
		s += "]";
		
		return s; 
		
		
	}
}

