package com.sirma.itt.seip.instance.archive;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.util.VersionUtil;
import com.sirma.itt.seip.model.BaseStringIdEntity;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Entity to represent a archive instance.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity(name = "emf_archivedEntity")
@Table(name = "emf_archivedEntity", indexes = { @Index(name = "idx_arce_id", columnList = "id"),
		@Index(name = "idx_arce_tanid", columnList = "transactionId"),
		@Index(name = "idx_arce_delon", columnList = "deletedOn"),
		@Index(name = "idx_arce_createdon", columnList = "createdOn"),
		@Index(name = "idx_arce_targetid", columnList = "targetid")})
@NamedQueries({
		@NamedQuery(name = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_BY_REFERENCE_ID_KEY, query = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_BY_TARGET_ID),
		@NamedQuery(name = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID_KEY, query = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID),
		@NamedQuery(name = ArchivedEntity.QUERY_ARCHIVED_ENTITIES_BY_ID_KEY, query = ArchivedEntity.QUERTY_ARCHIVED_ENTITIES_BY_ID),
		@NamedQuery(name = ArchivedEntity.QUERY_LAST_VERSION_ID_BY_TARGET_ID_AND_CREATED_ON_DATE_KEY, query = ArchivedEntity.QUERY_LAST_VERSION_ID_BY_TARGET_ID_AND_CREATED_ON_DATE) })
public class ArchivedEntity extends BaseStringIdEntity implements PathElement {

	private static final long serialVersionUID = -7802319405120374076L;

	public static final String QUERY_ARCHIVED_ENTITIES_BY_REFERENCE_ID_KEY = "QUERY_ARCHIVED_ENTITIES_BY_TARGET_ID";
	static final String QUERY_ARCHIVED_ENTITIES_BY_TARGET_ID = "select c from com.sirma.itt.seip.instance.archive.ArchivedEntity c where c.targetId in (:id) order by c.majorVersion desc, c.minorVersion desc";

	public static final String QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID_KEY = "QUERY_ARCHIVED_ENTITIES_COUNT_BY_REFERENCE_ID";
	static final String QUERY_ARCHIVED_ENTITIES_COUNT_BY_TARGET_ID = "select count(*) from com.sirma.itt.seip.instance.archive.ArchivedEntity c where c.targetId in (:id)";

	public static final String QUERY_ARCHIVED_ENTITIES_BY_ID_KEY = "QUERTY_ARCHIVED_ENTITIES_BY_ID";
	static final String QUERTY_ARCHIVED_ENTITIES_BY_ID = "select c from com.sirma.itt.seip.instance.archive.ArchivedEntity c where c.id in (:ids)";

	/**
	 * This query is used to retrieve versions for instances, which are not deleted. The first part of the query will
	 * retrieve all of the versions which creation date is before or equals to the passed date. This is achieved with
	 * the first sub-query which result is a set of records of the last versions which match the condition for the
	 * creation date.
	 */
	public static final String QUERY_LAST_VERSION_ID_BY_TARGET_ID_AND_CREATED_ON_DATE_KEY = "QUERY_LAST_VERSION_ID_BY_TARGET_ID_AND_CREATED_ON_DATE";
	static final String QUERY_LAST_VERSION_ID_BY_TARGET_ID_AND_CREATED_ON_DATE = "select ae.targetId, ae.id from com.sirma.itt.seip.instance.archive.ArchivedEntity ae where ae.createdOn in "
			+ "(select max(sub_ae.createdOn) from com.sirma.itt.seip.instance.archive.ArchivedEntity sub_ae where sub_ae.targetId in (:ids) and sub_ae.createdOn <= :versionDate group by sub_ae.targetId) and ae.targetId in (:ids) and ae.transactionId is null";


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
	 * @param version that should be set in the entry
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
	 * @param definitionId the definitionId to set
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
	 * @param definitionRevision the definitionRevision to set
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
	 * @param transactionId the transactionId to set
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
	 * @param deletedOn the deletedOn to set
	 */
	public void setDeletedOn(Date deletedOn) {
		this.deletedOn = deletedOn == null ? null : new Date(deletedOn.getTime());
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
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * For version setting use {@link #setVersion(String)}.
	 * <p>
	 * <b>This method is just for compatibility with hibernate and should not be used.</b>
	 *
	 * @param minorVerion the minorVersion to set
	 */
	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	/**
	 * For version setting use {@link #setVersion(String)}.
	 * <p>
	 * <b>This method is just for compatibility with hibernate and should not be used.</b>
	 *
	 * @param majorVersion the majorVersion to set
	 */
	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
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
	 * @param targetId the targetId to set
	 */
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (transactionId == null ? 0 : transactionId.hashCode());
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
