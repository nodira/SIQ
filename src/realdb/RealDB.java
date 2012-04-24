package realdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.washington.db.cqms.common.sqlparser.Pair;

import queryplan.AddAction;
import queryplan.Configuration;

import schema.ColumnSchema;
import schema.DBSchema;
import schema.DBSchema.SimpleForeignKey;
import schema.RelationSchema;
import symbolicdb.Assignment;
import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Tuple;


public class RealDB implements GeneralDB{
	List<GeneralRelation> relations = new ArrayList<GeneralRelation>(); 
	DBSchema schema; 
	Hashtable<RelationSchema, RealRelation> schema2Rel = new Hashtable<RelationSchema, RealRelation>(); 
	
	public RealDB(DBSchema schema){
		this.schema = schema; 
		for(RelationSchema rSchema : schema.getRelations()){
			RealRelation rel = new RealRelation(rSchema); 
			this.relations.add(rel); 
			schema2Rel.put(rSchema, rel); 
		}
	}
	
	public List<GeneralRelation> relations(){
		return relations; 
	}
	
	public DBSchema schema(){
		return schema; 
	}
	
	public GeneralRelation getRelation(String relationName){
		for(GeneralRelation rel : relations){
			if(rel.relationSchema().getRelationName().equals(relationName)){
				return rel; 
			}
		}
		return null; 
	}


	private void setRealRelation(RealRelation rel){
		for(int i =0; i<relations.size(); i++){
			if(relations.get(i).relationSchema() == rel.relationSchema()){
				relations.set(i, rel); 
			}
		}
		
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("DB: " + this.schema.getDBName() + "\n ---- \n");
		
		for(GeneralRelation rel: relations){
			sb.append(rel.toString() + "\n -------- \n"); 
		}
	
		
		return sb.toString();
		
	}

		public void addTuple(Tuple t){
		addTuple(getRelation(t.underlyingSchema().getRelationName()), t); 
	}
	
	public void addTuple(GeneralRelation rel, Tuple t){
		rel.addTuple(t); 
		
	}
	
	public static RealDB constructDB(SymbolicDB symbolicdb, Assignment asg){
		RealDB db = new RealDB(symbolicdb.schema());
		
		for(GeneralRelation r : symbolicdb.relations()){
			GeneralRelation rel = db.getRelation(r.relationSchema().getRelationName());
			
			for(Tuple t : r.getTuples()){
				RealTuple tuple = new RealTuple(t.underlyingSchema());
				for(int i=0; i<t.arity(); i++){
					tuple.setColumn(i, asg.valueOf(((SymbolicTuple)t).getColumn(i))); 
				}
				db.addTuple(rel, tuple);  
			}
		}
		
		return db; 
	}

	@Override
	public DBSchema getSchema() {
		return schema; 
	}
	
	
}
