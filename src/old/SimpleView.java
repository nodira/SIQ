package old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import query.QuerySession;

import schema.ColumnSchema;
import schema.DBSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

import constraints.BinaryConstraint;
import constraints.ComparisonOp;
import constraints.NumericConstraint;
import constraints.StringConstraint;
import constraints.UnaryConstraint;
import constraints.VariableConstraint;

import edu.washington.db.cqms.snipsuggest.features.F_ColumnInGroupBy;
import edu.washington.db.cqms.snipsuggest.features.F_PredicateInWhere;
import edu.washington.db.cqms.snipsuggest.features.F_TableInFrom;
import edu.washington.db.cqms.snipsuggest.features.QueryFeature;

/**
 * A wrapper class for a SymbolicRelation.
 * Keeps track of the underlying views, what additional snippet this view was created for, etc. 
 * 
 * Does not allow in-place tuple deletion. Adding a tuple may cause the addition of various constraints 
 * of variables. Removing the tuple will not undo those constraints.  
 * 
 * @author nodira
 *
 */

public class SimpleView {
	String viewName; 
	List<SimpleView> underlyingViews; 
	QueryFeature addedSnippet; 
	
	DBSchema schema; 
	
	//view content
	private SymbolicRelation viewContent; 
	List<String> columnNames; 
	
	List<Template> templates; 
	HashSet<Template> appliedTemplates = new HashSet<Template>(); 
	
	private SimpleView(){}
	
	public SimpleView(DBSchema schema, String tableName){
		this.schema = schema; 
		this.columnNames = new ArrayList<String>(); 
		this.underlyingViews = new ArrayList<SimpleView>();
		this.templates = new ArrayList<Template>(); 
		
		RelationSchema tableSchema = schema.get(tableName); 
		this.viewContent = new SymbolicRelation(tableSchema);
		for(ColumnSchema colName : tableSchema.getAttributes()){
			this.columnNames.add(tableName + "_" + colName); 
		}
		
		this.viewName = tableName; 
		
	}
	
	public SimpleView(String viewName, QueryFeature addedSnippet, DBSchema schema, SimpleView... underlyingViews){
		this.schema = schema; 
		this.viewName = viewName.toLowerCase(); 
		this.columnNames = new ArrayList<String>(); 
		this.underlyingViews = new ArrayList<SimpleView>();
		this.templates = new ArrayList<Template>(); 
		
		for(SimpleView underlyingView : underlyingViews){
			addUnderlyingView(underlyingView); 
		}
		
		this.addedSnippet = addedSnippet; 	
		
		if(addedSnippet instanceof F_TableInFrom){
			assert(underlyingViews.length == 1 || underlyingViews.length == 2);
			
			//columns from underlying views
			for(SimpleView underlyingView : underlyingViews){
				columnNames.addAll(underlyingView.columnNames); 
				templates.add(new Template(SymbolicTuple.constructTemplateTupleWithNewVariables(underlyingView.getSymbolicRelation().relationSchema())));
			}
			
		
		}else if(addedSnippet instanceof F_PredicateInWhere){
			assert(underlyingViews.length == 1);
			SimpleView underlyingView = underlyingViews[0]; 
			
			//columns from underlying view
			columnNames.addAll(underlyingView.columnNames); 
			
			F_PredicateInWhere pred = (F_PredicateInWhere) addedSnippet; 
			SymbolicRelation x = underlyingView.viewContent;
			UnaryConstraint c = UnaryConstraint.constructUnaryConstraint(pred); 
			if(c != null){ //indeed a unary constraint
				TemplateTuple passingTuple = SymbolicTuple.constructTemplateTupleWithNewVariables(underlyingView.viewContent.relationSchema());
				TemplateTuple failingTuple = SymbolicTuple.constructTemplateTupleWithNewVariables(underlyingView.viewContent.relationSchema());
				
				Variable passingV = passingTuple.getColumn(c.getColumnName(), underlyingView);
				passingV.addConstraint(c.getConstraint()); 
				
				Variable failingV = failingTuple.getColumn(c.getColumnName(), underlyingView);
				failingV.addConstraint(c.getConstraint().negate()); 
				
				templates.add(new Template(passingTuple)); 
				templates.add(new Template(failingTuple)); 

			}else{
				BinaryConstraint bc = BinaryConstraint.constructBinaryConstraint(pred); 
				
				//currently do not support any comparisonop except equals between vars. so: only one template
				TemplateTuple passingTuple = SymbolicTuple.constructTemplateTupleWithNewVariables(underlyingView.viewContent.relationSchema());
				passingTuple.setColumn(underlyingView.columnIndex(bc.getCol2()), passingTuple.getColumn(bc.getCol1(), underlyingView));

				templates.add(new Template(passingTuple)); 
			
			}
			
			
		
		}else if(addedSnippet instanceof F_ColumnInGroupBy){
			assert(underlyingViews.length == 1);
			SimpleView underlyingView = underlyingViews[0]; 
			
			F_ColumnInGroupBy addedGroupbyCol = (F_ColumnInGroupBy) addedSnippet;
			String colName = addedGroupbyCol.getTableName() + "_" + addedGroupbyCol.getColumnName(); 
			int colIndex = underlyingView.columnIndex(colName); 
			
			columnNames.add(colName);
			columnNames.add("countStar"); 
			
			TemplateTuple t1 = SymbolicTuple.constructTemplateTupleWithNewVariables(underlyingView.viewContent.relationSchema());
			TemplateTuple t2 = SymbolicTuple.constructTemplateTupleWithNewVariables(underlyingView.viewContent.relationSchema());
			t2.setColumn(colIndex, t1.getColumn(colIndex));
			templates.add(new Template(t1, t2)); 
			
		}
		
		RelationSchema relSchema = new RelationSchema(this.viewName); 
		for(String columnName: columnNames){
			relSchema.addAttribute(columnName); 
		}
		
		
		this.viewContent = new SymbolicRelation(relSchema); 
		
		
		
	}
	
	public int columnIndex(String columnName){
		return columnNames.indexOf(columnName); 
	}
	
	public void addUnderlyingView(SimpleView v){
		this.underlyingViews.add(v);
	}
	

	
	public void update(){
		//update the underlying views first. 
		for(SimpleView v : underlyingViews){
			v.update(); 
		}
		
		//try to satisfy templates first 
		for(Template template : templates){
			//if template is not already satisfied
			if(appliedTemplates.contains(template) == false && template.matchesTemplate(underlyingViews)){
				template.applyTemplate(underlyingViews);
				appliedTemplates.add(template); 
			}
		}
		
		//then we want tuples to pass
		if(addedSnippet instanceof F_TableInFrom){
			assert(underlyingViews.size() == 1 || underlyingViews.size() == 2); 
			if(underlyingViews.size() == 1){
				SymbolicRelation x = underlyingViews.get(0).viewContent;
				viewContent = SymbolicRelation.copy(x); 
				
			}else if(underlyingViews.size() == 2){
				SymbolicRelation x = underlyingViews.get(0).viewContent;
				SymbolicRelation y = underlyingViews.get(1).viewContent;
				viewContent = SymbolicRelation.cartesianProduct(x, y);
			}
			
		}else if(addedSnippet instanceof F_PredicateInWhere){
			assert(underlyingViews.size() ==1); 
			F_PredicateInWhere pred = (F_PredicateInWhere) addedSnippet; 
			SymbolicRelation x = underlyingViews.get(0).viewContent;
			UnaryConstraint c = UnaryConstraint.constructUnaryConstraint(pred); 
			if(c != null){ //indeed a unary constraint
				viewContent = (SymbolicRelation.filterWithUnaryConstraint(underlyingViews.get(0),x, c));   			
			}else{
				BinaryConstraint bc = BinaryConstraint.constructBinaryConstraint(pred); 
				viewContent = (SymbolicRelation.filterWithBinaryConstraint(underlyingViews.get(0),x, bc));
			}
			
		}else if(addedSnippet instanceof F_ColumnInGroupBy){
			assert(underlyingViews.size() == 1);
			//to col, count(*)
			F_ColumnInGroupBy col = (F_ColumnInGroupBy) addedSnippet;
			SymbolicRelation x = underlyingViews.get(0).viewContent;
			viewContent = (SymbolicRelation.groupByAndCount(underlyingViews.get(0),x, col.getTableName() + "_" + col.getColumnName()));   			
		}else if(addedSnippet == null){ //it is a base table
			//do nothing. no need to update. 			
		}else{//some other added snippet? 
			assert(underlyingViews.size() == 1);
			viewContent = (SymbolicRelation.copyRelationWithSameVariables(underlyingViews.get(0).viewContent));	
		}
	}
	
	
	
	
	public String toString(){
		StringBuilder s = new StringBuilder(viewName + "\n");
		s.append(viewContent.relationSchema().getRelationName() + "\n\n"); 
		
		s.append( "  addedSnippet: " + addedSnippet + "\n"); 
		s.append("  underlying: ");  
		for(SimpleView v : underlyingViews){
			s.append( v.viewName + " "); 
		}
		
		s.append( "\n" + viewContent + "\n  Templates: \n"); 
		
		for(Template t : templates){
			s.append( "  " + t); 
			if(appliedTemplates.contains(t)){
				s.append(" [yes]"); 
			}else{
				s.append(" [no]"); 
			}
			s.append("\n"); 
		}
		
		
		return s.toString(); 
	}
	
	public void replaceV1WithV2RecursivelyUp(Variable v1, Variable v2){
		viewContent.replaceV1WithV2(v1, v2);
		for(SimpleView v : underlyingViews){
			v.replaceV1WithV2RecursivelyUp(v1, v2); 
		}
		
	}
	
	public String recursiveToString(){
		StringBuilder sb = new StringBuilder(); 
		for(SimpleView v : underlyingViews){
			sb.append(v.recursiveToString()); 
			sb.append("\n-------------\n"); 
		}
		sb.append(this.toString()); 
		
		return sb.toString(); 
	}
	
	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	
	public List<SimpleView> findBaseTables(){
		List<SimpleView> baseTables = new ArrayList<SimpleView>(); 
		if(underlyingViews.size() == 0){
			baseTables.add(this);
		}else{
			for(SimpleView v : underlyingViews){
				List<SimpleView> vBaseTables = v.findBaseTables();
				for(SimpleView vBaseTable : vBaseTables){
					if(baseTables.contains(vBaseTable) == false){
						baseTables.add(vBaseTable); 
					}
				}
			}
			
		}
		return baseTables; 
	}
	
	public int numTemplates(){
		return templates.size();
	}
	
	public void addTupleWithNewVariables(){
		addTuple(SymbolicTuple.constructTupleWithNewVariables(viewContent.relationSchema())); 
	}
	
	public void addTuple(SymbolicTuple t){
		assert(underlyingViews.size() == 0); //can only insert tuples into the base tables
		viewContent.addTuple(t); 
		
	}
	
	public List<SymbolicTuple> getTuples(){
		return this.viewContent.getTuples(); 
	}
	

	public SimpleView cloneRecursively(){
		return cloneRecursively(new HashMap<Variable, Variable>()); 
	}
	
	private SimpleView cloneRecursively(HashMap<Variable, Variable> oldVarToNewVar){
		SimpleView clone = new SimpleView();
		clone.schema= this.schema;
		clone.viewName = this.viewName;
		clone.addedSnippet = this.addedSnippet;
		clone.columnNames = new ArrayList<String>();
		clone.columnNames.addAll(this.columnNames); 
		clone.underlyingViews = new ArrayList<SimpleView>(); 
		
		
		Hashtable<Template, Template> oldTemplateToNewTemplate = new Hashtable<Template, Template>(); 
		clone.templates = new ArrayList<Template>();
		for(Template template : this.templates){
			Template templateClone = template.clone(); 
			clone.templates.add(templateClone); 
			oldTemplateToNewTemplate.put(template, templateClone); 
		}
		clone.appliedTemplates = new HashSet<Template>(); 
		for(Template template : this.appliedTemplates){
			clone.appliedTemplates.add(oldTemplateToNewTemplate.get(template)); 
		}
		
		clone.viewContent = new SymbolicRelation(this.viewContent.relationSchema());
		for(SymbolicTuple t : this.viewContent.getTuples()){
			SymbolicTuple tClone = new SymbolicTuple(this.viewContent.relationSchema());
			for(int i =0 ; i<t.arity(); i++){
				Variable oldVar = t.getColumn(i);
				Variable newVar; 
				if(oldVarToNewVar.containsKey(oldVar)){
					newVar = oldVarToNewVar.get(oldVar);
				}else{
					newVar = oldVar.clone(); 
					oldVarToNewVar.put(oldVar, newVar); 
				}
				tClone.setColumn(i, newVar); 
			}
			clone.viewContent.addTuple(tClone); 
		}
		
		for(SimpleView v : underlyingViews){
			SimpleView vClone = v.cloneRecursively(oldVarToNewVar); 
			clone.addUnderlyingView(vClone); 
		}
		
		return clone; 
		
		
		
		
	}
	
	private int numTemplatesRecursive(){
		int numTemplates = this.templates.size();
		for(SimpleView v : underlyingViews){
			numTemplates += v.numTemplatesRecursive(); 
		}
		return numTemplates; 
	}
	
	private int numAppliedTemplatesRecursive(){
		int numAppliedTemplates = this.appliedTemplates.size();
		for(SimpleView v : underlyingViews){
			numAppliedTemplates += v.numAppliedTemplatesRecursive(); 
		}
		return numAppliedTemplates;
	}
	
	public double templateCoverageScore(){
		return ((double) numAppliedTemplatesRecursive()) / numTemplatesRecursive(); 
		
	}
	
	public void addNewTupleToBaseTable(String tableName){
		if(viewName.equals(tableName) && underlyingViews.size() == 0){ //i.e. base table
			this.addTuple(SymbolicTuple.constructTupleWithNewVariables(viewContent.relationSchema()));
			return; 
		}else{
			for(SimpleView v : underlyingViews){
				v.addNewTupleToBaseTable(tableName); 
			}
		}
	}
	
	public SymbolicRelation getSymbolicRelation(){
		return viewContent; 
	}
	
	public List<SimpleView> getUnderlyingViews(){
		return underlyingViews; 
	}
	
	public List<Template> getTemplates(){
		return templates; 
	}
	
	public static SimpleView constructSimpleViews(QuerySession qs){
		DBSchema schema = qs.getSchema(); 
		
		Map<String, SimpleView> viewName2View = new HashMap<String, SimpleView>(); 
		SimpleView lastView = null; 
		
		//iterate thru each step of query session
		for(int i=0; i < qs.numSteps(); i++){
			QueryFeature addedSnippet = qs.snippetsAddedAtStep(i).get(0);
			SimpleView v; 
			
			if(addedSnippet instanceof F_TableInFrom){
				F_TableInFrom table = (F_TableInFrom) addedSnippet; 
				SimpleView tableView = new SimpleView(schema, table.getTableName()); 
				viewName2View.put(tableView.getViewName(), tableView); 
				
				if(lastView == null){
					v =  new SimpleView("V" + i, addedSnippet, schema, tableView);
				}else{
					v =  new SimpleView("V" + i, addedSnippet, schema, tableView, lastView);
				}
				
			}else{
				v = new SimpleView("V" + i, addedSnippet, schema, lastView);
				
			}
			viewName2View.put(v.getViewName(), v); 
			lastView = v; 
		}
		
		return lastView; 
	}
	
	
	
	
	
	
	
	
	
}
