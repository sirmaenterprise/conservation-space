package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.VersionableEntity;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.seip.util.EqualsHelper.MapValueComparison;

/**
 * Class that represents a instance object with properties. The class is not persisted directly into the database but as
 * complex property of an object
 *
 * @author BBonev
 */
public class CommonInstance implements Instance, Copyable<CommonInstance>, VersionableEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4157922487540600925L;

	private Map<String, Serializable> properties;

	private Long revision;

	private String path;

	private String identifier;

	private Serializable id;

	private Long version;

	/** A reference for the current instance. */
	private transient InstanceReference reference;

	/**
	 * Instantiates a new common instance.
	 */
	public CommonInstance() {
		// default constructor
	}

	/**
	 * Instantiates a new common instance.
	 *
	 * @param name
	 *            the name
	 * @param path
	 *            the path
	 */
	public CommonInstance(String name, String path) {
		identifier = name;
		this.path = path;
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
		identifier = name;
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

	@Override
	public Map<String, Serializable> getProperties() {
		properties = EqualsHelper.getOrDefault(properties, new LinkedHashMap<String, Serializable>());
		return properties;
	}

	@Override
	public void setProperties(Map<String, Serializable> newProperties) {
		properties = newProperties;
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
		return path;
	}

	@Override
	public Serializable getId() {
		return id;
	}

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

	@Override
	public CommonInstance createCopy() {
		CommonInstance instance = new CommonInstance();
		instance.identifier = identifier;
		instance.id = id;
		instance.path = path;
		instance.revision = revision;
		instance.version = version;
		instance.properties = PropertiesUtil.cloneProperties(getProperties());
		return instance;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (id == null ? 0 : id.hashCode());
		result = PRIME * result + (path == null ? 0 : path.hashCode());
		result = PRIME * result + (properties == null ? 0 : properties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CommonInstance)) {
			return false;
		}
		CommonInstance other = (CommonInstance) obj;
		if (!nullSafeEquals(id, other.id) || ! nullSafeEquals(path, other.path)) {
			return false;
		}
		if (properties == null) {
			if (other.properties != null) {
				return false;
			}
		} else {
			// compare all sub properties
			Map<String, MapValueComparison> comparison = EqualsHelper.getMapComparison(properties, other.properties);
			for (MapValueComparison valueComparison : comparison.values()) {
				if (valueComparison != MapValueComparison.EQUAL) {
					return false;
				}
			}
			return true;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommonInstance [id=");
		builder.append(id);
		builder.append(", definitionId=");
		builder.append(identifier);
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

	@Override
	public Node getChild(String name) {
		Serializable serializable = getProperties().get(name);
		if (serializable instanceof Node) {
			return (Node) serializable;
		}
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
	public InstanceReference toReference() {
		if (reference == null) {
			reference = TypeConverterUtil.getConverter().convert(InstanceReference.class, this);
		}
		return reference;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

}
