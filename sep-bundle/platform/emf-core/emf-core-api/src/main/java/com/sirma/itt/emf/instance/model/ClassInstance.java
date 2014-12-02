package com.sirma.itt.emf.instance.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;

/**
 * Semantic class instance
 *
 * @author kirq4e
 */
public class ClassInstance implements Instance, OwnedModel {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3071671160282225130L;

	private Serializable id;

	private String identifier;

	private Long revision;

	private final Map<String, String> labels;

	private Instance owningInstance;

	private Map<String, Serializable> properties;

	private Map<String, PropertyInstance> fields;

	private Map<String, PropertyInstance> relations;

	private Map<String, ClassInstance> subClasses;

	/**
	 * Default constructor to initialize the Maps
	 */
	public ClassInstance() {
		properties = new HashMap<String, Serializable>();
		fields = new HashMap<String, PropertyInstance>();
		relations = new HashMap<String, PropertyInstance>();
		subClasses = new LinkedHashMap<String, ClassInstance>();
		labels = new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		return revision;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node getChild(String name) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setId(Serializable id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	@Override
	public InstanceReference getOwningReference() {
		return null;
	}

	@Override
	public void setOwningReference(InstanceReference reference) {
	}

	@Override
	public Instance getOwningInstance() {
		return owningInstance;
	}

	@Override
	public void setOwningInstance(Instance instance) {
		owningInstance = instance;
	}

	/**
	 * Getter method for fields.
	 *
	 * @return the fields
	 */
	public Map<String, PropertyInstance> getFields() {
		return fields;
	}

	/**
	 * Setter method for fields.
	 *
	 * @param fields
	 *            the fields to set
	 */
	public void setFields(Map<String, PropertyInstance> fields) {
		this.fields = fields;
	}

	/**
	 * Getter method for relations.
	 *
	 * @return the relations
	 */
	public Map<String, PropertyInstance> getRelations() {
		return relations;
	}

	/**
	 * Setter method for relations.
	 *
	 * @param relations
	 *            the relations to set
	 */
	public void setRelations(Map<String, PropertyInstance> relations) {
		this.relations = relations;
	}

	/**
	 * Getter method for subClasses.
	 *
	 * @return the subClasses
	 */
	public Map<String, ClassInstance> getSubClasses() {
		return subClasses;
	}

	/**
	 * Setter method for subClasses.
	 *
	 * @param subClasses
	 *            the subClasses to set
	 */
	public void setSubClasses(Map<String, ClassInstance> subClasses) {
		this.subClasses = subClasses;
	}

	/**
	 * Gets the label of the class for the given language
	 *
	 * @param language
	 *            The language that we want
	 * @return The label in the given language or default label
	 */
	public String getLabel(String language) {
		String label = this.labels.get(language);
		if (label == null) {
			label = this.labels.get("en");
		}
		return label;
	}

	/**
	 * Sets the label of the class for the given language
	 *
	 * @param language
	 *            Language of the label
	 * @param label
	 *            Class label
	 */
	public void setLabel(String language, String label) {
		this.labels.put(language, label);
	}

	@Override
	public InstanceReference toReference() {
		// not supported
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClassInstance [id=");
		builder.append(id);
		builder.append(", subClasses=");
		builder.append(subClasses.keySet());
		builder.append("]");
		return builder.toString();
	}

}
