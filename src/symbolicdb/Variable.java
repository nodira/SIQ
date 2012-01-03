package symbolicdb;

import java.util.ArrayList;
import java.util.List;

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
	
	public Variable(){
		this.variableName = "v" + numVariables++; 
		this.setConstraints(new ArrayList<VariableConstraint>()); 
		
	}
	

	public Variable clone(){
		Variable clone = new Variable();
		clone.variableType = this.variableType; 
		for(VariableConstraint c : constraints){
			clone.addConstraint(c); 
		}
		return clone; 
	}
	
	public boolean canBeMerged(Variable other){
		for(VariableConstraint c1 : getConstraints()){
			for(VariableConstraint c2 : other.getConstraints()){
				if(c1.isSatisfiableWith(c2) == false){
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
