package com.sirma.itt.cmf.integration.workflow.alfresco4;

import org.activiti.engine.delegate.DelegateTask;

/**
 * Handler for decisions. Sets the execution model data so constraints to be
 * satisfied.
 * 
 * @author bbanchev
 */
public class DecisionTaskHandler extends BaseTaskHandler {

	/** The Constant CMFWF_TASK_OUTCOME. */
	private static final String CMFWF_TASK_OUTCOME = "cmfwf_taskOutcome";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sirma.itt.cmf.integration.workflow.alfresco4.BaseTaskHandler#notify
	 * (org.activiti.engine.delegate.DelegateTask)
	 */
	@Override
	public void notify(DelegateTask delegateTask) {
		super.notify(delegateTask);
		// try {
		// String taskId = delegateTask.getId();
		// WorkflowTask taskById =
		// ServiceProvider.getWorkflowService().getTaskById(
		// "activiti$" + taskId);
		// WorkflowTaskDefinition definition = taskById.getDefinition();
		// // ServiceProvider.getDictionaryService().getType()
		// // System.out.println(definition.getMetadata().getProperties());
		// QName createQName =
		// QName.createQName("http://www.alfresco.org/model/bpm/1.0",
		// "outcomePropertyName");
		// Serializable serializable =
		// taskById.getProperties().get(createQName);
		// System.out.println(serializable);
		// NamespaceService n = ((NamespaceService)
		// ServiceProvider.getBean("namespaceService"));
		// QName resolveToQName = QName.resolveToQName(n,
		// serializable.toString());
		// String outcomeProp = resolveToQName.toPrefixString().replace(":",
		// "_");
		// System.out.println(definition.getMetadata().getProperties().get(createQName));
		if (delegateTask.hasVariable(CMFWF_TASK_OUTCOME)) {
			Object variable = delegateTask.getVariable(CMFWF_TASK_OUTCOME);
			delegateTask.getExecution().setVariable(CMFWF_TASK_OUTCOME, variable);
			debug("Decision outcome: " , variable , " for " , delegateTask.getId());
		}

	}

}
