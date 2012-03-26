package constraints;

import java.util.ArrayList;
import java.util.List;


public class NumericConstraint implements VariableConstraint{
	
	/**
	 *  = val, != val, >= val, <= val, > val, < val
	 */
	
	ComparisonOp op; 
	double value; 
	
	public NumericConstraint(ComparisonOp op, double value){
		this.op = op;
		this.value = value; 
	}
	
	public String toString(){
		return op + " " + value; 
	}
	
	private List<Range> toRanges(){
		List<Range> ranges = new ArrayList<Range>(); 
		
		if(op instanceof ComparisonOp.EQUALS){
			ranges.add(new Range(value, value, true, true)); 
		}else if(op instanceof ComparisonOp.NOT_EQUALS){
			ranges.add(new Range(Double.MIN_VALUE, value, true, false)); 
			ranges.add(new Range(value, Double.MAX_VALUE, false, true)); 
		}else if(op instanceof ComparisonOp.GEQ){
			ranges.add(new Range(value, Double.MAX_VALUE, true, true)); 
		}else if(op instanceof ComparisonOp.LEQ){
			ranges.add(new Range(Double.MIN_VALUE, value, true, true)); 
		}else if(op instanceof ComparisonOp.GT){
			ranges.add(new Range(value, Double.MAX_VALUE, false, true)); 
		}else if(op instanceof ComparisonOp.LT){
			ranges.add(new Range(Double.MIN_VALUE, value, true, false)); 
		}
		
		return ranges; 
	}
	
	///need to test this. 
	public boolean isSatisfiableWith(VariableConstraint constraint){
		if(constraint instanceof StringConstraint){
			return false; 
		}
		
		NumericConstraint other = (NumericConstraint) constraint; 
		
		
		List<Range> ranges = toRanges();
		List<Range> otherRanges = other.toRanges();
		
		for(Range r1 : ranges){
			for(Range r2 : otherRanges){
				if(r1.intersects(r2)){
					return true; 
				}
			}
		}
		
		return false; 
	}	
	


	@Override
	public VariableConstraint negate() {
		return new NumericConstraint(this.op.negate(), this.value); 
	}

	@Override
	public ComparisonOp getOp() {
		return op; 
	}

	@Override
	public String stringValue() {
		return value + ""; 
	}
	
	public double value(){
		return value; 
	}

	@Override
	public String toSqlString(String aliasDotCol) {
		return aliasDotCol + ComparisonOp.stringFromOp(op) + value; 
	}
	
	
}
