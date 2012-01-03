package symbolicdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import edu.washington.db.cqms.common.sqlparser.DBSchema;
import edu.washington.db.cqms.common.sqlparser.RelationSchema;

public class SymbolicDB {
	Hashtable<String, SymbolicRelation> relationNameToRelation = new Hashtable<String, SymbolicRelation>(); 
	List<SymbolicRelation> relations = new ArrayList<SymbolicRelation>(); 
	DBSchema schema; 
	
	
	public SymbolicDB(DBSchema schema){
		this.schema = schema; 
		//create empty symbolic relation for each table
		for(RelationSchema relSchema : schema.getRelations()){
			SymbolicRelation rel = new SymbolicRelation(relSchema.size());
			relations.add(rel);
			relationNameToRelation.put(relSchema.getRelationName(), rel); 
		}
	}
	
	public SymbolicDB clone(){
		HashMap<Variable, Variable> varToNewVar = new HashMap<Variable, Variable>(); 
		SymbolicDB clone = new SymbolicDB(this.schema);
		
		clone.relations = new ArrayList<SymbolicRelation>(); 
		for(String relName: this.relationNameToRelation.keySet()){
			SymbolicRelation relClone = this.relationNameToRelation.get(relName).cloneAccordingToMap(varToNewVar); 
			clone.relations.add(relClone);
			clone.relationNameToRelation.put(relName, relClone); 
		}
		
		return clone; 
	}

	
	
	public static SymbolicDB constructDB(SimpleView lastView){
		SymbolicDB db = new SymbolicDB(lastView.schema);
		
		//find base tables
		
		
		
		
		
		
		//copy over SymbolicRelations? 
		
		//dont we need names of the relations too? maybe not? 
		
		
		
		
		
		
		
		return db; 
	}
	
	
	
	
	
}
