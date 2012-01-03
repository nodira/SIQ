package view;

import java.util.ArrayList;
import java.util.List;

import query.QuerySession;

import edu.washington.db.cqms.common.sqlparser.DBSchema;
import edu.washington.db.cqms.common.sqlparser.RelationSchema;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInGroupBy;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInSelect;
import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;
import edu.washington.db.cqms.snipsuggest.features.F_TableInFrom;
import edu.washington.db.cqms.snipsuggest.features.QueryFeature;

public class QuerySessionWithViews {

	QuerySession underlyingQuerySession; 
	List<SymbolicDBView> correspondingViews;
	
	public List<SymbolicDBView> getCorrespondingViews() {
		return correspondingViews;
	}

	public QuerySessionWithViews(QuerySession underlyingQuerySession){
		this.underlyingQuerySession = underlyingQuerySession; 
		
		this.correspondingViews = new ArrayList<SymbolicDBView>(); 
		
		for(int i=0; i < underlyingQuerySession.numSteps(); i++){
			
			List<QueryFeature> step = underlyingQuerySession.getSteps().get(i); 
			assert(step.size() == 1); 
			
			StringBuilder viewDefinition = new StringBuilder("CREATE VIEW V" + i + " AS \nSELECT "); 
			List<String> viewColumns = new ArrayList<String>(); 
			
			QueryFeature addedSnippet = step.get(0); 
			if(i == 0){
				F_TableInFrom table = (F_TableInFrom) addedSnippet; 
				RelationSchema tableSchema = underlyingQuerySession.getSchema().get(table.getTableName());
				 
				for(String col : tableSchema.getAttributes()){
					String fullColName = tableSchema.getRelationName() + "." + col ; 
					viewDefinition.append( fullColName + " AS " + fullColName.replace(".", "") + "_" + i + ","); 
					viewColumns.add(fullColName.replace(".", "")+"_" + i);
				}
				viewDefinition.deleteCharAt(viewDefinition.length() - 1); //remove last comma
				viewDefinition.append("\nFROM " + table.getTableName() + ";"); 
				
			}else if(addedSnippet instanceof F_TableInFrom){
				F_TableInFrom addedTable = (F_TableInFrom) addedSnippet; 
				RelationSchema tableSchema = underlyingQuerySession.getSchema().get(addedTable.getTableName());
				
				//columns from V_i-1
				for(String oldColName : correspondingViews.get(i-1).columnNames){
					viewDefinition.append(oldColName + " AS " + oldColNameToNewColName(oldColName, i) + ",");
					viewColumns.add(oldColNameToNewColName(oldColName, i)); 
				}
				 
				//columns from addedTable
				for(String col : tableSchema.getAttributes()){
					String fullColName = tableSchema.getRelationName() + "." + col ; 
					viewDefinition.append( fullColName + " AS " + fullColName.replace(".", "") + "_" + i + ","); 
					viewColumns.add(fullColName.replace(".", "") + "_" + i);
				}
				viewDefinition.deleteCharAt(viewDefinition.length() - 1); //remove last comma
				viewDefinition.append("\nFROM V" + (i-1) + "," + addedTable.getTableName() + ";"); 
			
				
				
			}else if(addedSnippet instanceof F_PredicateInWhere){
				F_PredicateInWhere addedPred = (F_PredicateInWhere) addedSnippet;
				//columns from V_i-1
				
				for(String oldColName : correspondingViews.get(i-1).columnNames){
					viewDefinition.append(oldColName + " AS " + oldColNameToNewColName(oldColName, i) + ",");
					viewColumns.add(oldColNameToNewColName(oldColName, i)); 
				}
				viewDefinition.deleteCharAt(viewDefinition.length() - 1); //remove last comma
				viewDefinition.append("\nFROM V" + (i-1)); 
				viewDefinition.append("\nWHERE " + predicateToViewPredicate(addedPred, (i-1)) + ";"); 
			}else if(addedSnippet instanceof F_ColumnInGroupBy){
				F_ColumnInGroupBy addedGroupbyCol = (F_ColumnInGroupBy) addedSnippet;
				
				viewDefinition.append(colNameToViewColName(addedGroupbyCol.getTableName(), addedGroupbyCol.getColumnName(), i-1)  + " AS " + 
						colNameToViewColName(addedGroupbyCol.getTableName(), addedGroupbyCol.getColumnName(), i) ); 
				
				viewDefinition.append(", count(*)"); 
				viewDefinition.append("\nFROM V" + (i-1));
				viewDefinition.append("\nGROUP BY " + colNameToViewColName(addedGroupbyCol.getTableName(), addedGroupbyCol.getColumnName(), i-1)); 
				viewColumns.add(colNameToViewColName(addedGroupbyCol.getTableName(), addedGroupbyCol.getColumnName(), i)); 
				
				
			}
			
			SymbolicDBView sv = new SymbolicDBView("V" + i, viewDefinition.toString(), viewColumns);
			if(i>0){
				sv.setBackRule(createBackRule(underlyingQuerySession.getSchema(), addedSnippet, 
							sv, correspondingViews.get(i-1), i));
			}else{
				sv.setBackRule(createBackRule(underlyingQuerySession.getSchema(), addedSnippet, 
						sv, null, i));
			}
			correspondingViews.add(sv); 
			
			
			
			
		}
		
		
		
	}
	
	private static String colNameToViewColName(String tableName, String colName, int viewIndex){
		return tableName + colName + "_" + viewIndex; 
		
	}
	
	private static String oldColNameToNewColName(String oldColName, int newViewIndex){
		return oldColName.replace("_"+(newViewIndex-1), "_"+(newViewIndex)+"");
		
	}
	
	private static String predicateToViewPredicate(F_PredicateInWhere pred, int viewIndex){
		String newName1 = pred.getTable1() + pred.getCol1() + "_" + viewIndex;
		String newName2 = pred.getTable2() + pred.getCol2() + "_" + viewIndex;
		
		if(pred.lhsIsConst()){
			newName1 = "'" + pred.getCol1() + "'"; 
		}else if(pred.rhsIsConst()){
			newName2 = "'" + pred.getCol2() + "'"; 
		}
		return newName1 + pred.getOp() + newName2; 
	}
	
	private static String createBackRule(DBSchema baseSchema,
										 QueryFeature addedSnippet, 
										 SymbolicDBView newView, 
										 SymbolicDBView underlyingView,
										 int newViewIndex){
		
		StringBuilder backrule = new StringBuilder("CREATE RULE R_" + newView.getViewName() + " AS \n" + 
						  "ON INSERT TO " + newView.getViewName() + " DO INSTEAD \n");  
				
		if(newViewIndex == 0){
			F_TableInFrom addedTable = (F_TableInFrom) addedSnippet; 
			backrule.append("INSERT INTO " + addedTable.getTableName() + " VALUES ("); 
			
			//cols from newly added table
			RelationSchema tableSchema = baseSchema.get(addedTable.getTableName());
			for(String col : tableSchema.getAttributes()){
				backrule.append("NEW." + addedTable.getTableName() + col + "_" + newViewIndex + ","); 
			}
			backrule.deleteCharAt(backrule.length() -1); //remove last comma
			backrule.append(");"); 
			
		}else if(addedSnippet instanceof F_TableInFrom){
			F_TableInFrom addedTable = (F_TableInFrom) addedSnippet; 
			backrule.append("(INSERT INTO " + underlyingView.getViewName() + " VALUES ("); 
			
			//cols from underlying view
			for(String col : underlyingView.getColumnNames()){
				backrule.append( "NEW." + oldColNameToNewColName(col, newViewIndex) + ",") ; 
			}
			backrule.deleteCharAt(backrule.length() -1); //remove last comma
			backrule.append("); \nINSERT INTO " + addedTable.getTableName() + " VALUES(");
			
			//cols from newly added table
			RelationSchema tableSchema = baseSchema.get(addedTable.getTableName());
			for(String col : tableSchema.getAttributes()){
				backrule.append("NEW." + addedTable.getTableName() + col + "_" + newViewIndex + ","); 
			}
			backrule.deleteCharAt(backrule.length() -1); //remove last comma
			backrule.append("));"); 
		}else if(addedSnippet instanceof F_PredicateInWhere){
			F_PredicateInWhere addedPred = (F_PredicateInWhere) addedSnippet;
			
			backrule.append("(INSERT INTO " + underlyingView.getViewName() + " VALUES ("); 
			
			//cols from underlying view
			for(String col : newView.getColumnNames()){
				backrule.append( "NEW." + col + ",") ; 
			}
			backrule.deleteCharAt(backrule.length() -1); //remove last comma
			backrule.append(");");
			
			//add constraint
			
			if(addedPred.rhsIsConst()){
				backrule.append("\nINSERT INTO Constraints VALUES(");
				backrule.append("NEW." + addedPred.getTable1() + addedPred.getCol1() + "_" + newViewIndex + ",'" +
							addedPred.getOp() + addedPred.getCol2() + "')"); 
			}else if(addedPred.lhsIsConst()){
				backrule.append("\nINSERT INTO Constraints VALUES(");
				backrule.append("NEW." + addedPred.getTable2() + addedPred.getCol2() + "_" + newViewIndex + ",'" +
						"reverse(" + addedPred.getOp() + ")"+ addedPred.getCol1() + "')"); 
			}else{
				backrule.append("\nINSERT INTO VarConstraints VALUES(");
				backrule.append("NEW." + addedPred.getTable1() + addedPred.getCol1() + "_" + newViewIndex + "," + 
								"NEW." + addedPred.getTable2() + addedPred.getCol2() + "_" + newViewIndex + ",'" + 
								addedPred.getOp() + "')"); 
			}
			
			backrule.append("); ");
			
		}else if(addedSnippet instanceof F_ColumnInSelect){
			//direct backrule. since we ignore projects. 
			backrule.append("INSERT INTO " + underlyingView.getViewName() + " VALUES ("); 
			
			//cols from underlying view
			for(String col : underlyingView.getColumnNames()){
				backrule.append( "NEW." + oldColNameToNewColName(col, newViewIndex) + ",") ; 
			}
			backrule.deleteCharAt(backrule.length() -1); //remove last comma
			backrule.append(");"); 
		}
		
		//problem: grouped by col1 [v1], and then we add col2 to the grouping[v2]. 
		//v2 can not be computed from v1. 
		
		
		return backrule.toString(); 
		
		
	}
	
	
}
