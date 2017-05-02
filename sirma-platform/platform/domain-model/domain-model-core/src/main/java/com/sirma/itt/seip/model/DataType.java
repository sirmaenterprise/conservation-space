package com.sirma.itt.seip.model;

import static com.sirma.itt.seip.collections.CollectionUtils.createLinkedHashSet;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.QueryHints;
import org.hibernate.annotations.Table;
import org.json.JSONObject;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * The DataTypeDefinition implementation.
 *
 * @author BBonev
 */
@Entity
@Table(appliesTo = "emf_dataTypeDefinition", indexes = { @Index(name = "idx_dataType_name", columnNames = "name") })
@javax.persistence.Table(name = "emf_dataTypeDefinition")
@NamedQueries({
		@NamedQuery(name = DataType.QUERY_TYPE_DEFINITION_KEY, query = DataType.QUERY_TYPE_DEFINITION, hints = {
				@QueryHint(name = QueryHints.CACHEABLE, value = "true") }),
		@NamedQuery(name = DataType.QUERY_TYPE_DEFINITION_BY_URI_KEY, query = DataType.QUERY_TYPE_DEFINITION_BY_URI, hints = {
				@QueryHint(name = QueryHints.CACHEABLE, value = "true") }),
		@NamedQuery(name = DataType.QUERY_TYPE_DEFINITION_BY_CLASS_KEY, query = DataType.QUERY_TYPE_DEFINITION_BY_CLASS, hints = {
				@QueryHint(name = QueryHints.CACHEABLE, value = "true") }),
		@NamedQuery(name = DataType.QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS_KEY, query = DataType.QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS, hints = {
				@QueryHint(name = QueryHints.CACHEABLE, value = "true") }) })
public class DataType implements DataTypeDefinition, Serializable, Cloneable, JsonRepresentable {
	private static final long serialVersionUID = 8942431039642907077L;

	/** Get {@link DataType} by name */
	public static final String QUERY_TYPE_DEFINITION_KEY = "QUERY_TYPE_DEFINITION";
	static final String QUERY_TYPE_DEFINITION = "select d from DataType d where d.name=:name";

	/** Get {@link DataType} by matching uri */
	public static final String QUERY_TYPE_DEFINITION_BY_URI_KEY = "QUERY_TYPE_DEFINITION_BY_URI";
	static final String QUERY_TYPE_DEFINITION_BY_URI = "select d from DataType d where d.uri like :uri";

	/** Get {@link DataType} by class */
	public static final String QUERY_TYPE_DEFINITION_BY_CLASS_KEY = "QUERY_TYPE_DEFINITION_BY_CLASS";
	static final String QUERY_TYPE_DEFINITION_BY_CLASS = "select d from DataType d where d.javaClassName=:javaClassName";

	/** Get {@link DataType} by name and class */
	public static final String QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS_KEY = "QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS";
	static final String QUERY_TYPE_DEFINITION_BY_NAME_AND_CLASS = "select d from DataType d where d.name=:name AND d.javaClassName=:javaClassName";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Tag(1)
	protected Long id;

	@Column(name = "name", length = 100, nullable = false, unique = true)
	@Tag(2)
	protected String name;

	@Column(name = "title", length = 200, nullable = false)
	@Tag(4)
	protected String title;

	@Column(name = "description", length = 200, nullable = false)
	@Tag(5)
	protected String description;

	@Column(name = "javaClassName", length = 200, nullable = true)
	@Tag(3)
	protected String javaClassName;

	/** Cached java class. */
	private transient Class<?> javaClass;

	@Tag(6)
	@Column(name = "uri", length = 10240, nullable = true)
	protected String uri;

	private transient Set<String> uries;

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
	 * @param id
	 *            the id to set
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

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
		// reset the cached value
		javaClass = null;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (description == null ? 0 : description.hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (javaClassName == null ? 0 : javaClassName.hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (title == null ? 0 : title.hashCode());
		result = prime * result + getUries().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DataTypeDefinition)) {
			return false;
		}
		DataTypeDefinition other = (DataTypeDefinition) obj;
		if (!(nullSafeEquals(id, other.getId()) && nullSafeEquals(name, other.getName())
				&& nullSafeEquals(javaClassName, other.getJavaClassName()))) {
			return false;
		}
		return nullSafeEquals(description, other.getDescription()) && nullSafeEquals(title, other.getTitle())
				&& nullSafeEquals(getUries(), other.getUries());
	}

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
		if (javaClass == null && StringUtils.isNotNullOrEmpty(getJavaClassName())) {
			try {
				javaClass = TypeConverterUtil.getConverter().convert(Class.class, getJavaClassName());
			} catch (Exception e) {
				throw new EmfRuntimeException("Invalid class name " + getJavaClassName() + " for type " + getName(), e);
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
	 * @param uri
	 *            the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
		// reset the cache if already initialized
		uries = null;
		firstUri = null;
	}

	/**
	 * Setter method for javaClass.
	 *
	 * @param javaClass
	 *            the javaClass to set
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
				Set<String> temp = createLinkedHashSet(split.length);
				temp.addAll(Arrays.asList(split));
				uries = Collections.unmodifiableSet(temp);
			} else {
				uries = Collections.emptySet();
			}
		}
		return uries;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "name", getName());
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// Method not in used
	}

}
