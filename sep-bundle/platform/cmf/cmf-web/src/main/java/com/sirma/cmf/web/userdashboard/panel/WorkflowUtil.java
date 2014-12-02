package com.sirma.cmf.web.userdashboard.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.WorkflowProperties;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * <b>WorkflowUtil</b> helps for work-flow functionality by providing additional methods.
 * 
 * @author cdimitrov
 */
@Named
@ApplicationScoped
public class WorkflowUtil {

	/** THe definition identifier, property that will be merged. */
	private static final String DEFINITION_IDENTIFIER = "definitionId";

	/** The work-flow instance type, property that will be merged. */
	private static final String WORKFLOW_INSTANCE_TYPE = "instance";

	/** The work-flow container, property that will be merged. */
	private static final String WORKFLOW_CONTAINER = "container";

	/**
	 * Extract data from properties. This method will be removed when work-flows are returned from
	 * the semantic with properly located data.
	 * 
	 * @param instance
	 *            The current instance {@see WorkflowInstanceContext}
	 * @return same instance but with re-located data, from property
	 */
	private Instance extractDataFromProperties(Instance instance) {
		if (instance != null) {
			Map<String, Serializable> properties = instance.getProperties();
			instance.setRevision((Long) properties.get(WorkflowProperties.REVISION));
			instance.setIdentifier((String) properties.get(DEFINITION_IDENTIFIER));
			WorkflowInstanceContext workflowInstance = (WorkflowInstanceContext) instance;
			workflowInstance.setWorkflowInstanceId((String) properties.get(WORKFLOW_INSTANCE_TYPE));
			EmfInstance emfInstance = workflowInstance;
			emfInstance.setContainer((String) properties.get(WORKFLOW_CONTAINER));
			instance = emfInstance;
		}
		return instance;
	}

	/**
	 * Map activator, that will invoke the data extractor.
	 * 
	 * @param list
	 *            the list with workflow context instances
	 * @return the list with workflow instance
	 */
	public List<Instance> mapWorkflowData(List<Instance> list) {
		List<Instance> instances = new ArrayList<Instance>();
		for (Instance instace : list) {
			instances.add(extractDataFromProperties(instace));
		}
		return instances;
	}

}
