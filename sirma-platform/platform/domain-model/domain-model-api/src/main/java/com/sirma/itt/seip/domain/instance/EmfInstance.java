package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_DELETED;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.TenantAware;
import com.sirma.itt.seip.domain.TreeNode;
import com.sirma.itt.seip.domain.VersionableEntity;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Basic {@link Instance} implementation.
 *
 * @author BBonev
 */
public class EmfInstance implements Instance, VersionableEntity, TenantAware, CMInstance, DMSInstance, OwnedModel,
		TreeNode<Serializable>, Cloneable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8602663252096640490L;

	/** The id. */
	private Serializable id;

	/** The document management id. */
	private String dmsId;

	/** The content management id. */
	private String contentManagementId;

	/** The identifier. */
	private String identifier;

	/** The properties. */
	private Map<String, Serializable> properties;

	/** The case revision. */
	private Long revision;

	/** The container. */
	private String container;

	/** The version. */
	private transient Long version;

	/** The owning reference. */
	private InstanceReference owningReference;

	/** The owning instance. */
	private transient Instance owningInstance;

	/**
	 * Transient field that stores the instance direct parent if any. The main direct parent is fetched from the owning
	 * reference: <code>this.getOwningReference().getIdentifier();</code>>
	 */
	private transient Serializable parentId;

	/** A reference for the current instance. */
	private transient InstanceReference reference;

	private transient InstanceType instanceType;

	/** Flag to identify if the instance has been deleted or not */
	private Boolean deleted;

	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	@Override
	public Long getRevision() {
		return revision;
	}

	@Override
	public PathElement getParentElement() {
		return null;
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
		// if reference has been created update the id if possible
		if (reference != null && id != null) {
			reference.setIdentifier(id.toString());
		}
	}

	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	@Override
	public InstanceReference getOwningReference() {
		return owningReference;
	}

	@Override
	public Instance getOwningInstance() {
		if (owningInstance == null && getOwningReference() != null
				&& StringUtils.isNotNullOrEmpty(getOwningReference().getIdentifier())) {
			owningInstance = TypeConverterUtil
					.getConverter()
						.convert(InitializedInstance.class, getOwningReference())
						.getInstance();
		}
		return owningInstance;
	}

	@Override
	public String getDmsId() {
		return dmsId;
	}

	@Override
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	@Override
	public String getContentManagementId() {
		return contentManagementId;
	}

	@Override
	public void setContentManagementId(String contentManagementId) {
		this.contentManagementId = contentManagementId;
	}

	@Override
	public String getContainer() {
		return container;
	}

	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	@Override
	public Long getVersion() {
		return version;
	}

	@Override
	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public void setOwningInstance(Instance newParent) {
		if (equals(newParent)) {
			throw new EmfRuntimeException("Could not set same instance as owning: " + newParent);
		}
		owningInstance = newParent;

		if (owningReference == null && newParent != null) {
			owningReference = newParent.toReference();
		}
		// we link the parents only when the reference is initialized
		// if not initialized the converter to reference will update the parent
		if (reference != null && newParent != null) {
			// update the parent of the reference as well
			reference.setParent(newParent.toReference());
		}
	}

	@Override
	public void setOwningReference(InstanceReference reference) {
		if (this.reference != null && this.reference.equals(reference)) {
			throw new EmfRuntimeException("Could not set same reference as owning: " + reference);
		}
		if (InstanceReference.ROOT_REFERENCE.equals(reference)) {
			// if this happens it's the bug in the code and should be fixed not to reach here!
			throw new EmfRuntimeException("Could not set root reference as parent of an instance");
		}
		owningReference = reference;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getId() == null ? 0 : getId().hashCode());
		result = prime * result + (getContentManagementId() == null ? 0 : getContentManagementId().hashCode());
		result = prime * result + (getDmsId() == null ? 0 : getDmsId().hashCode());
		result = prime * result + (getIdentifier() == null ? 0 : getIdentifier().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EmfInstance)) {
			return false;
		}
		EmfInstance other = (EmfInstance) obj;
		if (!EqualsHelper.nullSafeEquals(getId(), other.getId())) {
			return false;
		} else if (!EqualsHelper.nullSafeEquals(getDmsId(), other.getDmsId())) {
			return false;
		} else if (!EqualsHelper.nullSafeEquals(getContentManagementId(), other.getContentManagementId())) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(getIdentifier(), other.getIdentifier());
	}

	@Override
	public Serializable getParentId() {
		if (parentId == null && getOwningReference() != null
				&& StringUtils.isNotNullOrEmpty(getOwningReference().getIdentifier())) {
			parentId = getOwningReference().getIdentifier();
		}
		return parentId;
	}

	@Override
	public void setParentId(Serializable parentId) {
		this.parentId = parentId;
	}

	/**
	 * Sets the a reference for the current instance.
	 *
	 * @param reference
	 *            the new a reference for the current instance
	 */
	public void setReference(InstanceReference reference) {
		this.reference = reference;
	}

	@Override
	public InstanceReference toReference() {
		if (reference == null) {
			reference = TypeConverterUtil.getConverter().convert(InstanceReference.class, this);
		}
		return reference;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmfInstance [id=");
		builder.append(id);
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", dmsId=");
		builder.append(dmsId);
		builder.append(", contentManagementId=");
		builder.append(contentManagementId);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", container=");
		builder.append(container);
		builder.append(", owningReference=");
		builder.append(owningReference);
		builder.append(", properties=");
		builder.append(properties);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean isDeleted() {
		if (deleted == null) {
			deleted = getAs(IS_DELETED, value -> Boolean.valueOf(value.toString()), () -> Boolean.FALSE);
		}
		return deleted.booleanValue();
	}

	@Override
	public Instance clone() {
		Instance clone;
		try {
			clone = (Instance) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new EmfRuntimeException(e);
		}
		clone.setProperties(PropertiesUtil.cloneProperties(getProperties()));
		return clone;
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
	public void markAsDeleted() {
		deleted = Boolean.TRUE;
		add(DefaultProperties.IS_DELETED, Boolean.TRUE);
	}

}
