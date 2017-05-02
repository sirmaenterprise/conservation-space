package com.sirma.itt.seip.eai.model.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sirma.itt.seip.eai.model.SealedModel;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * The EntityRelation is representation of relation between object for particular system and tenant. It holds the name,
 * uri and the mapping to a specific external system
 * 
 * @author bbanchev
 */
public class EntityRelation extends SealedModel {

	private String title;
	private String domain;
	private String range;
	private String uri;
	private List<String> mappings = new LinkedList<>();

	/**
	 * Gets all added external mappings
	 *
	 * @return the mappings
	 */
	@JsonProperty("externals")
	public List<String> getMappings() {
		return mappings;
	}

	/**
	 * Setter method for external name mapping. All externalNames are lower cased
	 *
	 * @param externalNames
	 *            the externalNames of the relation as array. Should not be null
	 */
	public void addMappings(String... externalNames) {
		if (isSealed()) {
			return;
		}
		for (String externalId : externalNames) {
			mappings.add(externalId.toLowerCase());
		}
	}

	/**
	 * Checks for mapping of relation in {@link #getMappings()}. Check is case insensitive
	 *
	 * @param externalName
	 *            the external name to search
	 * @return true if it has
	 */
	public boolean hasMapping(String externalName) {
		if (externalName == null) {
			return false;
		}
		return mappings.contains(externalName.toLowerCase());
	}

	/**
	 * Getter method for title - human readable string.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter method for title.
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
	 * Gets the domain - relationFrom range.
	 *
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Sets the domain.
	 *
	 * @param domain
	 *            the new domain
	 */
	public void setDomain(String domain) {
		if (isSealed()) {
			return;
		}
		this.domain = domain;
	}

	/**
	 * Gets the range - relationTo range.
	 *
	 * @return the range
	 */
	public String getRange() {
		return range;
	}

	/**
	 * Sets the range of relations.
	 *
	 * @param range
	 *            the new range
	 */
	public void setRange(String range) {
		if (isSealed()) {
			return;
		}
		this.range = range;
	}

	/**
	 * Gets the short uri.
	 *
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Sets the uri.
	 *
	 * @param uri
	 *            the new uri
	 */
	public void setUri(String uri) {
		if (isSealed()) {
			return;
		}
		this.uri = uri;
	}

	@Override
	public void seal() {
		mappings = Collections.unmodifiableList(mappings);
		super.seal();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		result = prime * result + ((mappings == null) ? 0 : mappings.hashCode());
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
		if (!(obj instanceof EntityRelation)) {
			return false;
		}
		EntityRelation other = (EntityRelation) obj;

		return EqualsHelper.nullSafeEquals(mappings, other.mappings) && EqualsHelper.nullSafeEquals(uri, other.uri);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getSimpleName());
		builder.append("[uri=");
		builder.append(uri);
		builder.append(", mappings=");
		builder.append(mappings);
		builder.append(", title=");
		builder.append(title);
		builder.append(", domain=");
		builder.append(domain);
		builder.append(", range=");
		builder.append(range);
		builder.append("]");
		return builder.toString();
	}

}
