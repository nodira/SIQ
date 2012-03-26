package datagenerator;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import constraints.VariableConstraint;

import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

public class SymbolicToRealSample {
	static final int NUM_INSTANCES_DESIRED = 10; 
	
	SymbolicDB symbolicDB;
	String propertiesFile;  
	
	public SymbolicToRealSample(SymbolicDB symbolicDB, String propertiesFile){
		this.symbolicDB = symbolicDB;
		this.propertiesFile = propertiesFile; 
	}
	
	private Connection createConn(){
		try{
			Properties imdbProps = new Properties();
			imdbProps.load(new FileInputStream(propertiesFile));
			String url = imdbProps.getProperty("url");
			String driver = imdbProps.getProperty("driver");
			Class.forName(driver); 
			
			return DriverManager.getConnection(url, imdbProps); 
		}catch(Exception ex){
			ex.printStackTrace(); 
			System.exit(-1);
			return null; 
		}
		
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
			String tableName = t.underlyingSchema().getRelationName(); 
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
				
				String aliasDotCol = alias + "." + t.underlyingSchema().getAttribute(j) ;
				
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
		
		
		String query = "use imdb; select top " + NUM_INSTANCES_DESIRED + "  * \n" + fromClause.toString() + whereClause.toString(); 
		System.out.println(query); 
		Connection connToDB = createConn(); 
		try{
			Statement stmt = connToDB.createStatement();
			ResultSet rs = stmt.executeQuery(query); 
			printResultSetAndClose(rs);
			rs.close(); 
			stmt.close();
			connToDB.close(); 
		    
		}catch(Exception ex){
			ex.printStackTrace(); 
			System.exit(-1); 
		}
		
		return ""; 
		
		
		
		
		
		
		
	}

	
	public static void printResultSetAndClose(ResultSet rs) throws Exception{
		ResultSetMetaData rsmd = rs.getMetaData();

		//printColTypes(rsmd);
		System.out.println("");

		int numberOfColumns = rsmd.getColumnCount();

		for (int i = 1; i <= numberOfColumns; i++) {
			if (i > 1) System.out.print(",  ");
			String columnName = rsmd.getColumnName(i);
			System.out.print(columnName);
		}
		System.out.println("");
 
		while (rs.next()) {
			for (int i = 1; i <= numberOfColumns; i++) {
				if (i > 1) System.out.print(",  ");
				String columnValue = rs.getString(i);
				System.out.print(columnValue);
			}
			System.out.println("");  
		}
	}
      
	
	

	public static void printColTypes(ResultSetMetaData rsmd)
	throws SQLException {
		int columns = rsmd.getColumnCount();
		for (int i = 1; i <= columns; i++) {
			int jdbcType = rsmd.getColumnType(i);
			String name = rsmd.getColumnTypeName(i);
			System.out.print("Column " + i + " is JDBC type " + jdbcType);
			System.out.println(", which the DBMS calls " + name);
		}
	}


	
	
}

