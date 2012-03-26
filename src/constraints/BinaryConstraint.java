package constraints;

import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;

public class BinaryConstraint {


	
	public BinaryConstraint(String col1, String col2, ComparisonOp op){
		this.col1 = col1;
		this.col2 = col2;
		this.op = op; 
	}
	
	public String toString(){
		return col1 + " " + ComparisonOp.stringFromOp(op) + " " + col2; 
	}
	
	
	public String getCol1() {
		return col1;
	}

	public void setCol1(String col1) {
		this.col1 = col1;
	}

	public String getCol2() {
		return col2;
	}

	public void setCol2(String col2) {
		this.col2 = col2;
	}

	public ComparisonOp getOp() {
		return op;
	}

	public void setOp(ComparisonOp op) {
		this.op = op;
	}

	String col1;
	String col2;
	ComparisonOp op;
	
	public static BinaryConstraint constructBinaryConstraint(F_PredicateInWhere pred){
		if(pred.lhsIsConst() == false && pred.rhsIsConst() == false){
			BinaryConstraint c = new BinaryConstraint(	pred.getTable1() + "_" + pred.getCol1(), 
														pred.getTable2() + "_" + pred.getCol2(), 
														ComparisonOp.opFromString(pred.getOp()));
			return c; 
		}else{
			return null; 
		}
	}
	
	
}
