package schema;

import java.util.ArrayList;
import java.util.List;

public final class RelationSchema {
	String 		   		 relationName; 
	List<ColumnSchema>   attributes; 
	
	
	public RelationSchema(String relationName){
		this.relationName   = relationName.toLowerCase(); 
		this.attributes 	= new ArrayList<ColumnSchema>(); 
		
	}
	
	public void addAttribute(String attrName){
		attributes.add(new ColumnSchema(this, attrName)); 
	}
	
	public void addAttribute(String attrName, boolean isKey){
		attributes.add(new ColumnSchema(this, attrName, isKey)); 
	}
	
	public void addKeyAttribute(String attribute){
		attributes.add(new ColumnSchema(this, attribute, true)); 
	}
	
	public String getRelationName(){
		return relationName; 
	}
	
	public int getAttributeIndex(String attribute){
		for(int i=0; i<attributes.size(); i++){
			if(attributes.get(i).columnName().equals(attribute)){
				return i; 
			}
		}
		return -1; 
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
	
	public ColumnSchema getAttribute(String attrName){
		return getAttribute(getAttributeIndex(attrName)); 
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
    
    public static RelationSchema cartesianProduct(RelationSchema r1, RelationSchema r2){
		RelationSchema schema = new RelationSchema(r1.getRelationName() + "_x_" + r2.getRelationName() );
		
		List<ColumnSchema> attrs = r1.getAttributes(); 
		for(ColumnSchema attr: attrs){
			schema.addAttribute(attr + "_1"); 
		}
		attrs = r2.getAttributes(); 
		for(ColumnSchema attr: attrs){
			schema.addAttribute(attr + "_2"); 
		}
		
		return schema; 
	}
    
    public String toString(){
    	StringBuilder sb = new StringBuilder();
    	sb.append(relationName + "("); 
    	
    	for(ColumnSchema c : attributes){
    		sb.append(c.columnName() + ","); 
    	}
    	if(sb.charAt(sb.length()-1) == ','){
    		sb.deleteCharAt(sb.length()-1); 
    	}
    	sb.append(")"); 
    	
    	
    	return sb.toString(); 
    	
    }
}
