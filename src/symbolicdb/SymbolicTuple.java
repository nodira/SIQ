package symbolicdb;

public class SymbolicTuple {
	int arity; 
	String[] tupleValues; 
	
	public SymbolicTuple(int arity){
		this.arity = arity; 
		this.tupleValues = new String[arity]; 
	}
	
	public SymbolicTuple(String... values){
		this.arity = values.length; 
		this.tupleValues = new String[values.length];
		for(int i=0; i<values.length; i++){
			this.tupleValues[i] = values[i]; 
		}
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("(" ) ; 
		for(int i=0; i<tupleValues.length; i++){
			sb.append(tupleValues);
			if(i != tupleValues.length -1){
				sb.append(","); 
			}
		}
		sb.append(")");
		return sb.toString(); 
	}
	
	
	
}
