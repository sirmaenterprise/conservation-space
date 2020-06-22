package org.alfresco.repo.workflow;

// TODO: Auto-generated Javadoc
/**
 * Interface for getting the correct WorkflowReportService.
 *
 * @author hlungov
 *
 */
public interface WorkflowReportServiceProvider {

	/** The Constant CMF_WORKFLOW_PREFIX. */
	final static String CMF_WORKFLOW_PREFIX = "WFTYPE";

	/** The Constant DEFAULT_ACTIVITI_WORKFLOWS_PREFIX. */
	final static String DEFAULT_ACTIVITI_WORKFLOWS_PREFIX = "activiti";

	/**
	 * Gets the workflow report service.
	 *
	 * @param workflowId
	 *            the workflow id
	 * @return the workflow report service
	 */
	WorkflowReportService getApplicableReportService(String workflowId);

	/**
	 * Gets the workflow report service.
	 *
	 * @param prefix
	 *            the prefix id for now only cmfwf or sirmawf
	 * @return the workflow report service
	 */
	WorkflowReportService getApplicableReportServiceByPrefix(String prefix);

}
