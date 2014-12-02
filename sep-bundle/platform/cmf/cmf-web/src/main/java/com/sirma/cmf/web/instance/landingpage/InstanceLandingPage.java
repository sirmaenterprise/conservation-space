package com.sirma.cmf.web.instance.landingpage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.richfaces.function.RichFunction;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.cmf.web.EntityAction;
import com.sirma.cmf.web.SelectorItem;
import com.sirma.cmf.web.constants.CMFConstants;
import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.instance.InstanceContextInitializer;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.NullInstance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.web.action.event.EMFActionEvent;

/**
 * InstanceLandingPage encapsulates base functionality for creating and visualizing of instances.
 * 
 * @param <I>
 *            The instance type
 * @param <D>
 *            The instance definition type
 */
public abstract class InstanceLandingPage<I extends Instance, D extends DefinitionModel> extends
		EntityAction implements InstanceItemSelector {

	private boolean debug;

	@Inject
	private ServiceRegister serviceRegister;

	@Inject
	private AllowedChildrenTypeProvider allowedChildrenTypeProvider;

	@Inject
	private InstanceContextInitializer instanceContextInitializer;

	/**
	 * List used for the definition types menu in the landing page form if a new instance is to be
	 * created.
	 */
	private List<SelectorItem> definitionItems;

	/** The selected trough the page definition type. */
	private String selectedType;

	/**
	 * Gets the instance definition class for which the landing page is opened.
	 * 
	 * @return the instance name
	 */
	protected abstract Class<D> getInstanceDefinitionClass();

	/**
	 * Gets the new instance.
	 * 
	 * @param selectedDefinition
	 *            the selected definition
	 * @param context
	 *            the context
	 * @return the new instance
	 */
	protected abstract I getNewInstance(D selectedDefinition, Instance context);

	/**
	 * Gets the instance class.
	 * 
	 * @return the instance class
	 */
	protected abstract Class<I> getInstanceClass();

	/**
	 * Gets the parent reference.
	 * 
	 * @return the parent reference
	 */
	protected abstract InstanceReference getParentReference();

	/**
	 * Save instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return the string
	 */
	protected abstract String saveInstance(I instance);

	/**
	 * Cancel edit.
	 * 
	 * @param instance
	 *            the instance
	 * @return the string
	 */
	protected abstract String cancelEditInstance(I instance);

	/**
	 * Called during initializing of the page for existing instance.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected abstract void onExistingInstanceInitPage(I instance);

	/**
	 * Called during initializing of the page for new instance.
	 * 
	 * @param instance
	 *            the instance
	 */
	protected abstract void onNewInstanceInitPage(I instance);

	/**
	 * Gets the form fiew mode. In default implementation the form view mode is taken from
	 * DocumentContext
	 * 
	 * @param instance
	 *            the instance
	 * @return the form fiew mode
	 */
	protected abstract FormViewMode getFormViewModeExternal(I instance);

	/**
	 * Gets the navigation string to the landing page.
	 * 
	 * @return the navigation string
	 */
	protected abstract String getNavigationString();

	/**
	 * Gets the definition filter type to narrow definition filter event selection.
	 * 
	 * @return the definition filter type
	 */
	protected abstract String getDefinitionFilterType();

	/**
	 * Gets the instance service.
	 * 
	 * @return the instance service
	 */
	protected abstract InstanceService<I, D> getInstanceService();

	/**
	 * Inits the landing page.
	 */
	@PostConstruct
	public void init() {
		debug = log.isDebugEnabled();
	}

	/**
	 * Inits the page to be rendered.
	 */
	public void initPage() {
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
		String currentInstanceId = params.get("currentInstanceId");
		String currentInstanceType = params.get("currentInstanceType");
		// when starting workflow within the object landing page
		if (StringUtils.isNotNullOrEmpty(currentInstanceId)
				&& StringUtils.isNotNullOrEmpty(currentInstanceType)) {
			Instance contextInstance = fetchInstance(currentInstanceId, currentInstanceType);
			instanceContextInitializer.restoreHierarchy(contextInstance, getDocumentContext()
					.getRootInstance());
			if (contextInstance != null) {
				initContextForInstanceEdit(contextInstance);
			}
		}

		Class<I> instanceClass = getInstanceClass();
		I instance = getDocumentContext().getInstance(instanceClass);
		boolean persisted = SequenceEntityGenerator.isPersisted(instance);

		TimeTracker timer = null;
		UIComponent panel = getPanel(CMFConstants.INSTANCE_DATA_PANEL);
		// if instance is not new one, then call definition reader to render the form
		if ((instance != null) && persisted) {
			if (panel == null) {
				return;
			}
			if (debug) {
				timer = TimeTracker.createAndStart();
				log.debug("Initializing landing page for instance[" + instanceClass.getSimpleName()
						+ "] with id[" + instance.getId() + "]");
			}
			if (!isAjaxRequest() && !hasErrorsOnPage()) {
				initForEdit(instanceClass, instance, panel);
			}
			// after ajax the form should be reloaded only if force reload flag is stored
			else if (isAjaxRequest() && (isForceReload() == Boolean.TRUE)) {
				initForEdit(instanceClass, instance, panel);
			} else if (isAjaxRequest()) {
				initForEdit(instanceClass, instance, panel);
			}
		} else {
			initForNewInstance(instanceClass, instance);
		}

		if (timer != null) {
			log.debug("Landing page initialization took " + timer.stopInSeconds() + " s");
		}
	}

	/**
	 * Checks if is force reload flag is stored in context. In some cases this flag is used to force
	 * form rebuild after ajax request. This is the case for task reassign for example.
	 * 
	 * @return the boolean
	 */
	protected Boolean isForceReload() {
		Boolean forceReload = (Boolean) getDocumentContext().get(DocumentContext.FORCE_RELOAD_FORM);
		return (forceReload != null) ? forceReload : Boolean.FALSE;
	}

	/**
	 * Inits the page for new instance creation.
	 * 
	 * @param instanceClass
	 *            the instance class
	 * @param instance
	 *            the instance
	 */
	protected void initForNewInstance(Class<I> instanceClass, I instance) {
		if (debug) {
			log.debug("CMFWeb: InstanceLandingPage.initPage for new ["
					+ instanceClass.getSimpleName() + "]");
		}

		// for new instance we load definitions to allow selection
		if (definitionItems == null) {
			definitionItems = loadDefinitions();
		}

		// automatically open the definition if only one exists
		if ((definitionItems.size() == 1) && (instance == null)) {
			setSelectedType(definitionItems.get(0).getType());
			itemSelectedAction();
		}

		// populate operation if provided trough a request parameter
		FacesContext fc = FacesContext.getCurrentInstance();
		Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
		String actionId = params.get("actionId");
		if (StringUtils.isNotNullOrEmpty(actionId)) {
			getDocumentContext().setCurrentOperation(instanceClass.getSimpleName(), actionId);
		}

		getDocumentContext().setFormMode(FormViewMode.EDIT);

		onNewInstanceInitPage(getDocumentContext().getInstance(getInstanceClass()));
	}

	/**
	 * Inits the page for editing an existing instance.
	 * 
	 * @param instanceClass
	 *            the instance class
	 * @param instance
	 *            the instance
	 * @param panel
	 *            the panel
	 */
	protected void initForEdit(Class<I> instanceClass, I instance, UIComponent panel) {
		clearFormPanel(panel);

		FormViewMode formViewMode = getFormViewMode(instance);

		if (debug) {
			log.debug("CMFWeb: InstanceLandingPage.initPage for [" + instanceClass.getSimpleName()
					+ "] in FormViewMode [" + formViewMode + "]");
		}

		DefinitionModel definition = dictionaryService.getInstanceDefinition(instance);
		if (definition == null) {
			log.warn("CMFWeb: InstanceLandingPage.initForEdit: Can't render page with null definition");
			return;
		}

		invokeReader((RegionDefinitionModel) definition, instance, panel, formViewMode, null,
				getCurrentOperation(instanceClass));

		onExistingInstanceInitPage(instance);
	}

	/**
	 * Produces definition names and builds list with item objects for rendering as menu in the web
	 * page.
	 * 
	 * @return List with items.
	 */
	public List<SelectorItem> loadDefinitions() {
		List<D> definitions = fetchDefinitions();

		List<SelectorItem> items = new ArrayList<SelectorItem>(definitions.size());

		for (D definition : definitions) {
			PropertyDefinition typeProperty = (PropertyDefinition) definition
					.getChild(DefaultProperties.TYPE);
			Integer codelist = null;
			if (typeProperty != null) {
				codelist = typeProperty.getCodelist();
			}

			String definitionId = getDefinitionId(definition);

			String descr = definitionId;
			if (codelist != null) {
				descr = codelistService.getDescription(codelist, definitionId);
				if (StringUtils.isNotNullOrEmpty(descr)) {
					descr += " (" + definitionId + ")";
				}
				if (StringUtils.isNullOrEmpty(descr)) {
					descr = definitionId;
				}
			}

			items.add(new SelectorItem(definitionId, definition.getIdentifier(), descr));
		}

		Collections.sort(items, SelectorItem.DESCRIPTION_COMPARATOR);

		return items;
	}

	/**
	 * Gets the definition id.
	 * 
	 * @param definition
	 *            the definition
	 * @return the definition id
	 */
	protected String getDefinitionId(D definition) {
		return definition.getIdentifier();
	}

	/**
	 * Load definitions of given type.
	 * 
	 * @return {@link ProjectDefinition} list.
	 */
	@SuppressWarnings("unchecked")
	private List<D> fetchDefinitions() {

		List<D> definitions = new ArrayList<D>();
		I contextInstance = (I) getDocumentContext().getContextInstance();

		if (contextInstance == null) {
			definitions = dictionaryService.getAllDefinitions(getInstanceDefinitionClass());
		} else {
			Map<String, List<DefinitionModel>> allowedChildren = instanceService
					.getAllowedChildren(contextInstance);
			String instance = allowedChildrenTypeProvider.getTypeByInstance(getInstanceClass());
			if (instance != null) {
				instance = instance.toUpperCase();
			}
			List<D> list = (List<D>) allowedChildren.get(instance);
			if (list != null) {
				definitions = list;
			}
		}

		if (debug) {
			log.debug("CMFWeb: Executed InstanceLandigPage.fetchDefinitions: found "
					+ definitions.size() + " definitions");
		}

		fireDefinitionFilterEvent(definitions);

		if (debug) {
			log.debug("CMFWeb: Executed InstanceLandigPage.fetchDefinitions: after filter found "
					+ definitions.size() + " definitions");
		}

		return definitions;
	}

	/**
	 * Fire definition filter event.
	 * 
	 * @param definitions
	 *            the definitions
	 */
	private void fireDefinitionFilterEvent(List<D> definitions) {
		if (debug) {
			log.debug("CMFWeb: Executed InstanceLandigPage.fireDefinitionFilterEvent type ["
					+ getDefinitionFilterType() + "]");
		}

		DefinitionsFilterEvent filterEvent = new DefinitionsFilterEvent(definitions);
		DefinitionFilterEventBinding binding = new DefinitionFilterEventBinding(
				getDefinitionFilterType());
		eventService.fire(filterEvent, binding);
	}

	/**
	 * Open selected instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return navigation string
	 */
	@SuppressWarnings("unchecked")
	public String open(I instance) {
		if (debug) {
			log.debug("CMFWeb: Executing InstanceLandigPage.open instance [" + instance + "]");
		}

		D instanceDefinition = (D) dictionaryService.getInstanceDefinition(instance);
		if (instanceDefinition == null) {
			log.error("CMFWeb: InstanceLandingPage.open cann't open page for null definition");
			return null;
		}

		getDocumentContext().populateContext(instance, getInstanceDefinitionClass(),
				instanceDefinition);

		Instance parent = getParent();
		// populate context with parent instance if exists
		if (parent != null) {
			// getDocumentContext().addInstance(parent);
			getDocumentContext().addContextInstance(parent);
		}

		return getNavigationString();
	}

	/**
	 * Gets the parent instance if any or null.
	 * 
	 * @return the parent
	 */
	protected Instance getParent() {
		InstanceReference owningReference = getParentReference();
		if (owningReference != null) {
			return owningReference.toInstance();
		}

		return null;
	}

	/**
	 * Check if selected instance has required fields. If there are some, then set the instance in
	 * document context and navigate to the landing page in edit mode. If there aren't we save the
	 * instance and apply navigation.
	 * 
	 * @param event
	 *            the event that is triggered - it may be triggered from project page or from any
	 *            other page.
	 * @param navigation
	 *            Navigation string to be returned with the event.
	 * @param formViewMode
	 *            The form view mode to be applied when the instance landing page form is rendered.
	 * @param autosave
	 *            if instance should be saved automatically if no required fields are found
	 */
	public void initInstanceOperation(EMFActionEvent event, String navigation,
			FormViewMode formViewMode, boolean autosave) {
		I selectedInstance = (I) event.getInstance();
		// check for available event instance
		if (selectedInstance == null) {
			return;
		}
		if (getDocumentContext().getInstance(getInstanceClass()) == null) {
			getDocumentContext().addInstance(selectedInstance);
		}
		Set<String> requiredFieldsByDefinition = getRequiredFieldsByDefinition(
				selectedInstance,
				getDocumentContext().getCurrentOperation(
						selectedInstance.getClass().getSimpleName()));
		boolean hasEmptyRequiredFields = hasEmptyRequiredFields(selectedInstance,
				requiredFieldsByDefinition);
		if (!hasEmptyRequiredFields) {
			event.setNavigation(navigation);
			if (autosave) {
				Instance savedInstance = instanceService.save(selectedInstance, createOperation());
				// replace the instance in event with the one returned by the save method because it
				// is sometimes detached
				event.setInstance(savedInstance);
				// on user dashboard, remove the instance from context if the action is immediate
				if ("dashboard".equalsIgnoreCase(getCurrentPageName())) {
					getDocumentContext().removeInstance(savedInstance);
				}
			}
		} else {
			initContextForInstanceEdit(selectedInstance);
			event.setNavigation(getNavigationString());
			// apply the form view mode before redirect to the landing page
			if (formViewMode != null) {
				getDocumentContext().setFormMode(formViewMode);
			} else {
				getDocumentContext().setFormMode(FormViewMode.EDIT);
			}
		}
	}

	/**
	 * Checks if in instance there are unpopulated required fields.
	 * 
	 * @param selectedInstance
	 *            the selected instance
	 * @param requiredFieldsByDefinition
	 *            the required fields by definition
	 * @return true, if is immediate
	 */
	protected boolean hasEmptyRequiredFields(I selectedInstance,
			Set<String> requiredFieldsByDefinition) {
		boolean result = !requiredFieldsByDefinition.isEmpty()
				&& !selectedInstance.getProperties().keySet()
						.containsAll(requiredFieldsByDefinition);
		return result;
	}

	/**
	 * Gets the current operation.
	 * 
	 * @param instanceClass
	 *            the instance class
	 * @return the current operation
	 */
	private String getCurrentOperation(Class<I> instanceClass) {
		return getDocumentContext().getCurrentOperation(instanceClass.getSimpleName());
	}

	/**
	 * Creates an operation that should be passed to backend service on save/update.
	 * 
	 * @return the operation
	 */
	public Operation createOperation() {
		String currentOperation = getDocumentContext().getCurrentOperation(
				getInstanceClass().getSimpleName());

		// FacesContext fc = FacesContext.getCurrentInstance();
		// Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
		// String actionId = params.get("actionId");
		// if (StringUtils.isNotNullOrEmpty(actionId)) {
		// currentOperation = actionId;
		// }

		// when no operation is set, then use NullInstance as target
		if (currentOperation == null) {
			currentOperation = getDocumentContext().getCurrentOperation(
					NullInstance.class.getSimpleName());
		}
		return new Operation(currentOperation);
	}

	/**
	 * Sets the current operation.
	 * 
	 * @param instanceName
	 *            the instance name
	 * @param operationId
	 *            the new current operation
	 */
	protected void setCurrentOperation(String instanceName, String operationId) {
		getDocumentContext().setCurrentOperation(instanceName, operationId);
	}

	@Override
	public void itemSelectedAction(String componentId) {
		// get value of the select component from the request map because if nasty bug related with
		// immediate attribute not working on selectOne tag
		UIInput component = (UIInput) RichFunction.findComponent(componentId);
		String submittedValue = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get(component.getClientId());
		setSelectedType(submittedValue);

		itemSelectedAction();
	}

	/**
	 * Fire open event to allow expressions and injections to be evaluated.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param newInstance
	 *            the new instance
	 */
	protected <T extends Instance> void fireInstanceOpenEvent(T newInstance) {
		InstanceEventProvider<Instance> eventProvider = serviceRegister
				.getEventProvider(newInstance);
		if (eventProvider != null) {
			InstanceOpenEvent<Instance> openEvent = eventProvider.createOpenEvent(newInstance);
			eventService.fire(openEvent);
		}
	}

	/**
	 * Save current instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return navigation string.
	 */
	public String save(I instance) {
		TimeTracker timer = null;
		if (debug) {
			timer = TimeTracker.createAndStart();
			log.debug("Saving " + instance.getClass().getSimpleName() + " with id="
					+ instance.getId());
		}
		// change form mode to preview
		setPreviewMode();
		// clear selected type
		setSelectedType(null);
		// remove selected action from context
		getDocumentContext().clearSelectedAction();

		// TODO: if operation should be removed
		// setCurrentOperation(instance, null);

		String navigation = saveInstance(instance);
		if (debug) {
			log.debug("Saved instance[" + instance.getClass().getSimpleName() + "] with id["
					+ instance.getId() + "] took " + timer.stopInSeconds() + " s");
		}
		return navigation;
	}

	/**
	 * Cancel edit operation for current instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return navigation string.
	 */
	public String cancelEdit(I instance) {
		if (debug) {
			log.debug("CMFWeb: executing InstanceLandingPageAction.cancelEdit instance ["
					+ instance + "]");
		}

		setCurrentOperation(instance.getClass().getSimpleName(), null);
		setSelectedType(null);
		setPreviewMode();
		getDocumentContext().setSelectedAction(null);
		// removeEditedInstanceFromContext(instance);

		return cancelEditInstance(instance);
	}

	/**
	 * Gets the form view mode.
	 * 
	 * @param instance
	 *            the instance
	 * @return the form view mode
	 */
	public FormViewMode getFormViewMode(I instance) {
		if (instance == null) {
			return FormViewMode.PREVIEW;
		}
		// external mode has precedence
		FormViewMode formViewMode = getFormViewModeExternal(instance);
		// if not provided externally, then check it out from context
		if (formViewMode == null) {
			formViewMode = getDocumentContext().getFormMode();
		}
		// always return PREVIEW mode if none provided
		if (formViewMode == null) {
			formViewMode = FormViewMode.PREVIEW;
		}

		getDocumentContext().setFormMode(formViewMode);
		return formViewMode;
	}

	/**
	 * Checks if is preview mode.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if is preview mode
	 */
	public boolean isPreviewMode(I instance) {
		return getFormViewMode(instance) == FormViewMode.PREVIEW;
	}

	/**
	 * Sets the preview mode.
	 */
	public void setPreviewMode() {
		getDocumentContext().setFormMode(FormViewMode.PREVIEW);
	}

	/**
	 * Definition selector visiblity style.
	 * 
	 * @return the string
	 */
	public String definitionSelectorVisiblityStyle() {
		String styleClass = "";
		if ((definitionItems == null) || (definitionItems.size() == 1)) {
			styleClass = "hide";
		}
		return styleClass;
	}

	/**
	 * Checks if is new instance.
	 * 
	 * @return true, if is new instance
	 */
	public boolean isNewInstance() {
		I instance = getDocumentContext().getInstance(getInstanceClass());
		return !SequenceEntityGenerator.isPersisted(instance);
	}

	/**
	 * Calculates render condition for the landing page form save and cancel buttons.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if successful
	 */
	// TODO: if this is used
	public boolean renderFormButtons(I instance) {
		boolean result = false;

		Action selectedAction = getDocumentContext().getSelectedAction();
		if (selectedAction != null) {
			result = !selectedAction.isImmediateAction();
		}
		// FormViewMode formViewMode = getFormViewMode(instance);
		// if ((StringUtils.isNotNullOrEmpty(selectedType))
		// || (((instance != null) && (instance.getId() != null)) && (formViewMode ==
		// FormViewMode.EDIT))) {
		// result = true;
		// }

		return result;
	}

	/**
	 * Calculate view mode.<br>
	 * If the instance is not persisted, then use the form mode that is set when the operation is
	 * initiated. Else if the action is immediate and there are not empty required fields, then
	 * render in preview mode.
	 * 
	 * @param instance
	 *            the instance
	 * @return the form view mode
	 */
	private FormViewMode calculateViewMode(I instance) {
		FormViewMode calculatedFormViewMode = getDocumentContext().getFormMode();
		boolean persisted = isPersisted(instance);
		if (!persisted) {
			return calculatedFormViewMode;
		}
		Action selectedAction = getDocumentContext().getSelectedAction();
		Set<String> requiredFieldsByDefinition = getRequiredFieldsByDefinition(instance,
				getCurrentOperation(getInstanceClass()));
		// Render in preview if the action is marked as immediate and there are not required fields
		// or there are un-populated required fields.
		boolean hasRequiredFields = (!requiredFieldsByDefinition.isEmpty() && !instance
				.getProperties().keySet().containsAll(requiredFieldsByDefinition));
		boolean renderInPreview = (selectedAction != null) && selectedAction.isImmediateAction()
				&& !hasRequiredFields;
		if (renderInPreview) {
			calculatedFormViewMode = FormViewMode.PREVIEW;
		}
		return calculatedFormViewMode;
	}

	/**
	 * Getter method for editMode.
	 * 
	 * @param instance
	 *            the instance
	 * @return the editMode
	 */
	public boolean isEditMode(I instance) {
		return calculateViewMode(instance) == FormViewMode.EDIT;
	}

	/**
	 * Checks if instance is persisted.
	 * 
	 * @param instance
	 *            the instance
	 * @return true, if is persisted
	 */
	public boolean isPersisted(I instance) {
		boolean persisted = false;
		if (instance != null) {
			persisted = InstanceUtil.isPersisted(instance);
		}
		return persisted;
	}

	/**
	 * Gets the current page name from the request. If no FacesContext is found, then empty string
	 * is returned.
	 * 
	 * @return the current page name
	 */
	protected String getCurrentPageName() {
		String currentPage = "";
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null) {
			String servletPath = facesContext.getExternalContext().getRequestServletPath();
			currentPage = servletPath.substring(servletPath.lastIndexOf("/") + 1,
					servletPath.lastIndexOf("."));
		}
		return currentPage;
	}

	@Override
	public void itemSelectedAction() {
		if (debug) {
			log.debug("CMFWeb: InstanceLandingPage.itemSelectedAction selected definition: ["
					+ getSelectedType() + "]");
		}

		if (StringUtils.isNullOrEmpty(selectedType)) {
			return;
		}

		// - get selected definition type
		D selectedDefinition = dictionaryService.getDefinition(getInstanceDefinitionClass(),
				selectedType);
		if (selectedDefinition == null) {
			log.error("CMFWeb: InstanceLandingPage.itemSelectedAction cann't create new instance with null definition");
			return;
		}

		// For every new instance except project we should have the context inside which it
		// should be created.
		Instance contextInstance = getDocumentContext().getContextInstance();

		I newInstance = getNewInstance(selectedDefinition, contextInstance);

		fireInstanceOpenEvent(newInstance);

		UIComponent panel = getPanel(CMFConstants.INSTANCE_DATA_PANEL);
		if ((panel != null) && (panel.getChildCount() > 0)) {
			panel.getChildren().clear();
		}

		try {
			invokeReader((RegionDefinitionModel) selectedDefinition, newInstance, panel,
					FormViewMode.EDIT, null);

			onExistingInstanceInitPage(newInstance);
		} catch (RuntimeException e) {
			log.error("", e);
		}

		getDocumentContext().populateContext(newInstance, getInstanceDefinitionClass(),
				selectedDefinition);
	}

	/**
	 * Checks for errors on page.
	 * 
	 * @return true, if successful
	 */
	private boolean hasErrorsOnPage() {
		return FacesContext.getCurrentInstance().isValidationFailed();
	}

	/**
	 * Getter method for definitionItems.
	 * 
	 * @return the definitionItems
	 */
	public List<SelectorItem> getDefinitionItems() {
		return definitionItems;
	}

	/**
	 * Setter method for definitionItems.
	 * 
	 * @param definitionItems
	 *            the definitionItems to set
	 */
	public void setDefinitionItems(List<SelectorItem> definitionItems) {
		this.definitionItems = definitionItems;
	}

	/**
	 * Getter method for selectedType definition.
	 * 
	 * @return the selectedType
	 */
	public String getSelectedType() {
		return selectedType;
	}

	/**
	 * Setter method for selectedType definition.
	 * 
	 * @param selectedType
	 *            the selectedType to set
	 */
	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}

}
