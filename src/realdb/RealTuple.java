package realdb;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.Assignment;
import symbolicdb.CellValue;
import symbolicdb.SymbolicTuple;
import symbolicdb.Tuple;
import symbolicdb.Variable;

public class RealTuple implements Tuple {
	private RealValue[] values; 
	protected RelationSchema underlyingSchema; 
	
	public RealTuple(RelationSchema underlyingSchema){
		this.underlyingSchema = underlyingSchema; 
		this.values = new RealValue[underlyingSchema.size()]; 
	}
	
	
	public RelationSchema underlyingSchema(){
		return underlyingSchema; 
	}
	
	public void setColumn(int columnIndex, CellValue v) {
		if(v instanceof RealValue){
			values[columnIndex] = (RealValue) v; 
		}else{
			throw new RuntimeException("Cellvalue must be of type Value."); 
		}
		
	}


	public void setColumn(String columnName, RealValue v){
		setColumn(underlyingSchema.getAttributeIndex(columnName), v); 
	}
	
	public RealValue getColumn(int colIndex){
		return values[colIndex]; 
	}
	
	public RealValue getColumn(String columnName){
		return getColumn(underlyingSchema.getAttributeIndex(columnName)); 
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("<");
		
		for(int i=0; i<values.length; i++){
			//sb.append(variables[i] + "\t\t"); 
			sb.append(values[i] + ", "); 
		}
		
		if(values.length > 0){
			sb.delete(sb.length()-2, sb.length());
		}
		
		sb.append(">"); 
		return sb.toString(); 
	}
		
	public List<RealValue> variables(){
		List<RealValue> vars = new ArrayList<RealValue>();
		for(int i=0; i < arity(); i++){
			vars.add(values[i]); 
		}
		return vars; 
	}

	public int arity() {
		return underlyingSchema.size();
	}
	
	@Override
	public Tuple constructNewTupleWithSchema(RelationSchema schema) {
		return new RealTuple(schema); 
	}


	@Override
	public Tuple reconstructForNewSchema(RelationSchema newSchema) {
		RealTuple tupleRenamed = new RealTuple(newSchema);
		for(int i=0; i<this.arity(); i++){
			tupleRenamed.setColumn(i, this.getColumn(i)); 
		}
		return tupleRenamed; 
	}
	
	public Assignment makeAssignment(SymbolicTuple st){
		if(this.matches(st)){
			Assignment asg = new Assignment(); 
			for(int i=0; i<st.arity(); i++){
				asg.assign(st.getColumn(i), this.getColumn(i)); 
			}
			return asg; 
		}else{
			throw new RuntimeException("Can not makeAssignment because st doesn't match."); 
		}
	}

	public boolean matches(SymbolicTuple st){
		Hashtable<Variable, RealValue> v2v = new Hashtable<Variable, RealValue>();
		
		for(int i=0; i<st.arity(); i++){
			Variable v = st.getColumn(i);
			RealValue rv = this.getColumn(i); 
			if(v.satisfiesConstraints(rv) == false){
				return false;
			}else if(v2v.containsKey(v) && (v2v.get(v).equals(rv) == false)){
				return false;
			}else{
				v2v.put(v, rv);
			}	
		}
		return true; 
	}
		
}
