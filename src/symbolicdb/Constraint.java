package symbolicdb;

import java.util.ArrayList;
import java.util.List;

//conjunction of basic constraints 

public class Constraint {
	
	List<String> basicConstraints = new ArrayList<String>();  
	
	public Constraint(){
		
		
	}
	
	public void addConstraint(String basicConstraint){
		basicConstraints.add(basicConstraint); 
	}
	
	
	
}
