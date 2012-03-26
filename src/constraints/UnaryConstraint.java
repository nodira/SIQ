package constraints;

import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;

public class UnaryConstraint {
	
	String columnName;
	VariableConstraint constraint; 
	
	public UnaryConstraint(String columnName, VariableConstraint constraint){
		this.columnName = columnName;
		this.constraint = constraint;
		
	}
	
	public String getColumnName(){
		return columnName;
	}
	
	public VariableConstraint getConstraint(){
		return constraint; 
	}
	
	public boolean equals(Object other){
		if(other instanceof UnaryConstraint){
			return other.toString().equals(this.toString()); 
		}else{
			return false; 
		}
	}
	
	public String toString(){
		return  constraint.toSqlString(columnName); 		 
		
	}
	
	public static UnaryConstraint constructUnaryConstraint(F_PredicateInWhere pred){
		String constForComparison; 
		ComparisonOp op; 
		
		if(pred.lhsIsConst()){
			constForComparison = pred.getCol1(); 
			op = ComparisonOp.opFromString(pred.getOp()).inverse(); 
		}else if(pred.rhsIsConst()){
			constForComparison = pred.getCol2();
			op = ComparisonOp.opFromString(pred.getOp()); 
		}else{
			return null; 
		}
		
		if(constForComparison.startsWith("'")){
			StringConstraint c = new StringConstraint(ComparisonOp.opFromString(pred.getOp()), constForComparison);
			return new UnaryConstraint(pred.getTable1() + "_" + pred.getCol1(), c); 
		}else{
			NumericConstraint c = new NumericConstraint(ComparisonOp.opFromString(pred.getOp()), Double.parseDouble(constForComparison));
			return new UnaryConstraint(pred.getTable1() + "_" + pred.getCol1(), c);
		}
	}
	
	
}
