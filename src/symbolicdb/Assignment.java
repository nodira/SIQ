package symbolicdb;

import java.util.Hashtable;

/**
 * This class represents an assignment to a set of variables. 
 * (can think of it as an instantiation of a symbolic db) 
 * @author nodira
 *
 */
public class Assignment {
	Hashtable<Variable, String> var2Value; 
	
	public Assignment(){
		var2Value = new Hashtable<Variable, String>();  
	}
	
	public void assign(Variable v, String value){
		if(var2Value.containsKey(v) && (value.equals(var2Value.get(v)) == false)){
			throw new RuntimeException(v + " has already been assigned to " + var2Value.get(v) +
									" and now trying to assign to " + value); 
		}
		var2Value.put(v, value);
	}
	
	public String valueOf(Variable v){
		return var2Value.get(v); 
	}
	
	
}
