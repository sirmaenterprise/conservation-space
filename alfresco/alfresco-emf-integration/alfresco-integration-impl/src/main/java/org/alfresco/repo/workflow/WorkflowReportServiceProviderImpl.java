package org.alfresco.repo.workflow;

import java.util.HashMap;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * The WorkflowReportServiceProviderImpl provides the reporting service for tasks.
 *
 * @author hlungov
 */
public class WorkflowReportServiceProviderImpl implements WorkflowReportServiceProvider {

	/** The services. */
	private ServiceRegistry services;

	/** The workflow services. */
	public HashMap<String, Object> workflowServices = new HashMap<String, Object>();

	private WorkflowReportService cmfReporter;

	/**
	 * Gets the workflow report service.
	 *
	 * @param workflowId
	 *            the workflow id
	 * @return the workflow report service
	 */
	@Override
	public WorkflowReportService getApplicableReportService(String workflowId) {
		// for all wf
		return getLazyWorkflowReportServices();
	}

	/*
	 * (non-Javadoc)
	 * @see org.alfresco.repo.workflow.WorkflowReportServiceProxy#
	 * getWorkflowReportServiceByPrefix(java.lang.String)
	 */
	@Override
	public WorkflowReportService getApplicableReportServiceByPrefix(String prefix) {
		// for all wf
		return getLazyWorkflowReportServices();
	}

	/**
	 * Loads lazy workflow report services.
	 *
	 * @return the report service
	 */
	private WorkflowReportService getLazyWorkflowReportServices() {
		if (cmfReporter == null) {
			cmfReporter = (WorkflowReportService) getServices().getService(
					QName.createQName(NamespaceService.ALFRESCO_URI, "taskReportService"));
		}
		return cmfReporter;
	}

	/**
	 * @return the services
	 */
	public ServiceRegistry getServices() {
		return services;
	}

	/**
	 * @param services
	 *            the services to set
	 */
	public void setServices(ServiceRegistry services) {
		this.services = services;
	}

}
