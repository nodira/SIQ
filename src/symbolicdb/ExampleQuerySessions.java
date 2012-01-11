package symbolicdb;

import query.QuerySession;
import schema.IMDBSchemaWithKeys;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInGroupBy;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInSelect;
import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;
import edu.washington.db.cqms.snipsuggest.features.F_TableInFrom;

public class ExampleQuerySessions {
	public static QuerySession getQS1(){
		QuerySession qs = new QuerySession("qs1", IMDBSchemaWithKeys.getInstance()); 
		
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
	
	public static QuerySession getQS2(){
		//List the first and last names of all the actors who played in the movie 'Officer 444'. [13 rows]

		/*
		 * select a.fname, a.lname
		 * from actors a, casts c, movies m
		 * where m.name = 'Officer 444' and 
		 * 		 c.mid = m.id and
		 * 		 c.pid = a.id 
		 */
	
		
		QuerySession qs = new QuerySession("qs2", IMDBSchemaWithKeys.getInstance()); 
		qs.addSnippets(new F_TableInFrom("actor")); 
		qs.addSnippets(new F_TableInFrom("casts")); 
		qs.addSnippets(new F_TableInFrom("movie")); 
		
		qs.addSnippets(new F_PredicateInWhere("casts", "pid", "actor", "id", "="));
		qs.addSnippets(new F_PredicateInWhere("casts", "mid", "movie", "id", "="));
		qs.addSnippets(new F_PredicateInWhere("movie", "name", null, "'Officer 444'", "="));
		
		qs.addSnippets(new F_ColumnInSelect("actor", "fname"));
		qs.addSnippets(new F_ColumnInSelect("actor", "lname"));
		
		
		
		
		return qs; 
	}
	
	
	
}
