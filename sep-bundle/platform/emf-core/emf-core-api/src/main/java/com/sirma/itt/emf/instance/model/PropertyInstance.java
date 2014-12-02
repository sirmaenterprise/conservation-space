package com.sirma.itt.emf.instance.model;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;

/**
 * Instance class for Semantic Property
 * 
 * @author kirq4e
 */
public class PropertyInstance implements Instance, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3922521586685542272L;

	private Serializable id;

	private String identifier;

	private Long revision;

	private Map<String, Serializable> properties;

	/**
	 * From class
	 */
	private String domainClass;
	/**
	 * To class for relation or property type
	 */
	private String rangeClass;

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
		return null;
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	/**
	 * Getter method for domainClass.
	 * 
	 * @return the domainClass
	 */
	public String getDomainClass() {
		return domainClass;
	}

	/**
	 * Setter method for domainClass.
	 * 
	 * @param domainClass
	 *            the domainClass to set
	 */
	public void setDomainClass(String domainClass) {
		this.domainClass = domainClass;
	}

	/**
	 * Getter method for rangeClass.
	 * 
	 * @return the rangeClass
	 */
	public String getRangeClass() {
		return rangeClass;
	}

	/**
	 * Setter method for rangeClass.
	 * 
	 * @param rangeClass
	 *            the rangeClass to set
	 */
	public void setRangeClass(String rangeClass) {
		this.rangeClass = rangeClass;
	}

	@Override
	public InstanceReference toReference() {
		// nothing to do here
		return null;
	}

}
