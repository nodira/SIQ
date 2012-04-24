package realdb;

import java.util.List;

import schema.DBSchema;
import symbolicdb.Tuple;

public interface GeneralDB {
	public DBSchema getSchema(); 
	public List<GeneralRelation> relations(); 
	public void addTuple(GeneralRelation rel, Tuple t); 
	public void addTuple(Tuple t); 
	public GeneralRelation getRelation(String name); 
	
	
}
