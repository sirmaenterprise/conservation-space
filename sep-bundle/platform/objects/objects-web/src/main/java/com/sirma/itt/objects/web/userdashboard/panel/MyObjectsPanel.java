package com.sirma.itt.objects.web.userdashboard.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * <b>MyObjectsPanel</b> manage functionality for dashlet, located in personal/user dashboard. The
 * content is represented as object records, actions and filters.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "UserDashboard")
@ViewAccessScoped
public class MyObjectsPanel extends DashboardPanelActionBase<ObjectInstance> implements
		Serializable, DashboardPanelController {

	/** The constant for serial version identifier. */
	private static final long serialVersionUID = -3402542641853854668L;

	private static final String USERDASHBOARD_DASHLET_OBJECTS = "userdashboard_dashlet_objects";

	private static final String OBJECT_TYPE = "objectinstance";

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
	public Set<String> dashletActionIds() {
		return null;
	}

	@Override
	public String targetDashletName() {
		// return "myobjects-dashlet";
		return USERDASHBOARD_DASHLET_OBJECTS;
	}

	@Override
	public Instance dashletActionsTarget() {
		return null;
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
