package old;

import java.util.List;



public class BackwardPropagation implements TemplateFillingTechnique {

	/**
	 * General algorithm works as follows: 
	 * 	- for each template: 
	 * 		- add tuples to underlying view to make it happen
	 * 		- propagate back to base tables
	 * 		- propagate forward to generate side-effect tuples
	 * 		- move to next template
	 * 
	 * - recurse for underlying views. 
	 */
	
	
	/**
	 * Alg 2: 
	 * 	- for each template: 
	 * 		- add tuples to underlying view to make it happen (if needed) 
	 * 		- propagate back to base tables
	 * 		- //no propagate forward coz we dont know what to move forward yet
	 * - recurse for underlying views. 
	 * 
	 * minimize symbolic db as follows:
	 * 	while there are pairs of tuples you can merge in base tables
	 * 		merge those tuples 
	 * 
	 * (tuple t1 and t2 are mergable if their variables are mergable, and same # templates are covered.) 
	 * */
	
	
	private void fillTemplates(SimpleView lastView, SimpleView currentView) {
		List<SimpleView> underlyingViews = currentView.getUnderlyingViews(); 
		
		//base case
		if(underlyingViews.size() == 0){
			return; //done
		}
		
		for(Template t : currentView.getTemplates()){
			/** - templates are always on underlying views
			 	- updates are not clear when we're not just trying to push through tuples 
				- maybe we propagate backwards only. and then propagate forward.
				- then we can minimize at end? 
			**/ 
		}
		//for each template - create necessary tuple in underlying views
		
		//
		
		
		
		
		//look at underlying views 
		
		
		//add needed tuples to it
		
		//forward propogate at each step
		
	}
	
	
	@Override
	public void fillTemplates(SimpleView lastView) {
		
		

	}

}
