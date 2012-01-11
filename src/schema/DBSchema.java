package schema; 

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class DBSchema {
	String 						dbName; 
    Map<String,RelationSchema>  relations;
    ArrayList<SimpleForeignKey> fks; 
	
	public DBSchema(String dbName) {
		this.dbName 	= dbName;
		this.relations 	= new HashMap<String,RelationSchema>(); 
		this.fks 		= new ArrayList<SimpleForeignKey>();
	}
	
	public void setDBName(String dbName){
		this.dbName = dbName; 
	}

      public void add(RelationSchema r) {
        relations.put(r.getRelationName().toLowerCase(),r);
    }
	
	public void addRelation(RelationSchema relation){
		relations.put(relation.getRelationName().toLowerCase(),relation); 
	}
	
	public void addForeignKey(String rel1, String col1, String rel2, String col2){
		fks.add(new SimpleForeignKey(rel1.toLowerCase(), col1.toLowerCase(), rel2.toLowerCase(), col2.toLowerCase())); 
	}
	
	
	public String getDBName(){
		return dbName; 
	}

    public RelationSchema get(String n) {
        return relations.get(n.toLowerCase());
    }

	public List<RelationSchema> getRelations(){
		return new ArrayList<RelationSchema>(relations.values()); 
	}
	
	public List<SimpleForeignKey> getForeignKeys(){
		return fks; 
	}
	
	public static class SimpleForeignKey{
		public String rel1;
		public String rel2;
		public String col1;
		public String col2;
		
		public SimpleForeignKey(String rel1, String col1, String rel2, String col2){
			this.rel1 = rel1.toLowerCase();
			this.col1 = col1.toLowerCase();
			this.rel2 = rel2.toLowerCase();
			this.col2 = col2.toLowerCase(); 
		}
		
		@Override 
		public boolean equals(Object other){
			if(other instanceof SimpleForeignKey){
				SimpleForeignKey o = (SimpleForeignKey) other; 
				return rel1.equals(o.rel1) && rel2.equals(o.rel2) && col1.equals(o.col1) && col2.equals(o.col2);
			}else{
				return false; 
			}
		}
		
		@Override
		public String toString(){
			return rel1 + "." + col1 + "=" + rel2 + "." + col2; 
		}
		
		@Override
		public int hashCode(){
			return toString().hashCode(); 
		}
		
	}
	
}
