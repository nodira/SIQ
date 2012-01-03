package templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import symbolicdb.SimpleView;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

import constraints.VariableConstraint;

import edu.washington.db.cqms.common.sqlparser.Pair;
import edu.washington.db.cqms.snipsuggest.features.F_TableInFrom;
import edu.washington.db.cqms.snipsuggest.features.QueryFeature;

public class Template {
	
	TemplateTuple[] templateTuples;
	
	//find the recurring variables in the templates. occurrence defined as (tuple-index, column-index)
	Hashtable<Variable, List<Pair<Integer, Integer>>> variableToOccurences;
	
	public Template(TemplateTuple...  templateTuples){
		this.templateTuples = templateTuples; 
		this.variableToOccurences = new Hashtable<Variable, List<Pair<Integer,Integer>>>();
		for(int i=0; i<templateTuples.length; i++){
			for(int j=0; j<templateTuples[i].getSymbolicTuple().getArity(); j++){
				Variable v = templateTuples[i].getSymbolicTuple().getColumn(j); 
				if(variableToOccurences.containsKey(v) == false){
					variableToOccurences.put(v, new ArrayList<Pair<Integer,Integer>>());
				}
				
				variableToOccurences.get(v).add(new Pair<Integer, Integer>(i, j)); 
				
			}
			
		}
		
	}
	
	public List<SymbolicTuple> potentialMatches(SymbolicTuple template, SimpleView... views){
		List<SymbolicTuple> potentialMatches = new ArrayList<SymbolicTuple>(); 
		for(SimpleView v : views){
			if(v.getViewName().equals(templateTuples[0].getRelationName())){
				for(SymbolicTuple t : v.getTuples()){
					if(t.canBeMerged(templateTuples[0].getSymbolicTuple())){
						potentialMatches.add(t); 
					}
				}
			}
		}
		
		return potentialMatches; 
	}
	
	public void applyTemplate(List<SimpleView> underlyingViews) {
		SimpleView[] views = new SimpleView[underlyingViews.size()];
		for(int i=0; i<views.length; i++){
			views[i] = underlyingViews.get(i); 
		}
		applyTemplate(views); 
	}
	
	public void applyTemplate(SimpleView... views){
		//make a clone so that we can go ahead and merge variables in the templates
		TemplateTuple[] templateClones = new TemplateTuple[templateTuples.length];
		for(int i=0; i< templateTuples.length; i++){
			templateClones[i] = templateTuples[i].clone(); 
		}
		
		if(templateTuples.length == 0){
			return; 
		}else if(templateTuples.length == 1){
			List<SymbolicTuple> potentialMatches = potentialMatches(templateTuples[0].getSymbolicTuple(), views);
			if(potentialMatches.size() > 0){
				templateClones[0].getSymbolicTuple().merge(potentialMatches.get(0)); 
				for(int i=0; i< potentialMatches.get(0).getArity(); i++){
					for(SimpleView v : views){
						v.replaceV1WithV2RecursivelyUp(potentialMatches.get(0).getColumn(i), templateClones[0].getSymbolicTuple().getColumn(i)); 
						v.update(); 
					}	
				}
				
			}else{
				System.err.println("This template does not match specified view(s). ");
				System.exit(-1); 
			}
		}else if(templateTuples.length == 2){
			TemplateTuple template1 = templateClones[0];
			TemplateTuple template2 = templateClones[1]; 
			List<SymbolicTuple> t1Matches = potentialMatches(template1.getSymbolicTuple(), views); 
			List<SymbolicTuple> t2Matches = potentialMatches(template2.getSymbolicTuple(), views); 
			
			//do any of these pairs of matches truly match? 
			for(SymbolicTuple t1Match : t1Matches){
				for(SymbolicTuple t2Match : t2Matches){
					if(t1Match != t2Match){
						if(trulyMatches(template1.getSymbolicTuple(), template2.getSymbolicTuple(), t1Match, t2Match)){
							templateClones[0].getSymbolicTuple().merge(t1Match); 
							templateClones[1].getSymbolicTuple().merge(t2Match);
							
							for(int i=0; i< t1Match.getArity(); i++){
								for(SimpleView v : views){
									v.replaceV1WithV2RecursivelyUp(t1Match.getColumn(i), template1.getSymbolicTuple().getColumn(i)); 
									v.update(); 
								}	
							}
							
							for(int i=0; i< t2Match.getArity(); i++){
								for(SimpleView v : views){
									v.replaceV1WithV2RecursivelyUp(t2Match.getColumn(i), template2.getSymbolicTuple().getColumn(i)); 
									v.update(); 
								}	
							}
							
							
						}	
					}
				}
			}
			
		}else{
			System.err.println("Can not yet support templates with more than two tuples.");
			return; 
		}
	}
	
	
	public boolean matchesTemplate(List<SimpleView> underlyingViews) {
		SimpleView[] views = new SimpleView[underlyingViews.size()];
		for(int i=0; i<views.length; i++){
			views[i] = underlyingViews.get(i); 
		}
		return matchesTemplate(views); 
	}
	
	public boolean matchesTemplate(SimpleView... views){
		if(templateTuples.length == 0){
			return true; 
		}else if(templateTuples.length == 1){
			if(potentialMatches(templateTuples[0].getSymbolicTuple(), views).size() > 0){
				return true; 
			}else{
				return false;
			}
		}else if(templateTuples.length == 2){
			SymbolicTuple template1 = templateTuples[0].getSymbolicTuple();
			SymbolicTuple template2 = templateTuples[1].getSymbolicTuple(); 
			List<SymbolicTuple> t1Matches = potentialMatches(template1, views); 
			List<SymbolicTuple> t2Matches = potentialMatches(template2, views); 
			
			//do any of these pairs of matches truly match? 
			for(SymbolicTuple t1Match : t1Matches){
				for(SymbolicTuple t2Match : t2Matches){
					if(t1Match != t2Match){
						if(trulyMatches(template1, template2, t1Match, t2Match)){
							return true; 
						}	
					}
				}
			}
			
			return false; 
		}else{
			System.err.println("Can not yet support templates with more than two tuples.");
			return false; 
		}
	}
	
	private boolean trulyMatches(SymbolicTuple template1, SymbolicTuple template2, 
								 SymbolicTuple t1, SymbolicTuple t2){
		
		Hashtable<Variable, List<Variable>> varsThatMustBeMergeableForTemplateVar = new Hashtable<Variable, List<Variable>>();
		for(int i=0; i<t1.getArity(); i++){
			Variable templateVar = template1.getColumn(i);
			Variable var = t1.getColumn(i);
			
			if(varsThatMustBeMergeableForTemplateVar.containsKey(templateVar) == false){
				varsThatMustBeMergeableForTemplateVar.put(templateVar, new ArrayList<Variable>());
			}
			varsThatMustBeMergeableForTemplateVar.get(templateVar).add(var); 
		}
		for(int i=0; i<t2.getArity(); i++){
			Variable templateVar = template1.getColumn(i);
			Variable var = t2.getColumn(i);
			
			if(varsThatMustBeMergeableForTemplateVar.containsKey(templateVar) == false){
				varsThatMustBeMergeableForTemplateVar.put(templateVar, new ArrayList<Variable>());
			}
			varsThatMustBeMergeableForTemplateVar.get(templateVar).add(var); 
		}
		
		
		
		//now check that for every template var, every pair in its set is mergeable
		for(Variable templateVar : varsThatMustBeMergeableForTemplateVar.keySet()){
			for(Variable v1 : varsThatMustBeMergeableForTemplateVar.get(templateVar)){
				for(Variable v2 : varsThatMustBeMergeableForTemplateVar.get(templateVar)){
					if(v1.canBeMerged(v2) == false){
						return false; 
					}
				}	
			}
			
		}
		
		return true; 
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder(); 
		for(TemplateTuple t : templateTuples){
			sb.append(t.getRelationName() + "("); 
			for(int i=0; i<t.getSymbolicTuple().getArity(); i++){
				sb.append(t.getSymbolicTuple().getColumn(i).getVariableName() + " "); 
			}
			sb.append(")     "); 
			List<Variable> varsInTuple = t.getSymbolicTuple().variables();
			for(Variable v : varsInTuple){
				for(VariableConstraint c : v.getConstraints()){
					sb.append(v.getVariableName() + " " +  c.getOp().getClass().getSimpleName() + " " + c.stringValue() + ","); 
				}
			}
				
		}
		return sb.toString(); 
		
	}

	public Template clone(){
		TemplateTuple[] templateClones = new TemplateTuple[templateTuples.length];
		
		HashMap<Variable, Variable> oldVarToNewVar = new HashMap<Variable, Variable>(); 
		
		for(int i=0; i< templateTuples.length; i++){
			SymbolicTuple t = templateTuples[i].getSymbolicTuple(); 
			SymbolicTuple tClone = new SymbolicTuple(null, t.getArity()); 
			
			for(int j=0; j < t.getArity(); j++){
				if(oldVarToNewVar.containsKey(t.getColumn(j)) == false){
					oldVarToNewVar.put(t.getColumn(j), t.getColumn(j).clone()); 
				}
				tClone.setColumn(j, oldVarToNewVar.get(t.getColumn(j))); 
			}
			templateClones[i] = new TemplateTuple(templateTuples[i].getRelationName(), tClone); 	
		}
		
		return new Template(templateClones); 
	}
	
	
}
