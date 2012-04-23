package symbolicdb;

import schema.RelationSchema;

public interface Tuple {
	
	public RelationSchema underlyingSchema();
	public void setColumn(int columnIndex, CellValue v); 
	public int arity(); 
	public CellValue getColumn(int columnIndex); 
	
	public Tuple reconstructForNewSchema(RelationSchema newSchema);
	
	public Tuple constructNewTupleWithSchema(RelationSchema schema);
	public CellValue getColumn(String columnName); 
	
}
