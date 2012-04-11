package symbolicdb;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import schema.ColumnSchema;
import schema.RelationSchema;

public class SymbolicTuple {
	private Variable[] variables; 
	protected RelationSchema underlyingSchema; 
	
	public SymbolicTuple(RelationSchema underlyingSchema){
		this.underlyingSchema = underlyingSchema; 
		this.variables = new Variable[underlyingSchema.size()]; 
	}
	
	
	public RelationSchema underlyingSchema(){
		return underlyingSchema; 
	}
	
	public static SymbolicTuple constructTupleWithNewVariables(RelationSchema underlyingSchema){
		SymbolicTuple t = new SymbolicTuple(underlyingSchema);
		for(int i=0; i<t.variables.length; i++){
			t.setColumn(i, new Variable(underlyingSchema.getAttribute(i))); 
		}
		return t; 
	}
	
	public void setColumn(int columnIndex, Variable v) {
		variables[columnIndex] = v; 
		v.addColumnSchema(underlyingSchema.getAttribute(columnIndex)); 
	}


	public void setColumn(String columnName, Variable v){
		//System.out.println("setColumn:: columnName: " + columnName); 
		//System.out.println("underlyingSchema: " + underlyingSchema.toString()); 
		setColumn(underlyingSchema.getAttributeIndex(columnName), v); 
	}
	
	
	public Variable getColumn(int colIndex){
		return variables[colIndex]; 
	}
	
	public Variable getColumn(String columnName){
		//System.out.println("getColumn:: columnName: " + columnName); 
		//System.out.println("underlyingSchema: " + underlyingSchema.toString()); 
		
		return getColumn(underlyingSchema.getAttributeIndex(columnName)); 
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("<");
		
		for(int i=0; i<variables.length; i++){
			//sb.append(variables[i] + "\t\t"); 
			sb.append(variables[i] + ", "); 
		}
		
		if(variables.length > 0){
			sb.delete(sb.length()-2, sb.length());
		}
		
		sb.append(">"); 
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
	
	public boolean canBeMergedDontCheckColumnSchemas(SymbolicTuple other){
		if(arity() != other.arity()){
			return false;
		}else{
			for(int i=0; i<arity(); i++){
				if(variables[i].canBeMerged(other.variables[i], null, false) == false){
					return false; 
				}
			}
			return true; 
			
		}
	}
	
	public boolean canBeMerged(SymbolicTuple other){
		if(arity() != other.arity()){
			return false;
		}else{
			for(int i=0; i<arity(); i++){
				ColumnSchema colException = underlyingSchema.getAttribute(i); 
				
				if(variables[i].canBeMerged(other.variables[i], colException) == false){
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
	
	public SymbolicTuple cloneWithColumnSchemas(){
		SymbolicTuple t = SymbolicTuple.constructTupleWithNewVariables(this.underlyingSchema);
		for(int i=0; i<arity(); i++){
			t.setColumn(i, this.getColumn(i).cloneWithColumnSchemas()); 
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
	
	
	
	
	public static <T> List<List<T>> product(List<List<T>> listofLists){
		if(listofLists.size() == 0){
			List<T> emptyList = new ArrayList<T>(); 
			List<List<T>> listContainingEmptyList = new ArrayList<List<T>>();
			
			listContainingEmptyList.add(emptyList);
			return listContainingEmptyList; 
		}else{
			List<List<T>> smallerProduct = product(listofLists.subList(0, listofLists.size()-1)); 
			List<List<T>> product = new ArrayList<List<T>>();
			
			for(T tuple : listofLists.get(listofLists.size()-1)){
				for(List<T> smallerList : smallerProduct){
					List<T> list = new ArrayList<T>();
					list.addAll(smallerList);
					list.add(tuple); 
					product.add(list); 
				}
			}
			return product; 
		}		
	}
	
	public void clearColumnSchemas(){
		for(Variable v: variables){
			v.columnSchemas = new ArrayList<ColumnSchema>(); 
		}
		
	}
	
	
	
}
