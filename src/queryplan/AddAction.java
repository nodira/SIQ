package queryplan;

import symbolicdb.SymbolicTuple;

//represents either adding a new tuple, or merging tuple t with existing tuple mergeCandidate. 
public class AddAction{
	public SymbolicTuple t;
	public SymbolicTuple mergeCandidate = null; 
	
	public AddAction(SymbolicTuple t, SymbolicTuple mergeCandidate){
		this.t = t;
		this.mergeCandidate = mergeCandidate; 
	}
	
	public boolean isAMerge(){
		return mergeCandidate != null; 
	}
	
	public String toString(){
		if(this.isAMerge()){
			return "merge-add(" + t + " -> " + mergeCandidate + ")"; 
		}else{
			return "new-add(" + t + ")"; 
		}
		
	}
	
}