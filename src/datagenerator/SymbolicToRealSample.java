package datagenerator;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import constraints.VariableConstraint;

import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

public class SymbolicToRealSample {
	SymbolicDB symbolicDB;
	Connection connToDB; 
	
	public SymbolicToRealSample(SymbolicDB symbolicDB, Connection connToDB){
		this.symbolicDB = symbolicDB;
		this.connToDB   = connToDB; 
	}
	
	public String printSample(){
		List<Variable> 		allVariables = new ArrayList<Variable>(); 
		List<SymbolicTuple> allTuples 	 = new ArrayList<SymbolicTuple>(); 
		Hashtable<SymbolicTuple, String>     symbolicTuple2Alias   = new Hashtable<SymbolicTuple, String>(); 
		Hashtable<Variable, List<String>> 	 variable2AliasDotCols = new Hashtable<Variable, List<String>>(); 
		
		for(SymbolicRelation rel: symbolicDB.relations()){
			for(SymbolicTuple tuple : rel.getTuples()){
				allTuples.add(tuple); 
			}
		}
		
		StringBuilder fromClause = new StringBuilder("FROM ");
		List<String> wherePredicates = new ArrayList<String>(); 
		
		for(int i=0; i<allTuples.size(); i++){
			SymbolicTuple t = allTuples.get(i); 
			String tableName = t.underlyingRelation().relationSchema().getRelationName(); 
			String alias 	 = "" + tableName.charAt(0) + i; 
			
			fromClause.append(tableName + " " + alias); 
			if(i < allTuples.size() -1){//not the last tuple
				fromClause.append(", "); 
			}else{
				fromClause.append("\n"); 
			}
			symbolicTuple2Alias.put(t, alias); //record mappings from tuple 2 alias
			
			//iterate thru each column of symbolic tuple, and collect variable occurence info
			for(int j=0; j<t.arity(); j++){
				Variable v = t.getColumn(j); 
				if(allVariables.contains(v) == false){
					allVariables.add(v); 
				}
				
				String aliasDotCol = alias + "." + t.underlyingRelation().relationSchema().getAttribute(j) ;
				
				if(variable2AliasDotCols.containsKey(v) == false){
					variable2AliasDotCols.put(v, new ArrayList<String>()); 
				}
				variable2AliasDotCols.get(v).add(aliasDotCol); 
			}
		}
		//add all the predicates from the repeated occurrence of variables
		for(Variable v : allVariables){
			List<String> occurences = variable2AliasDotCols.get(v);
			for(int j=1; j<occurences.size(); j++){
				wherePredicates.add(occurences.get(j-1) + " = " + occurences.get(j)); 
			}
		}
		
		
		
		//now need to add all the constraints as predicates
		for(Variable v : allVariables){
			List<String> occurences = variable2AliasDotCols.get(v);
			for(VariableConstraint c : v.getConstraints()){
				for(String aliasDotCol : occurences){
					wherePredicates.add(c.toSqlString(aliasDotCol)); 
				}
			}
		}
		
		
		//construct where clause from list of predicates
		StringBuilder whereClause = new StringBuilder(); 
		for(int i=0; i<wherePredicates.size(); i++){
			if(i == 0){
				whereClause.append("WHERE "); 
			}
			whereClause.append(wherePredicates.get(i));
			if(i != wherePredicates.size()-1){//not the last one
				whereClause.append(" AND "); 
			}else{
				whereClause.append("\n"); 
			}
		}
		
		
		return "select * \n" + fromClause.toString() + whereClause.toString(); 
		
		
		
		
		
		
		
	}
	
	
}
