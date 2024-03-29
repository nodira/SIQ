package datagenerator;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
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

import realdb.GeneralRelation;
import realdb.RealValue;
import symbolicdb.Assignment;
import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Tuple;
import symbolicdb.Variable;

public class SymbolicToRealSample {
	static final int NUM_INSTANCES_DESIRED = 1; 
	
	SymbolicDB symbolicDB;
	String propertiesFile;  
	
	public SymbolicToRealSample(SymbolicDB symbolicDB, String propertiesFile){
		this.symbolicDB = symbolicDB;
		this.propertiesFile = propertiesFile; 
	}
	
	public static Connection createConn(String propertiesFile){
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
	
	public Assignment printSampleAndReturnAssignment(){
		List<Variable> 		allVariables = new ArrayList<Variable>(); 
		List<SymbolicTuple> allTuples 	 = new ArrayList<SymbolicTuple>(); 
		Hashtable<SymbolicTuple, String>     symbolicTuple2Alias   = new Hashtable<SymbolicTuple, String>(); 
		Hashtable<Variable, List<String>> 	 variable2AliasDotCols = new Hashtable<Variable, List<String>>(); 
		
		Hashtable<Integer, Variable> columnIndex2Var = new Hashtable<Integer, Variable>(); 
		
		//collect all tuples
		for(GeneralRelation rel: symbolicDB.relations()){
			for(Tuple tuple : rel.getTuples()){
				allTuples.add((SymbolicTuple)tuple); 
			}
		}
		
		StringBuilder fromClause = new StringBuilder("FROM ");
		List<String> wherePredicates = new ArrayList<String>(); 
		List<String> aliases = new ArrayList<String>(); 
		
		int columnsAdded = 1;
		for(int i=0; i<allTuples.size(); i++){
			SymbolicTuple t = allTuples.get(i); 
			String tableName = t.underlyingSchema().getRelationName(); 
			String alias 	 = "" + tableName.charAt(0) + i; 
			
			aliases.add(alias); 
			
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
				columnIndex2Var.put(columnsAdded, v); 
				if(allVariables.contains(v) == false){
					allVariables.add(v); 
				}
				String aliasDotCol = alias + "." + t.underlyingSchema().getAttribute(j) ;
				columnsAdded++;
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
		
		//need to add predicates to say tuples can not be picked twice
		for(int i=0; i<aliases.size(); i++){
			String alias1 = aliases.get(i); 
			for(int j=i+1; j<aliases.size(); j++){
				String alias2 = aliases.get(j); 
				if(alias1.charAt(0) == alias2.charAt(0)){//potentially same table
					if(whereClause.length() == 0){
						whereClause.append(alias1 + ".oid <> " + alias2 + ".oid");
					}else{
						whereClause.append(" AND " + alias1 + ".oid <> " + alias2 + ".oid");
					}
				}
			}
		}
		
		
		//String query = "use imdb; select top " + NUM_INSTANCES_DESIRED + "  * \n" + fromClause.toString() + whereClause.toString(); 
		String query = "select * \n" + fromClause.toString() + whereClause.toString() + "\n LIMIT " + NUM_INSTANCES_DESIRED; 
		
		System.out.println(query); 
		Connection connToDB = createConn(propertiesFile); 
		
		Assignment asg = null;   
		try{
			Statement stmt = connToDB.createStatement();
			ResultSet rs = stmt.executeQuery(query); 
			asg = printResultSetAndClose(rs, columnIndex2Var);
			rs.close(); 
			stmt.close();
			connToDB.close(); 
		    
		}catch(Exception ex){
			ex.printStackTrace(); 
			System.exit(-1); 
		}
		
		return asg; 
		
		
		
		
		
		
		
	}

	
	public static Assignment printResultSetAndClose(ResultSet rs, Hashtable<Integer, Variable> columnIndex2Var) throws Exception{
		ResultSetMetaData rsmd = rs.getMetaData();
		Assignment asg = new Assignment(); 
		
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
				

				if(columnIndex2Var.containsKey(i)){//coz the other one is 
					//TODO: choose DoubleValue if it is indeed. 
					asg.assign(columnIndex2Var.get(i), new RealValue.StringValue(columnValue)); 
				}
			}
			System.out.println("");  
		}
		
		return asg; 
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

