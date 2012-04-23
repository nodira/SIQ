package symbolicdb;

import datagenerator.CascadingMerge;
import datagenerator.QueryPlanIllustrator;
import datagenerator.RandomSampleGenerator;
import datagenerator.SymbolicToRealSample;
import debugging.GraphvizPrinter;
import evaluation.Completeness;
import evaluation.Conciseness;
import evaluation.QualityMeasurer;
import query.QuerySession;
import queryplan.QueryPlan;
import realdb.RealDB;
import schema.DBSchema;
import schema.IMDBSchemaWithKeys;

public class Main {

    public static void main(String[] args) throws Exception{
    	DBSchema schema = IMDBSchemaWithKeys.getInstance();
    	SymbolicDB db = new SymbolicDB(schema);
    	SymbolicTuple actor1 = SymbolicTuple.constructTupleWithNewVariables(schema.get("actor")); 
    	SymbolicTuple casts1 = SymbolicTuple.constructTupleWithNewVariables(schema.get("casts")); 
    	SymbolicTuple casts2 = SymbolicTuple.constructTupleWithNewVariables(schema.get("casts")); 
    	SymbolicTuple movie1 = SymbolicTuple.constructTupleWithNewVariables(schema.get("movie")); 
    	SymbolicTuple movie2 = SymbolicTuple.constructTupleWithNewVariables(schema.get("movie")); 
    	
    	casts1.setColumn("pid", actor1.getColumn("id"));
    	casts2.setColumn("pid", actor1.getColumn("id"));

    	movie1.setColumn("id", casts1.getColumn("mid"));
    	movie2.setColumn("id", casts2.getColumn("mid"));
    	
    	db.addTuple(actor1); 
    	db.addTuple(movie1); 
    //	db.addTuple(movie2); 
    	db.addTuple(casts1); 
    	db.addTuple(casts2); 
    	
    	QuerySession qs1 = ExampleQuerySessions.getQS1(); 
		QueryPlan qp1 = QueryPlan.constructQueryPlan(qs1);  
		QueryPlanIllustrator illustrator1 = new QueryPlanIllustrator();  
		illustrator1.illustrate(qp1); 
		qp1.root().update(false); 
		
		
	/*	QuerySession qs2 = ExampleQuerySessions.getQS2(); 
		QueryPlan qp2 = QueryPlan.constructQueryPlan(qs2); 
		QueryPlanIllustrator illustrator2 = new QueryPlanIllustrator();  
		//illustrator2.illustrate(qp2); 
		qp2.setDB(db); 
		qp2.root().update(false); 
		
		SymbolicDB wellMergedDB = TupleByTupleMerger.BestEffortTupleByTupleMerger().
								merge((SymbolicDB)qp1.db(), (SymbolicDB)qp2.db()); 
		SymbolicDB unionMergedDB = TupleByTupleMerger.UnionMerger().
								merge((SymbolicDB)qp1.db(), (SymbolicDB)qp2.db());
			
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$"); */
		 
		
		/*GraphvizPrinter p1 = new GraphvizPrinter("/Users/nodira/Desktop/qp1.dot");
		p1.printDot(qp1);
		
		GraphvizPrinter p2 = new GraphvizPrinter("/Users/nodira/Desktop/qp2.dot");
		p2.printDot(qp2);
		
		GraphvizPrinter pBE = new GraphvizPrinter("/Users/nodira/Desktop/besteffort.dot"); 
		pBE.printDot(wellMergedDB); 
		
		GraphvizPrinter pUnion = new GraphvizPrinter("/Users/nodira/Desktop/union.dot"); 
		pUnion.printDot(unionMergedDB); 
		*/
		
		CascadingMerge merge = new CascadingMerge(CSE44Queries.Q1_FilmNoir(), 
				CSE44Queries.Q2_Officer444(),CSE44Queries.Q4_Directors3()); 
		SymbolicDB mergeDB = merge.mergedDB(); 
										 
		
		
		GraphvizPrinter p1 = new GraphvizPrinter("/Users/nodira/Desktop/qp1.dot");
		p1.printDot(qp1);
		
		SymbolicToRealSample strs = new SymbolicToRealSample((SymbolicDB)qp1.db(), "imdb-conn.properties"); 
		Assignment asg = strs.printSampleAndReturnAssignment(); 
		RealDB realdb = RealDB.constructDB((SymbolicDB)qp1.db(), asg);
		
		
//		RandomSampleGenerator random = new RandomSampleGenerator(2);
//		RealDB realdb = random.makeRandomSample(schema); 

		QueryPlan realPlan = QueryPlan.constructQueryPlan(qs1, realdb); 
		realPlan.root().update(false); 
		
		
		GraphvizPrinter pBER = new GraphvizPrinter("/Users/nodira/Desktop/real-sample.dot"); 
		pBER.printDot(realPlan); 
		
		
		
		QualityMeasurer measurer = new QualityMeasurer(new Completeness(), new Conciseness()); 
		measurer.printMetricResults(realPlan); 
		
		
		
	}
	
}