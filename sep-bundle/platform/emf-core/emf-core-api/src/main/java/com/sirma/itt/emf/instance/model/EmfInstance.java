package com.sirma.itt.emf.instance.model;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.TreeNode;
import com.sirma.itt.emf.domain.model.VersionableEntity;

/**
 * Basic {@link Instance} implementation.
 *
 * @author BBonev
 */
public class EmfInstance implements Instance, VersionableEntity, TenantAware, CMInstance,
		DMSInstance, OwnedModel, TreeNode<Serializable> {

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
	 * Transient field that stores the instance direct parent if any. The main direct parent is
	 * fetched from the owning reference: <code>this.getOwningReference().getIdentifier();</code>>
	 */
	private transient Serializable parentId;

	/** A reference for the current instance. */
	private transient InstanceReference reference;

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
		return revision;
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
		// if reference has been created update the id if possible
		if ((reference != null) && (id != null)) {
			reference.setIdentifier(id.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InstanceReference getOwningReference() {
		return owningReference;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance getOwningInstance() {
		if ((owningInstance == null) && (getOwningReference() != null)
				&& StringUtils.isNotNullOrEmpty(getOwningReference().getIdentifier())) {
			owningInstance = TypeConverterUtil.getConverter()
					.convert(InitializedInstance.class, getOwningReference()).getInstance();
		}
		return owningInstance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDmsId() {
		return dmsId;
	}

	@Override
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContentManagementId() {
		return contentManagementId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContentManagementId(String contentManagementId) {
		this.contentManagementId = contentManagementId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContainer() {
		return container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getVersion() {
		return version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setVersion(Long version) {
		this.version = version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOwningInstance(Instance instance) {
		owningInstance = instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOwningReference(InstanceReference reference) {
		owningReference = reference;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result)
				+ ((contentManagementId == null) ? 0 : contentManagementId.hashCode());
		result = (prime * result) + ((dmsId == null) ? 0 : dmsId.hashCode());
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		result = (prime * result) + ((identifier == null) ? 0 : identifier.hashCode());
		result = (prime * result) + ((revision == null) ? 0 : revision.hashCode());
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EmfInstance other = (EmfInstance) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (dmsId == null) {
			if (other.dmsId != null) {
				return false;
			}
		} else if (!dmsId.equals(other.dmsId)) {
			return false;
		}
		if (contentManagementId == null) {
			if (other.contentManagementId != null) {
				return false;
			}
		} else if (!contentManagementId.equals(other.contentManagementId)) {
			return false;
		}
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
		if (revision == null) {
			if (other.revision != null) {
				return false;
			}
		} else if (!revision.equals(other.revision)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Serializable getParentId() {
		if ((parentId == null) && (getOwningReference() != null)
				&& StringUtils.isNotNullOrEmpty(getOwningReference().getIdentifier())) {
			parentId = getOwningReference().getIdentifier();
		}
		return parentId;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

}
