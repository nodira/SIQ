package templates;

import symbolicdb.SimpleView;

public interface TemplateFillingTechnique {
	
	/**
	 * Fills in the templates in this view and its underlying views
	 * 
	 * @param lastView
	 * @return a score indicating what percentage of templates were successfully filled
	 */
	public void fillTemplates(SimpleView lastView); 	
}
