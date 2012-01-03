package symbolicdb;

import java.util.HashMap;
import java.util.Map;

import query.QuerySession;
import templates.RoundRobinTemplateFilling;
import edu.washington.db.cqms.common.sqlparser.DBSchema;
import edu.washington.db.cqms.common.sqlparser.IMDBSchema;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInGroupBy;
import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;
import edu.washington.db.cqms.snipsuggest.features.F_TableInFrom;
import edu.washington.db.cqms.snipsuggest.features.QueryFeature;

public class Main {

    
	public static void main(String[] args) throws Exception{
		QuerySession qs = getQS(); 
		generateViews(qs, IMDBSchema.getInstance()); 
	}
	
	public static QuerySession getQS(){
		QuerySession qs = new QuerySession("qs1", IMDBSchema.getInstance()); 
		
		//step 1
		qs.addSnippets(new F_TableInFrom("actor")); 
		
		//step 2
		qs.addSnippets(new F_PredicateInWhere("actor", "gender", null, "'f'", "=")); 
		
		//step 3
		qs.addSnippets(new F_TableInFrom("casts")); 
		
		//step 4
		qs.addSnippets(new F_PredicateInWhere("casts", "pid", "actor", "id", "="));
		
		//step 5
		qs.addSnippets(new F_ColumnInGroupBy("actor", "id")); 

		//step 6
		/*qs.addSnippets(new F_ColumnInSelect("actor", "id"));
		
		//step 7
		ArrayList<String> tables = new ArrayList<String>(); 
		tables.add("actor");
		ArrayList<String> cols = new ArrayList<String>(); 
		cols.add("id"); 
		
		qs.addSnippets(new F_AggregateInSelect("count", tables, cols));
		*/
		
		//System.out.println(qs.queryTextAt(6));
		
		return qs; 
	}

	public static void generateViews(QuerySession qs, DBSchema schema){
		Map<String, SimpleView> viewName2View = new HashMap<String, SimpleView>(); 
		SimpleView lastView = null; 
		
		//iterate thru each step of query session
		for(int i=0; i < qs.numSteps(); i++){
			QueryFeature addedSnippet = qs.snippetsAddedAtStep(i).get(0);
			SimpleView v; 
			
			if(addedSnippet instanceof F_TableInFrom){
				F_TableInFrom table = (F_TableInFrom) addedSnippet; 
				SimpleView tableView = new SimpleView(schema, table.getTableName()); 
				viewName2View.put(tableView.getViewName(), tableView); 
				
				if(lastView == null){
					v =  new SimpleView("V" + i, addedSnippet, schema, tableView);
				}else{
					v =  new SimpleView("V" + i, addedSnippet, schema, tableView, lastView);
				}
				
			}else{
				v = new SimpleView("V" + i, addedSnippet, schema, lastView);
				
			}
			viewName2View.put(v.getViewName(), v); 
			lastView = v; 
		}
		
		
		//add tuple
//		SymbolicTuple tActor1 = SymbolicTuple.constructTupleWithNewVariables(4); 
//		SymbolicTuple tActor2 = SymbolicTuple.constructTupleWithNewVariables(4); 
//		SymbolicTuple tActor3 = SymbolicTuple.constructTupleWithNewVariables(4); 
//		
//		viewName2View.get("actor").addTuple(tActor1); 
//		viewName2View.get("actor").addTuple(tActor2); 
//		viewName2View.get("actor").addTuple(tActor3); 
//		
//		
//		SymbolicTuple tCasts = new SymbolicTuple(new Variable(), new Variable(), new Variable());
//		viewName2View.get("casts").addTuple(tCasts); 
//				
//		lastView.update(); 
		System.out.println(lastView.recursiveToString()); 
		
		System.out.println("##########################"); 
		

		RoundRobinTemplateFilling fillingTechnique = new RoundRobinTemplateFilling(schema); 
		
		fillingTechnique.fillTemplates(lastView); 
		
		System.out.println(lastView.recursiveToString()); 
		
		
		
		
		
		
	}

}


