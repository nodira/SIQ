package symbolicdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

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

	public void addSymbolicRelation(SymbolicRelation rel){
		this.relations.add(rel); 
	}
	
	public static SymbolicDB constructDB(SimpleView lastView){
		SymbolicDB db = new SymbolicDB(lastView.schema);
		
		//find base tables
		List<SimpleView> baseTables = lastView.findBaseTables();
		for(SimpleView baseTable : baseTables){
			db.addSymbolicRelation(baseTable.getSymbolicRelation()); 
		}
		
		return db; 
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("DB: " + this.schema.getDBName() + "\n ---- \n");
		
		for(SymbolicRelation rel: relations){
			sb.append(rel.toString() + "\n -------- \n"); 
		}
	
		
		return sb.toString();
		
	}
	
	
	
	
}
