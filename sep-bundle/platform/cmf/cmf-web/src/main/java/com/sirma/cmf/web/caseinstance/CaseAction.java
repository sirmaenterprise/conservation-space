package com.sirma.cmf.web.caseinstance;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.caseinstance.tab.CaseTabConstants;
import com.sirma.cmf.web.constants.NavigationConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.NullInstance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.security.action.EMFAction;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;
import com.sirma.itt.emf.web.action.event.EmfImmediateActionEvent;

/**
 * CaseInstance processing manager.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class CaseAction extends CaseLandingPage {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2135275835545918747L;

	/** The case search action. */
	@Inject
	private CaseSearchAction caseSearchAction;

	/** Reason for closing the case. */
	private String caseClosingReason;

	/** The case section title. */
	private String caseSectionTitle;

	/**
	 * Observer for create action.
	 * 
	 * @param event
	 *            The event payload object.
	 */
	public void caseCreateNoContext(
			@Observes @EMFAction(value = ActionTypeConstants.CREATE_CASE, target = NullInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer CaseAction.caseCreate");
		navigationMenuAction.setSelectedMenu(NavigationConstants.NAVIGATE_MENU_CASE_LIST);
		getDocumentContext().clearAndLeaveContext();
		event.setNavigation(NavigationConstants.NAVIGATE_NEW_CASE);
	}

	/**
	 * Creates the case documents section.
	 * 
	 * @param event
	 *            the event
	 */
	public void createCaseDocumentsSection(
			@Observes @EMFAction(value = ActionTypeConstants.CREATE_DOCUMENTS_SECTION, target = CaseInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer CaseAction.createCaseDocumentsSection");
		getDocumentContext().getCurrentOperation(CaseInstance.class.getSimpleName());
	}

	/**
	 * Create the case section.
	 * 
	 * @return navigation string
	 */
	public String createCaseSection() {
		String currentOperation = getDocumentContext().getCurrentOperation(
				CaseInstance.class.getSimpleName());
		log.debug("Executing CaseAction operation [" + currentOperation + "], section title["
				+ caseSectionTitle + "]");

		String navigation = null;
		if (ActionTypeConstants.CREATE_DOCUMENTS_SECTION.equals(currentOperation)) {
			navigation = "case-documents";
			getDocumentContext().setSelectedTab("caseDocuments");
		} else if (ActionTypeConstants.CREATE_OBJECTS_SECTION.equals(currentOperation)) {
			// TODO: move this to objects module
			navigation = "case-objects";
			getDocumentContext().setSelectedTab("caseObjects");
		}
		return navigation;
	}

	/**
	 * Link case.
	 * 
	 * @param event
	 *            the event
	 */
	public void linkCase(
			@Observes @EMFAction(value = ActionTypeConstants.LINK, target = CaseInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer CaseAction.linkCase");
		CaseInstance selectedInstance = (CaseInstance) event.getInstance();
		if (selectedInstance != null) {
			getDocumentContext().setRootInstance(selectedInstance.getOwningInstance());
			getDocumentContext().addInstance(selectedInstance);
			event.setNavigation(NavigationConstants.NAVIGATE_CASE_LINK);
		}
	}

	/**
	 * Edit case opration observer. This operation should be executed only if there are case
	 * instance provided with the event and the case definition is found for that instance.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void caseEdit(
			@Observes @EMFAction(value = ActionTypeConstants.EDIT_DETAILS, target = CaseInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer CaseAction.caseEdit");
		String navigation = NavigationConstants.RELOAD_PAGE;
		CaseInstance selectedCaseInstance = (CaseInstance) event.getInstance();
		if (selectedCaseInstance != null) {
			getDocumentContext().setRootInstance(selectedCaseInstance.getOwningInstance());
			CaseDefinition caseDefinition = dictionaryService.getDefinition(
					getInstanceDefinitionClass(), selectedCaseInstance.getIdentifier());
			if (caseDefinition != null) {
				getDocumentContext().populateContext(selectedCaseInstance,
						getInstanceDefinitionClass(), caseDefinition);
				// TODO: why this is set here?
				setSelectedType((String) selectedCaseInstance.getProperties().get(
						CaseProperties.TYPE));
				getDocumentContext().setFormMode(FormViewMode.EDIT);
				navigation = NavigationConstants.NAVIGATE_TAB_CASE_DETAILS;
				getDocumentContext().setSelectedTab(CaseTabConstants.DETAILS);
			}
		}
		event.setNavigation(navigation);
	}

	/**
	 * Case close action observer.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void caseCloseObserver(
			@Observes @EMFAction(value = ActionTypeConstants.COMPLETE, target = CaseInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer CaseAction.caseCloseObserver");
		getDocumentContext().addInstance(event.getInstance());
	}

	/**
	 * (Immediate operation observer)Case close action observer.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void caseCloseImmediate(
			@Observes @EMFAction(value = ActionTypeConstants.COMPLETE, target = CaseInstance.class) final EmfImmediateActionEvent event) {
		log.debug("Executing observer CaseAction.caseCloseImmediate");
		event.setHandled(true);
	}

	/**
	 * Close selected case. This action is invoked when user clicks on 'ok' button from case closing
	 * reason popup panel.
	 * 
	 * @return navigation string
	 */
	public String caseClose() {
		if (!StringUtils.isNullOrEmpty(caseClosingReason)) {
			log.debug("Executing caseClose action");
			CaseInstance caseInstance = getDocumentContext().getInstance(getInstanceClass());
			instanceService.refresh(caseInstance);
			caseInstance.getProperties().put(CaseProperties.CLOSED_REASON, caseClosingReason);
			caseInstanceService.closeCaseInstance(caseInstance, createOperation());
			caseClosingReason = null;
		}
		return NavigationConstants.RELOAD_PAGE;
	}

	/**
	 * Case delete action.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void caseDelete(
			@Observes @EMFAction(value = ActionTypeConstants.DELETE, target = CaseInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer CaseAction.caseDelete");
		caseInstanceService.delete((CaseInstance) event.getInstance(), createOperation(), false);

		Instance currentInstance = getDocumentContext().getCurrentInstance();
		Instance contextInstance = getDocumentContext().getContextInstance();

		// default navigation when there is no current and context instance: suppose the delete is
		// performed from user dashboard or from a search result
		String navigation = NavigationConstants.RELOAD_PAGE;
		// if we are on case dashboard and have context like a project
		if ((currentInstance instanceof CaseInstance)
				&& (contextInstance instanceof RootInstanceContext)) {
			navigation = NavigationConstants.PROJECT_DASHBOARD;
			// TODO: removing is not needed if new currentInstance is added after that
			getDocumentContext().removeInstance(currentInstance);
			getDocumentContext().setCurrentInstance(contextInstance);
		}
		// if we are on project dashboard
		else if (currentInstance instanceof RootInstanceContext) {
			navigation = NavigationConstants.PROJECT_DASHBOARD;
		}

		event.setNavigation(navigation);
	}

	/**
	 * (Immediate operation observer)Case delete action.
	 * 
	 * @param event
	 *            Event payload object.
	 */
	public void caseDeleteImmediate(
			@Observes @EMFAction(value = ActionTypeConstants.DELETE, target = CaseInstance.class) final EmfImmediateActionEvent event) {
		log.debug("Executing observer CaseAction.caseDeleteImmediate");
		event.setHandled(true);
	}

	/**
	 * Invoked on case stop operation. Stop in dms and cmf
	 * 
	 * @param event
	 *            is the case stop event
	 */
	public void caseStopObserver(
			@Observes @EMFAction(value = ActionTypeConstants.STOP, target = CaseInstance.class) final EMFActionEvent event) {
		log.debug("Executing observer CaseAction.caseStopObserver");
		Instance instance = event.getInstance();
		instanceService.refresh(instance);
		getDocumentContext().addInstance(instance);
		caseInstanceService.closeCaseInstance((CaseInstance) instance, new Operation(
				ActionTypeConstants.STOP));
	}

	/**
	 * (Immediate operation observer)Invoked on case stop operation. Stop in dms and cmf
	 * 
	 * @param event
	 *            is the case stop event
	 */
	public void caseStopImmediate(
			@Observes @EMFAction(value = ActionTypeConstants.STOP, target = CaseInstance.class) final EmfImmediateActionEvent event) {
		log.debug("Executing observer CaseAction.caseStopImmediate");
		event.setHandled(true);
	}

	/**
	 * Getter method for caseClosingReason.
	 * 
	 * @return the caseClosingReason
	 */
	public String getCaseClosingReason() {
		return caseClosingReason;
	}

	/**
	 * Setter method for caseClosingReason.
	 * 
	 * @param caseClosingReason
	 *            the caseClosingReason to set
	 */
	public void setCaseClosingReason(String caseClosingReason) {
		this.caseClosingReason = caseClosingReason;
	}

	/**
	 * Getter method for caseSectionTitle.
	 * 
	 * @return the caseSectionTitle
	 */
	public String getCaseSectionTitle() {
		return caseSectionTitle;
	}

	/**
	 * Setter method for caseSectionTitle.
	 * 
	 * @param caseSectionTitle
	 *            the caseSectionTitle to set
	 */
	public void setCaseSectionTitle(String caseSectionTitle) {
		this.caseSectionTitle = caseSectionTitle;
	}

}
