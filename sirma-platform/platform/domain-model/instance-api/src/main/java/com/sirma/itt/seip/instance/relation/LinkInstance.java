package com.sirma.itt.seip.instance.relation;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.Link;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Instance class that represents a link. The link has a beginning and an end and primary direction. It can have a
 * reverse with the same or different link identifier which is non primary link. The primary direction is the direction
 * when the link was created and which objects was the first and which one was the second.
 *
 * @author BBonev
 */
public class LinkInstance implements Instance, Identity, Link<Instance, Instance> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/** The id. */
	private Serializable id;

	/** The identifier. */
	private String identifier;

	/** The from. */
	private Instance from;

	/** The to. */
	private Instance to;

	/** If this is the primary/initial direction link. */
	private Boolean primary;

	/** The properties. */
	private Map<String, Serializable> properties;

	/** The reverse Id of the reverse link if any. */
	private Serializable reverse;

	private Boolean deleted;

	private InstanceType instanceType;

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
		return 0L;
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
		return getIdentifier();
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
	 * Getter method for from.
	 *
	 * @return the from
	 */
	@Override
	public Instance getFrom() {
		return from;
	}

	/**
	 * Setter method for from.
	 *
	 * @param from
	 *            the from to set
	 */
	public void setFrom(Instance from) {
		this.from = from;
	}

	/**
	 * Getter method for to.
	 *
	 * @return the to
	 */
	@Override
	public Instance getTo() {
		return to;
	}

	/**
	 * Setter method for to.
	 *
	 * @param to
	 *            the to to set
	 */
	public void setTo(Instance to) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (identifier == null ? 0 : identifier.hashCode());
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
		if (!(obj instanceof LinkInstance)) {
			return false;
		}
		LinkInstance other = (LinkInstance) obj;
		return EqualsHelper.nullSafeEquals(id, other.id) && EqualsHelper.nullSafeEquals(identifier, other.identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LinkInstance [id=");
		builder.append(id);
		builder.append(", reverse=");
		builder.append(reverse);
		builder.append(", identifier=");
		builder.append(identifier);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		if (getProperties() == null) {
			return false;
		}
		for (Serializable serializable : getProperties().values()) {
			if (serializable instanceof Node) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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
		if (deleted == null) {
			return false;
		}
		return deleted.booleanValue();
	}

	/**
	 * Sets the deleted.
	 *
	 * @param deleted
	 *            the new deleted
	 */
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
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
