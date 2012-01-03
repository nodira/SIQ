package constraints;

import java.util.ArrayList;
import java.util.List;

//conjunction of basic constraints 

public interface VariableConstraint {
	
	
	public boolean isSatisfiableWith(VariableConstraint constraint); 
	public VariableConstraint negate(); 
	public ComparisonOp getOp(); 
	public String stringValue(); 
	
	public String toString(); 
}
