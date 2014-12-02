package com.sirma.itt.emf.link.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.entity.BaseStringIdEntity;
import com.sirma.itt.emf.entity.LinkSourceId;

/**
 * Entity class that represents a link instance.
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_links")
@org.hibernate.annotations.Table(appliesTo = "emf_links", indexes = {
		@Index(name = "idx_l_from", columnNames = { "fromId", "fromType" }),
		@Index(name = "idx_l_to", columnNames = { "toId", "toType" }),
		@Index(name = "idx_l_id_from", columnNames = { "link_id", "fromId", "fromType" }),
		@Index(name = "idx_l_id_to", columnNames = { "link_id", "toId", "toType" }) })
@AssociationOverrides(value = {
		@AssociationOverride(name = "from.sourceType", joinColumns = @JoinColumn(name = "fromType", nullable = false)),
		@AssociationOverride(name = "to.sourceType", joinColumns = @JoinColumn(name = "toType", nullable = false)) })
public class LinkEntity extends BaseStringIdEntity implements Identity, PathElement {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8461558920888392990L;

	/** The identifier. */
	@Column(name = "link_id")
	private String identifier;

	/** The primary. */
	@Column(name = "primarylink")
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	private Boolean primary;

	/** The from. */
	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "fromId", length = 50, nullable = false)) })
	private LinkSourceId from;

	/** The to. */
	@AttributeOverrides(value = { @AttributeOverride(name = "sourceId", column = @Column(name = "toId", length = 50, nullable = false)) })
	private LinkSourceId to;

	/**
	 * The ID of the forward link. This is set only in reverse link. A link that has empty reverse
	 * field is a primary link.
	 */
	@Column(name = "reverse", length = 50, nullable = true)
	private String reverse;

	/**
	 * Getter method for identifier.
	 *
	 * @return the identifier
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Setter method for identifier.
	 *
	 * @param identifier
	 *            the identifier to set
	 */
	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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
		this.primary = bidirectional;
	}

	/**
	 * Getter method for from.
	 *
	 * @return the from
	 */
	public LinkSourceId getFrom() {
		return from;
	}

	/**
	 * Setter method for from.
	 *
	 * @param from
	 *            the from to set
	 */
	public void setFrom(LinkSourceId from) {
		this.from = from;
	}

	/**
	 * Getter method for to.
	 *
	 * @return the to
	 */
	public LinkSourceId getTo() {
		return to;
	}

	/**
	 * Setter method for to.
	 *
	 * @param to
	 *            the to to set
	 */
	public void setTo(LinkSourceId to) {
		this.to = to;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LinkEntity [identifier=");
		builder.append(identifier);
		builder.append(", primary=");
		builder.append(primary);
		builder.append(", from=");
		builder.append(from);
		builder.append(", to=");
		builder.append(to);
		builder.append("]");
		return builder.toString();
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
	 * Getter method for reverse.
	 *
	 * @return the reverse
	 */
	public String getReverse() {
		return reverse;
	}

	/**
	 * Setter method for reverse.
	 *
	 * @param reverse the reverse to set
	 */
	public void setReverse(String reverse) {
		this.reverse = reverse;
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

}
