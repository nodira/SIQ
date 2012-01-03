package constraints;

import java.util.ArrayList;
import java.util.List;

public class StringConstraint implements VariableConstraint{
	
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
	public boolean isSatisfiableWith(VariableConstraint constraint){
		if(constraint instanceof NumericConstraint){
			return false; 
		}
		
		StringConstraint other = (StringConstraint) constraint; 
		
		if(op instanceof ComparisonOp.EQUALS && other.op instanceof ComparisonOp.EQUALS){
			if(value.equals(other.value)){
				return true; 
			}else{
				return false; 
			}
		}else if((op instanceof ComparisonOp.EQUALS && other.op instanceof ComparisonOp.NOT_EQUALS)||
				(op instanceof ComparisonOp.NOT_EQUALS && other.op instanceof ComparisonOp.EQUALS)){
			if(value.equals(other.value)){
				return false; 
			}else{
				return true; 
			}
		
		}else if(op instanceof ComparisonOp.NOT_EQUALS && other.op instanceof ComparisonOp.NOT_EQUALS){
			return true; 
		}else{
			//throw new Exception("Can only use EQUALS and NOT_EQUALS ops with StringConstraints"); 
			return false; 
		}
	}	
	
	public String toString(){
		return op.getClass().getCanonicalName() + value; 
	}





	@Override
	public VariableConstraint negate() {
		return new StringConstraint(this.op.negate(), this.value); 
	}
	
	

	@Override
	public ComparisonOp getOp() {
		return op; 
	}





	@Override
	public String stringValue() {
		return value; 
	}
	
	
	
}
