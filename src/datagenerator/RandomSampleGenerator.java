package datagenerator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import realdb.RealDB;
import realdb.RealTuple;
import realdb.RealValue.StringValue;
import realdb.RealValue; 
import schema.DBSchema;
import schema.DBSchema.SimpleForeignKey;
import schema.RelationSchema;


/**
 * Generates a random sample for the given schema. 
 * 
 * It basically runs the following query and returns the result as a RealDB
 * 
 * select * from A, C, M, MD, D, G
		where ... all the join predicates ...
		order by random()
		limit k. 
 * 
 * 
 * @author nodira
 *
 */
public class RandomSampleGenerator {
	int k; 
	
	public RandomSampleGenerator(int k){
		this.k = k; 
	}
	
	public RealDB makeRandomSample(DBSchema schema){
		String sql = sqlString(schema); 
		
		System.out.println("SQL: \n " + sql); 
		
		Connection conn = SymbolicToRealSample.createConn("imdb-conn.properties");
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql); 
			RealDB db = toRealDB(rs, schema); 
			rs.close(); 
			stmt.close();
			conn.close(); 
			
			return db; 
		}catch(Exception ex){
			ex.printStackTrace(); 
			return null; 
		}	
	}
	
	private RealDB toRealDB(ResultSet rs, DBSchema schema){
		try {
			RealDB db = new RealDB(schema);
			
			ResultSetMetaData rsmd = rs.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();
			while (rs.next()) {
				LinkedList<RealValue> cells = new LinkedList<RealValue>();
				for(int i=1; i <= numberOfColumns; i++){
					//TODO: support DoubleValue's too! 
					cells.add(new StringValue(rs.getString(i))); 
				}
				
				for(RelationSchema rel : schema.getRelations()){
					RealTuple t = new RealTuple(rel); 
					for(int i=0; i<t.arity(); i++){
						t.setColumn(i, cells.poll());
					}
					db.addTuple(t); 
				}
			}
			return db; 
		} catch (SQLException e) {
			e.printStackTrace();
			return null; 
		}
		
		
		
	}
	
	private String sqlString(DBSchema schema){
		StringBuilder sb = new StringBuilder("SELECT * FROM ");
		 
		for(int i=0; i < schema.getRelations().size(); i++){
			RelationSchema rel = schema.getRelations().get(i); 
			sb.append(rel.getRelationName()); //+ " " + rel.getRelationName().charAt(0) + i);
			
			if(i != schema.getRelations().size()-1){
				sb.append(", "); 
			}
		}
		
		sb.append("\nWHERE "); 
		
		for(int i=0; i<schema.getForeignKeys().size(); i++){
			SimpleForeignKey fk = schema.getForeignKeys().get(i);
			sb.append(fk.col1().relDotCol() + "=" + fk.col2().relDotCol()); 
			
			if(i != schema.getForeignKeys().size()-1){
				sb.append(" AND "); 
			}
		}
		
		sb.append("\nORDER BY random() \nLIMIT " + k);
		
		return sb.toString(); 
		
		/**
		 * THIS IS SUUUUPER SLOW. COULD EASILY MATERIALIZE THE FULL JOIN AND TAKE
		 * RANDOM TUPLES FROM THAT? JUST NEED A BIT OF COLUMN_RENAMING. 
		 * 
		 */
	}
}
