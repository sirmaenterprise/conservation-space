package com.sirma.itt.seip.testutil.mocks;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;

/**
 * Mock object.
 *
 * @author svelikov
 */
public class DataTypeDefinitionMock implements DataTypeDefinition {
	private static final long serialVersionUID = 8938236150483092200L;
	private final Class<?> clazz;
	private final String uri;
	private String name;
	private Long id = Long.valueOf(1L);

	/**
	 * Instantiates a new mock datatype.
	 *
	 * @param clazz
	 *            the clazz
	 * @param uri
	 *            the uri
	 */
	public DataTypeDefinitionMock(Class<?> clazz, String uri) {
		super();
		this.clazz = clazz;
		this.uri = uri;
	}

	/**
	 * Instantiates a new data type definition mock.
	 *
	 * @param instance
	 *            the instance
	 */
	public DataTypeDefinitionMock(Instance instance) {
		clazz = instance.getClass();
		uri = Optional.ofNullable(instance.type()).map(InstanceType::getId).map(Object::toString).orElse(null);
	}

	/**
	 * Instantiates a new data type definition mock.
	 *
	 * @param name
	 *            the name
	 */
	public DataTypeDefinitionMock(String name) {
		this.name = name;
		clazz = Object.class;
		uri = null;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		if (name == null) {
			return getJavaClass().getSimpleName().toLowerCase();
		}
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getJavaClassName() {
		return getJavaClass().getName();
	}

	@Override
	public Class<?> getJavaClass() {
		return clazz;
	}

	@Override
	public String getFirstUri() {
		return uri;
	}

	@Override
	public Set<String> getUries() {
		return Collections.emptySet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (getDescription() == null ? 0 : getDescription().hashCode());
		result = prime * result + (id == null ? 0 : id.hashCode());
		result = prime * result + (getJavaClassName() == null ? 0 : getJavaClassName().hashCode());
		result = prime * result + (name == null ? 0 : name.hashCode());
		result = prime * result + (getTitle() == null ? 0 : getTitle().hashCode());
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
		if (!(nullSafeEquals(getId(), other.getId()) && nullSafeEquals(getName(), other.getName())
				&& nullSafeEquals(getJavaClassName(), other.getJavaClassName()))) {
			return false;
		}
		return nullSafeEquals(getDescription(), other.getDescription()) && nullSafeEquals(getTitle(), other.getTitle())
				&& nullSafeEquals(getUries(), other.getUries());
	}

}
