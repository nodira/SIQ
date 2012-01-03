package view;

import java.util.List;

public class SymbolicDBView {
	String viewName; 
	
	String viewDefinition;
	List<String> columnNames; 
	
	String backRule; 
	
	public SymbolicDBView(String viewName, String viewDefinition, List<String> columnNames){
		this.viewName = viewName; 
		this.viewDefinition = viewDefinition;
		this.columnNames = columnNames; 
	}

	public String getViewDefinition() {
		return viewDefinition;
	}

	public void setViewDefinition(String viewDefinition) {
		this.viewDefinition = viewDefinition;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}
	
	public String getViewName(){
		return viewName; 
	}

	public String getBackRule() {
		return backRule;
	}

	public void setBackRule(String backRule) {
		this.backRule = backRule;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	
	
	
	
	
	
}
