package evaluation;

import java.util.LinkedList;
import java.util.Queue;

import queryplan.QueryOperator;
import queryplan.QueryOperator.BinaryQueryOperator;
import queryplan.QueryOperator.UnaryQueryOperator;
import queryplan.QueryPlan;
import realdb.RealDB;

public class QualityMeasurer {

	QualityMetric[] metrics;
	
	public QualityMeasurer(QualityMetric... metrics){
		this.metrics = metrics; 
	}
	
	public double[] metricResults(QueryPlan qp){
		if(qp.db() instanceof RealDB){
			double[] results = new double[metrics.length] ;
			for(int i=0; i<results.length; i++){
				results[i] = metricResult(metrics[i], qp); 
			}
			
			return results; 
		}else{
			throw new RuntimeException("Can only measure the quality of a queryplan with a RealDB. "); 
		}
	}
	
	public double metricResult(QualityMetric metric, QueryPlan qp){
		Queue<QueryOperator> ops = new LinkedList<QueryOperator>(); 
		ops.add(qp.root()); 
		
		double count = 0;
		double total = 0; 
		
		while(ops.isEmpty() == false){
			count++; 
			QueryOperator op = ops.poll();
			total+= metric.measureAtNode(op); 
			
			if(op instanceof UnaryQueryOperator){
				ops.add(((UnaryQueryOperator) op).underlyingOperator()); 
			}else if(op instanceof BinaryQueryOperator){
				ops.add(((BinaryQueryOperator) op).op1());
				ops.add(((BinaryQueryOperator) op).op2()); 
			}
		}
		
		return total / count; 	
	}
	
	public void printMetricResults(QueryPlan qp){
		System.out.println("Metric results: "); 
		for(QualityMetric metric : metrics){
			System.out.println("   " + metric.getClass().getCanonicalName() + ": " + 
					metricResult(metric, qp)); 
		}
	}
	
}
