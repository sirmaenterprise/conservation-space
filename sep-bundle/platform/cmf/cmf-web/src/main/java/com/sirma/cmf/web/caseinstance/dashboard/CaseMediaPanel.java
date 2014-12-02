package com.sirma.cmf.web.caseinstance.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * This class will display all object that have thumbnails. Will be used on the case dashboard.
 * 
 * @author cdimitrov
 */
@Named
@InstanceType(type = "CaseDashboard")
@ViewAccessScoped
public class CaseMediaPanel extends DashboardPanelActionBase<DocumentInstance> implements
		Serializable, DashboardPanelController {

	private static final long serialVersionUID = 1L;

	private static final String CASEDASHBOARD_DASHLET_MEDIA = "casedashboard_dashlet_media";

	private static final String TUMBNAIL_PREFIX = "data:image/png;base64,";

	/** The string splitter identifier. */
	private static final String STRING_SPLITTER = "/";

	@Inject
	private RenditionService renditionService;

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
		return CASEDASHBOARD_DASHLET_MEDIA;
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
	 * Filters {@link DocumentInstance} list and leaves only those with attachments.
	 * 
	 * @param documentInstances
	 *            List with {@link DocumentInstance} objects.
	 * @return List with {@link DocumentInstance} objects that has attachments.
	 */
	private List<DocumentInstance> getAllWithAttachments(List<Instance> documentInstances) {

		List<DocumentInstance> result = new ArrayList<DocumentInstance>(documentInstances.size());

		for (Instance documentInstance : documentInstances) {
			if ((documentInstance instanceof DocumentInstance)
					&& ((DocumentInstance) documentInstance).hasDocument()) {
				if (isImage((DocumentInstance) documentInstance)) {
					result.add((DocumentInstance) documentInstance);
				}
			}
		}

		return result;
	}

	/**
	 * This method help for detecting specific MIME types for the file. TODO: after mapping for MIME
	 * type is implemented this method will be removed.
	 * 
	 * @param documentInstance
	 *            document instance.
	 * @return boolean value
	 */
	protected boolean isImage(DocumentInstance documentInstance) {
		boolean isValid = false;
		if ((documentInstance == null) || (documentInstance.getProperties() == null)) {
			return isValid;
		}
		String mimeType = (String) documentInstance.getProperties()
				.get(DocumentProperties.MIMETYPE);
		if ((mimeType != null) && mimeType.startsWith("image")) {
			isValid = true;
		}
		return isValid;
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