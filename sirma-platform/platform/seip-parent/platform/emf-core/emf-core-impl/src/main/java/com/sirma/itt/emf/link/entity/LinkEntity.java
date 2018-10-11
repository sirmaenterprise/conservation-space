package com.sirma.itt.emf.link.entity;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.model.BaseStringIdEntity;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Entity class that represents a link instance.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_links", indexes = {
		@Index(name = "idx_l_from", columnList =  "fromId,fromType" ),
		@Index(name = "idx_l_to", columnList = "toId,toType"),
		@Index(name = "idx_l_id_from", columnList = "link_id,fromId,fromType"),
		@Index(name = "idx_l_id_to", columnList = "link_id,toId,toType") })
@AssociationOverrides(value = {
		@AssociationOverride(name = "from.referenceType", joinColumns = @JoinColumn(name = "fromType", nullable = false)),
		@AssociationOverride(name = "to.referenceType", joinColumns = @JoinColumn(name = "toType", nullable = false)) })
@NamedQueries(
		{ @NamedQuery(name = LinkEntity.QUERY_LINK_BY_SRC_AND_IDS_KEY, query = LinkEntity.QUERY_LINK_BY_SRC_AND_IDS),
				@NamedQuery(name = LinkEntity.QUERY_LINK_BY_SRC_KEY, query = LinkEntity.QUERY_LINK_BY_SRC),
				@NamedQuery(name = LinkEntity.QUERY_LINK_BY_TARGET_AND_IDS_KEY, query = LinkEntity.QUERY_LINK_BY_TARGET_AND_IDS),
				@NamedQuery(name = LinkEntity.QUERY_LINK_BY_TARGET_KEY, query = LinkEntity.QUERY_LINK_BY_TARGET),
				@NamedQuery(name = LinkEntity.QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY, query = LinkEntity.QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS),
				@NamedQuery(name = LinkEntity.QUERY_LINK_BY_TARGET_AND_SOURCE_KEY, query = LinkEntity.QUERY_LINK_BY_TARGET_AND_SOURCE) })
public class LinkEntity extends BaseStringIdEntity implements Identity, PathElement {

	public static final String QUERY_LINK_BY_SRC_AND_IDS_KEY = "QUERY_LINK_BY_SRC_AND_IDS";
	static final String QUERY_LINK_BY_SRC_AND_IDS = "select l from LinkEntity l inner join fetch l.to.referenceType inner join fetch l.from.referenceType where l.identifier in (:identifier) AND l.from.id = :fromId AND l.from.referenceType.id=:fromType order by l.id";

	public static final String QUERY_LINK_BY_TARGET_AND_IDS_KEY = "QUERY_LINK_BY_TARGET_AND_IDS";
	static final String QUERY_LINK_BY_TARGET_AND_IDS = "select l from LinkEntity l inner join fetch l.to.referenceType inner join fetch l.from.referenceType where l.identifier in (:identifier) AND l.to.id = :toId AND l.to.referenceType.id=:toType order by l.id";

	public static final String QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS_KEY = "QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS";
	static final String QUERY_LINK_BY_TARGET_AND_SOURCE_AND_IDS = "select l from LinkEntity l inner join fetch l.to.referenceType inner join fetch l.from.referenceType where l.identifier in (:identifier) AND l.to.id = :toId AND l.to.referenceType.id=:toType and l.from.id = :fromId AND l.from.referenceType.id=:fromType order by l.id";

	public static final String QUERY_LINK_BY_SRC_KEY = "QUERY_LINK_BY_SRC";
	static final String QUERY_LINK_BY_SRC = "select l from LinkEntity l inner join fetch l.to.referenceType inner join fetch l.from.referenceType where l.from.id = :fromId AND l.from.referenceType.id=:fromType order by l.id";

	public static final String QUERY_LINK_BY_TARGET_KEY = "QUERY_LINK_BY_TARGET";
	static final String QUERY_LINK_BY_TARGET = "select l from LinkEntity l inner join fetch l.to.referenceType inner join fetch l.from.referenceType where l.to.id = :toId AND l.to.referenceType.id=:toType order by l.id";

	public static final String QUERY_LINK_BY_TARGET_AND_SOURCE_KEY = "QUERY_LINK_BY_TARGET_AND_SOURCE";
	static final String QUERY_LINK_BY_TARGET_AND_SOURCE = "select l from LinkEntity l inner join fetch l.to.referenceType inner join fetch l.from.referenceType where l.to.id = :toId AND l.to.referenceType.id=:toType and l.from.id = :fromId AND l.from.referenceType.id=:fromType order by l.id";

	 /**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8461558920888392990L;

	/** The identifier. */
	@Column(name = "link_id")
	private String identifier;

	/** The primary. */
	@Column(name = "primarylink")
	@Type(type = "com.sirma.itt.seip.db.customtype.BooleanCustomType")
	private Boolean primary;

	/** The from. */
	@AttributeOverrides(value = {
			@AttributeOverride(name = "id", column = @Column(name = "fromId", length = 50, nullable = false)) })
	private LinkSourceId from;

	/** The to. */
	@AttributeOverrides(value = {
			@AttributeOverride(name = "id", column = @Column(name = "toId", length = 50, nullable = false)) })
	private LinkSourceId to;

	/**
	 * The ID of the forward link. This is set only in reverse link. A link that has empty reverse field is a primary
	 * link.
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
		primary = bidirectional;
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

	@Override
	public PathElement getParentElement() {
		return null;
	}

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
	 * @param reverse
	 *            the reverse to set
	 */
	public void setReverse(String reverse) {
		this.reverse = reverse;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

}
