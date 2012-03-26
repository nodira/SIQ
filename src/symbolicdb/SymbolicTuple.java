package symbolicdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import old.SimpleView;
import old.TemplateTuple;

import schema.RelationSchema;

public class SymbolicTuple {
	protected Variable[] variables; 
	protected RelationSchema underlyingSchema; 
	
	
	public SymbolicTuple(RelationSchema underlyingSchema){
		this.underlyingSchema = underlyingSchema; 
		this.variables = new Variable[underlyingSchema.size()]; 
	}
	
	
	public RelationSchema underlyingSchema(){
		return underlyingSchema; 
	}
	
	public SymbolicTuple(RelationSchema underlyingSchema, Variable... variables){
		this.underlyingSchema = underlyingSchema; 
		this.variables = new Variable[arity()];
		for(int i=0; i<arity(); i++){
			this.variables[i] = variables[i]; 
		}
	}
	
	public static SymbolicTuple constructTupleWithNewVariables(RelationSchema underlyingSchema){
		SymbolicTuple t = new SymbolicTuple(underlyingSchema);
		for(int i=0; i<t.variables.length; i++){
			t.variables[i] = new Variable(underlyingSchema.getAttribute(i)); 
		}
		return t; 
	}
	
	public static TemplateTuple constructTemplateTupleWithNewVariables(RelationSchema underlyingSchema){
		TemplateTuple t = new TemplateTuple(underlyingSchema);
		for(int i=0; i<t.variables.length; i++){
			t.variables[i] = new Variable(); //we make it without the ColumnSchema coz it's a template tuple
		}
		return t; 
	}
	


	
	public void setColumn(String columnName, Variable v, SimpleView view){
		setColumn(view.columnIndex(columnName), v); 
	}
	
	public void setColumn(int columnIndex, Variable v) {
		variables[columnIndex] = v; 
		
	}


	public void setColumn(String columnName, Variable v){
		setColumn(underlyingSchema.getAttributeIndex(columnName), v); 
	}
	
	
	public Variable getColumn(int colIndex){
		return variables[colIndex]; 
	}
	
	public Variable getColumn(String columnName){
		System.out.println("columnName: " + columnName); 
		System.out.println("underlyingSchema: " + underlyingSchema.toString()); 
		
		return getColumn(underlyingSchema.getAttributeIndex(columnName)); 
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
		SymbolicTuple t = new SymbolicTuple(this.underlyingSchema); 
		for(int i=0; i<arity(); i++){
			t.setColumn(i, this.getColumn(i).clone()); 
		}
		return t; 
	}
	
	protected SymbolicTuple cloneAccordingToMap(Hashtable<Variable, Variable> varToNewVar){
		SymbolicTuple t = new SymbolicTuple(this.underlyingSchema); 
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
		return underlyingSchema.size();
	}
}
