package templates;

import java.util.List;


import schema.DBSchema;
import schema.RelationSchema;
import symbolicdb.SimpleView;
import symbolicdb.SymbolicTuple;

public class RoundRobinTemplateFilling implements TemplateFillingTechnique{
	
	DBSchema schema; 
	public RoundRobinTemplateFilling(DBSchema schema){
		this.schema = schema; 
	}

	@Override
	public void fillTemplates(SimpleView lastView) {
		double bestImprovement = 0;  
		
		do{
			double origScore = lastView.templateCoverageScore(); 
			bestImprovement = 0; 
			//System.out.println("origScore: " + origScore); 
			
			String bestBaseTableToAddTo = null; 
			
			//add tuple that leads to best improvement
			for(RelationSchema baseTable : schema.getRelations()){
				SimpleView lastViewClone = lastView.cloneRecursively(); 
				lastViewClone.addNewTupleToBaseTable(baseTable.getRelationName());
				lastViewClone.update(); 
				
				System.out.println("      <CANDIDATE adding = " + baseTable.getRelationName() + " ________ "); 
				
				if(baseTable.getRelationName().equals("")){
					System.out.println(lastViewClone.recursiveToString()); 

				}
				double newScore = lastViewClone.templateCoverageScore();
				System.out.println("     </CANDIDATE " + baseTable.getRelationName() + ": " + newScore + " > ________ "); 
				
				if( (newScore - origScore) >= bestImprovement){
					bestImprovement = newScore - origScore;
					bestBaseTableToAddTo = baseTable.getRelationName(); 
				}
			}
			
			//if there is a good tuple to add
			System.out.println("Should add into: " + bestBaseTableToAddTo + " -- improvement: " + bestImprovement);
			lastView.addNewTupleToBaseTable(bestBaseTableToAddTo); 
			lastView.update(); 
			
			System.out.println(lastView.recursiveToString()); 
			System.out.println("##############"); 
			
			
		}while(bestImprovement > 0); 
		
	}
	
	
	
}
