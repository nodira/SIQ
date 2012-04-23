package symbolicdb;

import edu.washington.db.cqms.snipsuggest.features.F_ColumnInGroupBy;
import edu.washington.db.cqms.snipsuggest.features.F_ColumnInSelect;
import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;
import edu.washington.db.cqms.snipsuggest.features.F_TableInFrom;
import query.QuerySession;
import schema.IMDBSchemaWithKeys;

public class CSE44Queries {
	
	/**
	 * List all the directors who directed a 'Film-Noir' movie, 
	 * cant do this part: in a leap year.
	 * Your query should return director name, the movie name, and the year. 
	 *
	 * select   d.fname, d.lname, m.name, m.year
       from     movie m, movie_directors md, directors d, genre g
       where   m.id = md.mid AND md.did = d.id AND g.mid = m.id AND 
             g.genre = 'Film-Noir' 
	 * @return
	 */
	public static QuerySession Q1_FilmNoir(){
		QuerySession qs = new QuerySession("q1_filmnoir", IMDBSchemaWithKeys.getInstance()); 
		
		//tables
		qs.addSnippets(new F_TableInFrom("movie")); 
		qs.addSnippets(new F_TableInFrom("movie_directors"));
		qs.addSnippets(new F_TableInFrom("directors"));
		qs.addSnippets(new F_TableInFrom("genre")); 

		//join predicates
		qs.addSnippets(new F_PredicateInWhere("movie", "id", "movie_directors", "mid", "=")); 
		qs.addSnippets(new F_PredicateInWhere("movie_directors", "did", "directors", "id", "=")); 
		qs.addSnippets(new F_PredicateInWhere("genre", "mid", "movie", "id", "=")); 
		
		//film-noir predicate 
		qs.addSnippets(new F_PredicateInWhere("genre", "genre", null, "'Film-Noir'", "=")); 
		
		//project columns
		qs.addSnippets(	new F_ColumnInSelect("directors", "fname"), 
						new F_ColumnInSelect("directors", "lname"),
						new F_ColumnInSelect("movie", "name"),
						new F_ColumnInSelect("movie", "year"));
		
		return qs; 
	}

	/**
	 * List the first and last names of all the actors who played in 
	 * the movie 'Officer 444'. 
	 * 
	 * select a.fname, a.lname
	   from actors a, casts c, movies m
       where c.mid = m.id and
		 	 c.pid = a.id and
		 	 m.name = 'Officer 444'; 
	 * @return
	 */
	public static QuerySession Q2_Officer444(){
		QuerySession qs = new QuerySession("Q2_Officer444", IMDBSchemaWithKeys.getInstance()); 
		
		//tables
		qs.addSnippets(new F_TableInFrom("actor")); 
		qs.addSnippets(new F_TableInFrom("casts")); 
		qs.addSnippets(new F_TableInFrom("movie")); 
		
		//join predicates
		qs.addSnippets(new F_PredicateInWhere("casts", "mid", "movie", "id", "="));
		qs.addSnippets(new F_PredicateInWhere("casts", "pid", "actor", "id", "="));
		
		//officer_444 predicate
		qs.addSnippets(new F_PredicateInWhere("movie", "name", null, "'Officer 444'", "="));
		
		//SELECT clause
		qs.addSnippets(new F_ColumnInSelect("actor", "fname"), new F_ColumnInSelect("actor", "lname") );
		return qs; 
	}
	
	//cant do Q3 coz there is a self-join. 
	

	/**
	 * List all directors who directed 150 movies or more, in descending order of # movies.  
	 * Return the directors' names and the number of movies each of them directed.
	 * 
	 * select d.fname, d.lname, count(*) as c
       from directors d, movie_directors md
       where d.id = md.did
       group by d.id, d.fname, d.lname
       having count(*) >= 150

	 */
	public static QuerySession Q4_Directors3(){
		QuerySession qs = new QuerySession("Q4_Directors150", IMDBSchemaWithKeys.getInstance());
		
		//tables
		qs.addSnippets(new F_TableInFrom("directors")); 
		qs.addSnippets(new F_TableInFrom("movie_directors")); 
		
		//join pred
		qs.addSnippets(new F_PredicateInWhere("directors", "id", "movie_directors", "did", "="));
		
		//group-by 
		qs.addSnippets(new F_ColumnInGroupBy("directors", "id"),  
					   new F_ColumnInGroupBy("directors", "fname"),
					   new F_ColumnInGroupBy("directors", "lname"));  
		
		
		//having clause? 
		qs.addSnippets(new F_PredicateInWhere("G", "count", null, "3", ">=")); 
		
		
		return qs; 
	}
	
	
}
