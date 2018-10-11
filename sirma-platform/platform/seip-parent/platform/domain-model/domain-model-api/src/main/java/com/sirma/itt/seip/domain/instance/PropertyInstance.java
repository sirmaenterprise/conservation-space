package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;

/**
 * Instance class for Semantic Property/Relation
 *
 * @author kirq4e
 */
public class PropertyInstance implements Instance, Serializable {

	private static final long serialVersionUID = -3922521586685542272L;

	private static final String INVERSE_RELATION = "inverseRelation";
	private static final String IS_SEARCHABLE = "isSearchable";
	private static final String AUDIT_EVENT = "auditEvent";

	private Serializable id;

	private String identifier;

	private Long revision;

	private Map<String, String> labels;

	private Map<String, Serializable> properties;

	private String domainClass;

	private String rangeClass;

	private boolean locked = false;

	/**
	 * Instantiates a new property instance.
	 */
	public PropertyInstance() {
		labels = new HashMap<>();
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public Long getRevision() {
		return revision;
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	@Override
	public void setId(Serializable id) {
		this.id = id;
	}

	/**
	 * Setter method for domainClass.
	 *
	 * @param domainClass
	 *            the domainClass to set
	 */
	public void setDomainClass(String domainClass) {
		this.domainClass = domainClass;
	}

	/**
	 * Getter method for domainClass.
	 *
	 * @return the domainClass
	 */
	public String getDomainClass() {
		return domainClass;
	}

	/**
	 * Setter method for rangeClass.
	 *
	 * @param rangeClass
	 *            the rangeClass to set
	 */
	public void setRangeClass(String rangeClass) {
		this.rangeClass = rangeClass;
	}

	/**
	 * Getter method for rangeClass.
	 *
	 * @return the rangeClass
	 */
	public String getRangeClass() {
		return rangeClass;
	}

	@Override
	public InstanceReference toReference() {
		// nothing to do here
		return null;
	}

	@Override
	public boolean isDeleted() {
		return false;
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
	 * Sets the label of the class for the given language
	 *
	 * @param language
	 *            language of the label
	 * @param label
	 *            property label
	 */
	public void setLabel(String language, String label) {
		labels.put(language, label);
	}

	/**
	 * Retrieve a function that gets a label based on the provided language.
	 *
	 * @return the function
	 */
	public Function<String, String> getLabelProvider() {
		Map<String, String> localLabels = labels;
		return key -> localLabels.getOrDefault(key, id.toString());
	}

	@Override
	public void preventModifications() {
		if (locked) {
			return;
		}
		locked = true;
		Instance.super.preventModifications();
		labels = Collections.unmodifiableMap(labels);
	}

	/**
	 * Gets inverse relation for this instance, if there is defined one.
	 * <p>
	 * <b>NOTE - This property is specific for instances representing relations.</b>
	 *
	 * @return the inverse relation if it is defined or null
	 */
	public String getInverseRelation(){
		return getAsString(INVERSE_RELATION);
	}

	/**
	 * Checks if the current property should generate audit events
	 *
	 * @return true, if is auditable
	 */
	public boolean isAuditable() {
		return isValueNotNull(AUDIT_EVENT);
	}

	/**
	 * Gets the audit event that corresponds to the current property. The method will return non <code>null</code> value
	 * if the method {@link #isAuditable()} returns <code>true</code>.
	 *
	 * @return the audit event or <code>null</code> if not supported
	 */
	public String getAuditEvent() {
		return getAsString(AUDIT_EVENT);
	}

	/**
	 * Checks if the current relation is searchable.
	 *
	 * @return true, if is searchable
	 */
	public boolean isSearchable() {
		return getBoolean(IS_SEARCHABLE);
	}

	@Override
	public int hashCode() {
		int result = 1;
		final int PRIME = 31;
		result = PRIME * result + (id == null ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PropertyInstance)) {
			return false;
		}
		return nullSafeEquals(id, ((PropertyInstance) obj).id);
	}

}
