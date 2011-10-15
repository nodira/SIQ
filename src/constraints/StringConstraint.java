package constraints;

import java.util.ArrayList;
import java.util.List;

import constraints.Constraint.ComparisonOp;



public class StringConstraint {
	
	/**
	 *  = val, != val 
	 */
	
	ComparisonOp op; 
	String value; 
	
	public StringConstraint(ComparisonOp op, String value){
		this.op = op;
		this.value = value; 
	}
	
	
	
	///need to test this. 
	public boolean satisfiableWith(StringConstraint other) throws Exception{
		if(op == ComparisonOp.EQUALS && other.op == ComparisonOp.EQUALS){
			if(value.equals(other.value)){
				return true; 
			}else{
				return false; 
			}
		}else if((op == ComparisonOp.EQUALS && other.op == ComparisonOp.NOT_EQUALS)||
				(op == ComparisonOp.NOT_EQUALS && other.op == ComparisonOp.EQUALS)){
			if(value.equals(other.value)){
				return false; 
			}else{
				return true; 
			}
		
		}else if(op == ComparisonOp.NOT_EQUALS && other.op == ComparisonOp.NOT_EQUALS){
			return true; 
		}else{
			throw new Exception("Can only use EQUALS and NOT_EQUALS ops with StringConstraints"); 
			
		}
	}	
	
	
	
}
