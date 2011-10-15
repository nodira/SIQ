package constraints;

import java.util.ArrayList;
import java.util.List;

//conjunction of basic constraints 

public class Constraint {
	
	public static enum ComparisonOp {EQUALS, NOT_EQUALS, GEQ, LEQ, GT, LT}; 
	
	
	List<String> basicConstraints = new ArrayList<String>();  
	
	public Constraint(){
		
		
	}
	
	public void addConstraint(String basicConstraint){
		basicConstraints.add(basicConstraint); 
	}
	
	
	
}
