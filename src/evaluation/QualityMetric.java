package evaluation;

import java.util.LinkedList;
import java.util.Queue;

import query.QuerySession;
import queryplan.QueryOperator;
import queryplan.QueryPlan;
import queryplan.QueryOperator.BinaryQueryOperator;
import queryplan.QueryOperator.UnaryQueryOperator;
import realdb.RealDB;

public abstract class QualityMetric {
	public abstract double measureAtNode(QueryOperator op); 
	
	public double metricResult(QuerySession qs, RealDB db){
		QueryPlan qp = QueryPlan.constructQueryPlan(qs, db); 
		Queue<QueryOperator> ops = new LinkedList<QueryOperator>(); 
		ops.add(qp.root()); 
		
		double count = 0;
		double total = 0; 
		
		while(ops.isEmpty() == false){
			count++; 
			QueryOperator op = ops.poll();
			total+= this.measureAtNode(op); 
			
			if(op instanceof UnaryQueryOperator){
				ops.add(((UnaryQueryOperator) op).underlyingOperator()); 
			}else if(op instanceof BinaryQueryOperator){
				ops.add(((BinaryQueryOperator) op).op1());
				ops.add(((BinaryQueryOperator) op).op2()); 
			}
		}
		return total / count; 	
	}
}
