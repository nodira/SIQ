package constraints;

public class BinaryConstraint {


	
	public BinaryConstraint(String col1, String col2, ComparisonOp op){
		this.col1 = col1;
		this.col2 = col2;
		this.op = op; 
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
	
}
