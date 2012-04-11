package schema;

public class ColumnSchema {

	RelationSchema relationSchema; 
	String columnName;
	boolean isKey; 
	
	public ColumnSchema(RelationSchema relationSchema, String columnName, boolean isKey){
		this.relationSchema = relationSchema; 
		this.columnName = columnName; 
		this.isKey = isKey; 
	}
	
	public ColumnSchema(RelationSchema relationSchema, String columnName){
		this(relationSchema, columnName, false); 
	}
	
	public boolean isKey(){
		return isKey; 
	}
	
	public void setKey(boolean isKey){
		this.isKey = isKey; 
	}
	
	public String columnName(){
		return columnName; 
	}
	
	public String toString(){
		return columnName; 
	}
	
	public String toLongString(){
		return relationSchema.getRelationName() + "." + columnName + (isKey?"[key]":""); 
	}
	
	public RelationSchema relationSchema(){
		return relationSchema; 
	}
}
