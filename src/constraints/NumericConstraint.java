package constraints;

import java.util.ArrayList;
import java.util.List;

import constraints.Constraint.ComparisonOp;



public class NumericConstraint {
	
	/**
	 *  = val, != val, >= val, <= val, > val, < val
	 */
	
	ComparisonOp op; 
	double value; 
	
	public NumericConstraint(ComparisonOp op, double value){
		this.op = op;
		this.value = value; 
	}
	
	private List<Range> toRanges(){
		List<Range> ranges = new ArrayList<Range>(); 
		
		switch(op){
		case EQUALS:
			ranges.add(new Range(value, value, true, true)); 
			break; 
		case NOT_EQUALS:
			ranges.add(new Range(Double.MIN_VALUE, value, true, false)); 
			ranges.add(new Range(value, Double.MAX_VALUE, false, true)); 
			break;
		case GEQ:
			ranges.add(new Range(value, Double.MAX_VALUE, true, true)); 
			break;
		case LEQ:
			ranges.add(new Range(Double.MIN_VALUE, value, true, true)); 
			break;
		case GT:
			ranges.add(new Range(value, Double.MAX_VALUE, false, true)); 
			break;
		case LT: 
			ranges.add(new Range(Double.MIN_VALUE, value, true, false)); 
			break;
		}
		
		return ranges; 
	}
	
	///need to test this. 
	public boolean satisfiableWith(NumericConstraint other){
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
	
	
	
}
