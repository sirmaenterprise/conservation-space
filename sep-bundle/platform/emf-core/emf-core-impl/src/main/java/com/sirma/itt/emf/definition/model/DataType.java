package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * The DataTypeDefinition implementation.
 *
 * @author BBonev
 */
@Entity
@Table(appliesTo = "emf_dataTypeDefinition", indexes = { @Index(name = "idx_dataType_name", columnNames = "name") })
@javax.persistence.Table(name = "emf_dataTypeDefinition")
public class DataType implements DataTypeDefinition, Serializable, Cloneable {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8942431039642907077L;
	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Tag(1)
	protected Long id;
	/** The name. */
	@Column(name = "name", length = 100, nullable = false, unique = true)
	@Tag(2)
	protected String name;

	/** The title. */
	@Column(name = "title", length = 200, nullable = false)
	@Tag(4)
	protected String title;

	/** The description. */
	@Column(name = "description", length = 200, nullable = false)
	@Tag(5)
	protected String description;

	/** The java class name. */
	@Column(name = "javaClassName", length = 200, nullable = true)
	@Tag(3)
	protected String javaClassName;
	/** Cached java class. */
	private transient Class<?> javaClass;
	/** The uri. */
	@Tag(6)
	@Column(name = "uri", length = 10240, nullable = true)
	protected String uri;
	/** The uries. */
	private transient Set<String> uries;
	/** The first uri. */
	private transient String firstUri;


	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id the id to set
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getJavaClassName() {
		return javaClassName;
	}

	/**
	 * Setter method for name.
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter method for title.
	 *
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Setter method for description.
	 *
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Setter method for javaClassName.
	 *
	 * @param javaClassName
	 *            the javaClassName to set
	 */
	public void setJavaClassName(String javaClassName) {
		this.javaClassName = javaClassName;
	}

	@Override
	public DataType clone() {
		DataType dataType = new DataType();
		dataType.description = description;
		dataType.id = id;
		dataType.javaClassName = javaClassName;
		dataType.name = name;
		dataType.title = title;
		dataType.uri = uri;
		return dataType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((description == null) ? 0 : description.hashCode());
		result = (prime * result) + ((id == null) ? 0 : id.hashCode());
		result = (prime * result) + ((javaClassName == null) ? 0 : javaClassName.hashCode());
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		result = (prime * result) + ((title == null) ? 0 : title.hashCode());
		result = (prime * result) + ((uri == null) ? 0 : uri.hashCode());
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
		if (!(obj instanceof DataType)) {
			return false;
		}
		DataType other = (DataType) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (javaClassName == null) {
			if (other.javaClassName != null) {
				return false;
			}
		} else if (!javaClassName.equals(other.javaClassName)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (uri == null) {
			if (other.uri != null) {
				return false;
			}
		} else if (!uri.equals(other.uri)) {
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
		builder.append("DataType [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", javaClassName=");
		builder.append(javaClassName);
		builder.append(", uri=");
		builder.append(uri);
		builder.append(", title=");
		builder.append(title);
		builder.append(", description=");
		builder.append(description);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public Class<?> getJavaClass() {
		if ((javaClass == null) && StringUtils.isNotNullOrEmpty(getJavaClassName())) {
			try {
				javaClass = TypeConverterUtil.getConverter().convert(Class.class,
						getJavaClassName());
			} catch (Exception e) {
				throw new EmfRuntimeException("Invalid class name " + getJavaClassName()
						+ " for type " + getName(), e);
			}
		}
		return javaClass;
	}

	@Override
	public String getFirstUri() {
		if (firstUri == null) {
			Set<String> set = getUries();
			if (!set.isEmpty()) {
				firstUri = set.iterator().next();
			}
		}
		return firstUri;
	}

	/**
	 * Gets the uri.
	 * 
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Setter method for uri.
	 *
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Setter method for javaClass.
	 *
	 * @param javaClass the javaClass to set
	 */
	public void setJavaClass(Class<?> javaClass) {
		this.javaClass = javaClass;
	}

	@Override
	public Set<String> getUries() {
		if (uries == null) {
			String string = getUri();
			if (StringUtils.isNotNullOrEmpty(string)) {
				String[] split = string.split(",");
				uries = Collections.synchronizedSet(CollectionUtils
						.<String> createLinkedHashSet(split.length));
				uries.addAll(Arrays.asList(split));
				uries = Collections.unmodifiableSet(uries);
			} else {
				uries = Collections.emptySet();
			}
		}
		return uries;
	}

}
