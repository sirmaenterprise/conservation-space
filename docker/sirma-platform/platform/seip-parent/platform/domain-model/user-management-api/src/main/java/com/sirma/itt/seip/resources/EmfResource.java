package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_ACTIVE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_DELETED;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.util.EqualsHelper;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Base class for resource/user in the application.
 *
 * @author BBonev
 */
// do not remove the instance implementation because it breaks the type converter resolving
public class EmfResource implements Resource, Instance {

	private static final long serialVersionUID = 7560589120047817822L;

	@Tag(1)
	protected Serializable id;

	@Tag(2)
	protected String name;

	@Tag(3)
	protected String displayName;

	@Tag(4)
	protected ResourceType type;

	@Tag(5)
	private Map<String, Serializable> properties;

	/** A reference for the current instance. */
	protected transient InstanceReference reference;

	@Tag(6)
	private String source;

	/** The definition identifier. */
	@Tag(7)
	protected String identifier;

	/**
	 * @deprecated this is no longer valid and used. Cannot be removed for the serialization tag
	 */
	@Tag(8)
	@Deprecated
	protected boolean active = true;

	protected InstanceType instanceType;

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = id;
		// if reference has been created update the id if possible
		if (reference != null && id != null) {
			reference.setId(id.toString());
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
	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return "Resource [id=" + id + ", name=" + name + ", displayName=" + displayName + "]";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (id == null ? 0 : id.hashCode());
		result = PRIME * result + (name == null ? 0 : name.hashCode());
		result = PRIME * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if(this == obj){
			return true;
		}
		if (!(obj instanceof Resource)) {
			return false;
		}
		Resource otherResource = (Resource) obj;
		if (getType() != otherResource.getType() || !EqualsHelper.nullSafeEquals(id, otherResource.getId())) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(getName(), otherResource.getName());
	}

	@Override
	public Node getChild(String childName) {
		// no children to fetch
		return null;
	}

	@Override
	public PathElement getParentElement() {
		// resource has no parents
		return null;
	}

	@Override
	public boolean hasChildren() {
		// resources has no children
		return false;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<>();
		}
		return properties;
	}


	@Override
	public Long getRevision() {
		// does not support definition revision
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

	/**
	 * Sets the reference.
	 *
	 * @param reference
	 *            the new reference
	 */
	public void setReference(InstanceReference reference) {
		this.reference = reference;
	}

	@Override
	public boolean isDeleted() {
		return getBoolean(IS_DELETED, false);
	}

	@Override
	public String getSource() {
		return source;
	}

	/**
	 * Sets the source system id.
	 *
	 * @param source
	 *            the new source
	 */
	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isActive() {
		return getBoolean(IS_ACTIVE, Boolean.TRUE);
	}

	@Override
	public void setActive(boolean active) {
		add(IS_ACTIVE, active);
	}

	@Override
	public void markAsDeleted() {
		setActive(false);
		add(IS_DELETED, Boolean.TRUE);
	}

	@Override
	public String getLabel() {
		return getDisplayName();
	}

	@Override
	public InstanceType type() {
		return instanceType;
	}

	@Override
	public void setType(InstanceType type) {
		instanceType = type;
	}
}
