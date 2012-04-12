package symbolicdbmergers;

import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;

public class TupleByTupleMerger {

	public TupleByTupleMerger(){
		
	}
	
	public SymbolicDB merge(SymbolicDB db1, SymbolicDB db2){
		SymbolicDB db1Clone = db1.clone();
		SymbolicDB db2Clone = db2.clone(); 
		
		SymbolicDB mergeDB  = new SymbolicDB(db1.schema()); 
		
		
		for(SymbolicRelation rel1 : db1Clone.relations()){
			SymbolicRelation mergeRel = new SymbolicRelation(rel1.relationSchema()); 
			mergeDB.addSymbolicRelation(mergeRel); 
			
			//add each tuple into db1Clone
			SymbolicRelation rel2 = db2Clone.getRelation(rel1.relationSchema().getRelationName());
			for(SymbolicTuple t1 : rel1.getTuples()){
				for(SymbolicTuple t2: rel2.getTuples()){
					if(t1.canBeMerged(t2)){
						System.out.println("can be merged!"); 
						t1.merge(t2);
						rel2.removeTuple(t2); 
						break; 
					}
				}
				mergeRel.addTuple(t1); 
			}
			
			//if any tuples in rel2 are remaining - add them too! 
			for(SymbolicTuple t2 : rel2.getTuples()){
				mergeRel.addTuple(t2); 
			}
		}
		
		//if db2 has relations not in db1
		for(SymbolicRelation rel2 : db2Clone.relations()){
			if(mergeDB.getRelation(rel2.relationSchema().getRelationName()) == null){
				SymbolicRelation mergeRel = new SymbolicRelation(rel2.relationSchema()); 
				mergeDB.addSymbolicRelation(mergeRel); 
				
				for(SymbolicTuple t2 : rel2.getTuples()){
					mergeRel.addTuple(t2); 
				}
			}
		
		}
		
		return mergeDB; 
		
	}
}
