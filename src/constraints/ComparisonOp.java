package constraints;


public abstract class ComparisonOp {
	public static ComparisonOp opFromString(String opString){
		if(opString.equals("=")){
			return new EQUALS(); 
		}else if(opString.equals("<>")){
			return new NOT_EQUALS(); 
		}else if(opString.equals("<")){
			return new LT(); 
		}else if(opString.equals(">")){
			return new GT(); 
		}else if(opString.equals("<=")){
			return new LEQ();
		}else if(opString.equals(">=")){
			return new GEQ(); 
		}else{
			System.err.println("Couldn't recognize " + opString); 
			System.exit(-1); 
			return null; 
		}
		
		
		
		
	}
	public abstract ComparisonOp inverse(); 
	public abstract ComparisonOp negate(); 
	
	public static class EQUALS extends ComparisonOp{
		public EQUALS(){}

		public ComparisonOp inverse() {
			return new EQUALS(); 
		}

		public ComparisonOp negate() {
			return new NOT_EQUALS();
		}
		
		
	}
	
	public static class NOT_EQUALS extends ComparisonOp{
		public NOT_EQUALS(){}
		
		public ComparisonOp inverse() {
			return new NOT_EQUALS(); 
		}
		public ComparisonOp negate() {
			return new EQUALS();
		}
	}
	
	public static class GEQ extends ComparisonOp{
		public GEQ(){}
		
		public ComparisonOp inverse() {
			return new LEQ(); 
		}
		public ComparisonOp negate() {
			return new LT();
		}
	}
	
	public static class LEQ extends ComparisonOp{
		public LEQ(){}
		public ComparisonOp inverse() {
			return new GEQ(); 
		}
		public ComparisonOp negate() {
			return new GT();
		}
	}
	
	public static class GT extends ComparisonOp{
		public GT(){}
		public ComparisonOp inverse() {
			return new LT(); 
		}
		public ComparisonOp negate() {
			return new LEQ();
		}
	}
	
	public static class LT extends ComparisonOp{
		public LT(){}
		public ComparisonOp inverse() {
			return new GT(); 
		}
		public ComparisonOp negate() {
			return new GEQ();
		}
	}
	
}
