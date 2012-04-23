package symbolicdb;

import java.util.Hashtable;

import realdb.RealValue;


/**
 * This class represents an assignment to a set of variables. 
 * (can think of it as an instantiation of a symbolic db) 
 * @author nodira
 *
 */
public class Assignment {
	Hashtable<Variable, RealValue> var2Value; 
	
	public Assignment(){
		var2Value = new Hashtable<Variable, RealValue>();  
	}
	
	public void assign(Variable v, RealValue value){
		if(var2Value.containsKey(v) && (value.equals(var2Value.get(v)) == false)){
			throw new RuntimeException(v + " has already been assigned to " + var2Value.get(v) +
									" and now trying to assign to " + value); 
		}
		var2Value.put(v, value);
	}
	
	public RealValue valueOf(Variable v){
		return var2Value.get(v); 
	}
	
	public boolean consistentWith(Assignment other){
		for(Variable v : var2Value.keySet()){
			if(other.valueOf(v) == null || other.valueOf(v).equals(this.valueOf(v))){
				//this is good. continue;
			}else{
				return false; 
			}
		}
		return true; 
	}
	
	public Assignment combineWith(Assignment other){
		if(this.consistentWith(other)){
			Assignment asg = new Assignment();
			for(Variable v : this.var2Value.keySet()){
				asg.assign(v, this.var2Value.get(v)); 
			}
			
			for(Variable v : other.var2Value.keySet()){
				asg.assign(v, other.var2Value.get(v)); 
			}
	
			return asg; 
			
		}else{
			throw new RuntimeException("Can not combine two assignments that are inconsistent."); 
		}
		
	}
	
	
}
