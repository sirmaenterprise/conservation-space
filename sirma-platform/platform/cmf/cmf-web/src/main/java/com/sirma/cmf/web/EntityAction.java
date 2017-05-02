package com.sirma.cmf.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.inject.Inject;

import org.richfaces.function.RichFunction;

import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.form.Reader;
import com.sirma.itt.cmf.util.UserDisplayNameComparator;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.security.CurrentUser;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Common logic.
 *
 * @author svelikov
 */
public class EntityAction extends Action {

	@Inject
	protected DictionaryService dictionaryService;

	@Inject
	protected CodelistService codelistService;

	@Inject
	protected LabelProvider labelProvider;

	@Inject
	private StateService stateService;

	@Inject
	protected ResourceService resourceService;

	@Inject
	@CurrentUser
	protected User currentUser;

	@Inject
	protected Reader reader;

	@Inject
	protected StateTransitionManager transitionManager;

	@Inject
	protected InstanceService instanceService;

	@Inject
	protected InstanceContextInitializer instanceContextInitializer;

	private String documentName;

	private String currentInstanceId;

	private String currentInstanceType;

	/**
	 * Reload instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the instance
	 */
	protected Instance reloadInstance(Instance instance) {
		return instanceService.loadByDbId(instance.getId());
	}

	/**
	 * Inits the context for editing of instance.
	 *
	 * @param instance
	 *            the instance
	 */
	public void initContextForInstanceEdit(Instance instance) {
		if (instance != null) {
			getDocumentContext().addContextInstance(instance);
			getDocumentContext().setCurrentInstance(instance);
			// add root instance for the root instance header
			getDocumentContext().setRootInstance(InstanceUtil.getRootInstance(instance));
		}
	}

	/**
	 * Checks for editable fields.
	 *
	 * @param definitionModel
	 *            the definition model
	 * @return true, if successful
	 */
	public boolean hasEditableFields(DefinitionModel definitionModel) {

		boolean hasEditableFields = false;

		List<PropertyDefinition> fields = definitionModel.getFields();

		for (PropertyDefinition field : fields) {

			DisplayType displayType = field.getDisplayType();

			if (DisplayType.EDITABLE == displayType) {
				hasEditableFields = true;
				break;
			}
		}

		log.debug("CMFWeb: Checking if selected definition has editable fields [" + hasEditableFields + "]");

		return hasEditableFields;
	}

	/**
	 * Refresh instance.
	 *
	 * @param instance
	 *            the instance
	 */
	protected void refreshInstance(Instance instance) {
		if (instance != null && idManager.isPersisted(instance)) {
			instanceService.refresh(instance);
		}
	}

	/**
	 * Getter for property definition.
	 *
	 * @param key
	 *            Property key.
	 * @param instance
	 *            Instance from which to get a property.
	 * @return {@link PropertyDefinition} instance.
	 */
	public PropertyDefinition getProperty(String key, PropertyModel instance) {
		return dictionaryService.getProperty(key, instance);
	}

	/**
	 * Checks if the case is in status opened. FIXME: rename the method
	 *
	 * @param caseInstance
	 *            Current case instance.
	 * @return true if case is in status opened.
	 */
	public boolean isCaseOpened(Instance caseInstance) {
		boolean result = false;

		if (caseInstance != null) {
			return stateService.isInStates(caseInstance, PrimaryStates.OPENED, PrimaryStates.APPROVED,
					PrimaryStates.ON_HOLD);
		}

		return result;
	}

	/**
	 * Find the required panel in the view.
	 *
	 * @param panelId
	 *            the panel id
	 * @return UIComponent.
	 */
	protected UIComponent getPanel(String panelId) {
		return RichFunction.findComponent(panelId);
	}

	/**
	 * Clear dynamic form panel.
	 *
	 * @param panel
	 *            the panel
	 */
	public void clearFormPanel(UIComponent panel) {
		if (panel != null) {
			panel.getChildren().clear();
		}
	}

	/**
	 * Load users.
	 *
	 * @return the list
	 */
	public List<Resource> loadUsers() {
		List<Resource> allUsers = resourceService.getAllResources(ResourceType.USER, null);
		List<Resource> filteredUsers = new ArrayList<>(allUsers);
		Collections.sort(filteredUsers, new UserDisplayNameComparator());
		// TODO: perform filter by user group as defined in specification
		return filteredUsers;
	}

	/**
	 * Getter method for documentName.
	 *
	 * @return the documentName
	 */
	public String getDocumentName() {
		return documentName;
	}

	/**
	 * Setter method for documentName.
	 *
	 * @param documentName
	 *            the documentName to set
	 */
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	/**
	 * Merge fields from root and those from regions. <br>
	 * <b>Passed definition fields and regions are sorted but the object is modified too.</b>
	 *
	 * @param definition
	 *            the task definition
	 * @return the list
	 */
	// REVIEW: Passed definition fields and regions are sorted but the object is modified too.
	protected List<Ordinal> mergeFields(RegionDefinitionModel definition) {
		return DefinitionUtil.sortRegionsAndFields(definition.getFields(), definition.getRegions());
	}

	/**
	 * Invoke reader.
	 *
	 * @param definition
	 *            the definition
	 * @param instance
	 *            the instance
	 * @param panel
	 *            the panel
	 * @param formViewMode
	 *            the form view mode
	 * @param rootInstanceName
	 *            the root instance name
	 */
	protected void invokeReader(RegionDefinitionModel definition, Instance instance, UIComponent panel,
			FormViewMode formViewMode, String rootInstanceName) {
		TimeTracker timer = TimeTracker.createAndStart();
		List<Ordinal> fields = mergeFields(definition);
		reader.readSortables(fields, definition, instance, panel, formViewMode, rootInstanceName,
				Collections.EMPTY_SET);
		log.debug("Reading definition[" + definition.getClass().getSimpleName() + "] for instance["
				+ instance.getClass().getSimpleName() + "] and rendering form took " + timer.stopInSeconds() + " s");
	}

	/**
	 * Invoke reader.
	 *
	 * @param definition
	 *            the definition
	 * @param instance
	 *            the instance
	 * @param panel
	 *            the panel
	 * @param formViewMode
	 *            the form view mode
	 * @param rootInstanceName
	 *            the root instance name
	 * @param operation
	 *            the operation
	 */
	protected void invokeReader(RegionDefinitionModel definition, Instance instance, UIComponent panel,
			FormViewMode formViewMode, String rootInstanceName, String operation) {
		TimeTracker timer = TimeTracker.createAndStart();
		List<Ordinal> fields = mergeFields(definition);
		Set<String> requiredFields = getRequiredFieldsByDefinition(instance, operation);

		com.sirma.itt.seip.domain.security.Action selectedAction = getDocumentContext().getSelectedAction();
		FormViewMode calculatedFormViewMode = formViewMode;
		// Render in preview if the action is marked as immediate and there are not required fields
		// or there are un-populated required fields.
		boolean hasRequiredFields = !requiredFields.isEmpty()
				&& !instance.getProperties().keySet().containsAll(requiredFields);
		boolean renderInPreview = selectedAction != null && selectedAction.isImmediateAction() && !hasRequiredFields;
		if (renderInPreview) {
			calculatedFormViewMode = FormViewMode.PREVIEW;
		}

		reader.readSortables(fields, definition, instance, panel, calculatedFormViewMode, rootInstanceName,
				requiredFields);
		log.debug("Reading definition[" + definition.getClass().getSimpleName() + "] for instance["
				+ instance.getClass().getSimpleName() + "] and rendering form took " + timer.stopInSeconds() + " s");
	}

	/**
	 * Determine the required fields that need to be marked as such.
	 *
	 * @param instance
	 *            the instance
	 * @param operation
	 *            the operation
	 * @return the required fields by definition
	 */
	public Set<String> getRequiredFieldsByDefinition(Instance instance, String operation) {
		Set<String> requiredFields = transitionManager.getRequiredFields(instance,
				stateService.getPrimaryState(instance), operation);
		return requiredFields;
	}

	/**
	 * Getter method for currentInstanceId.
	 *
	 * @return the currentInstanceId
	 */
	public String getCurrentInstanceId() {
		return currentInstanceId;
	}

	/**
	 * Setter method for currentInstanceId.
	 *
	 * @param currentInstanceId
	 *            the currentInstanceId to set
	 */
	public void setCurrentInstanceId(String currentInstanceId) {
		this.currentInstanceId = currentInstanceId;
	}

	/**
	 * Getter method for currentInstanceType.
	 *
	 * @return the currentInstanceType
	 */
	public String getCurrentInstanceType() {
		return currentInstanceType;
	}

	/**
	 * Setter method for currentInstanceType.
	 *
	 * @param currentInstanceType
	 *            the currentInstanceType to set
	 */
	public void setCurrentInstanceType(String currentInstanceType) {
		this.currentInstanceType = currentInstanceType;
	}
}
