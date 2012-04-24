package query;
import java.util.List;
import java.util.ArrayList; 

import schema.DBSchema;

import edu.washington.db.cqms.snipsuggest.features.F_AggregateInSelect;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInGroupBy;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInSelect;
import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;
import edu.washington.db.cqms.snipsuggest.features.F_TableInFrom;
import edu.washington.db.cqms.snipsuggest.features.QueryFeature;


public class QuerySession {

	DBSchema schema; 
	String querySessionId; 
	List<List<QueryFeature>> steps;
	
	public QuerySession(String querySessionId, DBSchema schema){
		this.querySessionId = querySessionId; 
		this.schema = schema; 
		steps = new ArrayList<List<QueryFeature>>(); 
	}
	
	public int numSteps(){
		return steps.size(); 
	}
	
	public void addSnippets(List<QueryFeature> list){
		steps.add(list); 
	}
	
	public String getId(){
		return querySessionId; 
	}
	
	public void addSnippets(QueryFeature... snippets){
		ArrayList<QueryFeature> snippetSet = new ArrayList<QueryFeature>();
		for(QueryFeature snippet : snippets){
			snippetSet.add(snippet); 
		}
		addSnippets(snippetSet); 
		
		
	}
	
	public boolean singleSnippetAddedEachTime(){
		for(List<QueryFeature> step : steps){
			if(step.size() == 1){
				//good
			}else{
				return false; 
			}
		}
		return false; 
		
	}
	
	public List<List<QueryFeature>> getSteps(){
		return steps; 
	}
	
	public DBSchema getSchema(){
		return schema; 
	}
	
	public List<QueryFeature> snippetsAddedAtStep(int i){
		return steps.get(i); 
	}
	
	public String queryTextAt(int i){
		List<QueryFeature> allSnippets = new ArrayList<QueryFeature>();
		for(int j = 0; j<= i; j++){
			allSnippets.addAll(steps.get(j)); 
		}

		return snippetsToQueryText(allSnippets); 
	}
	
	private String snippetsToQueryText(List<QueryFeature> snippets){
		
		String FROM = "FROM ";
		String WHERE = "WHERE "; 
		String SELECT = "SELECT "; 
		String GROUPBY = "GROUP BY "; 
		
		for(QueryFeature snippet : snippets){
			if(snippet instanceof F_TableInFrom){
				FROM += ((F_TableInFrom) snippet).getTableName() + ","; 
			}else if(snippet instanceof F_PredicateInWhere){
				WHERE += ((F_PredicateInWhere) snippet).toQueryString() + " AND "; 
			}else if(snippet instanceof F_ColumnInGroupBy){
				F_ColumnInGroupBy col = (F_ColumnInGroupBy) snippet; 
				GROUPBY += col.getTableName() + "." + col.getColumnName() + ","; 
			}else if(snippet instanceof F_ColumnInSelect){
				F_ColumnInSelect col = (F_ColumnInSelect) snippet; 
				SELECT += col.getTableName() + "." + col.getColumnName() + ","; 
			}else if(snippet instanceof F_AggregateInSelect){
				F_AggregateInSelect agg = (F_AggregateInSelect) snippet;
				SELECT += agg.getAggregate() + "(" + agg.getTablesAndCols() + "),"; 
			}
			
		}
		
		FROM  = FROM.substring(0, FROM.length() - 1);
		WHERE = WHERE.substring(0, WHERE.length() - " AND ".length());
		
		if(SELECT.length() > "SELECT ".length()){
			SELECT  = SELECT.substring(0, SELECT.length() - 1);
		}else{
			SELECT = "SELECT *";
		}
		
		if(GROUPBY.length() > "GROUP BY ".length()){
			GROUPBY  = GROUPBY.substring(0, GROUPBY.length() - 1);
			
		}
		
		
		
		return SELECT + "\n" + FROM + "\n" + WHERE + "\n" + GROUPBY; 

	}	

	public QuerySession cut(int k){
		QuerySession clone = new QuerySession(this.querySessionId + "_cut" + k, this.schema);
		
		for(int i=0; i<k; i++){
			clone.addSnippets(this.steps.get(i)); 
			
		}
		
		return clone; 
		
	}
}
