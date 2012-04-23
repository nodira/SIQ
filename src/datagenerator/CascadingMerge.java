package datagenerator;

import query.QuerySession;
import queryplan.QueryPlan;
import realdb.RealDB;
import schema.DBSchema;
import symbolicdb.SymbolicDB;

public class CascadingMerge {
	
	QuerySession[] querySessions; 
	
	public CascadingMerge(QuerySession... querySessions){
		this.querySessions = querySessions; 
	}
	
	public SymbolicDB mergedDB(){
		QueryPlanIllustrator illustrator = new QueryPlanIllustrator();
		
		QueryPlan[] queryPlans = new QueryPlan[querySessions.length];
		DBSchema schema = querySessions[0].getSchema(); 
		SymbolicDB db = new SymbolicDB(schema); 
		
		for(int i=0; i<queryPlans.length; i++){
			queryPlans[i] = QueryPlan.constructQueryPlan(querySessions[i], db);
			
			illustrator.illustrate(queryPlans[i]); 
			
			db = (SymbolicDB) queryPlans[i].db(); 
		}
		
		return (SymbolicDB) queryPlans[queryPlans.length-1].db(); 
		
	}
	
	
}
