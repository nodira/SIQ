package symbolicdb;

import java.util.ArrayList;
import java.util.Collection;

public class SymbolicDB {
	String name; 
	Collection<SymbolicRelation> relations = new ArrayList<SymbolicRelation>(); 
	
	public SymbolicDB(String name){
		this.name = name; 
	}
	
	public static void main(String[] args){
		
	}
}
