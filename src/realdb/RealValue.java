package realdb;

import constraints.NumericConstraint;
import constraints.StringConstraint;
import constraints.VariableConstraint;
import symbolicdb.CellValue;


public interface RealValue extends CellValue {

	public boolean satisfies(VariableConstraint constraint); 
		
	public static class DoubleValue implements RealValue{
		double value;
		public DoubleValue(double value){
			this.value = value; 
		}
		
		public String toString(){
			return "" + value; 
		}
		
		@Override
		public boolean satisfies(VariableConstraint constraint){
			if(constraint instanceof NumericConstraint){
				NumericConstraint c = (NumericConstraint) constraint; 
				return c.doesValueSatisfy(value); 
			}else{
				throw new RuntimeException(constraint.getClass().getCanonicalName() + 
						" can not be evaluated on a DoubleValue."); 
			}
			
			 
		}
	}
	
	public static class StringValue implements RealValue{
		String value; 
		public StringValue(String value){
			this.value = value; 
		}
		
		public String toString(){
			return value; 
		}
		
		@Override
		public boolean equals(Object other){
			if(other instanceof StringValue){
				if(((StringValue) other).value.equals(this.value)){
					return true; 
				}
			}
			return false; 
		}
		
		@Override
		public int hashCode(){
			return value.hashCode(); 
		}
		
		@Override
		public boolean satisfies(VariableConstraint constraint){
			if(constraint instanceof StringConstraint){
				StringConstraint c = (StringConstraint) constraint;
				return c.doesValueSatisfy("'" + value + "'"); 
			}else{
				throw new RuntimeException(constraint.getClass().getCanonicalName() + 
						" can not be evaluated on a StringValue."); 
			}
		}
		
	}

	
}


