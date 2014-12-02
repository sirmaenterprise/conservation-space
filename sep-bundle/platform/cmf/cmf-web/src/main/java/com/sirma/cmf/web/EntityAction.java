package com.sirma.cmf.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.inject.Inject;

import org.richfaces.function.RichFunction;

import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.cmf.web.form.Reader;
import com.sirma.cmf.web.workflow.WorkflowSelectItemAction;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.cmf.util.UserDisplayNameComparator;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.CurrentUser;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.SortableComparator;

/**
 * Common logic.
 * 
 * @author svelikov
 */
public class EntityAction extends Action {

	/** The case instance service. {@link CaseService} instance. */
	@Inject
	protected CaseService caseInstanceService;

	/** The case instance service. {@link DocumentService} instance. */
	@Inject
	protected DocumentService documentService;

	/** The dictionary service. {@link DictionaryService} instance. */
	@Inject
	protected DictionaryService dictionaryService;

	/**
	 * CodelistService instance.
	 */
	@Inject
	protected CodelistService codelistService;

	/**
	 * Label provider instance.
	 */
	@Inject
	protected LabelProvider labelProvider;

	/**
	 * State service instance.
	 */
	@Inject
	private StateService stateService;

	/** The people service. */
	@Inject
	private ResourceService resourceService;

	/** Current logged user. */
	@Inject
	@CurrentUser
	protected User currentUser;

	/**
	 * The document name.
	 */
	private String documentName;

	/**
	 * Definition reader instance.
	 */
	@Inject
	protected Reader reader;

	/** The workflow select item action. */
	@Inject
	protected WorkflowSelectItemAction workflowSelectItemAction;

	/** The transition manager. */
	@Inject
	protected StateTransitionManager transitionManager;

	/** The event service. */
	@Inject
	protected EventService eventService;

	@Inject
	@Proxy
	protected InstanceService<Instance, DefinitionModel> instanceService;

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
			if (instance instanceof RootInstanceContext) {
				// add root instance for the root instance header
				getDocumentContext().setRootInstance(instance);
				getDocumentContext().addContextInstance(instance);
				getDocumentContext().addInstance(instance);
				getDocumentContext().setCurrentInstance(instance);
			} else {
				// add current instance needed for the header
				getDocumentContext().setCurrentInstance(instance);
				// add context instance needed for the the new instance to be initialized
				getDocumentContext().addContextInstance(instance);
				// add root instance for the root instance header
				Instance rootInstance = InstanceUtil.getRootInstance(instance, true);
				getDocumentContext().setRootInstance(rootInstance);
			}
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

		log.debug("CMFWeb: Checking if selected definition has editable fields ["
				+ hasEditableFields + "]");

		return hasEditableFields;
	}

	/**
	 * Reload case instance.
	 */
	protected void reloadCaseInstance() {
		// reload the case in order to get the changes after update
		CaseInstance caseInstance = getDocumentContext().getInstance(CaseInstance.class);
		if ((caseInstance != null) && SequenceEntityGenerator.isPersisted(caseInstance)) {
			caseInstanceService.refresh(caseInstance);
		}
	}

	/**
	 * Refresh instance.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected void refreshInstance(Instance instance) {
		if ((instance != null) && SequenceEntityGenerator.isPersisted(instance)) {
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
		return dictionaryService.getProperty(key, instance.getRevision(), instance);
	}

	/**
	 * Retrieves the {@link CaseInstance} status.
	 * 
	 * @return The status.
	 */
	// TODO: refactor this
	public String getCaseStatus() {
		DocumentContext documentContext = getDocumentContext();
		String state = null;
		if (documentContext != null) {
			CaseInstance caseInstance = documentContext.getInstance(CaseInstance.class);
			if (caseInstance != null) {
				Map<String, Serializable> props = caseInstance.getProperties();
				if (props != null) {
					state = (String) props.get(CaseProperties.STATUS);
				}
			}
		}

		return state;
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
			return stateService.isInStates(caseInstance, PrimaryStates.OPENED,
					PrimaryStates.APPROVED, PrimaryStates.ON_HOLD);
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
		List<Resource> filteredUsers = new ArrayList<Resource>(allUsers);
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
	protected List<Sortable> mergeFields(RegionDefinitionModel definition) {
		List<PropertyDefinition> fields = definition.getFields();

		List<RegionDefinition> regions = definition.getRegions();
		// sort fields in regions
		for (RegionDefinition regionDefinition : regions) {
			Collections.sort(regionDefinition.getFields(), new SortableComparator());
		}

		// put all the fields and regions in a list that will be passed to
		// definition reader
		List<Sortable> sortables = new ArrayList<Sortable>(fields.size() + regions.size());
		sortables.addAll(fields);
		sortables.addAll(regions);

		Collections.sort(sortables, new SortableComparator());
		return sortables;
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
	@SuppressWarnings("unchecked")
	protected void invokeReader(RegionDefinitionModel definition, Instance instance,
			UIComponent panel, FormViewMode formViewMode, String rootInstanceName) {
		TimeTracker timer = TimeTracker.createAndStart();
		List<Sortable> fields = mergeFields(definition);
		reader.readSortables(fields, definition, instance, panel, formViewMode, rootInstanceName,
				Collections.EMPTY_SET);
		log.debug("Reading definition[" + definition.getClass().getSimpleName() + "] for instance["
				+ instance.getClass().getSimpleName() + "] and rendering form took "
				+ timer.stopInSeconds() + " s");
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
	protected void invokeReader(RegionDefinitionModel definition, Instance instance,
			UIComponent panel, FormViewMode formViewMode, String rootInstanceName, String operation) {
		TimeTracker timer = TimeTracker.createAndStart();
		List<Sortable> fields = mergeFields(definition);
		Set<String> requiredFields = getRequiredFieldsByDefinition(instance, operation);

		com.sirma.itt.emf.security.model.Action selectedAction = getDocumentContext()
				.getSelectedAction();
		FormViewMode calculatedFormViewMode = formViewMode;
		// Render in preview if the action is marked as immediate and there are not required fields
		// or there are un-populated required fields.
		boolean hasRequiredFields = (!requiredFields.isEmpty() && !instance.getProperties()
				.keySet().containsAll(requiredFields));
		boolean renderInPreview = (selectedAction != null) && selectedAction.isImmediateAction()
				&& !hasRequiredFields;
		if (renderInPreview) {
			calculatedFormViewMode = FormViewMode.PREVIEW;
		}

		reader.readSortables(fields, definition, instance, panel, calculatedFormViewMode,
				rootInstanceName, requiredFields);
		log.debug("Reading definition[" + definition.getClass().getSimpleName() + "] for instance["
				+ instance.getClass().getSimpleName() + "] and rendering form took "
				+ timer.stopInSeconds() + " s");
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

}
