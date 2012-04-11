package debugging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.List;

import queryplan.CartesianOperator;
import queryplan.FilterOperator;
import queryplan.GroupbyCountOperator;
import queryplan.ProjectOperator;
import queryplan.QueryOperator;
import queryplan.QueryPlan;
import queryplan.TableOperator;


import schema.ColumnSchema;
import schema.RelationSchema;
import symbolicdb.SymbolicDB;
import symbolicdb.SymbolicRelation;
import symbolicdb.SymbolicTuple;
import symbolicdb.Variable;

import org.apache.commons.lang3.StringEscapeUtils; 

/***
 * This class prints out a query plan to a file in graphviz's .dot format. 
 * (for debugging) 
 * @author nodira
 *
 */
public class GraphvizPrinter {
	BufferedWriter bw; 
	Hashtable<Variable,String> var2Color; 
	Hashtable<QueryOperator, String> node2Id; 
	Hashtable<String, Integer> nodeType2Count; 
	
	String file, tempFile; 
	
	
	public GraphvizPrinter(String file){
		try{
			this.file = file;
			this.tempFile = file + "1"; 
			
			System.out.println("Writing to " + tempFile); 
			
			FileWriter fw = new FileWriter(tempFile, true); 
			this.bw = new BufferedWriter(fw); 
		}catch(Exception ex){
			ex.printStackTrace(); 
		}
	}
	
	private void writeLine(String line) throws Exception{
		this.bw.write(line);
		this.bw.newLine(); 
		
	}
	
	private QueryOperator leftChild(QueryOperator node){
		if(node instanceof QueryOperator.BinaryQueryOperator){
			return ((QueryOperator.BinaryQueryOperator) node).op1(); 
		}else if(node instanceof QueryOperator.UnaryQueryOperator){
			return ((QueryOperator.UnaryQueryOperator) node).underlyingOperator();
		}else if(node instanceof TableOperator){
			return null; 
		}
		return null; 
	}
	
	private QueryOperator rightChild(QueryOperator node){
		if(node instanceof QueryOperator.BinaryQueryOperator){
			return ((QueryOperator.BinaryQueryOperator) node).op2(); 
		}
		
		return null; 
	}
	
	public void printDot(SymbolicDB db){
		try{
			writeLine("digraph G {");
			
			this.var2Color 		= new Hashtable<Variable, String>(); 
			this.node2Id 		= new Hashtable<QueryOperator, String>(); 
			this.nodeType2Count = new Hashtable<String, Integer>(); 
			
			//print underlying symbolic relations first
			writeLine("{rank=sink;"); 
			writeSymbolicDB(db); 
			writeLine("}"); 
			bw.close(); 
			
			File f1 = new File(tempFile);
			File f = new File(file);
			f1.renameTo(f); 
			
		}catch(Exception ex){
			ex.printStackTrace(); 
		}
		
		
	}
	
	public void printDot(QueryPlan qp){
		try{
			writeLine("digraph G {");
			
			this.var2Color 		= new Hashtable<Variable, String>(); 
			this.node2Id 		= new Hashtable<QueryOperator, String>(); 
			this.nodeType2Count = new Hashtable<String, Integer>(); 
			
			//print underlying symbolic relations first
			writeLine("{rank=sink;"); 
			writeSymbolicDB(qp.db()); 
			writeLine("}"); 
			
			dot(qp.root(), var2Color); 
			
			//lists the variables in the query plan & the corresponding constraints
			writeLine("{rank=sink; Legend [margin=0, shape = none, label=<<table BGCOLOR=\"GREY\"><tr><td colspan=\"2\">Constraints </td></tr>");
			for(Variable v : var2Color.keySet()){
				if(v.getNumConstraints() > 0){
					if(v.hasDoubleValue() == false && v.hasStringValue() == false){
						writeLine("<tr><td bgcolor=\""  + var2Color.get(v) +"\" >" + v.getVariableName() + "</td><td>" + StringEscapeUtils.escapeHtml4(v.getConstraintString()) + "</td></tr>"); 
					}
				}
			}
			
			writeLine("</table>>]}");
			writeLine("}\n"); 
			bw.close(); 
			
			File f1 = new File(tempFile);
			File f = new File(file);
			f1.renameTo(f); 
			
		}catch(Exception ex){
			ex.printStackTrace(); 
		}
		
	}
	
	private String nodeId(QueryOperator node){
		if(node2Id.containsKey(node) == false){
			String nodeType = node.getClass().getSimpleName(); 
			if(nodeType2Count.containsKey(nodeType) == false){
				nodeType2Count.put(nodeType, 0); 
			}
			int count = nodeType2Count.get(nodeType);
			node2Id.put(node, nodeType+count); 
			nodeType2Count.put(nodeType, count+1); 	
		}
		return node2Id.get(node); 
	}
	
	private String nodeLabel(QueryOperator node){
		if(node instanceof CartesianOperator){
			return " &#215; ";
		}else if(node instanceof FilterOperator){
			return " &sigma;    [" + ((FilterOperator)node).predicateString() +"]"; 
		}else if(node instanceof GroupbyCountOperator){
			return " &gamma;    [" + ((GroupbyCountOperator)node).groupByColsString() + ", count(*)] "; 
		}else if(node instanceof ProjectOperator){
			return " &Pi; " + ((ProjectOperator) node).selectedColumnsString(); 
		}else{
			return nodeId(node); 
		}
		
	}
	
	private String nodeContent(QueryOperator node, Hashtable<Variable, String> var2Color){
		StringBuilder sbTable = new StringBuilder(); 
		sbTable.append("<<table>"); 
		
		int arity = 1; 
		List<SymbolicTuple> intResults = node.getIntermediateResults();
		if(intResults.size() > 0){
			arity = intResults.get(0).arity(); 
		}
		
		//if table - print out table name instead
		if(node instanceof TableOperator){
			sbTable.append("<tr><td colspan=\"" + arity + "\">"  + ((TableOperator) node).getUnderlyingRelation().relationSchema().getRelationName() + "</td></tr>"); 
		}else{
			sbTable.append("<tr><td colspan=\"" + arity + "\">" + nodeLabel(node) + "</td></tr>"); 
		}
		
		
		
		//print out schema
		sbTable.append(schemaToTableRow(node.schema())); 
		
				
		//print intermediate results
		sbTable.append(tuplesToTableRows(intResults)); 
		sbTable.append("</table>>"); 
		return sbTable.toString(); 
	}
	
	
	
	
	private void dot(QueryOperator node, Hashtable<Variable, String> var2Color) throws Exception{
		QueryOperator lc = leftChild(node);
		QueryOperator rc = rightChild(node);
		if(lc != null){
			writeLine("  " + nodeId(node) + "->" + nodeId(lc));
			dot(lc, var2Color); 
		}
		if(rc != null){
			writeLine("  " + nodeId(node) + "->" + nodeId(rc));
			dot(rc, var2Color); 
		}
		writeLine(nodeId(node) + " [shape=box, label=" +nodeContent(node, var2Color)+ "];"); 	
	}
	
	
	private void writeSymbolicDB(SymbolicDB db) throws Exception{
		for(SymbolicRelation rel : db.relations()){
			writeLine(rel.relationSchema().getRelationName() + "[shape=box, label=<<table>" + symbolicRelToTableRows(rel) + "</table>>];"); 
		}
	}
	
	private String symbolicRelToTableRows(SymbolicRelation rel){
		List<SymbolicTuple> tuples = rel.getTuples();
		String tableNameRow = "<tr><td colspan=\"" + rel.arity() + "\">"  + rel.relationSchema().getRelationName() + "</td></tr>"; 
		return tableNameRow + schemaToTableRow(rel.relationSchema()) + tuplesToTableRows(tuples); 
	}
	
	private String schemaToTableRow(RelationSchema schema){
		StringBuilder sbTable = new StringBuilder("<tr>"); 
		for(ColumnSchema c : schema.getAttributes()){
			sbTable.append("<td>" + c.columnName() + "</td>"); 
		}
		sbTable.append("</tr>"); 
		
		return sbTable.toString(); 
	}
	
	private String tuplesToTableRows(List<SymbolicTuple> rows){
		StringBuilder sbTable = new StringBuilder(); 
		
		//print intermediate results
		for(SymbolicTuple t : rows){
			sbTable.append("<tr>"); 
			
			for(int i=0; i<t.arity(); i++){
				Variable v = t.getColumn(i); 
				if(var2Color.containsKey(v) == false){
					var2Color.put(v, Colors.nextColor()); 
				}
				
				//if equality constraint - just print out value. 
				if(v.hasDoubleValue()){
					sbTable.append("<td bgcolor=\"" + var2Color.get(v) +"\">" + v.getVariableName() + ": " + v.getDoubleValue() + "</td>"); 
				}else if(v.hasStringValue()){
					sbTable.append("<td bgcolor=\"" + var2Color.get(v) +"\">" + v.getVariableName() + ": " + v.getStringValue() + "</td>"); 
				}else{
					sbTable.append("<td bgcolor=\"" + var2Color.get(v) +"\">" + v.getVariableName() + "</td>"); 
				}
				
			}
			sbTable.append("</tr>"); 
		}
		return sbTable.toString(); 
	}
	
}
