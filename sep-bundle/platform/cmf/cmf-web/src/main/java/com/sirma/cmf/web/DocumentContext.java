package com.sirma.cmf.web;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.form.FormViewMode;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.RootInstanceContext;

/**
 * Case instance (document) context object.<br>
 * <b>This explicitly should be set in lower scope because we support multiple browser tabs!</b>
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class DocumentContext extends AbstractMap<String, Serializable> implements Serializable {

	private static final long serialVersionUID = 3344415602819096430L;

	public static final String CURRENT_INSTANCE = "currentInstance";

	public static final String ROOT_INSTANCE = "rootInstance";

	public static final String CONTEXT_INSTANCE = "contextInstance";

	public static final String OPERATION_SUFFIX = "_operation";

	public static final String FORM_MODE = "formMode";

	public static final String DOCUMENT_INSTANCE = "documentInstance";

	public static final String DOCUMENT_DEFINITION = "documentDefinition";

	public static final String SELECTED_TAB = "selectedTab";

	public static final String STRUCTURED_DOCUMENT_PATH = "structuredDocumentPath";

	public static final String TOPIC_INSTANCE = "topicInstance";

	public static final String FORCE_RELOAD_FORM = "forceReloadForm";

	public static final String SELECTED_ACTION = "selectedAction";

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	@Proxy
	private InstanceService<Instance, DefinitionModel> instanceService;

	private final Map<String, Serializable> context = new HashMap<String, Serializable>();

	/**
	 * Serialize document context.
	 * 
	 * @return the serialized document context
	 */
	public SerializedDocumentContext serialize() {
		SerializedDocumentContext serialized = new SerializedDocumentContext();
		Map<String, Serializable> serializedContext = serialized.getContext();
		for (Entry<String, Serializable> entry : this.entrySet()) {
			String key = entry.getKey();
			Serializable serializable = entry.getValue();
			// Instances are not serialized and are stored as is.

			// Definitions are not serialized but their instance is stored instead
			// for definitions we store a Pair object in order to allow them to be recognized and
			// properly deserialized.
			if (serializable instanceof DefinitionModel) {
				String simpleName = serializable.getClass().getSimpleName();
				Instance instanceByDefinitionTypeKey = this
						.getInstanceByDefinitionTypeKey(simpleName);
				if (instanceByDefinitionTypeKey != null) {
					Pair<String, InstanceReference> pair = new Pair<String, InstanceReference>(key,
							instanceByDefinitionTypeKey.toReference());
					serializedContext.put(key, pair);
					continue;
				}
			}
			serializedContext.put(key, serializable);
		}
		return serialized;
	}

	/**
	 * De-serialize document context.
	 * 
	 * @param serialized
	 *            the serialized
	 */
	public void deserialize(SerializedDocumentContext serialized) {
		if ((serialized == null) || (serialized.getContext() == null)) {
			return;
		}
		Map<String, Serializable> serializedContext = serialized.getContext();
		for (Entry<String, Serializable> entry : serializedContext.entrySet()) {
			String key = entry.getKey();
			Serializable serializable = entry.getValue();
			// Instances are refreshed and restored in context
			if (serializable instanceof Instance) {
				instanceService.refresh((Instance) serializable);
				this.put(key, serializable);
				continue;
			}
			// Only definitions are 'serialized' in Pair object
			// get instance reference from the pair, restore the instance, load the definition and
			// store in context.
			if (serializable instanceof Pair<?, ?>) {
				InstanceReference instanceReference = (InstanceReference) ((Pair<?, ?>) serializable)
						.getSecond();
				if (instanceReference != null) {
					Instance instance = instanceReference.toInstance();
					DefinitionModel instanceDefinition = dictionaryService
							.getInstanceDefinition(instance);
					this.put(key, instanceDefinition);
					this.definitionKeyToInstanceValue(key, instance);
				}
				continue;
			}
			this.put(key, serializable);
		}
	}

	/**
	 * Clear workflow data.
	 */
	public void clearWorkflowData() {
		put(WorkflowInstanceContext.class.getSimpleName(), null);
		put(WorkflowDefinition.class.getSimpleName(), null);
		put(TaskDefinitionRef.class.getSimpleName(), null);
		put(TaskInstance.class.getSimpleName(), null);
	}

	/**
	 * Clear and leave context if is ProjectInstance.
	 */
	public void clearAndLeaveContext() {
		Instance projectInstance = (Instance) this.get("ProjectInstance");
		Serializable projectDefinition = this.get("ProjectDefinition");
		Instance contextInstance = this.getContextInstance();
		Instance rootInstance = this.getRootInstance();
		this.clear();
		if (projectInstance != null) {
			this.put("ProjectInstance", projectInstance);
			this.setCurrentInstance(projectInstance);
		}
		if (projectDefinition != null) {
			this.put("ProjectDefinition", projectDefinition);
		}
		// If context instance is not a ProjectInstance, we search up the instance tree in order to
		// find if there is another root instance. If there is one, and is same as current root
		// instance that is in the document context, we use it as context instance. Otherwise
		// context instance is left unitialized.
		if (contextInstance != null) {
			if (contextInstance instanceof RootInstanceContext) {
				this.addContextInstance(contextInstance);
			} else {
				Instance root = InstanceUtil.getRootInstance(contextInstance, false);
				if ((root != null) && rootInstance.getId().equals(root.getId())) {
					this.addContextInstance(rootInstance);
				}
			}
		}
		if (rootInstance != null) {
			this.setRootInstance(rootInstance);
		}
	}

	/**
	 * Gets the current instance whatever it is by common key.
	 * 
	 * @return the current instance
	 */
	public Instance getCurrentInstance() {
		return (Instance) get(get(CURRENT_INSTANCE));
	}

	/**
	 * Sets the current instance if not null.
	 * 
	 * @param instance
	 *            the new current instance
	 */
	public void setCurrentInstance(Instance instance) {
		if (instance != null) {
			put(CURRENT_INSTANCE, instance.getClass().getSimpleName());
			addInstance(instance);
		}
	}

	/**
	 * Removes the current instance.
	 */
	public void removeCurrentInstance() {
		remove(CURRENT_INSTANCE);
	}

	/**
	 * Update current instance if exists.
	 * 
	 * @param instance
	 *            the instance
	 */
	public void updateCurrentInstance(Instance instance) {
		if (getCurrentInstance() != null) {
			setCurrentInstance(instance);
		}
	}

	/**
	 * Sets the root instance. The root instance is the top most instance in the current instance
	 * hierarchy.
	 * 
	 * @param instance
	 *            the new root instance
	 */
	public void setRootInstance(Instance instance) {
		if (instance != null) {
			put(ROOT_INSTANCE, instance);
		}
	}

	/**
	 * Gets the root instance.
	 * 
	 * @return the root instance
	 */
	public Instance getRootInstance() {
		return (Instance) get(ROOT_INSTANCE);
	}

	/**
	 * Update root instance if exists.
	 * 
	 * @param instance
	 *            the instance
	 */
	public void updateRootInstance(Instance instance) {
		if (getRootInstance() != null) {
			setRootInstance(instance);
		}
	}

	/**
	 * Populate context if not null value for instance and definition are provided.
	 * 
	 * @param <D>
	 *            the generic type
	 * @param instance
	 *            the instance
	 * @param type
	 *            the type
	 * @param definition
	 *            the definition
	 */
	public <D extends DefinitionModel> void populateContext(Instance instance, Class<D> type,
			D definition) {
		if ((instance != null) && (definition != null)) {
			setCurrentInstance(instance);
			addDefinition(type, definition);
			definitionKeyToInstanceValue(type.getSimpleName(), instance);
		}
	}

	/**
	 * Definition key to instance value connection.
	 * 
	 * @param type
	 *            the type
	 * @param instance
	 *            the instance
	 */
	public void definitionKeyToInstanceValue(String type, Instance instance) {
		this.put(type + "_link", instance);
	}

	/**
	 * Gets the instance by definition type key.
	 * 
	 * @param type
	 *            the type
	 * @return the instance by definition type key
	 */
	public Instance getInstanceByDefinitionTypeKey(String type) {
		return (Instance) this.get(type + "_link");
	}

	/**
	 * Sets the form mode.
	 * 
	 * @param formViewMode
	 *            the new form mode
	 */
	public void setFormMode(FormViewMode formViewMode) {
		put(FORM_MODE, formViewMode);
	}

	/**
	 * Gets the form mode and if there is stored form mode in context, then returns that mode.
	 * Otherwise return preview mode.
	 * 
	 * @return the form mode
	 */
	public FormViewMode getFormMode() {
		FormViewMode mode = (FormViewMode) get(FORM_MODE);
		if (mode == null) {
			mode = FormViewMode.PREVIEW;
		}
		return mode;
	}

	/**
	 * Gets the current operation.
	 * 
	 * @param instanceName
	 *            the instance name
	 * @return the current operation
	 */
	public String getCurrentOperation(String instanceName) {
		return (String) get(instanceName + OPERATION_SUFFIX);
	}

	/**
	 * Sets the current operation.
	 * 
	 * @param instanceName
	 *            the instance name
	 * @param operationId
	 *            the operation id
	 */
	public void setCurrentOperation(String instanceName, String operationId) {
		put(instanceName + OPERATION_SUFFIX, operationId);
	}

	/**
	 * Remove current operation for given instance name.
	 * 
	 * @param instanceName
	 *            the instance name
	 */
	public void resetCurrentOperation(String instanceName) {
		remove(instanceName + OPERATION_SUFFIX);
	}

	/**
	 * Adds the context instance. A context instance is one that is immediate parent of the current
	 * instance being visualized. For example a case created in a project and opened would have the
	 * project as context.
	 * 
	 * @param instance
	 *            the instance
	 */
	public void addContextInstance(Instance instance) {
		if (instance != null) {
			put(CONTEXT_INSTANCE, instance);
		}
	}

	/**
	 * Gets the context instance.
	 * 
	 * @return the context instance
	 */
	public Instance getContextInstance() {
		return (Instance) get(CONTEXT_INSTANCE);
	}

	/**
	 * Update context instance if exists.
	 * 
	 * @param instance
	 *            the instance
	 */
	public void updateContextInstance(Instance instance) {
		if (getContextInstance() != null) {
			addContextInstance(instance);
		}
	}

	/**
	 * Adds the instance if not null.
	 * 
	 * @param instance
	 *            the instance
	 */
	public void addInstance(Instance instance) {
		if (instance != null) {
			put(instance.getClass().getSimpleName(), instance);
		}
	}

	/**
	 * Gets the single instance of DocumentContext.
	 * 
	 * @param <I>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return single instance of DocumentContext
	 */
	public <I extends Instance> I getInstance(Class<I> type) {
		return type.cast(get(type.getSimpleName()));
	}

	/**
	 * Update instance if exists.
	 * 
	 * @param instance
	 *            the instance
	 */
	public void updateInstance(Instance instance) {
		if (getInstance(instance.getClass()) != null) {
			addInstance(instance);
		}
	}

	/**
	 * Removes instance from context.
	 * 
	 * @param instance
	 *            the instance
	 */
	public void removeInstance(Instance instance) {
		this.remove(instance.getClass().getSimpleName());
	}

	/**
	 * Adds the definition if not null.
	 * 
	 * @param <D>
	 *            the generic type
	 * @param type
	 *            the type
	 * @param definition
	 *            the definition
	 */
	public <D extends DefinitionModel> void addDefinition(Class<D> type, D definition) {
		if (definition != null) {
			put(type.getSimpleName(), definition);
		}
	}

	/**
	 * Gets the definition.
	 * 
	 * @param <D>
	 *            the generic type
	 * @param type
	 *            the type
	 * @return the definition
	 */
	public <D extends DefinitionModel> D getDefinition(Class<D> type) {
		return type.cast(get(type.getSimpleName()));
	}

	/**
	 * Removes definition of given type from context.
	 * 
	 * @param <D>
	 *            the generic type
	 * @param type
	 *            the type
	 */
	public <D extends DefinitionModel> void removeDefinition(Class<D> type) {
		this.remove(type.getSimpleName());
	}

	/**
	 * Sets the selected action.
	 * 
	 * @param action
	 *            the new selected action
	 */
	public void setSelectedAction(com.sirma.itt.emf.security.model.Action action) {
		this.put(SELECTED_ACTION, action);
	}

	/**
	 * Gets the selected action.
	 * 
	 * @return the selected action
	 */
	public com.sirma.itt.emf.security.model.Action getSelectedAction() {
		return (com.sirma.itt.emf.security.model.Action) this.get(SELECTED_ACTION);
	}

	/**
	 * Clear selected action from context.
	 */
	public void clearSelectedAction() {
		this.remove(SELECTED_ACTION);
	}

	/**
	 * Sets the document instance.
	 * 
	 * @param documentInstance
	 *            the new document instance
	 */
	public void setDocumentInstance(DocumentInstance documentInstance) {
		put(DOCUMENT_INSTANCE, documentInstance);
	}

	/**
	 * Removes the document data.
	 */
	public void removeDocumentData() {
		remove(DOCUMENT_INSTANCE);
		remove(DOCUMENT_DEFINITION);
	}

	/**
	 * Sets the document definition.
	 * 
	 * @param documentDefinition
	 *            the new document definition
	 */
	public void setDocumentDefinition(DocumentDefinitionRef documentDefinition) {
		put(DOCUMENT_DEFINITION, documentDefinition);
	}

	/**
	 * Sets the selected tab.
	 * 
	 * @param selectedTab
	 *            the new selected tab
	 */
	public void setSelectedTab(String selectedTab) {
		put(SELECTED_TAB, selectedTab);
	}

	/**
	 * Set topic instance.
	 * 
	 * @param topicInstance
	 *            topic instance
	 */
	public void setTopicInstance(TopicInstance topicInstance) {
		put(TOPIC_INSTANCE, topicInstance);
	}

	/**
	 * Gets the document instance.
	 * 
	 * @return the document instance
	 */
	public DocumentInstance getDocumentInstance() {
		return (DocumentInstance) get(DOCUMENT_INSTANCE);
	}

	/**
	 * Gets the document definition.
	 * 
	 * @return the document definition
	 */
	public DocumentDefinitionRef getDocumentDefinition() {
		return (DocumentDefinitionRef) get(DOCUMENT_DEFINITION);
	}

	/**
	 * Gets the selected tab.
	 * 
	 * @return the selected tab
	 */
	public String getSelectedTab() {
		return (String) get(SELECTED_TAB);
	}

	/**
	 * Get topic instance.
	 * 
	 * @return topic instance
	 */
	public TopicInstance getTopicInstance() {
		return (TopicInstance) get(TOPIC_INSTANCE);
	}

	// Overriden AbstractMap methods

	@Override
	public boolean containsKey(Object key) {

		return context.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {

		return context.containsValue(value);
	}

	@Override
	public Serializable get(Object key) {

		return context.get(key);
	}

	@Override
	public Serializable put(String key, Serializable value) {

		return context.put(key, value);
	}

	@Override
	public Serializable remove(Object key) {

		return context.remove(key);
	}

	@Override
	public void clear() {

		context.clear();
	}

	@Override
	public Set<String> keySet() {

		return context.keySet();
	}

	@Override
	public Collection<Serializable> values() {

		return context.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Serializable>> entrySet() {

		return context.entrySet();
	}

}
