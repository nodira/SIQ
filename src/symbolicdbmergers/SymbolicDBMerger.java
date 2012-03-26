package symbolicdbmergers;

import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;

public interface SymbolicDBMerger {
	public SymbolicDB merge(SymbolicDB db1, SymbolicDB db2);
	
	
}
