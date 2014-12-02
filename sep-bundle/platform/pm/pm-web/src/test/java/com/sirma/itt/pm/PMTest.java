package com.sirma.itt.pm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.definitions.impl.ProjectDefinitionImpl;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The Class PMTest.
 * 
 * @author svelikov
 */
public class PMTest {

	/** The Constant LOG. */
	protected static final org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(PMTest.class);

	/** The Constant slf4j logger. */
	protected static final Logger SLF4J_LOG = LoggerFactory.getLogger(PMTest.class);

	/**
	 * Creates the project instance.
	 * 
	 * @param id
	 *            the id
	 * @param dmsId
	 *            the dms id
	 * @return the project instance
	 */
	public ProjectInstance createProjectInstance(Long id, String dmsId) {
		ProjectInstance projectInstance = new ProjectInstance();
		projectInstance.setId(id);
		projectInstance.setDmsId(dmsId);
		projectInstance.setIdentifier(dmsId);
		return projectInstance;
	}

	/**
	 * Creates the project definition.
	 * 
	 * @param dmsId
	 *            the dms id
	 * @return the project definition
	 */
	public ProjectDefinition createProjectDefinition(String dmsId) {
		ProjectDefinitionImpl definitionImpl = new ProjectDefinitionImpl();
		definitionImpl.setDmsId(dmsId);
		return definitionImpl;
	}

	/**
	 * Creates the event object.
	 * 
	 * @param navigation
	 *            the navigation
	 * @param instance
	 *            the instance
	 * @param actionId
	 *            the action id
	 * @param action
	 *            the action
	 * @return the cMF action event
	 */
	public EMFActionEvent createEventObject(String navigation, Instance instance, String actionId,
			Action action) {

		return new EMFActionEvent(instance, navigation, actionId, action);
	}

}
