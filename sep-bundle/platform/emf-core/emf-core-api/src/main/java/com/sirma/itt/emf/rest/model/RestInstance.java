package com.sirma.itt.emf.rest.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.security.model.Action;

/**
 * Provides the content and metadata of a single instance as a REST service result.
 *
 * @author Adrian Mitev
 * @author BBonev
 */
public class RestInstance implements Entity<String>, Identity {

	/**
	 * The id of the object/instance that is represented by the current instance.
	 */
	private String id;

	/** The id of the parent object/instance. */
	private String parentId;

	/**
	 * The type of the current object. The type should be compatible as argument for
	 * {@link com.sirma.itt.emf.definition.DictionaryService#getDataTypeDefinition(String)}
	 */
	private String type;

	/** The view id. The identifier for the content location. */
	private String viewId;

	/**
	 * The owning instance id. The id of the instance that is a context/parent/holder for the
	 * current instance.
	 */
	private String owningInstanceId;

	/**
	 * The owning instance type. The type should be compatible as argument for
	 * {@link com.sirma.itt.emf.definition.DictionaryService#getDataTypeDefinition(String)} .
	 */
	private String owningInstanceType;

	/**
	 * The definition id of the current object. It's main purpose is to be used when creating new
	 * instance of the current type.
	 */
	private String definitionId;

	/** The properties of the current instance. */
	private Map<String, Object> properties;

	/** The properties of the loaded view. */
	private Map<String, Object> viewProperties;

	/** The content that need to be displayed for the current instance. */
	private String content;

	private boolean overwrite;

	/**
	 * Version number of the loaded historic version of the view, {@code null} if current version is
	 * loaded.
	 */
	private String loadedViewVersion;

	/**
	 * The current operation id. The operation that is performed to trigger the open of the current
	 * instance. Required when saving the instance changes.
	 */
	private String currentOperation;

	/** Default header for the current instance. */
	private String defaultHeader;

	/** The allowed actions that need to be displayed for the current instance. */
	private Set<Action> actions;


	/**
	 * Initializes document properties map.
	 */
	public RestInstance() {
		properties = new HashMap<String, Object>();
		viewProperties = new HashMap<String, Object>();
		actions = new HashSet<>();
	}

	/**
	 * Getter method for content.
	 *
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Setter method for content.
	 *
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the loadedViewVersion
	 */
	public String getLoadedViewVersion() {
		return loadedViewVersion;
	}

	/**
	 * @param loadedViewVersion
	 *            the loadedViewVersion to set
	 */
	public void setLoadedViewVersion(String loadedViewVersion) {
		this.loadedViewVersion = loadedViewVersion;
	}

	/**
	 * Getter method for properties.
	 *
	 * @return the properties
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * Setter method for properties.
	 *
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id
	 *            the id to set
	 */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Getter method for parentId.
	 *
	 * @return the parent id
	 */
	public String getParentId() {
		return parentId;
	}

	/**
	 * Setter method for parentId.
	 *
	 * @param parentId
	 *            the parentId to set
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * Getter method for definitionId.
	 *
	 * @return the definitionId
	 */
	public String getDefinitionId() {
		return definitionId;
	}

	/**
	 * Setter method for definitionId.
	 *
	 * @param definitionId
	 *            the definitionId to set
	 */
	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}

	/**
	 * Getter method for actions.
	 *
	 * @return the actions
	 */
	public Set<Action> getActions() {
		return actions;
	}

	/**
	 * Setter method for actions.
	 *
	 * @param actions
	 *            the actions to set
	 */
	public void setActions(Set<Action> actions) {
		this.actions = actions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return getDefinitionId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		definitionId = identifier;
	}

	/**
	 * Getter method for viewId.
	 *
	 * @return the viewId
	 */
	public String getViewId() {
		return viewId;
	}

	/**
	 * Setter method for viewId.
	 *
	 * @param viewId
	 *            the viewId to set
	 */
	public void setViewId(String viewId) {
		this.viewId = viewId;
	}

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for owningInstanceId.
	 *
	 * @return the owningInstanceId
	 */
	public String getOwningInstanceId() {
		return owningInstanceId;
	}

	/**
	 * Setter method for owningInstanceId.
	 *
	 * @param owningInstanceId
	 *            the owningInstanceId to set
	 */
	public void setOwningInstanceId(String owningInstanceId) {
		this.owningInstanceId = owningInstanceId;
	}

	/**
	 * Getter method for owningInstanceType.
	 *
	 * @return the owningInstanceType
	 */
	public String getOwningInstanceType() {
		return owningInstanceType;
	}

	/**
	 * Setter method for owningInstanceType.
	 *
	 * @param owningInstanceType
	 *            the owningInstanceType to set
	 */
	public void setOwningInstanceType(String owningInstanceType) {
		this.owningInstanceType = owningInstanceType;
	}

	/**
	 * Getter method for currentOperation.
	 *
	 * @return the currentOperation
	 */
	public String getCurrentOperation() {
		return currentOperation;
	}

	/**
	 * Setter method for currentOperation.
	 *
	 * @param currentOperation
	 *            the currentOperation to set
	 */
	public void setCurrentOperation(String currentOperation) {
		this.currentOperation = currentOperation;
	}

	/**
	 * Getter method for viewProperties.
	 *
	 * @return the viewProperties
	 */
	public Map<String, Object> getViewProperties() {
		return viewProperties;
	}

	/**
	 * Setter method for defaultHeader.
	 *
	 * @param defaultHeader
	 *            the default header
	 */
	public void setDefaultHeader(String defaultHeader) {
		this.defaultHeader = defaultHeader;
	}

	/**
	 * Getter method for defaultHeader.
	 *
	 * @return the default header
	 */
	public String getDefaultHeader() {
		return defaultHeader;
	}

	/**
	 * Setter method for viewProperties.
	 *
	 * @param viewProperties
	 *            the viewProperties to set
	 */
	public void setViewProperties(Map<String, Object> viewProperties) {
		this.viewProperties = viewProperties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RestInstance [id=");
		builder.append(id);
		builder.append(", type=");
		builder.append(type);
		builder.append(", viewId=");
		builder.append(viewId);
		builder.append(", owningInstanceId=");
		builder.append(owningInstanceId);
		builder.append(", owningInstanceType=");
		builder.append(owningInstanceType);
		builder.append(", definitionId=");
		builder.append(definitionId);
		builder.append(", properties=");
		builder.append(properties);
		builder.append(", viewProperties=");
		builder.append(viewProperties);
		builder.append(", content=");
		builder.append(content == null ? "NULL" : "NOT_NULL");
		builder.append(", currentOperation=");
		builder.append(currentOperation);
		builder.append(", actions=");
		builder.append(actions);
		builder.append(", dafault_header=");
		builder.append(defaultHeader);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the overwrite
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	/**
	 * @param overwrite
	 *            the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

}
