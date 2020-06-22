package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Semantic class instance
 *
 * @author kirq4e
 */
public class ClassInstance extends EmfInstance implements InstanceType {

	private static final long serialVersionUID = 3437555723181699518L;

	private Map<String, String> labels;

	private Map<String, PropertyInstance> fields;

	private Map<String, PropertyInstance> relations;

	private Map<String, ClassInstance> subClasses;

	private List<ClassInstance> superClasses;

	private String library;

	private boolean locked = false;

	/**
	 * Default constructor to initialize the Maps
	 */
	public ClassInstance() {
		setProperties(new HashMap<>());
		fields = new HashMap<>();
		relations = new HashMap<>();
		subClasses = new LinkedHashMap<>();
		labels = new HashMap<>();
		superClasses = new LinkedList<>();
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

	@Override
	public boolean hasSubType(InstanceType type) {
		if (type == null) {
			return false;
		}
		// this implementation is more optimal than the default due to the fact that it does not relay on sets that are
		// created dynamically: see the implementation of the getSubTypes() in here
		if (getSubClasses().containsKey(type.getId())) {
			return true;
		}
		for (ClassInstance subClass : getSubClasses().values()) {
			if (subClass.hasSubType(type)) {
				return true;
			}
		}
		return false;
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
	 * Gets the default label of the class. The default label is in English
	 *
	 * @return The default label
	 */
	@Override
	public String getLabel() {
		return labels.get("en");
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
		labels.put(language, label);
	}

	/**
	 * Gets the label of the class for the given language
	 *
	 * @param language
	 *            The language that we want
	 * @return The label in the given language or default label
	 */
	public String getLabel(String language) {
		String label = labels.get(language);
		if (label == null) {
			label = getLabel();
		}
		return label;
	}

	public Map<String, String> getLabels() {
		return Collections.unmodifiableMap(labels);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClassInstance [id=");
		builder.append(getId());
		builder.append(", subClasses=");
		builder.append(subClasses.keySet());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns list of super classes of the current class. One class can have multiple super classes
	 *
	 * @return list of super classes of the current class.
	 */
	public List<ClassInstance> getSuperClasses() {
		return superClasses;
	}

	/**
	 * Sets list of super classes of the current class
	 *
	 * @param superClasses
	 *            list of super classes of the current class
	 */
	public void setSuperClasses(List<ClassInstance> superClasses) {
		this.superClasses = superClasses;
	}

	@Override
	public int hashCode() {
		return 31 + (getId() == null ? 0 : getId().hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof InstanceType)) {
			return false;
		}
		InstanceType other = (InstanceType) obj;
		return nullSafeEquals(getId(), other.getId());
	}

	/**
	 * @return the library
	 */
	public String getLibrary() {
		return library;
	}

	/**
	 * @param library the library to set
	 */
	public void setLibrary(String library) {
		this.library = library;
	}

	/**
	 * Sets the category.
	 *
	 * @param category
	 *            the new category
	 */
	public void setCategory(String category) {
		add("classCategory", category);
	}

	@Override
	public InstanceType type() {
		return this;
	}

	@Override
	public void setType(InstanceType type) {
		// do not allow type modifications
	}

	@Override
	public void preventModifications() {
		if (locked) {
			return;
		}
		// do not lock more than once
		locked = true;

		super.preventModifications();
		labels = Collections.unmodifiableMap(labels);

		fields = Collections.unmodifiableMap(fields);
		fields.forEach((key, value) -> value.preventModifications());

		relations = Collections.unmodifiableMap(relations);
		relations.forEach((key, value) -> value.preventModifications());

		subClasses = Collections.unmodifiableMap(subClasses);
		subClasses.forEach((key, value) -> value.preventModifications());

		superClasses = Collections.unmodifiableList(superClasses);
		superClasses.forEach(ClassInstance::preventModifications);
	}

	@Override
	public String getCategory() {
		return getString("classCategory");
	}

	@Override
	public Set<InstanceType> getSuperTypes() {
		return new HashSet<>(getSuperClasses());
	}

	@Override
	public Set<InstanceType> getSubTypes() {
		return new HashSet<>(getSubClasses().values());
	}

	@Override
	public boolean hasTrait(String trait) {
		if (StringUtils.isBlank(trait)) {
			return false;
		}
		return getBoolean(trait);
	}

	@Override
	public String getProperty(String propertyName) {
		return getString(propertyName);
	}

	/**
	 * Gets shallow mutable copy of the current instance. The super classes, sub classes and relation instances will not
	 * be mutable.
	 *
	 * @return the mutable copy
	 */
	public ClassInstance getMutableCopy() {
		ClassInstance copy = new ClassInstance();
		copy.setId(getId());
		copy.addAllProperties(getProperties());
		copy.setIdentifier(getIdentifier());
		copy.setRevision(getRevision());
		copy.labels = new HashMap<>(labels);
		copy.library = library;
		copy.getRelations().putAll(getRelations());
		copy.getSubClasses().putAll(getSubClasses());
		copy.getSuperClasses().addAll(getSuperClasses());
		copy.getFields().putAll(getFields());
		return copy;
	}

	@Override
	public boolean isVersionable() {
		return getBoolean("versionable", true);
	}
}
