package com.sirma.itt.seip.eai.model.mapping.search;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityProperty;
import com.sirma.itt.seip.eai.model.mapping.EntityRelation;
import com.sirma.itt.seip.eai.model.mapping.EntityType;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The {@link EntitySearchType} represents a type entry used for search configuration required in search-api
 *
 * @author bbanchev
 */
public class EntitySearchType extends EntityType {
	private String type;

	/**
	 * Gets the type type - definition or class.
	 *
	 * @return the type
	 */
	@JsonProperty
	public String getType() {
		return type;
	}

	/**
	 * Sets the type type - class, definition, etc.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(String type) {
		if (isSealed()) {
			return;
		}
		this.type = type;
	}

	@Override
	@JsonIgnore
	public List<EntityRelation> getRelations() {
		return super.getRelations();
	}

	@Override
	@JsonIgnore
	public List<EntityProperty> getProperties() {
		return super.getProperties();
	}

	@Override
	@JsonIgnore
	public Set<String> getMappings() {
		return super.getMappings();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EntitySearchType)) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		EntitySearchType other = (EntitySearchType) obj;
		return EqualsHelper.nullSafeEquals(type, other.type, true);
	}

}