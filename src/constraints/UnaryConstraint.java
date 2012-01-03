package constraints;

public class UnaryConstraint {
	
	String columnName;
	VariableConstraint constraint; 
	
	public UnaryConstraint(String columnName, VariableConstraint constraint){
		this.columnName = columnName;
		this.constraint = constraint;
		
	}
	
	public String getColumnName(){
		return columnName;
	}
	
	public VariableConstraint getConstraint(){
		return constraint; 
	}
	
	public boolean equals(Object other){
		if(other instanceof UnaryConstraint){
			return other.toString().equals(this.toString()); 
		}else{
			return false; 
		}
	}
	
	public int hashCode(){
		return this.toString().hashCode(); 
	}
	
	
}
