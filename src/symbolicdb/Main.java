package symbolicdb;

import constraints.Range;

public class Main {

	public static void main(String[] args){
		
		Range r = Range.getRange("(1, 10]");
		
		Range r2 = Range.getRange("[10, 11)"); 
		
		System.out.println(r.intersects(r2)); 
		
	}
}
