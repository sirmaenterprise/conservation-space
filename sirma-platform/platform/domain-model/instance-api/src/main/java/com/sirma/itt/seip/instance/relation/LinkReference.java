package com.sirma.itt.seip.instance.relation;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Link;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Link representation between instances but the instances are not yet initialized. The method provides only means check
 * the main link data without the payload of loading the actual instance and properties.
 *
 * @author BBonev
 */
public class LinkReference implements Instance, Link<InstanceReference, InstanceReference> {

	private static final long serialVersionUID = 1L;

	/** The id. */
	@Tag(1)
	private Serializable id;
	@Tag(2)
	/** The identifier. */
	private String identifier;
	@Tag(3)
	/** The from. */
	private InstanceReference from;
	@Tag(4)
	/** The to. */
	private InstanceReference to;
	@Tag(5)
	/** If this is the primary/initial direction link. */
	private Boolean primary;
	@Tag(6)
	/** The reverse Id of the reverse link if any. */
	private Serializable reverse;
	@Tag(7)
	private Map<String, Serializable> properties;

	private InstanceType instanceType;

	@Override
	public Map<String, Serializable> getProperties() {
		if (properties == null) {
			// get properties before initialization so we set them
			properties = new LinkedHashMap<>();
		}
		return properties;
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	@Override
	public Long getRevision() {
		return 0L;
	}

	@Override
	public PathElement getParentElement() {
		// no parents
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = id;
	}

	/**
	 * Gets the link identifier. That is the link id between the two instances.
	 *
	 * @return the identifier
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Getter method for from.
	 *
	 * @return the from
	 */
	@Override
	public InstanceReference getFrom() {
		return from;
	}

	/**
	 * Setter method for from.
	 *
	 * @param from
	 *            the from to set
	 */
	public void setFrom(InstanceReference from) {
		this.from = from;
	}

	/**
	 * Getter method for to.
	 *
	 * @return the to
	 */
	@Override
	public InstanceReference getTo() {
		return to;
	}

	/**
	 * Setter method for to.
	 *
	 * @param to
	 *            the to to set
	 */
	public void setTo(InstanceReference to) {
		this.to = to;
	}

	/**
	 * Getter method for primary.
	 *
	 * @return the primary
	 */
	public Boolean getPrimary() {
		return primary;
	}

	/**
	 * Setter method for primary.
	 *
	 * @param bidirectional
	 *            the new primary
	 */
	public void setPrimary(Boolean bidirectional) {
		primary = bidirectional;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (identifier == null ? 0 : identifier.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		LinkReference other = (LinkReference) obj;
		return EqualsHelper.nullSafeEquals(id, other.id) && EqualsHelper.nullSafeEquals(identifier, other.identifier);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LinkReference [id=");
		builder.append(id);
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", reverse=");
		builder.append(reverse);
		builder.append(", primary=");
		builder.append(primary);
		builder.append(", from=");
		builder.append(from);
		builder.append(", to=");
		builder.append(to);
		builder.append(", properties=");
		builder.append(properties);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean hasChildren() {
		for (Serializable serializable : getProperties().values()) {
			if (serializable instanceof Node) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Node getChild(String name) {
		if (hasChildren()) {
			Serializable serializable = getProperties().get(name);
			if (serializable instanceof Node) {
				return (Node) serializable;
			}
		}
		return null;
	}

	@Override
	public void setRevision(Long revision) {
		// nothing to do here
	}

	/**
	 * Getter method for reverse.
	 *
	 * @return the reverse
	 */
	public Serializable getReverse() {
		return reverse;
	}

	/**
	 * Setter method for reverse.
	 *
	 * @param reverse
	 *            the reverse to set
	 */
	public void setReverse(Serializable reverse) {
		this.reverse = reverse;
	}

	@Override
	public InstanceReference toReference() {
		// not supported
		return null;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	/**
	 * Converts the current link reference to link instance. The instances returned from the {@link #getFrom()} and
	 * {@link #getTo()} methods will not be loaded but just converted.
	 *
	 * @return simple copy of the reference to instance
	 */
	public LinkInstance toLinkInstance() {
		LinkInstance instance = new LinkInstance();

		instance.setFrom(InstanceReference.instantiate(getFrom()));
		instance.setTo(InstanceReference.instantiate(getTo()));
		instance.setId(getId());
		instance.setIdentifier(getIdentifier());
		instance.setProperties(getProperties());
		instance.setPrimary(getPrimary());
		instance.setReverse(getReverse());
		instance.setType(type());

		return instance;
	}

	@Override
	public void setType(InstanceType type) {
		instanceType = type;
	}

	@Override
	public InstanceType type() {
		return instanceType;
	}
}
