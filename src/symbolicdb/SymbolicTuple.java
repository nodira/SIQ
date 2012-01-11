package symbolicdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import templates.TemplateTuple;

public class SymbolicTuple {
	protected Variable[] variables; 
	
	//should this be symbolicrelation or relationschema? 
	protected SymbolicRelation underlyingRelation; 
	
	
	public SymbolicTuple(SymbolicRelation underlyingRelation){
		this.underlyingRelation = underlyingRelation; 
		this.variables = new Variable[underlyingRelation.arity()]; 
	}
	
	
	public SymbolicRelation underlyingRelation(){
		return underlyingRelation; 
	}
	
	public SymbolicTuple(SymbolicRelation underlyingRelation, Variable... variables){
		this.underlyingRelation = underlyingRelation; 
		this.variables = new Variable[arity()];
		for(int i=0; i<arity(); i++){
			this.variables[i] = variables[i]; 
		}
	}
	
	public static SymbolicTuple constructTupleWithNewVariables(SymbolicRelation underlyingRelation){
		SymbolicTuple t = new SymbolicTuple(underlyingRelation);
		for(int i=0; i<t.variables.length; i++){
			t.variables[i] = new Variable(underlyingRelation.relationSchema().getAttribute(i)); 
		}
		return t; 
	}
	
	public static TemplateTuple constructTemplateTupleWithNewVariables(SymbolicRelation underlyingRelation){
		TemplateTuple t = new TemplateTuple(underlyingRelation);
		for(int i=0; i<t.variables.length; i++){
			t.variables[i] = new Variable(); //we make it without the ColumnSchema coz it's a template tuple
		}
		return t; 
	}
	

	public void setColumn(int colIndex, Variable v){
		variables[colIndex] = v; 
		v.addColumnSchema(underlyingRelation.relationSchema().getAttribute(colIndex)); 
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
		for(int i=0; i<arity() ; i++){
			if(variables[i] == v1){
				variables[i] = v2; 
			}
		}
	}
	
	public void merge(SymbolicTuple other){
		assert(this.arity() == other.arity());
		
		for(int i=0; i<arity(); i++){
			variables[i].merge(other.variables[i]); 
		}
		
	}
	
	public boolean canBeMerged(SymbolicTuple other){
		if(arity() != other.arity()){
			return false;
		}else{
			for(int i=0; i<arity(); i++){
				if(variables[i].canBeMerged(other.variables[i]) == false){
					return false; 
				}
			}
			return true; 
			
		}
	}

	public SymbolicTuple clone(){
		SymbolicTuple t = new SymbolicTuple(this.underlyingRelation); 
		for(int i=0; i<arity(); i++){
			t.setColumn(i, this.getColumn(i).clone()); 
		}
		return t; 
	}
	
	protected SymbolicTuple cloneAccordingToMap(HashMap<Variable, Variable> varToNewVar){
		SymbolicTuple t = new SymbolicTuple(this.underlyingRelation); 
		for(int i=0; i<arity(); i++){
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
		for(int i=0; i < arity(); i++){
			vars.add(variables[i]); 
		}
		return vars; 
	}

	public int arity() {
		return underlyingRelation.arity();
	}
}
