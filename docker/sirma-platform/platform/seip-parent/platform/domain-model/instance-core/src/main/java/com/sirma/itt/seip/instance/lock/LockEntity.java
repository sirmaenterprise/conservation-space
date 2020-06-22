/**
 *
 */
package com.sirma.itt.seip.instance.lock;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Lock entity that represents a single locked instance.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "seip_lockedentity", indexes = {
		@Index(name = "idx_lock_id", columnList = "lockedinstanceid", unique = true) })
@AssociationOverrides(value = {
		@AssociationOverride(name = "lockedInstance.referenceType", joinColumns = @JoinColumn(name = "lockedinstancetype", nullable = false)) })
@NamedQueries({ @NamedQuery(name = LockEntity.UNLOCK_INSTANCE_KEY, query = LockEntity.UNLOCK_INSTANCE),
		@NamedQuery(name = LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE_KEY, query = LockEntity.QUERY_LOCK_INFO_FOR_INSTANCE),
		@NamedQuery(name = LockEntity.QUERY_LOCK_INFO_FOR_INSTANCES_KEY, query = LockEntity.QUERY_LOCK_INFO_FOR_INSTANCES) })
public class LockEntity extends BaseEntity {
	private static final long serialVersionUID = -4423200522440646174L;

	static final String UNLOCK_INSTANCE_KEY = "UNLOCK_INSTANCE";
	static final String UNLOCK_INSTANCE = "delete from LockEntity where lockedInstance.id = :instanceId and lockedBy = :lockedBy";

	static final String QUERY_LOCK_INFO_FOR_INSTANCE_KEY = "QUERY_LOCK_INFO_FOR_INSTANCE";
	static final String QUERY_LOCK_INFO_FOR_INSTANCE = "from LockEntity where lockedInstance.id = :instanceId";

	static final String QUERY_LOCK_INFO_FOR_INSTANCES_KEY = "QUERY_LOCK_INFO_FOR_INSTANCES";
	static final String QUERY_LOCK_INFO_FOR_INSTANCES = "from LockEntity where lockedInstance.id in (:instanceId)";

	@AttributeOverrides(value = {
			@AttributeOverride(name = "id", column = @Column(name = "lockedinstanceid", length = 128, nullable = false)) })
	private LinkSourceId lockedInstance;

	@Column(name = "lockedby", length = 64, nullable = false)
	private String lockedBy;

	@Column(name = "lockedon")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lockedOn;

	@Column(name = "locktype", length = 128)
	private String lockType;

	/**
	 * Gets the locked instance.
	 *
	 * @return the lockedInstance
	 */
	public LinkSourceId getLockedInstance() {
		return lockedInstance;
	}

	/**
	 * Sets the locked instance.
	 *
	 * @param lockedInstance
	 *            the lockedInstance to set
	 */
	public void setLockedInstance(LinkSourceId lockedInstance) {
		this.lockedInstance = lockedInstance;
	}

	/**
	 * Gets the locked by.
	 *
	 * @return the lockedBy
	 */
	public String getLockedBy() {
		return lockedBy;
	}

	/**
	 * Sets the locked by.
	 *
	 * @param lockedBy
	 *            the lockedBy to set
	 */
	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}

	/**
	 * Gets the locked on.
	 *
	 * @return the lockedOn
	 */
	public Date getLockedOn() {
		return lockedOn;
	}

	/**
	 * Sets the locked on.
	 *
	 * @param lockedOn
	 *            the lockedOn to set
	 */
	public void setLockedOn(Date lockedOn) {
		this.lockedOn = lockedOn;
	}

	/**
	 * Gets the lock type.
	 *
	 * @return the lockType
	 */
	public String getLockType() {
		return lockType;
	}

	/**
	 * Sets the lock type.
	 *
	 * @param lockType
	 *            the lockType to set
	 */
	public void setLockType(String lockType) {
		this.lockType = lockType;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (lockedInstance == null ? 0 : lockedInstance.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		LockEntity other = (LockEntity) obj;
		return EqualsHelper.nullSafeEquals(lockedInstance, other.lockedInstance);
	}

}
