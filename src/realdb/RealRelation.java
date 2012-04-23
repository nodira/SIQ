package realdb;

import java.util.ArrayList;
import java.util.List;
import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.Tuple;


public class RealRelation implements GeneralRelation{
	
	private RelationSchema relationSchema; 
	private List<Tuple> tuples = new ArrayList<Tuple>();
	
	
	
	public RealRelation(RelationSchema relationSchema){
		this.relationSchema = relationSchema;  
	}
	
	public void addTuple(Tuple tuple){
		getTuples().add(tuple); 	
		
	}
		
	public String toString(){
		StringBuilder s = new StringBuilder();  
		s.append(relationSchema.getRelationName() + "\n"); 
		for(ColumnSchema c : relationSchema.getAttributes()){
			s.append(c + "\t\t") ; 
		}
		s.append("\n"); 
		
		for(Tuple t : getTuples()){
			s.append(t.toString() + "\n"); 
		}
		
		
		return s.toString(); 
	}
	

	public List<Tuple> getTuples() {
		return tuples;
	}
	
	public int arity(){
		return relationSchema.size(); 
	}
	
	public RelationSchema relationSchema(){
		return relationSchema; 
	}
	
	
	
	
	
}
