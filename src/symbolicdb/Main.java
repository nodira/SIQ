package symbolicdb;

import old.RoundRobinTemplateFilling;
import old.SimpleView;
import constraints.ComparisonOp;
import constraints.NumericConstraint;
import debugging.GraphvizPrinter;


import query.QuerySession;
import queryplan.QueryPlan;

public class Main {

    public static void main(String[] args) throws Exception{
		QuerySession qs1 = ExampleQuerySessions.getQS1(); 
		
		QueryPlan qp = QueryPlan.constructQueryPlan(qs1); 
		
		SymbolicTuple t = SymbolicTuple.constructTupleWithNewVariables(qp.root().schema()); 
		t.getColumn(1).addConstraint(new NumericConstraint(new ComparisonOp.EQUALS(), 2)); 
		qp.root().request(t, true, true);
		qp.root().update(true); 
		
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$"); 
		
		GraphvizPrinter printer = new GraphvizPrinter("/Users/nodira/Desktop/queryplan.dot", qp); 
		printer.printDot(); 
		
	}
    

	
		
		
		
		
	
}


