package evaluation;

import queryplan.QueryOperator;

public interface QualityMetric {
	public double measureAtNode(QueryOperator op); 
	
}
