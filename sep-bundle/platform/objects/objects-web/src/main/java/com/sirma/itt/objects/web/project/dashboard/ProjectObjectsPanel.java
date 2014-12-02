package com.sirma.itt.objects.web.project.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * <b>ProjectObjectsPanel</b> manage functionality for dashlet, located in project dashboard. The
 * content is represented as object records, actions and filters.
 *
 * @author cdimitrov
 */
@Named
@InstanceType(type = "ProjectDashboard")
@ViewAccessScoped
public class ProjectObjectsPanel extends DashboardPanelActionBase<ObjectInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = -3402542641853854668L;

	private static final String PROJECTDASHBOARD_DASHLET_OBJECTS = "projectdashboard_dashlet_objects";

	/** The constant for object type. */
	private static final String OBJECT_TYPE = "objectinstance";

	/** Constant that will holds actions for the dashlet. */
	private static final Set<String> dashletActions = new HashSet<String>(Arrays.asList(
			ActionTypeConstants.CREATE_OBJECT, ActionTypeConstants.ATTACH_OBJECT));

	/** The current project instance retrieved from the document context. */
	private Instance projectInstance;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeForAsynchronousInvocation() {
		projectInstance = getDocumentContext().getCurrentInstance();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initData() {
		onOpen();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	/**
	 * Get data type definition.
	 *
	 * @return combination of URIs as string
	 */
	protected String getDomainObjectType() {
		return dictionaryService.getDataTypeDefinition(OBJECT_TYPE).getFirstUri();
	}

	/**
	 * Get project URI.
	 *
	 * @param instance
	 *            current context instance(Project instance)
	 * @return instance uri
	 */
	protected String getProjectUri(Instance instance) {
		return (String) instance.getId();
	}

	/**
	 * This method will construct URI holder needed for the semantic query<br>
	 * <b>NOTE:</b> REG(STR(...)) if we use and return as String.
	 *
	 * @param objectTypeUris
	 *            the object type URI
	 * @return wrapped URI
	 */
	protected ArrayList<String> uriWrapper(String objectTypeUris) {
		ArrayList<String> wrappedUriList = null;

		if (StringUtils.isNotNullOrEmpty(objectTypeUris)) {
			wrappedUriList = new ArrayList<String>(1);
			wrappedUriList.add(objectTypeUris);
		}

		return wrappedUriList;
	}

	@Override
	public Set<String> dashletActionIds() {
		return dashletActions;
	}

	@Override
	public String targetDashletName() {
		return PROJECTDASHBOARD_DASHLET_OBJECTS;
	}

	@Override
	public Instance dashletActionsTarget() {
		if (projectInstance == null) {
			// if this is called before initialization
			projectInstance = getDocumentContext().getCurrentInstance();
		}
		return projectInstance;
	}

	@Override
	public void updateSearchArguments(SearchArguments<ObjectInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub

	}

}
