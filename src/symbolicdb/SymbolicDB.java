package symbolicdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SymbolicDB {
	String name; 

	List<Variable> variables = new ArrayList<Variable>(); 
	List<SymbolicRelation> relations = new ArrayList<SymbolicRelation>(); 
	
	public SymbolicDB(String name){
		this.name = name; 
	}
	
	public static void main(String[] args){
		
	}
}
