package com.sirma.itt.seip.instance.archive;

import java.util.Date;

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

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.util.VersionUtil;
import com.sirma.itt.seip.model.BaseStringIdEntity;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Entity to represent a archive instance.
 *
 * @author BBonev
 */
@Entity(name = "emf_archivedEntity")
@Table(name = "emf_archivedEntity", indexes = { @Index(name = "idx_arce_id", columnList = "id"),
		@Index(name = "idx_arce_owninstid", columnList = "owninginstanceid"),
		@Index(name = "idx_arce_tanid", columnList = "transactionId"),
		@Index(name = "idx_arce_delon", columnList = "deletedOn") })
@AssociationOverrides(value = {
		@AssociationOverride(name = "owningInstance.sourceType", joinColumns = @JoinColumn(name = "owninginstancetype", nullable = true)) })
@NamedQueries({
		@NamedQuery(name = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_BY_REFERENCE_ID_KEY, query = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_BY_TARGET_ID),
		@NamedQuery(name = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID_KEY, query = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID),
		@NamedQuery(name = ArchivedEntity.QUERY_DELETED_INSTANCE_BY_OWN_REF_KEY, query = ArchivedEntity.QUERY_DELETED_INSTANCE_BY_OWN_REF),
		@NamedQuery(name = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_BY_ID_KEY, query = ArchivedEntity.QUERTY_ARCHIVED_ENTITIES_BY_ID),
		@NamedQuery(name = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_ID_BY_TARGET_ID_AND_CREATED_ON_DATE_KEY, query = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_ID_BY_TARGET_ID_AND_CREATED_ON_DATE) })
public class ArchivedEntity extends BaseStringIdEntity implements PathElement {

	private static final long serialVersionUID = -7802319405120374076L;

	public static final String QUERY_ARCHIVED_ENTITIES_BY_REFERENCE_ID_KEY = "QUERY_ARCHIVED_ENTITIES_BY_TARGET_ID";
	static final String QUERY_ARCHIVED_ENTITIES_BY_TARGET_ID = "select c from com.sirma.itt.seip.instance.archive.ArchivedEntity c where c.targetId in (:id) order by c.majorVersion desc, c.minorVersion desc";

	public static final String QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID_KEY = "QUERY_ARCHIVED_ENTITIES_COUNT_BY_REFERENCE_ID";
	static final String QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID = "select count(*) from com.sirma.itt.seip.instance.archive.ArchivedEntity c where c.targetId in (:id) and c.createdOn is not null";

	public static final String QUERY_DELETED_INSTANCE_BY_OWN_REF_KEY = "QUERY_DELETED_INSTANCE_BY_OWN_REF";
	static final String QUERY_DELETED_INSTANCE_BY_OWN_REF = "select t.id from com.sirma.itt.seip.instance.archive.ArchivedEntity t where t.owningInstance.sourceId=:sourceId AND t.owningInstance.sourceType.id = :sourceType AND t.transactionId = :transactionId";

	public static final String QUERY_ARCHIVED_ENTITIES_BY_ID_KEY = "QUERTY_ARCHIVED_ENTITIES_BY_ID";
	static final String QUERTY_ARCHIVED_ENTITIES_BY_ID = "select c from com.sirma.itt.seip.instance.archive.ArchivedEntity c where c.id in (:ids) and c.createdOn is not null";

	public static final String QUERY_ARCHIVED_ENTITIES_ID_BY_TARGET_ID_AND_CREATED_ON_DATE_KEY = "QUERY_ARCHIVED_ENTITIES_ID_BY_TARGET_ID_AND_CREATED_ON_DATE";
	static final String QUERY_ARCHIVED_ENTITIES_ID_BY_TARGET_ID_AND_CREATED_ON_DATE = "select distinct ae from com.sirma.itt.seip.instance.archive.ArchivedEntity ae where ae.createdOn in "
			+ "(select max(sub_ae.createdOn) from com.sirma.itt.seip.instance.archive.ArchivedEntity sub_ae where sub_ae.targetId in (:ids) and sub_ae.createdOn <= :versionDate group by sub_ae.targetId) and ae.targetId in (:ids)";

	@AttributeOverrides(value = {
			@AttributeOverride(name = "sourceId", column = @Column(name = "owninginstanceid", length = 50, nullable = true)) })
	private LinkSourceId owningInstance;
	@Column(name = "definitionId", length = 100, nullable = false)
	private String definitionId;
	@Column(name = "definitionRevision", nullable = false)
	private Long definitionRevision;
	@Column(name = "transactionId", length = 100, nullable = false)
	private String transactionId;
	@Column(name = "deletedOn", nullable = false)
	private Date deletedOn;
	@Column(name = "majorVersion", nullable = false)
	private int majorVersion;
	@Column(name = "minorVersion", nullable = false)
	private int minorVersion;
	/**
	 * Used as a marker when specific instance versions are created. It will be the same for instances that are saved in
	 * one transaction.
	 */
	@Column(name = "createdOn")
	private Date createdOn;
	/** The id of the original instance which is archived. */
	@Column(name = "targetid", nullable = false)
	private String targetId;

	/**
	 * Setter for the instance version.
	 *
	 * @param version
	 *            that should be set in the entry
	 */
	public void setVersion(String version) {
		Pair<Integer, Integer> versionPair = VersionUtil.split(version);
		majorVersion = versionPair.getFirst();
		minorVersion = versionPair.getSecond();
	}

	/**
	 * Getter for instance version.
	 *
	 * @return the version for the instance
	 */
	public String getVersion() {
		return VersionUtil.combine(majorVersion, minorVersion);
	}

	/**
	 * Getter method for owningInstance.
	 *
	 * @return the owningInstance
	 */
	public LinkSourceId getOwningInstance() {
		return owningInstance;
	}

	/**
	 * Setter method for owningInstance.
	 *
	 * @param owningInstance
	 *            the owningInstance to set
	 */
	public void setOwningInstance(LinkSourceId owningInstance) {
		this.owningInstance = owningInstance;
	}

	/**
	 * Getter method for definitionId.
	 *
	 * @return the definitionId
	 */
	public String getDefinitionId() {
		return definitionId;
	}

	/**
	 * Setter method for definitionId.
	 *
	 * @param definitionId
	 *            the definitionId to set
	 */
	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}

	/**
	 * Getter method for definitionRevision.
	 *
	 * @return the definitionRevision
	 */
	public Long getDefinitionRevision() {
		return definitionRevision;
	}

	/**
	 * Setter method for definitionRevision.
	 *
	 * @param definitionRevision
	 *            the definitionRevision to set
	 */
	public void setDefinitionRevision(Long definitionRevision) {
		this.definitionRevision = definitionRevision;
	}

	/**
	 * Getter method for transactionId.
	 *
	 * @return the transactionId
	 */
	public String getTransactionId() {
		return transactionId;
	}

	/**
	 * Setter method for transactionId.
	 *
	 * @param transactionId
	 *            the transactionId to set
	 */
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	/**
	 * Getter method for deletedOn.
	 *
	 * @return the deletedOn
	 */
	public Date getDeletedOn() {
		return deletedOn == null ? null : new Date(deletedOn.getTime());
	}

	/**
	 * Setter method for deletedOn.
	 *
	 * @param deletedOn
	 *            the deletedOn to set
	 */
	public void setDeletedOn(Date deletedOn) {
		this.deletedOn = deletedOn == null ? null : new Date(deletedOn.getTime());
	}

	/**
	 * For version retrieving use {@link #getVersion()}.
	 * <p>
	 * <b>This method is just for compatibility with hibernate and should not be used.</b>
	 *
	 * @return the majorVersion
	 */
	public int getMajorVersion() {
		return majorVersion;
	}

	/**
	 * For version setting use {@link #setVersion(String)}.
	 * <p>
	 * <b>This method is just for compatibility with hibernate and should not be used.</b>
	 *
	 * @param majorVersion
	 *            the majorVersion to set
	 */
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	/**
	 * For version retrieving use {@link #getVersion()}.
	 * <p>
	 * <b>This method is just for compatibility with hibernate and should not be used.</b>
	 *
	 * @return the minorVersion
	 */
	public int getMinorVersion() {
		return minorVersion;
	}

	/**
	 * For version setting use {@link #setVersion(String)}.
	 * <p>
	 * <b>This method is just for compatibility with hibernate and should not be used.</b>
	 *
	 * @param minorVerion
	 *            the minorVersion to set
	 */
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	/**
	 * Getter for the date when specific instance version is created. It is used later for the version loading. It is
	 * different then {@link DefaultProperties#CREATED_ON}.
	 *
	 * @return the createdOn
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	/**
	 * Setter for the date when specific instance version is created. It is used later for the version loading. It is
	 * different then {@link DefaultProperties#CREATED_ON}.
	 *
	 * @param createdOn
	 *            the createdOn to set
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * Getter for the id of the original instance that was archived.
	 *
	 * @return the targetId
	 */
	public String getTargetId() {
		return targetId;
	}

	/**
	 * Setter for the id of the original instance that is archived.
	 *
	 * @param targetId
	 *            the targetId to set
	 */
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (transactionId == null ? 0 : transactionId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ArchivedEntity)) {
			return false;
		}
		ArchivedEntity other = (ArchivedEntity) obj;
		return EqualsHelper.nullSafeEquals(transactionId, other.transactionId);
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

	@Override
	public String getIdentifier() {
		return getDefinitionId();
	}

	@Override
	public void setIdentifier(String identifier) {
		setDefinitionId(identifier);
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

}
