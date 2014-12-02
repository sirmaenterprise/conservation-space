package com.sirma.itt.cmf.beans.model;

import java.io.Serializable;
import java.util.LinkedHashMap;

import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Lockable;

/**
 * Represents an instantiated document definition.
 *
 * @author BBonev
 */
public class DocumentInstance extends EmfInstance implements BidirectionalMapping, Cloneable,
		Purposable, Lockable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -241432559926854137L;
	/**
	 * The document ref id. The unique id of the source definition for this instance
	 */
	private String documentRefId;

	/** The purpose. */
	private String purpose;

	/** The structured. */
	private Boolean structured;

	/**
	 * Explicit parent path if the document instance have been moved to other section/case.
	 */
	private String parentPath;

	/** If the document is standalone. */
	private boolean standalone = false;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((documentRefId == null) ? 0 : documentRefId.hashCode());
		result = (prime * result) + ((parentPath == null) ? 0 : parentPath.hashCode());
		result = (prime * result) + (hasDocument() ? 1231 : 1237);
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
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DocumentInstance other = (DocumentInstance) obj;
		if (documentRefId == null) {
			if (other.documentRefId != null) {
				return false;
			}
		} else if (!documentRefId.equals(other.documentRefId)) {
			return false;
		}
		if (parentPath == null) {
			if (other.parentPath != null) {
				return false;
			}
		} else if (!parentPath.equals(other.parentPath)) {
			return false;
		}
		if (hasDocument() != other.hasDocument()) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DocumentInstance [id=");
		builder.append(getId());
		builder.append(", revision=");
		builder.append(getRevision());
		builder.append(", documentId=");
		builder.append(getIdentifier());
		builder.append(", purpose=");
		builder.append(purpose);
		builder.append(", standalone=");
		builder.append(standalone);
		builder.append(", structured=");
		builder.append(structured);
		builder.append(", documentRefId=");
		builder.append(documentRefId);
		builder.append(", parentPath=");
		builder.append(parentPath);
		builder.append(", container=");
		builder.append(getContainer());
		builder.append(", properties=");
		builder.append(getProperties());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PathElement getParentElement() {
		if (isStandalone()) {
			return null;
		}
		if (getParentPath() == null) {
			return getOwningInstance();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		if (isStandalone()) {
			return getIdentifier();
		}
		if (getParentPath() == null) {
			return getDocumentRefId();
		}
		return getParentPath();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {
		// nothing to do here for know
	}

	/**
	 * Checks if there is attached document in this document instance.
	 *
	 * @return true, document is present and <code>false</code> if not and need to be uploaded
	 */
	public boolean hasDocument() {
		return (getProperties() != null)
				&& (StringUtils.isNotNullOrEmpty((String) getProperties().get(
						DocumentProperties.ATTACHMENT_LOCATION)) || (getProperties().get(
						DocumentProperties.FILE_LOCATOR) != null));
	}

	/**
	 * Checks if the current document instance is locked.
	 *
	 * @return true, if is locked
	 */
	@Override
	public boolean isLocked() {
		return getProperties().get(DocumentProperties.LOCKED_BY) != null;
	}

	/**
	 * Checks if the current document instance is locked and has a is working copy.
	 *
	 * @return true, if is working copy
	 */
	public boolean isWorkingCopy() {
		return isLocked() && getProperties().containsKey(DocumentProperties.WORKING_COPY_LOCATION);
	}

	/**
	 * Checks if is history instance.
	 *
	 * @return true, if is history instance
	 */
	public boolean isHistoryInstance() {
		return getProperties().containsKey(DocumentProperties.DOCUMENT_CURRENT_VERSION_INSTANCE);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * NOTE: the default values should be repopulated via
	 * {@link com.sirma.itt.emf.instance.dao.InstanceDao#populateProperties(com.sirma.itt.emf.properties.model.PropertyModel, java.util.List)}
	 */
	@Override
	public DocumentInstance clone() {
		DocumentInstance instance = new DocumentInstance();
		instance.setId(null);
		instance.setIdentifier(getIdentifier());
		instance.setContainer(getContainer());
		instance.setRevision(getRevision());
		instance.parentPath = getParentPath();
		instance.documentRefId = getDocumentRefId();
		instance.purpose = getPurpose();
		instance.structured = getStructured();
		instance.standalone = standalone;
		// should not copy
		instance.setDmsId(null);
		instance.setOwningReference(getOwningReference());
		instance.setProperties(new LinkedHashMap<String, Serializable>());
		return instance;
	}

	/**
	 * Getter method for documentRefId.
	 *
	 * @return the documentRefId
	 */
	public String getDocumentRefId() {
		return documentRefId;
	}

	/**
	 * Setter method for documentRefId.
	 *
	 * @param documentRefId
	 *            the documentRefId to set
	 */
	public void setDocumentRefId(String documentRefId) {
		this.documentRefId = documentRefId;
	}

	/**
	 * Getter method for purpose.
	 *
	 * @return the purpose
	 */
	@Override
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter method for purpose.
	 *
	 * @param purpose
	 *            the purpose to set
	 */
	@Override
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	/**
	 * Getter method for structured.
	 *
	 * @return the structured
	 */
	public Boolean getStructured() {
		return structured;
	}

	/**
	 * Setter method for structured.
	 *
	 * @param structured
	 *            the structured to set
	 */
	public void setStructured(Boolean structured) {
		this.structured = structured;
	}

	/**
	 * Getter method for parentPath.
	 *
	 * @return the parentPath
	 */
	public String getParentPath() {
		return parentPath;
	}

	/**
	 * Setter method for parentPath.
	 *
	 * @param parentPath
	 *            the parentPath to set
	 */
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
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
		Serializable serializable = getProperties().get(name);
		if (serializable instanceof Node) {
			return (Node) serializable;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContentManagementId() {
		return getProperties() != null ? (String) getProperties().get(
				DocumentProperties.UNIQUE_DOCUMENT_IDENTIFIER) : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContentManagementId(String contentManagementId) {
		if (getProperties() != null) {
			getProperties().put(DocumentProperties.UNIQUE_DOCUMENT_IDENTIFIER, contentManagementId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getLockedBy() {
		return (String) getProperties().get(DocumentProperties.LOCKED_BY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLockedBy(String lockedBy) {
		getProperties().put(DocumentProperties.LOCKED_BY, lockedBy);
	}

	/**
	 * Getter method for standalone.
	 * 
	 * @return the standalone
	 */
	public boolean isStandalone() {
		return standalone;
	}

	/**
	 * Setter method for standalone.
	 * 
	 * @param standalone
	 *            the standalone to set
	 */
	public void setStandalone(boolean standalone) {
		this.standalone = standalone;
	}
}
