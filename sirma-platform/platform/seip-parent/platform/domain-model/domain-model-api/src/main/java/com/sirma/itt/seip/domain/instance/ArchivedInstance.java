package com.sirma.itt.seip.domain.instance;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.util.VersionUtil;

/**
 * Instance class that represents a deleted instance data
 *
 * @author BBonev
 */
public class ArchivedInstance implements Instance {

	private static final long serialVersionUID = 4063676736242788269L;
	private Serializable id;
	private String identifier;
	private Long revision;
	private Map<String, Serializable> properties;
	private String transactionId;
	private Date deletedOn;
	private int majorVersion;
	private int minorVersion;
	private Date createdOn;

	private Serializable targetId;
	/** Reference for the current instance. */
	private InstanceReference reference;
	private InstanceType instanceType;

	/**
	 * Setter for the instance version.
	 *
	 * @param version
	 *            that should be set in the entry
	 */
	public void setVersion(String version) {
		IntegerPair splitVersion = VersionUtil.split(version);
		majorVersion = splitVersion.getFirst();
		minorVersion = splitVersion.getSecond();
	}

	/**
	 * Getter for instance version.
	 *
	 * @return the version for the instance
	 */
	public String getVersion() {
		return VersionUtil.combine(majorVersion, minorVersion);
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	@Override
	public String getPath() {
		return getIdentifier();
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
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = id;
	}

	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	@Override
	public InstanceReference toReference() {
		if (reference == null) {
			reference = TypeConverterUtil.getConverter().convert(ArchivedInstanceReference.class, this);
		}
		return reference;
	}

	@Override
	public boolean isDeleted() {
		return true;
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
		if (deletedOn != null) {
			this.deletedOn = new Date(deletedOn.getTime());
		} else {
			this.deletedOn = null;
		}
	}

	public int getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(int majorVersion) {
		this.majorVersion = majorVersion;
	}

	public int getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(int minorVersion) {
		this.minorVersion = minorVersion;
	}

	/**
	 * Getter for the date, when specific instance version is saved. It is used as marker, when the versions are loaded
	 * and their content is displayed.
	 *
	 * @return the createdOn
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	/**
	 * Setter for date, when the specific instance version is saved. It is needed, when the instance is loaded to
	 * execute correct searches for the instances that are displayed in the different widgets/content.
	 *
	 * @param createdOn
	 *            the createdOn to set
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * Getter for the id of the instance that was archived.
	 *
	 * @return the targetId
	 */
	public Serializable getTargetId() {
		return targetId;
	}

	/**
	 * Setter for the id of the instance that is archived.
	 *
	 * @param targetId
	 *            the targetId to set
	 */
	public void setTargetId(Serializable targetId) {
		this.targetId = targetId;
	}

	@Override
	public InstanceType type() {
		return instanceType;
	}

	@Override
	public void setType(InstanceType type) {
		instanceType = type;
	}

	@Override
	public Long getRevision() {
		return revision;
	}
}
