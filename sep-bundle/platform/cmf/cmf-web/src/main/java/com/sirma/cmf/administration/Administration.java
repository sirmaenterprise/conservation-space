package com.sirma.cmf.administration;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.ViewHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.services.ServerAdministration;
import com.sirma.itt.emf.definition.DictionaryService;

/**
 * Handles admin tasks in cmf.
 * 
 * @author y.yordanov
 */

@ManagedBean
@ViewScoped
public class Administration implements Serializable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3224230890293885573L;

	/**
	 * Cases group constant.
	 */
	private static final String CASES_GROUP = "cases";

	/**
	 * Documents group constant.
	 */
	private static final String DOCUMENTS_GROUP = "documents";

	/**
	 * Task group constant.
	 */
	private static final String WORKFLOW_GROUP = "workflows";

	@Inject
	ServerAdministration serverAdministration;

	@Inject
	DictionaryService dictionaryService;

	/**
	 * Definition group on server.
	 */

	private String serverGroup;

	/**
	 * Definition type on server.
	 */

	private String definitionType;

	/**
	 * CaseDefinitionList.
	 */
	private List<CaseDefinition> caseDefinitionList;

	/**
	 * Getter method for serverGroup.
	 * 
	 * @return the serverGroup
	 */
	public String getServerGroup() {
		return serverGroup;
	}

	/**
	 * Setter method for serverGroup.
	 * 
	 * @param serverGroup
	 *            the serverGroup to set
	 */
	public void setServerGroup(String serverGroup) {
		this.serverGroup = serverGroup;
	}

	/**
	 * Server groups refresh.
	 */
	public void refreshServerGroup() {
		if (serverGroup.equalsIgnoreCase(CASES_GROUP)
				|| serverGroup.equalsIgnoreCase(WORKFLOW_GROUP)) {
			serverAdministration.refreshDefinitions();
		} else if (serverGroup.equalsIgnoreCase(DOCUMENTS_GROUP)) {
			serverAdministration.refreshTemplateDefinitions();
		}
		setServerGroup(null);
	}

	/**
	 * Loads value into server group property.
	 * 
	 * @param event
	 *            - value change event
	 */
	public void preloadServerGroup(ValueChangeEvent event) {
		Object newValue = event.getNewValue();
		if (newValue != null) {
			setServerGroup(newValue.toString());
		} else {
			setServerGroup(null);
			// reloads page to disable submit button on form
			FacesContext context = FacesContext.getCurrentInstance();
			String viewId = context.getViewRoot().getViewId();
			ViewHandler handler = context.getApplication().getViewHandler();
			UIViewRoot root = handler.createView(context, viewId);
			root.setViewId(viewId);
			context.setViewRoot(root);
		}

	}

	/**
	 * Getter method for definitionType.
	 * 
	 * @return the definitionType
	 */
	public String getDefinitionType() {
		return definitionType;
	}

	/**
	 * Setter method for definitionType.
	 * 
	 * @param definitionType
	 *            the definitionType to set
	 */
	public void setDefinitionType(String definitionType) {
		caseDefinitionList = dictionaryService.getAllDefinitions(CaseDefinition.class);
		this.definitionType = definitionType;
	}

	/**
	 * Action listener for preview definitions button.
	 * 
	 * @param actionEvent
	 *            {@link ActionEvent}
	 */
	public void getRequiredDefinitions(ActionEvent actionEvent) {
		caseDefinitionList = dictionaryService.getAllDefinitions(CaseDefinition.class);
		// TODO refactor when corresponding core-api methods are available
	}

	/**
	 * Getter method for caseDefinitionList.
	 * 
	 * @return the caseDefinitionList
	 */
	public List<CaseDefinition> getCaseDefinitionList() {
		return caseDefinitionList;
	}

	/**
	 * Setter method for caseDefinitionList.
	 * 
	 * @param caseDefinitionList
	 *            the caseDefinitionList to set
	 */
	public void setCaseDefinitionList(List<CaseDefinition> caseDefinitionList) {
		this.caseDefinitionList = caseDefinitionList;
	}
}
