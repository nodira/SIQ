package symbolicdb;

import java.util.ArrayList;
import java.util.List;

import datagenerator.SymbolicToRealSample;
import debugging.GraphvizPrinter;


import query.QuerySession;
import queryplan.QueryPlan;
import queryplan.QueryPlanIllustrator;
import symbolicdbmergers.TupleByTupleMerger;

public class Main {

    public static void main(String[] args) throws Exception{
		QuerySession qs1 = ExampleQuerySessions.getQS2(); 
		QueryPlan qp1 = QueryPlan.constructQueryPlan(qs1); 
		QueryPlanIllustrator illustrator1 = new QueryPlanIllustrator();  
		illustrator1.illustrate(qp1); 
		qp1.root().update(false); 
		
		QuerySession qs2 = ExampleQuerySessions.getQS2(); 
		QueryPlan qp2 = QueryPlan.constructQueryPlan(qs2); 
		QueryPlanIllustrator illustrator2 = new QueryPlanIllustrator();  
		illustrator2.illustrate(qp2); 
		qp2.root().update(false); 
		
		SymbolicDB wellMergedDB = TupleByTupleMerger.BestEffortTupleByTupleMerger().merge(qp1.db(), qp2.db()); 
		SymbolicDB unionMergedDB = TupleByTupleMerger.UnionMerger().merge(qp1.db(), qp2.db());
			
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$"); 
		 
		
		/*GraphvizPrinter p1 = new GraphvizPrinter("/Users/nodira/Desktop/qp1.dot");
		p1.printDot(qp1);
		
		GraphvizPrinter p2 = new GraphvizPrinter("/Users/nodira/Desktop/qp2.dot");
		p2.printDot(qp2);
		
		GraphvizPrinter pBE = new GraphvizPrinter("/Users/nodira/Desktop/besteffort.dot"); 
		pBE.printDot(wellMergedDB); 
		
		GraphvizPrinter pUnion = new GraphvizPrinter("/Users/nodira/Desktop/union.dot"); 
		pUnion.printDot(unionMergedDB); 
		*/
		
		
		GraphvizPrinter p1 = new GraphvizPrinter("/Users/nodira/Desktop/qp1.dot");
		p1.printDot(qp1);
		SymbolicToRealSample strs = new SymbolicToRealSample(qp1.db(), "imdb-conn.properties"); 
		Assignment asg = strs.printSampleAndReturnAssignment(); 
		GraphvizPrinter pBER = new GraphvizPrinter("/Users/nodira/Desktop/real-sample.dot"); 
		pBER.printDot(qp1, asg); 
		
	}
	
}