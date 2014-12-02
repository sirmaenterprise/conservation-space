package com.sirma.itt.cmf.beans.model;

import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Purposable;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Instance object that represents a section/folder in the system.
 * 
 * @author BBonev
 */
public class SectionInstance extends EmfInstance implements BidirectionalMapping, Purposable,
		Sortable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 2121297874616936682L;
	/** The content. */
	private List<Instance> content = new LinkedList<>();
	/** The definition path. */
	private String definitionPath;
	/** The purpose. */
	private String purpose;
	/** The index. */
	private Long index;
	/** The standalone. */
	private boolean standalone = false;

	/**
	 * Getter method for documents.
	 * 
	 * @return the documents
	 */
	public List<Instance> getContent() {
		return content;
	}

	/**
	 * Setter method for documents.
	 * 
	 * @param documents
	 *            the documents to set
	 */
	public void setContent(List<Instance> documents) {
		this.content = documents;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((definitionPath == null) ? 0 : definitionPath.hashCode());
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
		SectionInstance other = (SectionInstance) obj;
		if (definitionPath == null) {
			if (other.definitionPath != null) {
				return false;
			}
		} else if (!definitionPath.equals(other.definitionPath)) {
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
		builder.append("SectionInstance [id=");
		builder.append(getId());
		builder.append(",revision=");
		builder.append(getRevision());
		builder.append(",sectionid=");
		builder.append(getIdentifier());
		builder.append(",\nproperties=");
		builder.append(getProperties());
		builder.append(",\ncontent=");
		builder.append(content);
		builder.append("\n]");
		return builder.toString();
	}

	/**
	 * Gets the parent element.
	 * 
	 * @return the parent element
	 */
	@Override
	public PathElement getParentElement() {
		return null;
	}

	/**
	 * Gets the path.
	 * 
	 * @return the path
	 */
	@Override
	public String getPath() {
		return getDefinitionPath();
	}

	/**
	 * Initializes the bidirection.
	 */
	@Override
	public void initBidirection() {
		for (Instance instance : content) {
			if (instance instanceof OwnedModel) {
				((OwnedModel) instance).setOwningInstance(this);
			}
			if (instance instanceof BidirectionalMapping) {
				((BidirectionalMapping) instance).initBidirection();
			}
		}
	}

	/**
	 * Checks for children.
	 * 
	 * @return true, if successful
	 */
	@Override
	public boolean hasChildren() {
		return !getContent().isEmpty();
	}

	/**
	 * Gets the child.
	 * 
	 * @param name
	 *            the name
	 * @return the child
	 */
	@Override
	public Node getChild(String name) {
		return PathHelper.find(getContent(), name);
	}

	/**
	 * Getter method for definitionPath.
	 * 
	 * @return the definitionPath
	 */
	public String getDefinitionPath() {
		return definitionPath;
	}

	/**
	 * Setter method for definitionPath.
	 * 
	 * @param definitionPath
	 *            the definitionPath to set
	 */
	public void setDefinitionPath(String definitionPath) {
		this.definitionPath = definitionPath;
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
	* {@inheritDoc}
	*/
	@Override
	public Integer getOrder() {
		if (getIndex() == null) {
			return null;
		}
		return getIndex().intValue();
	}

	/**
	 * Getter method for index.
	 * 
	 * @return the index
	 */
	public Long getIndex() {
		return index;
	}

	/**
	 * Setter method for index.
	 * 
	 * @param index
	 *            the index to set
	 */
	public void setIndex(Long index) {
		this.index = index;
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
