package com.sirma.itt.emf.resources.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.resources.ResourceType;

/**
 * Base class for resource/user in the application.
 *
 * @author BBonev
 */
public class EmfResource implements Resource {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8998119993178746394L;
	/** The id. */
	@Tag(value = 1)
	protected Serializable id;
	/** The identifier. */
	@Tag(2)
	protected String identifier;
	/** The display name. */
	@Tag(3)
	protected String displayName;
	/** The type. */
	@Tag(4)
	protected ResourceType type;
	/** The properties. */
	@Tag(5)
	protected Map<String, Serializable> properties;
	/** A reference for the current instance. */
	protected transient InstanceReference reference;

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
		// if reference has been created update the id if possible
		if ((reference != null) && (id != null)) {
			reference.setIdentifier(id.toString());
		}
	}

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	@Override
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name.
	 *
	 * @param displayName
	 *            the new display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
	public String toString() {
		return "Resource [id=" + id + ", identifier=" + identifier + ", displayName=" + displayName
				+ "]";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		result = (prime * result) + ((identifier == null) ? 0 : identifier.hashCode());
		result = (prime * result) + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || !(obj instanceof Resource)) {
			return false;
		}
		Resource other = (Resource) obj;
		if (id == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!id.equals(other.getId())) {
			return false;
		}
		if (identifier == null) {
			if (other.getIdentifier() != null) {
				return false;
			}
		} else if (!identifier.equals(other.getIdentifier())) {
			return false;
		}
		if (type != other.getType()) {
			return false;
		}
		return true;
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
	public Map<String, Serializable> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<String, Serializable>();
		}
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
		return null;
	}

	@Override
	public void setRevision(Long revision) {
		// no revision
	}

	/**
	 * Getter method for type.
	 *
	 * @return the type
	 */
	@Override
	public ResourceType getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 *
	 * @param type
	 *            the type to set
	 */
	public void setType(ResourceType type) {
		this.type = type;
	}

	@Override
	public InstanceReference toReference() {
		if (reference == null) {
			reference = TypeConverterUtil.getConverter().convert(InstanceReference.class, this);
		}
		return reference;
	}
}