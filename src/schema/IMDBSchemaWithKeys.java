package schema; 

public final class IMDBSchemaWithKeys {
    public static DBSchema getInstance() {
        DBSchema schema = new DBSchema("imdb");

        RelationSchema actor = new RelationSchema("actor");
        actor.addKeyAttribute("id");
        actor.addAttribute("fname");
        actor.addAttribute("lname");
        actor.addAttribute("gender");
        schema.add(actor);

        RelationSchema movie = new RelationSchema("movie");
        movie.addKeyAttribute("id");
        movie.addAttribute("name");
        movie.addAttribute("year");
        movie.addAttribute("rank");
        schema.add(movie);

        RelationSchema directors = new RelationSchema("directors");
        directors.addKeyAttribute("id");
        directors.addAttribute("fname");
        directors.addAttribute("lname");
        schema.add(directors);

        RelationSchema casts = new RelationSchema("casts");
        casts.addAttribute("pid");
        casts.addAttribute("mid");
        casts.addAttribute("role");
        schema.add(casts);

        RelationSchema movie_directors = new RelationSchema("movie_directors");
        movie_directors.addAttribute("did");
        movie_directors.addAttribute("mid");
        schema.add(movie_directors);

        RelationSchema genre = new RelationSchema("genre");
        genre.addAttribute("genre");
        genre.addAttribute("mid");
        schema.add(genre);

        RelationSchema director_genre = new RelationSchema("director_genre");
        director_genre.addAttribute("did");
        director_genre.addAttribute("genre");
        director_genre.addAttribute("prob");
        schema.add(director_genre);
        
        schema.addForeignKey(casts.getAttribute("pid"), actor.getAttribute("id")); 
        schema.addForeignKey(casts.getAttribute("mid"), movie.getAttribute("id"));
        schema.addForeignKey(movie_directors.getAttribute("did"), directors.getAttribute("id"));
        schema.addForeignKey(movie_directors.getAttribute("mid"), movie.getAttribute("id")); 
        schema.addForeignKey(genre.getAttribute("mid"), movie.getAttribute("id")); 
        
        return schema;
    }
}

