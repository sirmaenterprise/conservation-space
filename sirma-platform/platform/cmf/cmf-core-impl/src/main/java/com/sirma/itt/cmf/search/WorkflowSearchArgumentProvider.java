package com.sirma.itt.cmf.search;

import javax.inject.Inject;

import com.sirma.itt.cmf.workflows.WorkflowConfigurations;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.SearchArgumentProvider;

/**
 * Search arguments extension
 *
 * @author sdjulgerova
 */
@Extension(target = SearchArgumentProvider.ARGUMENTS_EXTENSION_POINT)
public class WorkflowSearchArgumentProvider implements SearchArgumentProvider {

	@Inject
	private WorkflowConfigurations workflowConfigurations;

	@Override
	public void provide(SearchRequest request, Context<String, Object> context) {
		context.put("highpriority", workflowConfigurations.getWorkflowPriorityHigh());
		context.put("lowpriority", workflowConfigurations.getWorkflowPriorityLow());
		context.put("normalpriority", workflowConfigurations.getWorkflowPriorityNormal());
	}

}
