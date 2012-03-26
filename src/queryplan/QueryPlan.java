package queryplan;
import java.util.ArrayList;
import java.util.List;

import edu.washington.db.cqms.snipsuggest.features.F_AggregateInSelect;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInGroupBy;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInSelect;
import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;
import edu.washington.db.cqms.snipsuggest.features.F_SubqueryInWhere;
import edu.washington.db.cqms.snipsuggest.features.F_TableInFrom;
import edu.washington.db.cqms.snipsuggest.features.QueryFeature;
import query.QuerySession;
import schema.DBSchema;
import symbolicdb.SymbolicRelation;


public class QueryPlan {
	QueryOperator rootOperator;
	
	public QueryPlan(QueryOperator root){
		this.rootOperator = root; 	
	}
	
	public QueryOperator root(){
		return rootOperator; 
	}
	
	public static QueryPlan constructQueryPlan(QuerySession qs){
		DBSchema schema = qs.getSchema(); 
		
		QueryOperator lastOp = null;
		
		//iterate thru each step of query session
		for(int i=0; i < qs.numSteps(); i++){
			List<QueryFeature> step = qs.snippetsAddedAtStep(i); 
			QueryFeature addedSnippet = step.get(0);
			
			if(addedSnippet instanceof F_TableInFrom){
				F_TableInFrom table = (F_TableInFrom) addedSnippet; 
				TableOperator rawOp = new TableOperator(new SymbolicRelation(schema.get(table.getTableName()))); 
				
				if(lastOp == null){
					lastOp = rawOp; 
				}else{
					CartesianOperator cartesianOp = new CartesianOperator(lastOp, rawOp); 
					lastOp = cartesianOp; 
				}
			}else if(addedSnippet instanceof F_PredicateInWhere){
				F_PredicateInWhere pred = (F_PredicateInWhere) addedSnippet; 
				FilterOperator filterOp = new FilterOperator(lastOp, pred); 
				lastOp = filterOp;
				
				
			}else if(addedSnippet instanceof F_ColumnInGroupBy){
				F_ColumnInGroupBy groupbyCol = (F_ColumnInGroupBy) addedSnippet;
				int colIndex = lastOp.currentSchema.getAttributeIndex(groupbyCol.getTableName() + "_" + groupbyCol.getColumnName());
				GroupbyCountOperator groupOp = new GroupbyCountOperator(lastOp, colIndex); 
				lastOp = groupOp; 
			}else if(addedSnippet instanceof F_ColumnInSelect){
				List<Integer> selectedCols = new ArrayList<Integer>();
				for(QueryFeature qf : step){
					F_ColumnInSelect selectedCol = (F_ColumnInSelect) qf; 
					int colIndex = lastOp.currentSchema.getAttributeIndex(selectedCol.getColumnName());
					selectedCols.add(colIndex); 
				}
				ProjectOperator projectOp = new ProjectOperator(lastOp, selectedCols);
				lastOp = projectOp; 
			}else if(addedSnippet instanceof F_AggregateInSelect){
				throw new RuntimeException("Not supported: " + addedSnippet.getClass()); 
				
			}else if(addedSnippet instanceof F_SubqueryInWhere){
				throw new RuntimeException("Not supported: " + addedSnippet.getClass()); 
			}
		}
		return new QueryPlan(lastOp); 
		
	}
	
}
