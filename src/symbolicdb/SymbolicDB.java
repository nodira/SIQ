package symbolicdb;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.washington.db.cqms.common.sqlparser.Pair;

import queryplan.AddAction;
import queryplan.Configuration;
import realdb.GeneralDB;
import realdb.GeneralRelation;

import schema.ColumnSchema;
import schema.DBSchema;
import schema.DBSchema.SimpleForeignKey;
import schema.RelationSchema;


public class SymbolicDB implements GeneralDB {
	List<GeneralRelation> relations = new ArrayList<GeneralRelation>(); 
	DBSchema schema; 
	Hashtable<RelationSchema, GeneralRelation> schema2Rel = new Hashtable<RelationSchema, GeneralRelation>(); 
	
	public SymbolicDB(DBSchema schema){
		this.schema = schema; 
		for(RelationSchema rSchema : schema.getRelations()){
			SymbolicRelation rel = new SymbolicRelation(rSchema); 
			this.relations.add(rel); 
			schema2Rel.put(rSchema, rel); 
		}
	}
	
	public List<GeneralRelation> relations(){
		return relations; 
	}
	
	public DBSchema schema(){
		return schema; 
	}
	
	public GeneralRelation getRelation(String relationName){
		for(GeneralRelation rel : relations){
			if(rel.relationSchema().getRelationName().equals(relationName)){
				return rel; 
			}
		}
		return null; 
	}


	private void setGeneralRelation(GeneralRelation rel){
		for(int i =0; i<relations.size(); i++){
			if(relations.get(i).relationSchema() == rel.relationSchema()){
				relations.set(i, rel); 
			}
		}
		
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("DB: " + this.schema.getDBName() + "\n ---- \n");
		
		for(GeneralRelation rel: relations){
			sb.append(rel.toString() + "\n -------- \n"); 
		}
	
		
		return sb.toString();
		
	}
	
	public Pair<SymbolicDB, Hashtable<Variable, Variable>> cloneAndReturnMapping(){
		Hashtable<Variable, Variable> var2VarClone = new Hashtable<Variable, Variable>(); 
		
		SymbolicDB clone = new SymbolicDB(this.schema); 
		
		for(GeneralRelation rel : relations){
			clone.setGeneralRelation(((SymbolicRelation)rel).cloneAccordingToMap(var2VarClone)); 
		}
		
		return new Pair<SymbolicDB, Hashtable<Variable, Variable>>(clone, var2VarClone); 
	}
	
	public SymbolicDB clone(){
		return cloneAndReturnMapping().x(); 
	}

	
	public String prettyPrint(){
		Hashtable<Variable, Variable> simplifiedVars = new Hashtable<Variable, Variable>();
		
		int numVars = 0; 
		char a = 'a'; 
		for(GeneralRelation rel: relations){
			for(Tuple t: rel.getTuples()){
				List<Variable> vars = ((SymbolicTuple)t).variables(); 
				for(Variable v: vars){
					if(simplifiedVars.containsKey(v) == false){
						simplifiedVars.put(v, v.clone(( ((char)(a + numVars++)) + ""))); 
					}
				}
				
			}
		}
		
		SymbolicDB simplifiedClone = new SymbolicDB(this.schema); 
		for(GeneralRelation rel : relations){
			simplifiedClone.setGeneralRelation(((SymbolicRelation)rel).cloneAccordingToMap(simplifiedVars)); 
		}
		
		return simplifiedClone.toString(); 
		
	}
	
	public void addTuple(GeneralRelation rel, Tuple t){
		addTuple(rel, t, true); 
	}
	
	public void addTuple(Tuple t){
		addTuple(getRelation(t.underlyingSchema().getRelationName()), t); 
	}
	
	public void addTuple(Tuple t, boolean recursively){
		addTuple(getRelation(t.underlyingSchema().getRelationName()), t, recursively); 
	}
	
	public void mergeAdd(SymbolicTuple toAdd, SymbolicTuple existingTuple){
		existingTuple.merge(toAdd); 
		for(int i=0; i<toAdd.arity(); i++){
			replaceVariableV1WithV2(toAdd.getColumn(i), existingTuple.getColumn(i)); 
		}
	}
	
	public List<SymbolicTuple> atomicTuplesFor(SymbolicTuple tuple){
		Queue<SymbolicTuple> tuplesWanted = new LinkedList<SymbolicTuple>(); 
		tuplesWanted.add(tuple); 
		
		List<SymbolicTuple> tuplesNeeded = new ArrayList<SymbolicTuple>(); 
		
		while(tuplesWanted.isEmpty() == false){
			SymbolicTuple t = tuplesWanted.remove();
			tuplesNeeded.add(t); 
			
			GeneralRelation rel = getRelation(t.underlyingSchema.getRelationName()); 
			
			for(SimpleForeignKey k : schema.getForeignKeys()){
				if(k.col1().relationSchema() == rel.relationSchema()){
					GeneralRelation otherRel = schema2Rel.get(k.col2().relationSchema());
					String col1 = k.col1().columnName(); 
					String col2 = k.col2().columnName(); 
					
					SymbolicTuple newTuple = SymbolicTuple.constructTupleWithNewVariables(otherRel.relationSchema());
					newTuple.setColumn(col2, t.getColumn(col1));
					
					tuplesWanted.add(newTuple); 
				}
			}
		}
		
		System.out.println("atomicTuplesFor(" + tuple + "[" +  tuple.underlyingSchema.getRelationName() + "]):");
		for(SymbolicTuple t: tuplesNeeded){
			System.out.println("   " + t + "[" +  t.underlyingSchema.getRelationName() + "]):");
			
		}
		
		return tuplesNeeded; 
	}
	
	
	public void addTuple(GeneralRelation rel, Tuple t, boolean addRecursively){
		((SymbolicRelation)rel).addTuple(t); 
		
		if(addRecursively){
			//add tuples into the dependent tables too
			for(SimpleForeignKey k : schema.getForeignKeys()){
				if(k.col1().relationSchema() == rel.relationSchema()){
					GeneralRelation otherRel = schema2Rel.get(k.col2().relationSchema());
					String col1 = k.col1().columnName(); 
					String col2 = k.col2().columnName(); 
					
					boolean matchFound = false; 
					
					//check if it already has a matching tuple there 
					for(Tuple potentialMatch : otherRel.getTuples()){
						if(potentialMatch.getColumn(col2) == t.getColumn(col1)){
							matchFound = true;
							break; 
						}
					}
					
					//if not: then add it	
					if(matchFound == false){
						SymbolicTuple newTuple = SymbolicTuple.constructTupleWithNewVariables(otherRel.relationSchema());
						newTuple.setColumn(col2, ((SymbolicTuple)t).getColumn(col1));
						addTuple(otherRel, newTuple); 
					}
				}
			}
		}
		
	}
	
	public void ensureFKsSatisfied(){
		for(GeneralRelation rel : relations){
			for(Tuple t: rel.getTuples()){
				ensureFKsSatisfied(((SymbolicTuple)t), rel); 
			}
			
		}
		
	}
	
	private void ensureFKsSatisfied(SymbolicTuple t, GeneralRelation rel){
		//add tuples into the dependent tables too
		for(SimpleForeignKey k : schema.getForeignKeys()){
			if(k.col1().relationSchema() == rel.relationSchema()){
				GeneralRelation otherRel = schema2Rel.get(k.col2().relationSchema());
				String col1 = k.col1().columnName(); 
				String col2 = k.col2().columnName(); 
				
				boolean matchFound = false; 
				
				//check if it already has a matching tuple there 
				for(Tuple potentialMatch : otherRel.getTuples()){
					if(potentialMatch.getColumn(col2) == t.getColumn(col1)){
						matchFound = true;
						break; 
					}
				}
				
				//if not: then add it	
				if(matchFound == false){
					SymbolicTuple newTuple = SymbolicTuple.constructTupleWithNewVariables(otherRel.relationSchema());
					newTuple.setColumn(col2, t.getColumn(col1));
					addTuple(otherRel, newTuple); 
				}
			}
		}
	
	}
	
	public void replaceVariableV1WithV2(Variable v1, Variable v2){
		for(GeneralRelation rel: relations){
			for(Tuple t: rel.getTuples()){
				((SymbolicTuple)t).replaceV1WithV2(v1, v2); 
			}
		}
		
		
	}
	
	public List<SymbolicTuple> allTuplesThatMergeWith(SymbolicTuple tuple){
		List<SymbolicTuple> candidates = new ArrayList<SymbolicTuple>();
		GeneralRelation rel = this.getRelation(tuple.underlyingSchema.getRelationName());
		for(Tuple t : rel.getTuples()){
			//im more certain now that this should indeed be dontcheckcolumnschemas (not 100% though) 
			if(((SymbolicTuple)t).canBeMergedDontCheckColumnSchemas(tuple)){
				candidates.add(((SymbolicTuple)t)); 
			}
		}
		return candidates; 
	}
	
	public void applyConfiguration(Configuration configuration){
		for(int i=0; i < configuration.size(); i++){
			AddAction addAction = configuration.get(i);
			
			if(addAction.isAMerge() == false){//we're not merging this tuple
				addTuple(addAction.t, false); //dont add FK dependencies
			}else{
				mergeAdd(addAction.t, addAction.mergeCandidate); 
				
				//we need to do the variable replacement! dont we? 
				for(int a=0; a<addAction.t.arity(); a++){
					configuration.replaceV1WithV2(addAction.t.getColumn(a), addAction.mergeCandidate.getColumn(a)); 
				} 
			}
		}
		
		ensureFKsSatisfied(); 
	}
	
	public boolean canBeApplied(Configuration configuration){
		Pair<SymbolicDB, Hashtable<Variable, Variable>> cloneAndMapping = this.cloneAndReturnMapping();
		SymbolicDB dbClone = cloneAndMapping.x();
		Hashtable<Variable, Variable> varMapping = cloneAndMapping.y(); 
		Configuration configClone = configuration.cloneWithMapping(varMapping); 
		
		dbClone.applyConfiguration(configClone); 
		
		return dbClone.allVariablesSatisfiable() && dbClone.arePKsSatisfied(); 
	}
	
	public boolean allVariablesSatisfiable(){
		for(GeneralRelation rel : relations){
			for(Tuple t : rel.getTuples()){
				for(Variable v : ((SymbolicTuple)t).variables()){
					if(v.satisfiable() == false){
						return false; 
					}
				}
			}
		}
		
		return true; 
	}
	
	public boolean arePKsSatisfied(){
		for(GeneralRelation rel : relations){
			for(int i=0; i<rel.arity(); i++){
				if(rel.relationSchema().getAttribute(i).isKey()){//this is a key. check for repeats
					HashSet<Variable> varsSeen = new HashSet<Variable>();
					for(Tuple t : rel.getTuples()){
						SymbolicTuple tt = (SymbolicTuple) t; 
						if(varsSeen.contains(t.getColumn(i))){
							return false;
						}else{
							varsSeen.add(tt.getColumn(i)); 
						}
						
					}
				}
			}
		}
		
		
		return true; 
	}

	@Override
	public DBSchema getSchema() {
		return schema; 
	}
	
	
}
