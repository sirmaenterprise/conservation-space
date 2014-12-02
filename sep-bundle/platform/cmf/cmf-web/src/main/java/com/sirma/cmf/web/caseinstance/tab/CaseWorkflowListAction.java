package com.sirma.cmf.web.caseinstance.tab;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.EntityAction;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Backing bean for case worklfow list tab. Selecting and fetching the workflows for current
 * instance to be ready for the web page.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class CaseWorkflowListAction extends EntityAction implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4964890926019654553L;

	/** The workflow service. */
	@Inject
	private WorkflowService workflowService;

	/** The workflow instance contexts. */
	private List<WorkflowInstanceContext> workflowInstanceContexts;

	/**
	 * Retrieve all workflows by given case instance. This method will be invoked from hidden link
	 * after click.
	 * 
	 * @param caseInstance
	 *            current case instance
	 */
	@SuppressWarnings("unchecked")
	public void retrieveWorkflows(Instance caseInstance) {
		log.debug("CMFWeb: Executing CaseWorkflowListAction.retrieveWorkflows");
		workflowInstanceContexts = CollectionUtils.EMPTY_LIST;
		if (caseInstance != null) {
			workflowInstanceContexts = workflowService.getWorkflowsHistory(caseInstance);
		}
	}

	/**
	 * Getter for supported workflow instances.
	 * 
	 * @return list with workflow instance
	 */
	public List<WorkflowInstanceContext> getWorkflowInstanceContexts() {
		return workflowInstanceContexts;
	}

	/**
	 * Setter for workflow instances.
	 * 
	 * @param workflowInstanceContexts
	 *            current workflow instances
	 */
	public void setWorkflowInstanceContexts(List<WorkflowInstanceContext> workflowInstanceContexts) {
		this.workflowInstanceContexts = workflowInstanceContexts;
	}

}
