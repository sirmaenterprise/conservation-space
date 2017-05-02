package com.sirma.cmf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.menu.NavigationMenuEvent;
import com.sirma.cmf.web.navigation.history.event.NavigationHistoryEvent;
import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * CMFTest.
 *
 * @author svelikov
 */
public class CMFTest extends CmfTest {

	/** The Constant LOG. */
	protected static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CMFTest.class);

	/** The Constant slf4j logger. */
	protected static final Logger SLF4J_LOG = LoggerFactory.getLogger(CMFTest.class);

	/**
	 * If id is not null, then use it or return default value instead.
	 *
	 * @param id
	 *            the id
	 * @return the id
	 */
	private Long getId(Long id) {
		return id != null ? id : Long.valueOf(1L);
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
	public EMFActionEvent createEventObject(String navigation, Instance instance, String actionId, Action action) {

		return new EMFActionEvent(instance, navigation, actionId, action);
	}

	/**
	 * Creates the navigation menu event.
	 *
	 * @param menu
	 *            the menu
	 * @param action
	 *            the action
	 * @return the navigation menu event
	 */
	public NavigationMenuEvent createNavigationMenuEvent(String menu, String action) {
		NavigationMenuEvent event = new NavigationMenuEvent(menu, action);
		return event;
	}

	/**
	 * Creates the navigation history event.
	 *
	 * @return the navigation history event
	 */
	public NavigationHistoryEvent createNavigationHistoryEvent() {
		NavigationHistoryEvent event = new NavigationHistoryEvent();
		return event;
	}

	/**
	 * Creates the workflow definition.
	 *
	 * @param dmsId
	 *            the dms id
	 * @return the workflow definition
	 */
	public GenericDefinition createWorkflowDefinition(String dmsId) {
		GenericDefinition workflowDefinitionImpl = new GenericDefinitionImpl();
		workflowDefinitionImpl.setDmsId(dmsId);
		return workflowDefinitionImpl;
	}

	/**
	 * Creates the instance reference.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @return the instance reference
	 */
	public InstanceReference createInstanceReference(String id, Class<?> type) {
		DataType sourceType = new DataType();
		sourceType.setName(type.getSimpleName().toLowerCase());
		sourceType.setJavaClassName(type.getCanonicalName());
		sourceType.setJavaClass(type);
		InstanceReference ref = new LinkSourceId(id.toString(), sourceType);
		return ref;
	}
}
