package realdb;

import java.util.List;

import symbolicdb.Tuple;

public interface GeneralDB {
	public List<GeneralRelation> relations(); 
	public void addTuple(GeneralRelation rel, Tuple t); 
	public void addTuple(Tuple t); 
	public GeneralRelation getRelation(String name); 
	
	
}
