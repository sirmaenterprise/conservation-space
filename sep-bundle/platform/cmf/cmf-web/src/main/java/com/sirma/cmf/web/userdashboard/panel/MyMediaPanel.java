package com.sirma.cmf.web.userdashboard.panel;

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

/**
 * <b>MyMediaPanel</b> manage functionality for dashlet, located in personal/user dashboard. The
 * content is represented as document with image records, actions and filters.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "UserDashboard")
@ViewAccessScoped
public class MyMediaPanel extends DashboardPanelActionBase<DocumentInstance> implements
		Serializable, DashboardPanelController {

	/** The Constant serial version identifier. */
	private static final long serialVersionUID = 1L;

	/** The thumbnail prefix needed to represent the image as <b>base64</b> */
	private static final String TUMBNAIL_PREFIX = "data:image/png;base64,";

	@Inject
	private RenditionService renditionService;

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
	public Set<String> dashletActionIds() {
		// auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String targetDashletName() {
		return USERDASHBOARD_DASHLET_MEDIA;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance dashletActionsTarget() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
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
		// auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// auto-generated method stub

	}

}