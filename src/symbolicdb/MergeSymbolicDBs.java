package symbolicdb;

import java.util.HashMap;

public class MergeSymbolicDBs {
	public static SymbolicDB mergeDBs(SymbolicDB db1, SymbolicDB db2, Mapping mapping){
		SymbolicDB db = db1.clone(); 
		
		
		
		
		
		
		
		
		return db; 
		
	}
	
	static class Mapping{
		HashMap<Variable, Variable> varToVar = new HashMap<Variable, Variable>();
		
		public Mapping(){
			
		}
		
		public boolean addMapping(Variable v1, Variable v2){
			if(varToVar.containsKey(v1) || varToVar.containsKey(v2)){
				return false;
			}else{
				varToVar.put(v1, v2);
				varToVar.put(v2, v1); 
				return true; 
			}
		}
		
		//construct a mapping
		
		//check if mapping is valid. what is the algorithm exactly? 
		
		
		
	}
	
}
