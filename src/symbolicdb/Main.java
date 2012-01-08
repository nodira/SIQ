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
		QuerySession qs = ExampleQuerySessions.getQS1(); 
		generateViews(qs, IMDBSchema.getInstance()); 
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
		
		
		System.out.println(lastView.recursiveToString()); 
		
		System.out.println("##########################"); 
		

		RoundRobinTemplateFilling fillingTechnique = new RoundRobinTemplateFilling(schema); 
		
		fillingTechnique.fillTemplates(lastView); 
		
		System.out.println(lastView.recursiveToString()); 
		
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%#"); 
		
		SymbolicDB symDB = SymbolicDB.constructDB(lastView);
		System.out.println(symDB); 
		
		
		
		
	}

}


