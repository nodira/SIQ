package schema;

import java.util.ArrayList;
import java.util.List;

public final class RelationSchema {
	String 		   relationName; 
	List<ColumnSchema>   attributes; 
	
	
	public RelationSchema(String relationName){
		this.relationName   = relationName.toLowerCase(); 
		this.attributes = new ArrayList<ColumnSchema>(); 
		
	}
	
	public void addAttribute(String attribute){
		attributes.add(new ColumnSchema(this, attribute)); 
	}
	
	public void addKeyAttribute(String attribute){
		attributes.add(new ColumnSchema(this, attribute, true)); 
	}
	
	public String getRelationName(){
		return relationName; 
	}

    public boolean contains(String attr) {
        return attributes.contains(attr);
    }

	public List<ColumnSchema> getAttributes(){
		return attributes; 
	}
	
	public ColumnSchema getAttribute(int i){
		return attributes.get(i); 
	}

    public int size() { return attributes.size(); }

    public int hashCode() { return relationName.hashCode(); }

    public boolean equals(Object o) {
        if ( this == o ) return true;
        if ( o instanceof String ) {
            return relationName.equalsIgnoreCase((String)o);
        } else if ( o instanceof RelationSchema ) {
            return relationName.equals( ((RelationSchema)o).relationName );
        }
        return false;
    }
}
