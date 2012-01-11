package symbolicdb;

import java.util.ArrayList;
import java.util.List;

import schema.ColumnSchema;

import constraints.NumericConstraint;
import constraints.StringConstraint;
import constraints.UnaryConstraint;
import constraints.VariableConstraint;

public class Variable implements Cloneable{
	static int numVariables = 0; 
	enum VariableType{ numericType, stringType } 
	
	String variableName;
	private List<VariableConstraint> constraints; 
	VariableType variableType; 
	
	//list of all the columns this variable currently appears in 
	List<ColumnSchema> columnSchemas;  
	
	protected Variable(){
		this.variableName = "v" + numVariables++; 
		this.setConstraints(new ArrayList<VariableConstraint>()); 
		this.columnSchemas = new ArrayList<ColumnSchema>(); 
	}
	
	
	public Variable(ColumnSchema columnSchema){
		this(); 
		addColumnSchema(columnSchema); 
	}
	
	protected void addColumnSchema(ColumnSchema columnSchema){
		if(columnSchemas.contains(columnSchema) == false){
			columnSchemas.add(columnSchema); 
		}
	}
	
	
	public Variable clone(){
		Variable clone = new Variable();
		clone.variableType = this.variableType; 
		for(VariableConstraint c : constraints){
			clone.addConstraint(c); 
		}
		
		//if we're making a new variable - the places this variable appears should not be 
		//copied over
		//for(ColumnSchema columnSchema: columnSchemas){
			//clone.addColumnSchema(columnSchema); 
		//}
		return clone; 
	}
	
	public boolean canBeMerged(Variable other){
		if(this == other){
			return true; 
		}
		
		for(VariableConstraint c1 : getConstraints()){
			for(VariableConstraint c2 : other.getConstraints()){
				if(c1.isSatisfiableWith(c2) == false){
					return false; 
				}
			}
		}
		
		//if same columnSchema and columnSchema isKey
		for(ColumnSchema columnSchema: columnSchemas){
			for(ColumnSchema otherColumnSchema: other.columnSchemas){
				if(otherColumnSchema == columnSchema && columnSchema.isKey()){
					return false; 
				}
			}
		}
		
		return true; 
	}
	
	
	
	public void merge(Variable other){
		for(VariableConstraint c : other.getConstraints()){
			addConstraint(c); 
		}
		for(ColumnSchema columnSchema: other.columnSchemas){
			addColumnSchema(columnSchema); 
		}
	}
	
	public void setVariableType(VariableType variableType){
		this.variableType = variableType; 
	}
	
	public String getVariableName() {
		return variableName;
	}


	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}


	
	
	public void addConstraint(VariableConstraint c){
		for(VariableConstraint cc : constraints){
			if(cc.toString().equals(c.toString())){
				return; 
			}
		}
		constraints.add(c); 
	}
	
	public boolean satisfiableWith(VariableConstraint constraint){
		for(VariableConstraint c : this.getConstraints()){
			//if unsatisfiable with any - return false
			if(c.isSatisfiableWith(constraint) == false){
				return false; 
			}
		}
		return true; 
	}
	
	public String toString(){
		return variableName;
	}
	
	public String toStringWithConstraint(){
		StringBuilder sb = new StringBuilder(variableName + ": " ); 
		for(VariableConstraint c : getConstraints()){
			sb.append(c + " ^ "); 
		}
		if(getConstraints().size() > 0){
			sb.delete(sb.length()-2, sb.length()); 
		}
		return sb.toString(); 
	}

	public void setConstraints(List<VariableConstraint> constraints) {
		this.constraints = constraints;
	}

	public List<VariableConstraint> getConstraints() {
		return constraints;
	}
	
	
	
}
