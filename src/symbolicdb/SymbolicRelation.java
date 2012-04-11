package symbolicdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;


import schema.ColumnSchema;
import schema.RelationSchema;

import constraints.BinaryConstraint;
import constraints.ComparisonOp;
import constraints.NumericConstraint;
import constraints.UnaryConstraint;
import constraints.VariableConstraint;


public class SymbolicRelation {
	
	private RelationSchema relationSchema; 
	private List<SymbolicTuple> tuples = new ArrayList<SymbolicTuple>();
	
	
	
	public SymbolicRelation(RelationSchema relationSchema){
		this.relationSchema = relationSchema;  
	}
	
	protected void addTuple(SymbolicTuple tuple){
		getTuples().add(tuple); 	
		
	}
	
	
	protected SymbolicRelation cloneAccordingToMap(Hashtable<Variable, Variable> varToNewVar){
		SymbolicRelation clone = new SymbolicRelation(this.relationSchema); 
		
		for(SymbolicTuple t : tuples){
			clone.addTuple(t.cloneAccordingToMap(varToNewVar)); 
		}
		
		return clone; 
	}
	
	public static SymbolicRelation copy(SymbolicRelation x){
		SymbolicRelation copy = new SymbolicRelation(x.relationSchema); 
		for(SymbolicTuple tx : x.getTuples()){
			SymbolicTuple t = new SymbolicTuple(x.relationSchema);
			for(int i=0; i<x.relationSchema.size(); i++){
				t.setColumn(i, tx.getColumn(i)); 
			}
			copy.addTuple(t); 
		}
		return copy; 
		
	}
	
	public static SymbolicRelation cartesianProduct(SymbolicRelation x, SymbolicRelation y){
		RelationSchema newSchema = RelationSchema.cartesianProduct(x.relationSchema, y.relationSchema); 
		SymbolicRelation xy = new SymbolicRelation(newSchema);
		
		for(SymbolicTuple tx : x.getTuples()){
			for(SymbolicTuple ty : y.getTuples()){
				SymbolicTuple t = new SymbolicTuple(newSchema);
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
	
	public static SymbolicRelation copyRelationWithSameVariables(SymbolicRelation x){
		SymbolicRelation r = new SymbolicRelation(x.relationSchema);
		for(SymbolicTuple t : x.getTuples()){
			SymbolicTuple tt = new SymbolicTuple(x.relationSchema);
			for(int i=0; i < r.relationSchema.size(); i++){
				tt.setColumn(i, t.getColumn(i)); 
			}
			r.addTuple(tt); 
		}
		return r; 
	}
	

	
	public String toString(){
		StringBuilder s = new StringBuilder();  
		s.append(relationSchema.getRelationName() + "\n"); 
		for(ColumnSchema c : relationSchema.getAttributes()){
			s.append(c + "\t\t") ; 
		}
		s.append("\n"); 
		
		
		for(SymbolicTuple t : getTuples()){
			s.append(t.toString() + "\n"); 
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
