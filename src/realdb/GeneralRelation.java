package realdb;

import java.util.List;

import schema.RelationSchema;
import symbolicdb.Tuple;

public interface GeneralRelation {

	public RelationSchema relationSchema(); 
	
	public int arity(); 
	
	public List<Tuple> getTuples(); 
	
	public void addTuple(Tuple t); 
}
