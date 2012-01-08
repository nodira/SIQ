package symbolicdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import constraints.BinaryConstraint;
import constraints.ComparisonOp;
import constraints.NumericConstraint;
import constraints.UnaryConstraint;
import constraints.VariableConstraint;
import edu.washington.db.cqms.common.sqlparser.RelationSchema;

public class SymbolicRelation {
	
	private RelationSchema relationSchema; 
	private List<SymbolicTuple> tuples = new ArrayList<SymbolicTuple>();
	
	
	
	public SymbolicRelation(RelationSchema relationSchema){
		this.relationSchema = relationSchema;  
	}
	
	public void addTuple(SymbolicTuple tuple){
		getTuples().add(tuple); 	
		
	}
	
	protected SymbolicRelation cloneAccordingToMap(HashMap<Variable, Variable> varToNewVar){
		SymbolicRelation clone = new SymbolicRelation(this.relationSchema); 
		
		for(SymbolicTuple t : tuples){
			clone.addTuple(t.cloneAccordingToMap(varToNewVar)); 
		}
		
		return clone; 
	}
	
	public static SymbolicRelation copy(SymbolicRelation x){
		SymbolicRelation copy = new SymbolicRelation(x.relationSchema); 
		for(SymbolicTuple tx : x.getTuples()){
			SymbolicTuple t = new SymbolicTuple(copy, x.relationSchema.size());
			for(int i=0; i<x.relationSchema.size(); i++){
				t.setColumn(i, tx.getColumn(i)); 
			}
			copy.addTuple(t); 
		}
		return copy; 
		
	}
	
	///TODO: should be moved into RelationSchema class. 
	public static RelationSchema cartesianProduct(RelationSchema r1, RelationSchema r2){
		RelationSchema schema = new RelationSchema(r1.getRelationName() + "_x_" + r2.getRelationName() );
		
		List<String> attrs = r1.getAttributes(); 
		for(String attr: attrs){
			schema.addAttribute(attr + "_1"); 
		}
		attrs = r2.getAttributes(); 
		for(String attr: attrs){
			schema.addAttribute(attr + "_2"); 
		}
		
		return schema; 
	}
	
	
	public static SymbolicRelation cartesianProduct(SymbolicRelation x, SymbolicRelation y){
		SymbolicRelation xy = new SymbolicRelation(cartesianProduct(x.relationSchema, y.relationSchema));
		
		for(SymbolicTuple tx : x.getTuples()){
			for(SymbolicTuple ty : y.getTuples()){
				SymbolicTuple t = new SymbolicTuple(xy, xy.relationSchema.size());
				for(int i=0; i<xy.relationSchema.size(); i++){
					if(i < x.relationSchema.size()){
						t.setColumn(i, tx.getColumn(i));
					}else{
						t.setColumn(i, ty.getColumn(i-x.relationSchema.size())); 
					}
				}
				xy.addTuple(t);
			} 
		}
		
		return xy;
	}
	
	/**
	 * Returns new relation with all tuples that could possibly pass the filter. 
	 * Add constraint to the passing tuples. 
	 * 
	 * @param x
	 * @param c
	 * @param name
	 * @return
	 */
	public static SymbolicRelation filterWithUnaryConstraint(SimpleView view, SymbolicRelation x, UnaryConstraint c){
		SymbolicRelation r = new SymbolicRelation(x.relationSchema); 
		for(SymbolicTuple t : x.getTuples()){
			Variable v = t.getColumn(c.getColumnName(), view); 
			if(v.satisfiableWith(c.getConstraint())){
				v.addConstraint(c.getConstraint()); 
				r.addTuple(t); 
			}
		}
		return r; 
	}
	
	public static SymbolicRelation filterWithBinaryConstraint(SimpleView view, SymbolicRelation x, BinaryConstraint c){
		SymbolicRelation r = new SymbolicRelation(x.relationSchema); 
		for(SymbolicTuple t : x.getTuples()){
			Variable v1 = t.getColumn(c.getCol1(), view);
			Variable v2 = t.getColumn(c.getCol2(), view);
			
			if(v1.canBeMerged(v2)){
				v1.merge(v2);
				view.replaceV1WithV2RecursivelyUp(v2, v1); 
				
				t.setColumn(c.getCol2(), v1, view); 
				r.addTuple(t); 
			}
		}
		return r; 
		
		
	}
	
	public static SymbolicRelation copyRelationWithSameVariables(SymbolicRelation x){
		SymbolicRelation r = new SymbolicRelation(x.relationSchema);
		for(SymbolicTuple t : x.getTuples()){
			SymbolicTuple tt = new SymbolicTuple(r, r.relationSchema.size());
			for(int i=0; i < r.relationSchema.size(); i++){
				tt.setColumn(i, t.getColumn(i)); 
			}
			r.addTuple(tt); 
		}
		return r; 
	}
	
	public static SymbolicRelation groupByAndCount(SimpleView view, SymbolicRelation x, String column){
		RelationSchema schema = new RelationSchema(x.relationSchema.getRelationName() + "_groupby"); 
		schema.addAttribute(column);
		schema.addAttribute("countStar"); 
		
		SymbolicRelation r = new SymbolicRelation(schema);
		
		Hashtable<Variable, Integer> varToCount = new Hashtable<Variable, Integer>(); 
		
		for(SymbolicTuple t : x.getTuples()){
			Variable v = t.getColumn(column, view); 
			if(varToCount.containsKey(v) == false){
				varToCount.put(v, 0);
			}
			
			varToCount.put(v, varToCount.get(v) + 1); 
		}
		
		for(Variable v : varToCount.keySet()){
			SymbolicTuple t = new SymbolicTuple(r, 2);
			t.setColumn(0, v);
			
			int count = varToCount.get(v); 
			Variable countVar = new Variable();
			countVar.addConstraint(new NumericConstraint(new ComparisonOp.EQUALS(), count)); 
			t.setColumn(1, countVar); 
			
			r.addTuple(t); 
		}
		
		return r; 
		
		
	}
	
	public String toString(){
		StringBuilder s = new StringBuilder();  
		
		s.append(relationSchema.getRelationName() + "\n"); 
		for(String c : relationSchema.getAttributes()){
			s.append(c + "\t") ; 
		}
		s.append("\n"); 
		
		
		for(SymbolicTuple t : getTuples()){
			s.append(t + "\n"); 
		}
		s.append("\n Constraints: \n"); 
		HashSet<Variable> printedVars = new HashSet<Variable>();
		for(SymbolicTuple t : getTuples()){
			for(int i=0; i<this.relationSchema.size(); i++){
				Variable v = t.getColumn(i);
				if(printedVars.contains(v) == false){
					if(v.getConstraints().size() > 0){
						s.append(v.toStringWithConstraint() + "\n"); 
					}
					printedVars.add(v); 
				}
			}
		}
		
		return s.toString(); 
	}
	
	public void replaceV1WithV2(Variable v1, Variable v2){
		for(SymbolicTuple t : getTuples()){
			t.replaceV1WithV2(v1, v2); 
		}
	}

	
	public void setTuples(List<SymbolicTuple> tuples) {
		this.tuples = tuples;
	}

	public List<SymbolicTuple> getTuples() {
		return tuples;
	}
	
	public int arity(){
		return relationSchema.size(); 
	}
	
	public RelationSchema relationSchema(){
		return relationSchema; 
	}
	
	
	
	
	
}
