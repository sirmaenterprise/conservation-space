package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.IS_DELETED;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.TenantAware;
import com.sirma.itt.seip.domain.TreeNode;
import com.sirma.itt.seip.domain.VersionableEntity;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.seip.util.LoggingUtil;

/**
 * Basic {@link Instance} implementation.
 *
 * @author BBonev
 */
public class EmfInstance implements Instance, VersionableEntity, TenantAware, CMInstance, DMSInstance,
		TreeNode<Serializable>, Copyable<Instance> {

	private static final long serialVersionUID = 8602663252096640490L;

	private Serializable id;

	private String dmsId;

	private String contentManagementId;

	private String identifier;

	private Map<String, Serializable> properties;

	private Long revision;

	private String container;

	private transient Long version;

	/** A reference for the current instance. Cached and reused as result for {@link #toReference()} */
	private transient InstanceReference reference;

	private transient InstanceType instanceType;

	/** Flag to identify if the instance has been deleted or not */
	private Boolean deleted;

	/**
	 * Default constructor.
	 */
	public EmfInstance() {
		// empty body
	}

	/**
	 * Constructor with id.
	 *
	 * @param id
	 *            the instance id
	 */
	public EmfInstance(Serializable id) {
		this.id = id;
	}

	@Override
	public void setProperties(Map<String, Serializable> properties) {
		this.properties = properties;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	@Override
	public Long getRevision() {
		return revision;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	public PathElement getParentElement() {
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
	public Node getChild(String name) {
		return null;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Serializable getId() {
		if (id == null && reference instanceof InstanceReferenceImpl) {
			id = ((InstanceReferenceImpl) reference).id;
		}
		return id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = id;
		// if reference has been created update the id if possible
		if (reference != null && !EqualsHelper.nullSafeEquals(id, reference.getId())) {
			reference.setId(Objects.toString(id, null));
		}
	}

	@Override
	public InstanceType type() {
		if (instanceType == null && reference instanceof InstanceReferenceImpl) {
			instanceType = ((InstanceReferenceImpl) reference).type;
		}
		return instanceType;
	}

	@Override
	public void setType(InstanceType type) {
		instanceType = type;
		// if reference has been created update the id if possible
		if (reference != null && !EqualsHelper.nullSafeEquals(type, reference.getType())) {
			reference.setType(type);
		}
	}

	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
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
	public boolean isDeleted() {
		if (deleted == null) {
			deleted = getAs(IS_DELETED, value -> Boolean.valueOf(value.toString()), () -> Boolean.FALSE);
		}
		return deleted.booleanValue();
	}

	@Override
	public void markAsDeleted() {
		deleted = Boolean.TRUE;
		add(DefaultProperties.IS_DELETED, Boolean.TRUE);
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
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getClass().getName());
		builder.append(" [id=");
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
		builder.append(", properties=");
		// prevent printing huge property values
		builder.append(LoggingUtil.toString(properties));
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Instance createCopy() {
		EmfInstance clone = new EmfInstance();
		clone.id = id;
		clone.identifier = identifier;
		clone.dmsId = dmsId;
		clone.contentManagementId = contentManagementId;
		clone.revision = revision;
		clone.container = container;
		clone.deleted = deleted;
		clone.instanceType = instanceType;
		clone.version = version;
		clone.reference = reference;

		clone.setProperties(PropertiesUtil.cloneProperties(getProperties()));
		return clone;
	}

}
