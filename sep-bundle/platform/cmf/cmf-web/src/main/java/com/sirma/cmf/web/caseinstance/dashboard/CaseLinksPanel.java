package com.sirma.cmf.web.caseinstance.dashboard;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.userdashboard.DashboardPanelActionBase;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.web.dashboard.panel.DashboardPanelController;

/**
 * CaseLinksPanel backing bean.
 * 
 * @author svelikov
 */
@Named
@InstanceType(type = "CaseDashboard")
@ViewAccessScoped
public class CaseLinksPanel extends DashboardPanelActionBase<CaseInstance> implements Serializable,
		DashboardPanelController {

	private static final long serialVersionUID = 3517737978029467244L;

	private static final String CASEDASHBOARD_DASHLET_CASE_LINKS = "casedashboard_dashlet_case_links";

	private final Set<String> dashletActions = new HashSet<String>(
			Arrays.asList(ActionTypeConstants.LINK));

	@Inject
	private LinkService linkService;

	private List<LinkInstance> caseLinks;

	@Override
	public void initData() {
		onOpen();
	}

	@Override
	protected boolean isAsynchronousLoadingSupported() {
		return false;
	}

	@Override
	public void executeDefaultFilter() {
		CaseInstance instance = getDocumentContext().getInstance(CaseInstance.class);
		if (instance == null) {
			log.warn("CMFWeb: No instance to load the links for!");
			caseLinks = new LinkedList<>();
		} else {
			// CMF-4238
			List<LinkReference> links = linkService.getLinks(instance.toReference(),
					LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID, CaseInstance.class);
			caseLinks = linkService.convertToLinkInstance(links, false);
		}
		log.debug("CMFWeb: found [" + caseLinks.size() + "] case links");
	}

	/**
	 * Removes a selected case link.
	 * 
	 * @param linkInstance
	 *            the link instance
	 */
	public void removeCaseLink(LinkInstance linkInstance) {
		log.debug("CMFWeb: Executing CaseLinksPanel.removeCaseLink for case ["
				+ linkInstance.getFrom().getIdentifier() + "]");
		linkService.removeLink(linkInstance);
		executeDefaultFilter();
	}

	/**
	 * Getter method for caseLinks.
	 * 
	 * @return the caseLinks
	 */
	public List<LinkInstance> getCaseLinks() {
		return caseLinks;
	}

	/**
	 * Setter method for caseLinks.
	 * 
	 * @param caseLinks
	 *            the caseLinks to set
	 */
	public void setCaseLinks(List<LinkInstance> caseLinks) {
		this.caseLinks = caseLinks;
	}

	@Override
	public Set<String> dashletActionIds() {
		return dashletActions;
	}

	@Override
	public String targetDashletName() {
		return CASEDASHBOARD_DASHLET_CASE_LINKS;
	}

	@Override
	public Instance dashletActionsTarget() {
		return getDocumentContext().getCurrentInstance();
	}

	@Override
	public void updateSearchArguments(SearchArguments<CaseInstance> searchArguments,
			SearchFilter selectedSearchFilter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateSearchContext(Context<String, Object> context) {
		// TODO Auto-generated method stub

	}

}
