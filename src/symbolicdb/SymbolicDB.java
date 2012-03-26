package symbolicdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import old.SimpleView;

import schema.DBSchema;


public class SymbolicDB {
	List<SymbolicRelation> relations = new ArrayList<SymbolicRelation>(); 
	DBSchema schema; 
	
	public SymbolicDB(DBSchema schema){
		this.schema = schema; 
	}
	
	public List<SymbolicRelation> relations(){
		return relations; 
	}
	
	public DBSchema schema(){
		return schema; 
	}
	
	public SymbolicRelation getRelation(String relName){
		for(SymbolicRelation rel: relations){
			if(rel.relationSchema().getRelationName().equals(relName)){
				return rel; 
			}
		}
		return null; 
	}

	public void addSymbolicRelation(SymbolicRelation rel){
		this.relations.add(rel); 
	}
	
	/*public static SymbolicDB constructDB(SimpleView lastView){
		SymbolicDB db = new SymbolicDB(lastView.schema);
		
		//find base tables
		List<SimpleView> baseTables = lastView.findBaseTables();
		for(SimpleView baseTable : baseTables){
			db.addSymbolicRelation(baseTable.getSymbolicRelation()); 
		}
		
		return db; 
	}*/
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("DB: " + this.schema.getDBName() + "\n ---- \n");
		
		for(SymbolicRelation rel: relations){
			sb.append(rel.toString() + "\n -------- \n"); 
		}
	
		
		return sb.toString();
		
	}
	
	public SymbolicDB clone(){
		Hashtable<Variable, Variable> var2VarClone = new Hashtable<Variable, Variable>(); 
		SymbolicDB clone = new SymbolicDB(this.schema); 
		for(SymbolicRelation rel : relations){
			clone.addSymbolicRelation(rel.cloneAccordingToMap(var2VarClone)); 
		}
		return clone; 
	}
	

	
	public String prettyPrint(){
		Hashtable<Variable, Variable> simplifiedVars = new Hashtable<Variable, Variable>();
		
		int numVars = 0; 
		char a = 'a'; 
		for(SymbolicRelation rel: relations){
			for(SymbolicTuple t: rel.getTuples()){
				List<Variable> vars = t.variables(); 
				for(Variable v: vars){
					if(simplifiedVars.containsKey(v) == false){
						simplifiedVars.put(v, v.clone(( ((char)(a + numVars++)) + ""))); 
					}
				}
				
			}
		}
		
		SymbolicDB simplifiedClone = new SymbolicDB(this.schema); 
		for(SymbolicRelation rel : relations){
			simplifiedClone.addSymbolicRelation(rel.cloneAccordingToMap(simplifiedVars)); 
		}
		
		return simplifiedClone.toString(); 
		
	}
	
	
	
}
