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
	
	public void addForeignKey(ColumnSchema col1, ColumnSchema col2){
		fks.add(new SimpleForeignKey(col1, col2)); 
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
		ColumnSchema col1;
		ColumnSchema col2; 
		
		public SimpleForeignKey(ColumnSchema col1, ColumnSchema col2){
			this.col1 = col1;
			this.col2 = col2; 
		}
		
		public ColumnSchema col1(){
			return col1; 
		}
		
		public ColumnSchema col2(){
			return col2; 
		}
		
		@Override 
		public boolean equals(Object other){
			if(other instanceof SimpleForeignKey){
				SimpleForeignKey o = (SimpleForeignKey) other; 
				return col1.equals(o.col1) && col2.equals(o.col2);
			}else{
				return false; 
			}
		}
		
		@Override
		public String toString(){
			return col1 + "=" + col2; 
		}
		
		@Override
		public int hashCode(){
			return toString().hashCode(); 
		}
		
	}
	
}
