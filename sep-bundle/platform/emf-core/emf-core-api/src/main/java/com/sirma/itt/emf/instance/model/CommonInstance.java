package com.sirma.itt.emf.instance.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.VersionableEntity;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.EqualsHelper.MapValueComparison;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Class that represents a instance object with properties. The class is not persisted directly into
 * the database but as complex property of an object
 *
 * @author BBonev
 */
public class CommonInstance implements Instance, Cloneable, VersionableEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4157922487540600925L;

	/** The properties. */
	private Map<String, Serializable> properties;

	/** The revision. */
	private Long revision;

	/** The path. */
	private String path;

	/** The definition id. */
	private String definitionId;

	/** The id. */
	private Serializable id;

	/** The version. */
	private Long version;

	/** A reference for the current instance. */
	private transient InstanceReference reference;

	/**
	 * Instantiates a new common instance.
	 */
	public CommonInstance() {

	}

	/**
	 * Instantiates a new common instance.
	 *
	 * @param name
	 *            the name
	 * @param revision
	 *            the revision
	 * @param path
	 *            the path
	 */
	public CommonInstance(String name, Long revision, String path) {
		definitionId = name;
		this.revision = revision;
		this.path = path;
	}

	/**
	 * Instantiates a new common instance.
	 *
	 * @param name
	 *            the name
	 * @param revision
	 *            the revision
	 * @param parentElement
	 *            the parent element to construct the path
	 */
	public CommonInstance(String name, Long revision, PathElement parentElement) {
		this(name, revision, PathHelper.getPath(parentElement));
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
		return path;
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
	 * Setter method for revision.
	 *
	 * @param revision
	 *            the revision to set
	 */
	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	/**
	 * Setter method for path.
	 *
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Setter method for definitionId.
	 *
	 * @param definitionId
	 *            the definitionId to set
	 */
	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CommonInstance clone() {
		CommonInstance instance = new CommonInstance();
		instance.definitionId = definitionId;
		instance.id = id;
		instance.path = path;
		instance.revision = revision;
		instance.version = version;
		instance.properties = PropertiesUtil.cloneProperties(getProperties());
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		result = (prime * result) + ((path == null) ? 0 : path.hashCode());
		result = (prime * result) + ((properties == null) ? 0 : properties.hashCode());
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
		if (!(obj instanceof CommonInstance)) {
			return false;
		}
		CommonInstance other = (CommonInstance) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (properties == null) {
			if (other.properties != null) {
				return false;
			}
		} else {
			// compare all sub properties
			Map<String, MapValueComparison> comparison = EqualsHelper.getMapComparison(properties,
					other.properties);
			for (MapValueComparison valueComparison : comparison.values()) {
				if (valueComparison != MapValueComparison.EQUAL) {
					return false;
				}
			}
			return true;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommonInstance [id=");
		builder.append(id);
		builder.append(", definitionId=");
		builder.append(definitionId);
		builder.append(", revision=");
		builder.append(revision);
		builder.append(", path=");
		builder.append(path);
		builder.append(", properties=");
		builder.append(properties);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for version.
	 *
	 * @return the version
	 */
	@Override
	public Long getVersion() {
		return version;
	}

	/**
	 * Setter method for version.
	 *
	 * @param version
	 *            the version to set
	 */
	@Override
	public void setVersion(Long version) {
		this.version = version;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren() {
		if (getProperties().isEmpty()) {
			return false;
		}
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
	public String getIdentifier() {
		return definitionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		setDefinitionId(identifier);
	}

	@Override
	public InstanceReference toReference() {
		if (reference == null) {
			reference = TypeConverterUtil.getConverter().convert(InstanceReference.class, this);
		}
		return reference;
	}

}
