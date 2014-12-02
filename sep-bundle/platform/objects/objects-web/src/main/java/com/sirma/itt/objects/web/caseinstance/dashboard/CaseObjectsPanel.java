package com.sirma.itt.objects.web.caseinstance.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
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
 * This class integrate object into case dashboard. <b>NOTE: In this class we use workaround for
 * retrieving object, this allow to use action for current object that need section instance
 * inside.</b>
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "CaseDashboard")
@ViewAccessScoped
public class CaseObjectsPanel extends DashboardPanelActionBase<ObjectInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = -3402542641853854668L;

	private static final String CASEDASHBOARD_DASHLET_OBJECTS = "casedashboard_dashlet_objects";

	/** The object type property for retrieving the full URI. */
	private static final String OBJECT_TYPE = "objectinstance";

	private CaseInstance caseInstance;

	/** The action for current panel. */
	private static final Set<String> dashletActions = new HashSet<String>(
			Arrays.asList(ActionTypeConstants.CREATE_OBJECT));

	@Override
	protected void initializeForAsynchronousInvocation() {
		caseInstance = getDocumentContext().getInstance(CaseInstance.class);
	}

	@Override
	public void initData() {
		onOpen();
	}

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
	 *            current context instance(Case instance)
	 * @return instance uri
	 */
	protected String getCaseUri(Instance instance) {
		return (String) instance.getId();
	}

	/**
	 * This method will construct URI holder needed for the semantic query NOTE: REG(STR(...)) if we
	 * use and return as String.
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
	public String targetDashletName() {
		return CASEDASHBOARD_DASHLET_OBJECTS;
	}

	@Override
	public Instance dashletActionsTarget() {
		if (caseInstance == null) {
			// if this is called before initialization
			caseInstance = (CaseInstance) getDocumentContext().getCurrentInstance();
		}
		return caseInstance;
	}

	@Override
	public Set<String> dashletActionIds() {
		return dashletActions;
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
