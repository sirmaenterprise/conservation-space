package com.sirma.itt.pm.schedule.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.instance.model.TenantAware;

/**
 * Instance class that represents the particular schedule.
 * 
 * @author BBonev
 */
public class ScheduleInstance implements Serializable, Instance, BidirectionalMapping, TenantAware,
		OwnedModel {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4135755373368302657L;
	/** The id. */
	private Serializable id;
	/** The definition identifier of the target instance that need to be created. */
	private String identifier;
	/** The revision of the project definition. */
	private Long revision;
	/** The container. */
	private String container;
	/** The owning reference. */
	private InstanceReference owningReference;
	/** The properties. */
	private Map<String, Serializable> properties;

	private transient Instance owningInstance;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Serializable> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<String, Serializable>();
		}
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
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	/**
	 * Getter method for container.
	 *
	 * @return the container
	 */
	@Override
	public String getContainer() {
		return container;
	}

	/**
	 * Setter method for container.
	 *
	 * @param container the container to set
	 */
	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * Getter method for owningReference.
	 *
	 * @return the owningReference
	 */
	@Override
	public InstanceReference getOwningReference() {
		return owningReference;
	}

	/**
	 * Setter method for owningReference.
	 *
	 * @param owningReference the owningReference to set
	 */
	@Override
	public void setOwningReference(InstanceReference owningReference) {
		this.owningReference = owningReference;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Schedule [id=");
		builder.append(id);
		builder.append(", identifier=");
		builder.append(identifier);
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
	public Instance getOwningInstance() {
		if ((owningInstance == null) && (getOwningReference() != null)
				&& StringUtils.isNotNullOrEmpty(getOwningReference().getIdentifier())) {
			owningInstance = TypeConverterUtil.getConverter()
					.convert(InitializedInstance.class, getOwningReference()).getInstance();
		}
		return owningInstance;
	}

	/**
	 * Setter method for owningInstance.
	 * 
	 * @param owningInstance
	 *            the owningInstance to set
	 */
	@Override
	public void setOwningInstance(Instance owningInstance) {
		this.owningInstance = owningInstance;
	}

	@Override
	public InstanceReference toReference() {
		// not supported for now
		return null;
	}

}
