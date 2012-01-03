package symbolicdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SymbolicTuple {
	private int arity;
	Variable[] variables; 
	
	SymbolicRelation underlyingRelation; 
	
	public SymbolicTuple(SymbolicRelation underlyingRelation, int arity){
		this.underlyingRelation = underlyingRelation; 
		this.setArity(arity); 
		this.variables = new Variable[arity]; 
	}
	
	public SymbolicTuple(SymbolicRelation underlyingRelation, Variable... variables){
		this.underlyingRelation = underlyingRelation; 
		this.setArity(variables.length);
		this.variables = new Variable[getArity()];
		for(int i=0; i<getArity(); i++){
			this.variables[i] = variables[i]; 
		}
	}
	
	public static SymbolicTuple constructTupleWithNewVariables(SymbolicRelation underlyingRelation, int arity){
		SymbolicTuple t = new SymbolicTuple(underlyingRelation, arity);
		for(int i=0; i<t.variables.length; i++){
			t.variables[i] = new Variable(); 
		}
		return t; 
	}

	public void setColumn(int colIndex, Variable v){
		variables[colIndex] = v; 
	}
	
	public void setColumn(String columnName, Variable v, SimpleView view){
		setColumn(view.columnIndex(columnName), v); 
	}
	
	public Variable getColumn(int colIndex){
		return variables[colIndex]; 
	}
	
	public Variable getColumn(String columnName, SimpleView view){
		return getColumn(view.columnIndex(columnName)); 
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<variables.length; i++){
			sb.append(variables[i] + "\t\t"); 
		}
		
		return sb.toString(); 
	}
	
	public void replaceV1WithV2(Variable v1, Variable v2){
		for(int i=0; i<getArity() ; i++){
			if(variables[i] == v1){
				variables[i] = v2; 
			}
		}
	}
	
	public void merge(SymbolicTuple other){
		assert(this.getArity() == other.getArity());
		
		for(int i=0; i<getArity(); i++){
			variables[i].merge(other.variables[i]); 
		}
		
	}
	
	public boolean canBeMerged(SymbolicTuple other){
		if(getArity() != other.getArity()){
			return false;
		}else{
			for(int i=0; i<getArity(); i++){
				if(variables[i].canBeMerged(other.variables[i]) == false){
					return false; 
				}
			}
			return true; 
			
		}
	}

	public SymbolicTuple clone(){
		SymbolicTuple t = new SymbolicTuple(this.underlyingRelation, this.getArity()); 
		for(int i=0; i<getArity(); i++){
			t.setColumn(i, this.getColumn(i).clone()); 
		}
		return t; 
	}
	
	protected SymbolicTuple cloneAccordingToMap(HashMap<Variable, Variable> varToNewVar){
		SymbolicTuple t = new SymbolicTuple(this.underlyingRelation, this.getArity()); 
		for(int i=0; i<getArity(); i++){
			Variable v = this.getColumn(i); 
			if(varToNewVar.containsKey(v) == false){
				varToNewVar.put(v, v.clone()); 
			}
			t.setColumn(i, varToNewVar.get(v)); 
		}
		return t; 
		
	}
	
	
	public List<Variable> variables(){
		List<Variable> vars = new ArrayList<Variable>();
		for(int i=0; i < getArity(); i++){
			vars.add(variables[i]); 
		}
		return vars; 
	}

	public void setArity(int arity) {
		this.arity = arity;
	}

	public int getArity() {
		return arity;
	}
}
