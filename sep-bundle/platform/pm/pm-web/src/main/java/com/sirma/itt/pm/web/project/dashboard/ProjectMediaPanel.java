package com.sirma.itt.pm.web.project.dashboard;

import java.io.Serializable;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * <b>ProjectMediaPanel</b> manage functionality for dashlet, located in project dashboard. The
 * content is represented as document with image records, actions and filters.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "ProjectDashboard")
@ViewAccessScoped
public class ProjectMediaPanel extends DashboardPanelActionBase<DocumentInstance> implements
		Serializable, DashboardPanelController {

	/** The Constant serailVersionUID */
	private static final long serialVersionUID = 1L;

	/** The thumbnail prefix. */
	private static final String TUMBNAIL_PREFIX = "data:image/png;base64,";

	/** The rendition service. */
	@Inject
	private RenditionService renditionService;

	/** The context. */
	private ProjectInstance context;

	@Override
	protected void initializeForAsynchronousInvocation() {
		context = getDocumentContext().getInstance(ProjectInstance.class);
	}

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	public Set<String> dashletActionIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String targetDashletName() {
		return PROJECTDASHBOARD_DASHLET_MEDIA;
	}

	@Override
	public Instance dashletActionsTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeDefaultFilter() {
		searchCriteriaChanged();
	}

	/**
	 * Get compact tree header for current document instance.
	 * 
	 * @param instance
	 *            document instance
	 * @return compact tree header
	 */
	public String getDefaultHeader(DocumentInstance instance) {
		if ((instance == null) || (instance.getProperties() == null)) {
			return null;
		}
		return (String) instance.getProperties().get(DefaultProperties.HEADER_COMPACT);
	}

	/**
	 * Get thumbnail based on the instance.
	 * 
	 * @param instance
	 *            current instance
	 * @return thumbnail
	 */
	public String checkForThumbnail(Instance instance) {

		String thumbnail = renditionService.getThumbnail(instance);

		if (thumbnail != null) {
			return TUMBNAIL_PREFIX + thumbnail;
		}
		return null;
	}

	@Override
	public void updateSearchArguments(SearchArguments<DocumentInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub

	}

}