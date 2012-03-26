package symbolicdbmergers;

import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;

public class UnionMerger implements SymbolicDBMerger {
	
	public UnionMerger(){}
	
	public SymbolicDB merge(SymbolicDB db1, SymbolicDB db2){
		SymbolicDB db1Clone = db1.clone();
		SymbolicDB db2Clone = db2.clone(); 
		
		for(SymbolicRelation rel : db2Clone.relations()){
			//add each tuple into db1Clone
			SymbolicRelation sameRel = db1Clone.getRelation(rel.relationSchema().getRelationName());
			if(sameRel == null){
				sameRel = new SymbolicRelation(rel.relationSchema()); 
				db1Clone.addSymbolicRelation(sameRel); 
			}
			
			for(SymbolicTuple t : rel.getTuples()){
				sameRel.addTuple(t); 
			}
		}
		return db1Clone; 
		
	}
	
}
