package symbolicdbmergers;

import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;

public class TupleByTupleMerger {

	boolean simplyUnion = false; 
	
	private TupleByTupleMerger(boolean simplyUnion){
		this.simplyUnion = simplyUnion; 
	}
	
	public static TupleByTupleMerger BestEffortTupleByTupleMerger(){
		return new TupleByTupleMerger(false); 
	}
	
	public static TupleByTupleMerger UnionMerger(){
		return new TupleByTupleMerger(true); 
	}
	
	public SymbolicDB merge(SymbolicDB db1, SymbolicDB db2){
		SymbolicDB db1Clone = db1.clone();
		SymbolicDB db2Clone = db2.clone(); 
		
		SymbolicDB mergeDB  = new SymbolicDB(db1.schema()); 
		
		for(SymbolicRelation rel1 : db1Clone.relations()){
			SymbolicRelation mergeRel = mergeDB.getRelation(rel1.relationSchema().getRelationName()); 
			
			//add every tuple from db1Clone
			SymbolicRelation rel2 = db2Clone.getRelation(rel1.relationSchema().getRelationName());
			for(SymbolicTuple t1 : rel1.getTuples()){
				mergeDB.addTuple(mergeRel, t1, false); 
			}
			
			//add every tuple from db2Clone unless it can be merged with some existing tuple
			for(SymbolicTuple t2: rel2.getTuples()){
				boolean mergedIntoTupleFromT1 = false; 
				if(simplyUnion == false){
					for(SymbolicTuple t1 : rel1.getTuples()){
						if(t1.canBeMerged(t2)){
							t1.merge(t2);
							
							for(int i=0; i<t1.arity(); i++){
								db2Clone.replaceVariableV1WithV2(t1.getColumn(i), t2.getColumn(i)); 
								mergeDB.replaceVariableV1WithV2(t1.getColumn(i), t2.getColumn(i)); 
							}
							
							mergedIntoTupleFromT1 = true; 
							break; 
						}
					}
				}
				if(mergedIntoTupleFromT1 == false){
					mergeDB.addTuple(mergeRel, t2, false); 
				}
			}
		}
		
		mergeDB.ensureFKsSatisfied(); 
		return mergeDB; 
		
	}
}
