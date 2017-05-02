package com.sirma.itt.seip.eai.model.mapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.sirma.itt.seip.eai.model.SealedModel;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The EntityType is representation of an external system object type valid only for particular system and tenant. It
 * holds the possible properties and relation and the mapping to a specific external system
 * 
 * @author bbanchev
 */
public class EntityType extends SealedModel {
	/** Definition Id. */
	private String identifier;
	/** System URI. */
	private String uri;
	/** Display name for the type. */
	private String title;

	private Set<String> mappings = new HashSet<>(2);
	private List<EntityProperty> properties = new LinkedList<>();
	private List<EntityRelation> relations = new LinkedList<>();

	/**
	 * Getter method for identifier.
	 *
	 * @return the identifier
	 */
	@JsonProperty
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Setter method for identifier - {@link #identifier}.
	 *
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		if (isSealed()) {
			return;
		}
		this.identifier = identifier;
	}

	/**
	 * Getter method for uri.
	 *
	 * @return the uri
	 */
	@JsonProperty
	public String getUri() {
		return uri;
	}

	/**
	 * Setter method for uri {@link #uri}.
	 *
	 * @param uri
	 *            the uri to set
	 */
	public void setUri(String uri) {
		if (isSealed()) {
			return;
		}
		this.uri = uri;
	}

	/**
	 * Getter method for display name.
	 *
	 * @return the title
	 */
	@JsonProperty
	public String getTitle() {
		return title;
	}

	/**
	 * Setter method for title- {@link #title}.
	 *
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		if (isSealed()) {
			return;
		}
		this.title = title;
	}

	/**
	 * Check if this type is mapped to external value name.
	 * 
	 * @param value
	 *            is the mapping to check (case is ignored)
	 * @return true if mapping is contained
	 */
	public boolean hasMapping(String value) {
		return mappings
				.stream()
					.filter(mapping -> EqualsHelper.nullSafeEquals(mapping, value, true))
					.findFirst()
					.isPresent();
	}

	/**
	 * Sets the mappings as single id.
	 *
	 * @param mapping
	 *            the new mappings to set
	 */
	@JsonSetter("externals")
	public void setMapping(String mapping) {
		if (mapping == null) {
			return;
		}
		if (!isSealed()) {
			mappings.add(mapping);
		}
	}

	/**
	 * Gets the mappings for this type.
	 *
	 * @return the mappings
	 */
	public Set<String> getMappings() {
		return mappings;
	}

	/**
	 * Sets the mappings for this type.
	 *
	 * @param mappings
	 *            the new mappings
	 */
	public void setMappings(Set<String> mappings) {
		if (isSealed()) {
			return;
		}
		this.mappings = mappings;
	}

	/**
	 * Adds the properties to the existing collection.
	 *
	 * @param newProperties
	 *            the properties to add
	 */
	public void addProperties(List<EntityProperty> newProperties) {
		if (isSealed()) {
			return;
		}
		this.properties.addAll(newProperties);
	}

	/**
	 * Adds the relation to the existing collection.
	 *
	 * @param newRelation
	 *            the relation
	 */
	public void addRelation(EntityRelation newRelation) {
		if (isSealed()) {
			return;
		}
		this.relations.add(newRelation);
	}

	/**
	 * Adds the relations to the existing collection.
	 *
	 * @param newRelations
	 *            the relations to add
	 */
	public void addRelations(List<EntityRelation> newRelations) {
		if (isSealed()) {
			return;
		}
		this.relations.addAll(newRelations);
	}

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	@JsonProperty
	public List<EntityProperty> getProperties() {
		return properties;
	}

	/**
	 * Gets the relations.
	 *
	 * @return the relations
	 */
	@JsonProperty
	public List<EntityRelation> getRelations() {
		return relations;
	}

	@Override
	@JsonIgnore
	public boolean isSealed() {
		return super.isSealed();
	}

	@Override
	public void seal() {
		properties = Collections.unmodifiableList(properties);
		for (EntityProperty entityProperty : properties) {
			entityProperty.seal();
		}
		relations = Collections.unmodifiableList(relations);
		for (EntityRelation entityRelation : relations) {
			entityRelation.seal();
		}
		mappings = Collections.unmodifiableSet(mappings);
		super.seal();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getIdentifier() == null) ? 0 : getIdentifier().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof EntityType)) {
			return false;
		}
		EntityType other = (EntityType) obj;
		return EqualsHelper.nullSafeEquals(getIdentifier(), other.getIdentifier(), true);
	}

}
