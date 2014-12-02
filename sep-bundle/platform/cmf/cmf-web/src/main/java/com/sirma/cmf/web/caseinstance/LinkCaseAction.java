package com.sirma.cmf.web.caseinstance;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.search.modal.AbstractBrowserHandler;
import com.sirma.cmf.web.search.modal.CmfEntityBrowser;
import com.sirma.cmf.web.search.modal.EntityBrowserHandler;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfAction;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * LinkCaseAction is responsible for operations in case link entity browser.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class LinkCaseAction extends
		AbstractBrowserHandler<CaseInstance, DocumentInstance, CaseInstance> implements
		EntityBrowserHandler, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2591052869042206026L;

	/** The cmf entity browser. */
	@Inject
	private CmfEntityBrowser cmfEntityBrowser;

	/** The link service. */
	@Inject
	private LinkService linkService;

	/** The selected section instance. */
	private CaseInstance selectedCaseInstance;

	/** The case link description. */
	private String caseLinkDescription;

	/**
	 * Open entity browser.
	 */
	public void openEntityBrowser() {
		EmfAction caseLinkAction = new EmfAction(ActionTypeConstants.LINK);
		cmfEntityBrowser.initBrowser(caseLinkAction,
				getDocumentContext().getInstance(CaseInstance.class));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void toggleTarget(CaseInstance caseInstance) {
		selectedCaseInstance = caseInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String acceptSelectedTarget() {
		log.debug("CMFWeb: Executing DocumentMoveAction.acceptSelectedTarget");
		return NavigationConstants.RELOAD_PAGE;
	}

	/**
	 * Makes the actual linking trough the current case and the selected one.
	 * 
	 * @return Navigation string.
	 */
	public String linkCases() {
		CaseInstance currentInstance = getDocumentContext().getInstance(CaseInstance.class);
		if ((getSelectedCaseInstance() != null) && (currentInstance != null)) {

			log.debug("CMFWeb: executing LinkCaseAction.linkCases: link case with id["
					+ currentInstance.getId() + "] and target case with id["
					+ selectedCaseInstance.getId() + "]");

			Map<String, Serializable> properties = new LinkedHashMap<String, Serializable>();
			properties.put(LinkConstants.LINK_DESCRIPTION, this.caseLinkDescription);
			linkService.link(currentInstance, selectedCaseInstance,
					LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID,
					LinkConstants.MANUAL_CASE_TO_CASE_LINK_ID, properties);
			return NavigationConstants.BACKWARD;
		}
		return NavigationConstants.RELOAD_PAGE;
	}

	/**
	 * Cancel case linking.
	 * 
	 * @return navigation string
	 */
	public String cancelCaseLink() {
		clear();
		return NavigationConstants.BACKWARD;
	}

	@Override
	public void initialize(CaseInstance caseInstance) {
		log.debug("CMFWeb: Executing LinkCaseAction.initialize");
		clear();
	}

	@Override
	public boolean isCurrent(CaseInstance caseInstance) {
		return getSelectedCaseInstance() != null ? EqualsHelper.nullSafeEquals(
				caseInstance.getDmsId(), getSelectedCaseInstance().getDmsId(), false) : false;
	}

	@Override
	public boolean canHandle(Action action) {
		String actionId = action.getActionId();
		return ActionTypeConstants.LINK.equals(actionId);
	}

	@Override
	public <S extends SearchArguments<Instance>> void afterSearch(S searchData) {
		String currentCaseDmsId = getDocumentContext().getInstance(CaseInstance.class).getDmsId();
		for (Iterator<Instance> iterator = searchData.getResult().iterator(); iterator.hasNext();) {
			CaseInstance caseInstance = (CaseInstance) iterator.next();
			if (EqualsHelper.nullSafeEquals(caseInstance.getDmsId(), currentCaseDmsId, false)) {
				iterator.remove();
				break;
			}
		}
	}

	@Override
	public String cancelSelection() {
		clear();
		return NavigationConstants.RELOAD_PAGE;
	}

	/**
	 * Removes the selected case.
	 */
	public void removeSelectedCase() {
		clear();
	}

	/**
	 * Clear selected data.
	 */
	private void clear() {
		this.selectedCaseInstance = null;
	}

	/**
	 * Getter method for selectedCaseInstance.
	 * 
	 * @return the selectedCaseInstance
	 */
	public CaseInstance getSelectedCaseInstance() {
		return selectedCaseInstance;
	}

	/**
	 * Getter method for caseLinkDescription.
	 * 
	 * @return the caseLinkDescription
	 */
	public String getCaseLinkDescription() {
		return caseLinkDescription;
	}

	/**
	 * Setter method for caseLinkDescription.
	 * 
	 * @param caseLinkDescription
	 *            the caseLinkDescription to set
	 */
	public void setCaseLinkDescription(String caseLinkDescription) {
		this.caseLinkDescription = caseLinkDescription;
	}

	/**
	 * Setter method for selectedCaseInstance.
	 * 
	 * @param selectedCaseInstance
	 *            the selectedCaseInstance to set
	 */
	public void setSelectedCaseInstance(CaseInstance selectedCaseInstance) {
		this.selectedCaseInstance = selectedCaseInstance;
	}

}
