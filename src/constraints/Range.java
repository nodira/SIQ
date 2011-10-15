package constraints;

public class Range {
	
	double min;
	double max;
	boolean minInclusive;
	boolean maxInclusive;
	
	public Range(double min, double max, boolean minInclusive, boolean maxInclusive){
		this.min = min;
		this.max = max;
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive; 
		
	}
	
	public boolean contains(double d){
		if(d > min || (d == min && minInclusive)){
			if(d < max || (d == max && maxInclusive)){
				return true; 
			}
		}
		return false; 
	}
	
	public boolean intersects(Range other){
		//if other.min is contained in this range
		if(other.min > min || (other.min == min && minInclusive && other.minInclusive)){
			if(other.min < max || (other.min == max && maxInclusive && other.minInclusive)){
				return true; 
			}
		}
		
		//if other.max is contained in this range
		if(other.max > min || (other.max == min && minInclusive && other.maxInclusive)){
			if(other.max < max || (other.max == max && maxInclusive && other.maxInclusive)){
				return true; 
			}
		}
		
		//if exactly the same
		if(other.min == min && other.max == max){
			return true;
		}
		
		return false; 
		
	}
	
	public static Range getRange(String s){
		boolean minInclusive = s.charAt(0) == '[';
		boolean maxInclusive = s.charAt(s.length()-1) == ']';
		s = s.substring(1, s.length()-1); 
		
		double min = Double.parseDouble(s.substring(0, s.indexOf(','))); 
		double max = Double.parseDouble(s.substring(s.indexOf(',') + 1)); 
		
		return new Range(min, max, minInclusive, maxInclusive); 
	}
		
		
		
	

}
